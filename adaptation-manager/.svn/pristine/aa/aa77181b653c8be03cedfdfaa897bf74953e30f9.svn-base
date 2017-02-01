package eu.fbk.das.adaptation;

import eu.fbk.das.composer.api.CompositionProblem;
import eu.fbk.das.composer.api.CompositionStatus;
import eu.fbk.das.composer.api.elements.Precondition;
import eu.fbk.das.composer.api.elements.ServiceAction;
import eu.fbk.das.process.engine.api.domain.ProcessDiagram;

/**
 * Represents the result of the adaptation done by AdaptationManager
 */
public class GeneralAdaptationResult {
    private ProcessDiagram adaptationProcess;
    private CompositionProblem initialProblem = null;
    private CompositionProblem optimizedProblem = null;
    private CompositionStatus compositionStatus;

    public GeneralAdaptationResult(ProcessDiagram adaptationProcess) {
	this.adaptationProcess = adaptationProcess;
    }

    public GeneralAdaptationResult(ProcessDiagram adaptationProcess,
	    CompositionProblem initialProblem,
	    CompositionProblem optimizedProblem,
	    CompositionStatus compositionStatus) {
	this.adaptationProcess = adaptationProcess;
	this.initialProblem = initialProblem;
	this.optimizedProblem = optimizedProblem;
	this.compositionStatus = compositionStatus;
    }

    public ProcessDiagram getAdaptationProcess() {
	return adaptationProcess;
    }

    public CompositionProblem getInitialProblem() {
	return initialProblem;
    }

    public CompositionProblem getOptimizedProblem() {
	return optimizedProblem;
    }

    public CompositionStatus getCompositionStatus() {
	return compositionStatus;
    }

    public Precondition getViolatedPrecondition() {
	Precondition prec = null;
	if (initialProblem.getNextActions() != null) {
	    ServiceAction a = initialProblem.getNextActions().get(0);
	    for (Precondition p : initialProblem.getPreconditions()) {
		if (p.getAction().equals(a.getAction())
			&& p.getSid().equals(a.getSid())) {
		    prec = p;
		    break;
		}
	    }
	}
	return prec;
    }

}
