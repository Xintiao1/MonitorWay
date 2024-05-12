package cn.mw.monitor.service.assets.param;

import lombok.Data;

import java.util.List;

@Data
public class MwAssetsMainTainV1Month{

    private List<Integer> monthIndexes;

    private List<Integer> weekIndexPerMonth;

    private List<Integer> weekIndexes;

    private List<Integer> dayIndexes;
}
