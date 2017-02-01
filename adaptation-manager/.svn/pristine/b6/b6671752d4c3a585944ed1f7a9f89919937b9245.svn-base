package eu.fbk.das.adaptation;

import java.util.List;
import java.util.Map;

import eu.fbk.das.composer.api.elements.ServiceAction;
import eu.fbk.das.composer.api.elements.SyncPoint;

/**
 * Adaptation Goal definition, used by {@link AdaptationThread}
 */
public class AdaptationGoal {
    private ServiceAction nextAction = null;
    private Map<Integer, List<SyncPoint>> goalPoints = null;
    private List<Map<Integer, List<SyncPoint>>> compensationGoalPoints = null;

    public AdaptationGoal(ServiceAction nextAction) {
	this.nextAction = nextAction;
    }

    public AdaptationGoal(Map<Integer, List<SyncPoint>> goalPoints) {
	this.goalPoints = goalPoints;
    }

    public AdaptationGoal(
	    List<Map<Integer, List<SyncPoint>>> compensationGoalPoints) {
	this.compensationGoalPoints = compensationGoalPoints;
    }

    public ServiceAction getNextAction() {
	return nextAction;
    }

    public Map<Integer, List<SyncPoint>> getGoalPoints() {
	return goalPoints;
    }

    public List<Map<Integer, List<SyncPoint>>> getCompensationGoalPoints() {
	return compensationGoalPoints;
    }

}
