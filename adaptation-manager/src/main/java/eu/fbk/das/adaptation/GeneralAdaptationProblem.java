package eu.fbk.das.adaptation;

import java.util.List;
import java.util.Map;

import eu.fbk.das.process.engine.api.DomainObjectInstance;

/**
 * Represents a general adaptation problem that contains all information that is
 * passed directly to the domain builder.
 */
public class GeneralAdaptationProblem {
    private String apid;
    private List<DomainObjectInstance> relevantEntities;
    private Map<String, List<String>> relevantServices;
    private AdaptationGoal goal;
    private DomainObjectInstance origin;

    public GeneralAdaptationProblem(String apid,
	    List<DomainObjectInstance> relevantEntities,
	    Map<String, List<String>> relevantServices, AdaptationGoal goal,
	    DomainObjectInstance origin) {
	this.apid = apid;
	this.relevantEntities = relevantEntities;
	this.relevantServices = relevantServices;
	this.goal = goal;
	this.origin = origin;
    }

    public String getApid() {
	return apid;
    }

    public List<DomainObjectInstance> getRelevantEntities() {
	return relevantEntities;
    }

    public Map<String, List<String>> getRelevantServices() {
	return relevantServices;
    }

    public AdaptationGoal getGoal() {
	return goal;
    }

    public DomainObjectInstance getOrigin() {
	return origin;
    }

}
