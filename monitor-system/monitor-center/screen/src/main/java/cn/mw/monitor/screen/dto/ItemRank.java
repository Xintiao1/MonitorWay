package cn.mw.monitor.screen.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author xhy
 * @date 2020/6/3 17:25
 */
@Data
public class ItemRank implements Serializable {
    private static final long serialVersionUID = 6707209115473246564L;
    private List<ItemNameRank> itemNameRankList;
    private List<String> titleNode;
    private List<TitleRank> titleRanks;
}
