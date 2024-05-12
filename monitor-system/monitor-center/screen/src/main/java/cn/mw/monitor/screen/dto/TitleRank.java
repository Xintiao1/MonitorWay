package cn.mw.monitor.screen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xhy
 * @date 2020/6/18 8:54
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TitleRank {
   private String name;
   private String fieldName;
}
