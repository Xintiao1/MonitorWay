package cn.mw.monitor.service.rule;


public class TestSubNode extends TestNode {
    private String test;

    public TestSubNode(){
        super("line");
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }
}
