package eu.fbk.das.adaptation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.fbk.das.adaptation.api.AdaptationManagerInterface;
import eu.fbk.das.composer.api.ComposerInterface;
import eu.fbk.das.process.engine.api.AdaptationProblem;
import eu.fbk.das.process.engine.api.AdaptationResult;
import eu.fbk.das.process.engine.api.ProcessEngine;

/**
 * This class contains the logics of the strategy manager for CAptEvo framework
 */
public class AdaptationManager implements AdaptationManagerInterface {

    private static final Logger logger = LogManager
	    .getLogger(AdaptationManager.class);
    private final int MAX_THREADS = 2;
    private static int adaptId = 0;
    /**
     * is used to execute threads
     */
    private ExecutorService executor;

    /**
     * contains the resolution status of all the adaptation problems currently
     * being processed
     */
    private Map<String, AdaptationResult> adaptationRegistry = new HashMap<String, AdaptationResult>();

    private ComposerInterface composer;

    private ProcessEngine processEngine;

    public AdaptationManager(ComposerInterface composer) {
	this.composer = composer;
	executor = Executors.newFixedThreadPool(MAX_THREADS);
    }

    /**
     * registers an adaptation problem within the adaptation manager manager
     * 
     * @param apid
     *            an identifier of the problem to be registered. It is later
     *            used to retrieve the result
     * @param problem
     */
    private void registerProblem(String apid) {
	try {
	    logger.debug("Registered problem with id: " + apid);
	    synchronized (adaptationRegistry) {
		adaptationRegistry.put(apid, null);
	    }
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	}
    }

    /**
     * registers the result for the existing problem
     * 
     * @param apid
     * @param ar
     * @throws RegistryException
     */
    @Override
    public void registerResult(String apid, AdaptationResult ar)
	    throws RegistryException {
	logger.debug("Registered result for problem with id: " + apid);
	synchronized (adaptationRegistry) {
	    if (!adaptationRegistry.containsKey(apid))
		throw new RegistryException();
	    adaptationRegistry.put(apid, ar);
	}
    }

    /**
     * is used to get the adaptation result, if it is ready. The adaptation
     * result is place to the registry, by adaptation threads.
     * 
     * @param apid
     *            problem identifier
     * @return adaptation result structure for a problem with id apid, or null,
     *         if the result is not yet ready
     */
    private AdaptationResult extractResult(String apid) {
	synchronized (adaptationRegistry) {
	    AdaptationResult ar = adaptationRegistry.get(apid);
	    if (ar != null) {
		adaptationRegistry.remove(apid);
	    }
	    return ar;
	}
    }

    /**
     * @return a new unique id
     */
    private String getAdaptationId() {
	return "AP_" + adaptId++;
    }

    @Override
    public String submitProblem(AdaptationProblem problem, String workingFolder)
	    throws NullPointerException {
	if (problem == null) {
	    logger.error("Adaptation problem must be not null");
	    throw new NullPointerException(
		    "Adaptation problem must be not null");
	}
	String id = getAdaptationId();
	// registers the problem on the registry
	registerProblem(id);

	// Here we create a new adaptation thread and submit it to the pool
	AdaptationThread aThread = new AdaptationThread(problem, this, id,
		composer, workingFolder);
	executor.execute(aThread);
	logger.debug("AdaptationThread created correctly");
	return id;
    }

    @Override
    public AdaptationResult getResult(String adaptationId) {
	return extractResult(adaptationId);
    }

    @Override
    public void setProcessEngine(ProcessEngine pe) {
	this.processEngine = pe;
    }

    @Override
    public ProcessEngine getProcessEngine() {
	return processEngine;
    }

}
