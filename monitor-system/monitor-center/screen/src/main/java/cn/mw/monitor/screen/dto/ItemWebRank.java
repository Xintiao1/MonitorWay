package cn.mw.monitor.screen.dto;

import lombok.Data;

import java.util.Comparator;

/**
 * @author xhy
 * @date 2020/5/4 17:07
 */
@Data
public class ItemWebRank implements Comparator<ItemWebRank> {
    private String webName;
    private String ipAddress;
    private String lastValue;


    @Override
    public int compare(ItemWebRank o1, ItemWebRank o2) {
        return o1.getLastValue().compareTo(o2.getLastValue());
    }
}
