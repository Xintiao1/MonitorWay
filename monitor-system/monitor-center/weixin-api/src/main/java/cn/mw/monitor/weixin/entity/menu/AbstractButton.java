package cn.mw.monitor.weixin.entity.menu;

/**
 * @author bkc
 * @create 2020-07-01 11:00
 */
public abstract  class AbstractButton {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AbstractButton(String name) {
        this.name = name;
    }
}
