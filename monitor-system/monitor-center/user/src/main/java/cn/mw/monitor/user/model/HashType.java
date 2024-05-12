package cn.mw.monitor.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by dev on 2020/2/14.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HashType {
    private Integer id;
    private String hashName;
    private String implClass;
    private Integer iterations;
}
