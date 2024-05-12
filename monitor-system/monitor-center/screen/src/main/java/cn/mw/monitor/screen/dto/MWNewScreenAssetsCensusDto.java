package cn.mw.monitor.screen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName MWNewScreenAssetsCensusDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/11/29 14:56
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MWNewScreenAssetsCensusDto {

    private List<Integer> count;

    private List<String> date;

    private Integer total;

    private Integer compareYesterday;
}
