package cn.mw.monitor.weixin.entity.menu;

/**
 * @author bkc
 * @create 2020-07-01 11:27
 */
public class ClickButton extends AbstractButton{

    private String type;
    private String key;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ClickButton(String name,String key) {
        super(name);
        this.type = "click";
        this.key = key;
    }
}
