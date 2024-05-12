package cn.mw.monitor.ipaddressmanage.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;


/**
 * @author baochengbin
 * @date 2020/3/30
 */
@Data
@Accessors(chain = true)
public class IpamProcessHistoryDTO {


    private Integer id;

    private Integer applicant;

    private Date applicantDate;
}
