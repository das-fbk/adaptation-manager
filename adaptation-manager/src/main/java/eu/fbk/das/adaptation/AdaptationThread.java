package eu.fbk.das.adaptation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.fbk.das.adaptation.api.AdaptationManagerInterface;
import eu.fbk.das.adaptation.util.XMLAdapter;
import eu.fbk.das.composer.api.ComposerInterface;
import eu.fbk.das.composer.api.CompositionProblem;
import eu.fbk.das.composer.api.CompositionProblemBuilder;
import eu.fbk.das.composer.api.CompositionStatus;
import eu.fbk.das.composer.api.DotParser;
import eu.fbk.das.composer.api.Parser;
import eu.fbk.das.composer.api.elements.ServiceAction;
import eu.fbk.das.composer.api.elements.SyncPoint;
import eu.fbk.das.composer.api.exceptions.CompositionDuplicateOidException;
import eu.fbk.das.composer.api.exceptions.CompositionDuplicateSidException;
import eu.fbk.das.composer.api.exceptions.InvalidCompositionEffectException;
import eu.fbk.das.composer.api.exceptions.InvalidCompositionPreconditionException;
import eu.fbk.das.composer.api.exceptions.InvalidServiceCurrentStateException;
import eu.fbk.das.composer.api.exceptions.InvalidServiceObjectAssignmentException;
import eu.fbk.das.composer.api.exceptions.ServiceGroundingTypeMismatchException;
import eu.fbk.das.process.engine.api.AdaptationProblem;
import eu.fbk.das.process.engine.api.AdaptationResult;
import eu.fbk.das.process.engine.api.AdaptationStrategy;
import eu.fbk.das.process.engine.api.DomainObjectInstance;
import eu.fbk.das.process.engine.api.domain.AbstractActivity;
import eu.fbk.das.process.engine.api.domain.DomainObjectDefinition;
import eu.fbk.das.process.engine.api.domain.ProcessActivity;
import eu.fbk.das.process.engine.api.domain.ProcessDiagram;
import eu.fbk.das.process.engine.api.domain.ServiceTransitionGlobal;
import eu.fbk.das.process.engine.api.domain.exceptions.InvalidFlowActivityException;
import eu.fbk.das.process.engine.api.domain.exceptions.InvalidFlowInitialStateException;
import eu.fbk.das.process.engine.api.domain.exceptions.InvalidObjectCurrentStateException;
import eu.fbk.das.process.engine.api.jaxb.ClauseType.Point;
import eu.fbk.das.process.engine.api.jaxb.ClauseType.Point.DomainProperty;
import eu.fbk.das.process.engine.api.jaxb.Fragment;
import eu.fbk.das.process.engine.api.jaxb.Fragment.Action;
import eu.fbk.das.process.engine.api.jaxb.GoalType;

/**
 * This class represents a thread within which a particular adaptation problem
 * is resolved. It contains all procedures from deciding on the strategy to
 * composing solution to analysing it in case no plan is found.
 * 
 * @see AdaptationProblem
 * @see AdaptationManagerInterface
 */
public class AdaptationThread extends Thread {

	private static final Logger logger = LogManager
			.getLogger(AdaptationThread.class);

	private AdaptationProblem ap;
	private AdaptationManagerInterface am;
	private String apid;
	private String workingFolder;

	private static final String FILE_SEPARATOR = System
			.getProperty("file.separator");

	/**
	 * stores stepId corresponding to
	 */
	private ComposerInterface composer;

	public AdaptationThread(AdaptationProblem ap, AdaptationManager am,
			String apid, ComposerInterface composer, String workingFolder) {
		this.ap = ap;
		this.am = am;
		this.apid = apid;
		this.composer = composer;
		this.workingFolder = workingFolder;
	}

