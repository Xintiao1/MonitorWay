package cn.mw.monitor.smartdisc.model;

import lombok.Data;

@Data
public class MWNmapExplore {

   //任务id
   private Integer id;
   //任务类型
   private Integer taskType;
   //任务名称
   private String taskName;
   //起止时间
   private String startEndTime;
   //探测范围
   private String detectRange;
   //端口范围
   private String portRange;
   //扫描次数
   private Integer detectTimes;
   //进度
   private String detectSchedule;
   //扫描结果个数
   private Integer resultCount;
   //任务详情
//   private MWNmapExploreTask taskDetails;

}
