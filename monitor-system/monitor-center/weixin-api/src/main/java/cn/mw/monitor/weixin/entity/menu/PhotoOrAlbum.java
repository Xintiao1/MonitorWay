package cn.mw.monitor.weixin.entity.menu;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bkc
 * @create 2020-07-01 11:48
 */
public class PhotoOrAlbum extends AbstractButton{

    private String type;
    private String key;
    private List<AbstractButton> sub_button ;

    public PhotoOrAlbum(String name, String key) {
        super(name);
        this.type = "pic_photo_or_album";
        this.key = key;
        sub_button = new ArrayList<>();
    }

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

    public List<AbstractButton> getSub_button() {
        return sub_button;
    }

    public void setSub_button(List<AbstractButton> sub_button) {
        this.sub_button = sub_button;
    }
}
