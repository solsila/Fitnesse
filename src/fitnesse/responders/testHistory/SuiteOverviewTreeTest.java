package fitnesse.responders.testHistory;

import org.junit.Test;

import java.util.LinkedList;

public class SuiteOverviewTreeTest {


    @SuppressWarnings("deprecation")
    @Test
    public void testTestsInSameDirectoryAppearOnSameLevel() {
        LinkedList<String> pageNames = new LinkedList<String>();
        pageNames.add("FitNesse.MyTests.Test1");
        pageNames.add("FitNesse.MyTests.Test2");
        pageNames.add("FitNesse.MyTests.Test3");

        SuiteOverviewTree tree = new SuiteOverviewTree(pageNames, null);

    }

    @SuppressWarnings("deprecation")
    @Test
    public void testTestsInDiffDirectoryAreSplit() {
        LinkedList<String> pageNames = new LinkedList<String>();
        pageNames.add("FitNesse.MyTestsA.Test1");
        pageNames.add("FitNesse.MyTestsA.Test2");
        pageNames.add("FitNesse.MyTestsB.Test3");

        SuiteOverviewTree tree = new SuiteOverviewTree(pageNames, null);

    }
}
