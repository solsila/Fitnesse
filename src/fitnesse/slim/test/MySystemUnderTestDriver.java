package fitnesse.slim.test;

import fitnesse.slim.SystemUnderTestInterface;

public class MySystemUnderTestDriver {

    @SystemUnderTestInterface
    public MySystemUnderTest_NotRunAsUnit systemUnderTest = new MySystemUnderTest_NotRunAsUnit();

    private boolean called;

    public void foo() {
        called = true;
    }

    public boolean driverCalled() {
        return called;
    }
}
