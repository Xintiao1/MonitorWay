package cn.mw.module.solarwind.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @author baochengbin
 * @date 2020/4/27
 */
@Data
@Builder
public class MwHistoryDTO {
    private Date date;
    private String value;

}
