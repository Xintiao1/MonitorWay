package cn.mw.monitor.credential.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Created by zy.quaee on 2021/6/2 14:25.
 **/
@Data
@Builder
public class MwSNMPCredDTO {

    private List<String> portList;

    private List<String> commNameList;
}
