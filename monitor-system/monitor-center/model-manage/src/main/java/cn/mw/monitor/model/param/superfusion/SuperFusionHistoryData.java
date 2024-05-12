package cn.mw.monitor.model.param.superfusion;

import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/8/2
 */
@Data
public class SuperFusionHistoryData {
   private List<String> data;
   private List<String> time;
   private String units;
   private String name;
}
