package cn.joinhealth.echarts.echart;

import cn.joinhealth.echarts.entity.BarSimple;
import cn.joinhealth.echarts.entity.ExtMapData;

import java.util.ArrayList;
import java.util.List;

/**
 * 统计资产
 */
public abstract class AbstractBarCount extends BsaeEchartsBean<BarSimple> {
    protected abstract List<String> getLegendList();

    protected abstract List<ExtMapData<String, String>> getSeriesData();

    public BarSimple initEchartsData() {
        List<Object> serieDataList = new ArrayList<Object>();
        BarSimple barSimple = new BarSimple();
        List<String> legendList = getLegendList();
        List<ExtMapData<String, String>> listData = getSeriesData();
        List<String> xAxisDataList = new ArrayList<String>();

        List<List<Object>> serieDataListSum = new ArrayList<List<Object>>();

        for (ExtMapData<String, String> extMapData : listData) {
            xAxisDataList.add(String.valueOf(extMapData.getName()));
            serieDataList.add(String.valueOf(extMapData.getValue()));
        }

        barSimple.setLegendData(legendList);
        barSimple.setXAxisData(xAxisDataList);
        serieDataListSum.add(serieDataList);
        barSimple.setSeriesData(serieDataListSum);
        return barSimple;
    }
}
