package eu.fbk.das.adaptation;

import org.junit.Test;

import eu.fbk.das.adaptation.api.AdaptationManagerInterface;

public class AdaptationManagerTest {

    private AdaptationManagerInterface am;

    @Test(expected = NullPointerException.class)
    public void nullAdaptationManagerTest() {
	am = new AdaptationManager(null);
	am.submitProblem(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void notSolvableAdaptationProblemTest() {
	am = new AdaptationManager(null);
	am.submitProblem(null, null);
    }
}
