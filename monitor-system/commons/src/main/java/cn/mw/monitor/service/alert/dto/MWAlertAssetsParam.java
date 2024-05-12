package cn.mw.monitor.service.alert.dto;

import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import org.apache.shiro.crypto.hash.Hash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author
 * @date
 */

public class MWAlertAssetsParam {
    //改成ConcurrentHashMap
    public static ConcurrentHashMap<String,MwTangibleassetsDTO> tangibleassetsDTOMap = new ConcurrentHashMap<String,MwTangibleassetsDTO>();
    public static ConcurrentHashMap<String,List<MwAssetsLabelDTO>> mwAssetsLabelDTOMap = new ConcurrentHashMap<>();

}
