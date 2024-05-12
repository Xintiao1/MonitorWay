package cn.mw.monitor.model.param.superfusion;

import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/8/2
 */
@Data
public class QuerySuperFusionHistoryParam {
   //节点Id
   private String nodeId;
   //net(流速趋势)、cpu(cpu趋势)、mem(内存趋势)、io_speed(IO速率趋势)、io_oper(IO次数趋势)
   private String dataType;
   //hour，day，month
   private String timeFrame;
   //父节点Id
   private String pId;
}
