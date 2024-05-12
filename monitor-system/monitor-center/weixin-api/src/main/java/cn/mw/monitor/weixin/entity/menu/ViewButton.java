package cn.mw.monitor.weixin.entity.menu;

/**
 * @author bkc
 * @create 2020-07-01 11:32
 */
public class ViewButton extends AbstractButton {

    private String type;
    private String url;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ViewButton(String name, String url) {
        super(name);
        this.type = "view";
        this.url = url;
    }
}
