package cn.mw.monitor.service.server.api.dto;

import lombok.*;

/**
 * @author xhy
 * @date 2020/4/29 9:21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NetListInfoDto {
    private Long allNum;
    private Long upNum;
    private Long downNum;
}
