package eu.fbk.das.adaptation.api;

import eu.fbk.das.adaptation.RegistryException;
import eu.fbk.das.process.engine.api.AdaptationProblem;
import eu.fbk.das.process.engine.api.AdaptationResult;
import eu.fbk.das.process.engine.api.ProcessEngine;

/**
 * Interface for AdaptationManager. <br/>
 * An AdaptationManager is a system that receive an {@link AdaptationProblem}
 * and provide after some time an {@link AdaptationResult}. During execution
 * AdaptationManager could interact with {@link ProcessEngine} to get runtime
 * information about processes execution
 * 
 * @see AdaptationProblem
 * @see AdaptationResult
 * @see ProcessEngine
 */
public interface AdaptationManagerInterface {

    /**
     * This method is used to submit the problem to the adaptation manager. It
     * uses threads to parallelize the process of solution derivation.
     * Consequently, it returns almost immediately. All the adaptation specific
     * tasks are processed within a separate thread. A getRusult method is used
     * to obtain the adaptation result.
     * 
     * @param problem
     *            contains full information on the process to be adapted and its
     *            execution environment
     * @param workingFolder
     * @return an id under which the adaptation is registered within the
     *         adaptation manager
     */
    public String submitProblem(AdaptationProblem problem, String workingFolder)
	    throws NullPointerException;

    /**
     * This method is used to check the status of the resolution of an
     * adaptation problem previously submitted using submitProblem.
     * 
     * @param adaptationId
     *            is the id under which the problem was registered
     * @return null if the problem has not yet been solved and AdaptationResult
     *         structure if the solution has already been found.
     */
    public AdaptationResult getResult(String adaptationId);

    /**
     * Set used ProcessEngine
     * 
     * @param pe
     */
    public void setProcessEngine(ProcessEngine pe);

    /**
     * @return used ProcessEngine
     */
    public ProcessEngine getProcessEngine();

    /**
     * 
     * @param apid
     *            name of the problem
     * @param ar
     *            adaptation result to register
     * @throws RegistryException
     * 
     * @see {@link AdaptationResult}
     */
    public void registerResult(String apid, AdaptationResult ar)
	    throws RegistryException;

}