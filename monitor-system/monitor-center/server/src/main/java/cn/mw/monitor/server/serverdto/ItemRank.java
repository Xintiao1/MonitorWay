package cn.mw.monitor.server.serverdto;

import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2020/5/8 14:36
 * @Version 1.0
 */
@Data
public class ItemRank {
    private String lastTime;
    private List<ItemNameRank> itemNameRankList;
}
