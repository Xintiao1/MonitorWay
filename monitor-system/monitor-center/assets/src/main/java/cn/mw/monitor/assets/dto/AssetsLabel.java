package cn.mw.monitor.assets.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author syt
 * @Date 2021/7/25 13:13
 * @Version 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssetsLabel {
    private String labelName;
    private List<String> assetsIds;
}
