package cn.mw.monitor.weixin.entity.menu;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bkc
 * @create 2020-07-01 10:59
 */
public class Button {

    private List<AbstractButton> button = new ArrayList<>();

    public List<AbstractButton> getButton() {
        return button;
    }

    public void setButtons(List<AbstractButton> button) {
        this.button = button;
    }
}