	@Override
	public void run() {
		try {
			logger.debug("Adaptation thread started");
			// This are the ids that will be used to identify different
			// mechanism
			String refId = null;

			// here we put the result
			AdaptationResult ar = new AdaptationResult();

			logger.debug("Relevant services: "
					+ ap.getRelevantServices().toString());

			// more daptation strategies could be added here
			AdaptationGoal ag = null;
			GeneralAdaptationProblem gap = null;
			GeneralAdaptationResult gar = null;
			refId = apid;
			try {
				ag = verticalAdaptationGoal(ap.getCurrentAbstract(),
						ap.getMainProcess(), ap.getRelevantEntities());
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}

			// Generating adaptation process
			gap = new GeneralAdaptationProblem(apid, ap.getRelevantEntities(),
					ap.getRelevantServices(), ag, ap.getDomainObjectInstance());
			try {
				gar = generalAdaptation(gap, refId);
			} catch (Exception e1) {
				logger.error("Error during adaptation " + e1.getMessage(), e1);
			}
			// build relevant models
			List<DomainObjectDefinition> relevantModels = getModelsFromInstances(ap
					.getRelevantEntities());

			if (gar != null && gar.getAdaptationProcess() != null) {
				for (ProcessActivity act : gar.getAdaptationProcess()
						.getActivities()) {
					for (DomainObjectDefinition dod : relevantModels) {
						for (Fragment f : dod.getFragments()) {
							for (Action a : f.getAction()) {
								if (a.getName().equals(act.getName())) {
									act.setPrecondition(a.getPrecondition());
									act.setEffect(a.getEffect());
									act.setServiceActionVariables(a
											.getActionVariable());
									if (act.isAbstract()) {
										if (a.getGoal() != null) {
											// normal abstract fragment
											// action
											((AbstractActivity) act).setGoal(a
													.getGoal());
										} else {
											// abstract fragment action with
											// receivegoal
											if (a.getType() != null
													&& a.getReceiveVar() != null) {
												// hoaa case
												handleHoaa(a,
														(AbstractActivity) act);
												continue;
											}
											if (a.getReceiveGoal() == null) {
												logger.error("Abstract action with name "
														+ a.getName()
														+ " is without goal and receive goal");
												return;
											}
											// buildGoal is done after
											// return of AdaptationThread,
											// see AbstractActivityHandler
											((AbstractActivity) act)
													.setReceiveGoal(a
															.getReceiveGoal());

										}
									}
									ar.addUsedFragment(f);
								}
							}

						}
					}
				}
			}

			// we return null as next activity if there is no next activity
			try {
				ar.setStrategy(AdaptationStrategy.REFINEMENT);
				if (ap.getMainProcess().getNextActivity().size() > 0)
					ar.setRefinement(gar.getAdaptationProcess(), refId, ap
							.getMainProcess().getNextActivity().get(0));
				else
					ar.setRefinement(gar.getAdaptationProcess(), refId, null);
			} catch (Exception e1) {
				logger.error("Error during refinement next activity", e1);
			}

			try {
				am.registerResult(apid, ar);
			} catch (RegistryException e) {
				logger.error("Error during adaptation registry result ", e);
			}
			logger.debug("AdaptationThread completed");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void handleHoaa(Action a, AbstractActivity act) {
		act.setAbstractType(a.getType());
		act.setReceiveVar(a.getReceiveVar());
	}

	private List<DomainObjectDefinition> getModelsFromInstances(
			List<DomainObjectInstance> relevantEntities) {
		List<DomainObjectDefinition> response = new ArrayList<DomainObjectDefinition>();
		for (DomainObjectInstance doi : relevantEntities) {
			DomainObjectDefinition dod = am.getProcessEngine()
					.getDomainObjectDefinition(doi.getProcess());
			if (dod != null) {
				response.add(dod);
			}
		}
		return response;
	}

	public GeneralAdaptationResult generalAdaptation(
			GeneralAdaptationProblem gap, String id) throws IOException,
			CompositionDuplicateOidException, CompositionDuplicateSidException,
			InvalidCompositionPreconditionException,
			InvalidCompositionEffectException,
			InvalidObjectCurrentStateException,
			InvalidServiceCurrentStateException,
			InvalidServiceObjectAssignmentException,
			ServiceGroundingTypeMismatchException,
			InvalidFlowInitialStateException, InvalidFlowActivityException,
			JAXBException {
		logger.debug("General Adaptation - start");

		// create a composition spec using composition builder

		CompositionProblemBuilder builder = new CompositionProblemBuilder(
				workingFolder, workingFolder);

		if (gap.getGoal().getGoalPoints() != null) {
			builder.buildComposition(id, gap.getRelevantEntities(),
					gap.getRelevantServices(), gap.getGoal().getGoalPoints(),
					gap.getOrigin());
		} else {
			// SO FAR WE PLAN ONLY FOR ONE COMPENSATION GOAL
			builder.buildComposition(id, gap.getRelevantEntities(),
					gap.getRelevantServices(), gap.getGoal()
							.getCompensationGoalPoints().get(0),
					gap.getOrigin());

		}

		Parser parser = new Parser(workingFolder, workingFolder);
		CompositionProblem cp = parser.parseCompositionProblem(id);

		CompositionProblem ocp = cp.clone();

		logger.debug("Optimization started...");

		long startTime = System.currentTimeMillis();
		long endTime = System.currentTimeMillis();
		logger.debug("Optimization time: " + ((float) (endTime - startTime))
				/ 1000 + " seconds\n");

		startTime = System.currentTimeMillis();
		CompositionStatus cs = composer.compose(ocp, id, false, false, id);
		endTime = System.currentTimeMillis();

		logger.debug("Composition Status: " + composer.getStatusMessage(cs)
				+ "\n");
		logger.debug("Full Composition time: "
				+ ((float) (endTime - startTime)) / 1000 + " seconds\n");

		// if we know the adaptation failed we stop and return
		if (cs != CompositionStatus.OK)
			return new GeneralAdaptationResult(null, cp, ocp, cs);

		// Processing the result and creating process diagram
		List<ServiceTransitionGlobal> adaptationTransitions = DotParser
				.parseDotProcess(workingFolder + FILE_SEPARATOR
						+ "compositions" + FILE_SEPARATOR + id + FILE_SEPARATOR
						+ id + ".dot", ocp);

		ProcessDiagram adaptation = null;
		String refinedActivity = "";
		if (!adaptationTransitions.isEmpty()) {
			adaptation = ProcessDiagram.AdaptationToFlowModel(
					adaptationTransitions, adaptationTransitions.get(0)
							.getFrom(), ap.getMainProcess().getpid(),
					convertToListOfString(cp.getGlobalAbstracts()),
					convertToListOfString(cp.getGlobalConcretes()));
			refinedActivity = XMLAdapter.marshal(adaptation);
		}

		FileWriter writer;
		writer = new FileWriter(workingFolder + FILE_SEPARATOR + "Compositions"
				+ FILE_SEPARATOR + id + FILE_SEPARATOR + id + ".prc");
		writer.write(refinedActivity);
		writer.close();

		return new GeneralAdaptationResult(adaptation, cp, ocp, cs);
	}

	private List<String> convertToListOfString(
			List<ServiceAction> globalAbstracts) {
		List<String> response = new ArrayList<String>();
		for (ServiceAction serviceAction : globalAbstracts) {
			response.add(serviceAction.getAction());
		}
		return response;
	}

	public AdaptationGoal verticalAdaptationGoal(AbstractActivity abstractAct,
			ProcessDiagram proc, List<DomainObjectInstance> entities)
			throws IOException, JAXBException,
			CompositionDuplicateOidException, CompositionDuplicateSidException,
			InvalidCompositionPreconditionException,
			InvalidCompositionEffectException,
			InvalidObjectCurrentStateException,
			InvalidServiceCurrentStateException,
			InvalidServiceObjectAssignmentException,
			ServiceGroundingTypeMismatchException,
			InvalidFlowInitialStateException, InvalidFlowActivityException {

		GoalType goalJAXB = abstractAct.getGoal();

		// Parse the goals associated with the abstract activity to refine
		List<SyncPoint> goals = new ArrayList<SyncPoint>();
		for (int j = 0; j < goalJAXB.getPoint().size(); j++) {
			Point currentPoint = goalJAXB.getPoint().get(j);
			Map<String, List<String>> oid2States = new HashMap<String, List<String>>();
			for (int k = 0; k < currentPoint.getDomainProperty().size(); k++) {

				DomainProperty obj = currentPoint.getDomainProperty().get(k);
				String objName = obj.getDpName();
				List<String> objStates = obj.getState();
				oid2States.put(objName, objStates);
			}
			SyncPoint goal = new SyncPoint(oid2States);
			goals.add(goal);
		}

		Map<Integer, List<SyncPoint>> finalGoal = new HashMap<Integer, List<SyncPoint>>();
		finalGoal.put(0, goals);

		return new AdaptationGoal(finalGoal);
	}

}
