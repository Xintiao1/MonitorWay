package cn.mw.monitor.hybridclouds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author qzg
 * @Date 2021/6/6
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BasicDto {
    private String name;
    private String value;
}
