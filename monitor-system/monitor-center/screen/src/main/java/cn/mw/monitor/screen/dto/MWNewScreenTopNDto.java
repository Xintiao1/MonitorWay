package cn.mw.monitor.screen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @ClassName MWNewScreenTopNDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/11/30 15:22
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MWNewScreenTopNDto {

    private List<ItemNameRank> itemNameRankList;
    private List<String> titleNode;
    private List<TitleRank> titleRanks;
    private List<Map<String,Object>> title;
}
