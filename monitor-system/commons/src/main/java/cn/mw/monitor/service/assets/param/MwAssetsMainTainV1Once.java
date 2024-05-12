package cn.mw.monitor.service.assets.param;

import lombok.Data;

import java.util.List;

@Data
public class MwAssetsMainTainV1Once{
    private String startDay;

    private List<String> onceDates;
}
