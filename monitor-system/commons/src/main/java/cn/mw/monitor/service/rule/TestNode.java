package cn.mw.monitor.service.rule;


public class TestNode implements TestInterface{
    protected String elementType;

    public TestNode(String elementType){
        this.elementType = elementType;
    }

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }
}
