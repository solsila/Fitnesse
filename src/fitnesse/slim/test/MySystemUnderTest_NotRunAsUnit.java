package fitnesse.slim.test;

public class MySystemUnderTest_NotRunAsUnit {
    private boolean called;

    public void bar() {
        called = true;
    }


    public boolean systemUnderTestCalled() {
        return called;
    }

}
