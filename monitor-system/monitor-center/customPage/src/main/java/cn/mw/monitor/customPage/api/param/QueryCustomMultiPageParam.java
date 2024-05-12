package cn.mw.monitor.customPage.api.param;

import lombok.Data;

import java.util.List;

@Data
public class QueryCustomMultiPageParam {

    private Integer userId;

    private List<Integer> pageIds;

}
