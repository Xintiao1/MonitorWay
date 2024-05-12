package cn.mw.monitor.service.alert.dto;

import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author
 * @date
 */

public class MWAlertLevelParam {

    public static ConcurrentHashMap<String,String> alertLevelMap = new ConcurrentHashMap<>();

    public static LinkedHashMap<String,String> actionAlertLevelMap = new LinkedHashMap<>();

    public static List<Integer> severities = new ArrayList<>();
}
