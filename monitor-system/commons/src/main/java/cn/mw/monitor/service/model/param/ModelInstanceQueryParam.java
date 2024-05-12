package cn.mw.monitor.service.model.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author qzg
 * @date 2021/12/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModelInstanceQueryParam {
    private String queryField;
    private String queryValue;
}
