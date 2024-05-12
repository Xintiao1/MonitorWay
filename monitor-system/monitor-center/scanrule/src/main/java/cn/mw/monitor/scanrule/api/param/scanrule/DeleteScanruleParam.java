package cn.mw.monitor.scanrule.api.param.scanrule;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/17
 */
@Data
public class DeleteScanruleParam {

    List<Integer> idList = new ArrayList<>();
}
