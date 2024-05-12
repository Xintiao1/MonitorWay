package cn.mw.monitor.model.util;

import cn.mw.monitor.graph.modelAsset.ComboParam;
import cn.mw.monitor.graph.modelAsset.LastData;
import cn.mw.monitor.graph.modelAsset.ModelRelationInfo;
import cn.mw.monitor.model.dto.ModelType;
import cn.mw.monitor.model.param.AddAndUpdateModelRelationParam;
import cn.mw.monitor.service.graph.EdgeParam;
import cn.mw.monitor.service.graph.NodeParam;
import cn.mwpaas.common.constant.DateConstant;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author xhy
 * @date 2021/2/25 16:50
 */
@Slf4j
public class ModelUtils {
    public static final String OWN_KEY = "own";
    public static final String RELATION_KEY = "r";
    public static final String OPPO_KEY = "oppo";
    public static HashMap<Integer, ModelType> modelTypeHashMap = new HashMap<>();

    static {
        ModelType[] values = ModelType.values();
        for (int i = 0; i < values.length; i++) {
            modelTypeHashMap.put(values[i].getTypeId(), values[i]);
        }
    }

    public static ModelRelationInfo genOwnModelRelationInfo(AddAndUpdateModelRelationParam param) {
        ModelRelationInfo modelRelationInfo = new ModelRelationInfo();
        modelRelationInfo.setModelId(param.getOwnModelId());
        modelRelationInfo.setRelationName(param.getOwnRelationName());
        modelRelationInfo.setNum(param.getOwnRelationNum());
        modelRelationInfo.setId(param.getOwnRelationId());
        return modelRelationInfo;
    }

    public static ModelRelationInfo genOppoModelRelationInfo(AddAndUpdateModelRelationParam param) {
        ModelRelationInfo modelRelationInfo = new ModelRelationInfo();
        modelRelationInfo.setModelId(param.getOppositeModelId());
        modelRelationInfo.setRelationName(param.getOppositeRelationName());
        modelRelationInfo.setNum(param.getOppositeRelationNum());
        modelRelationInfo.setId(param.getOppositeRelationId());
        return modelRelationInfo;
    }

    public static String genRelationKey(String startId, String endId) {
        List<String> ids = new ArrayList<>();
        ids.add(startId);
        ids.add(endId);
        Collections.sort(ids);
        String key = StringUtils.join(ids, "-");
        return key;
    }

    public static LastData sortLastData(NodeParam start, List<EdgeParam> newEdges, List<NodeParam> origNodeParams, List<ComboParam> comboParams) {
        //把边按照modelId分组
        Map<String, List<EdgeParam>> edgeMap = new HashMap<>();
        for (EdgeParam edgeParam : newEdges) {
            List<EdgeParam> list = edgeMap.get(edgeParam.getSource());
            if (null == list) {
                list = new ArrayList<>();
                edgeMap.put(edgeParam.getSource(), list);
            }
            list.add(edgeParam);
        }

        Map<String, NodeParam> nodeParamMap = origNodeParams.stream().collect(Collectors.toMap(NodeParam::getId, Function.identity()));

        List<NodeParam> nodeParams = new ArrayList<>();
        nodeParams.add(start);
        List<EdgeParam> sortEdges = ModelUtils.doFillNodeParamsAndSortEdge(start, nodeParams, edgeMap);
        Map<Integer, ComboParam> comboParamMap = comboParams.stream().collect(Collectors.toMap(ComboParam::getId, Function.identity()));
        List<ComboParam> sortComboParam = ModelUtils.sortComboParams(nodeParams, comboParamMap);

        for (NodeParam nodeParam : nodeParams) {
            NodeParam oriNodeParam = nodeParamMap.get(nodeParam.getId());
            if (null != oriNodeParam) {
                nodeParam.setLabel(oriNodeParam.getLabel());
            }
        }

        LastData lastData = new LastData();
        lastData.setNodes(nodeParams);
        lastData.setEdges(sortEdges);
        lastData.setCombos(sortComboParam);

        return lastData;
    }

    public static List<EdgeParam> doFillNodeParamsAndSortEdge(NodeParam start, List<NodeParam> nodeParams, Map<String, List<EdgeParam>> edgeMap) {
        List<EdgeParam> sortEdges = new ArrayList<>();
        Queue<NodeParam> queue = new LinkedList();
        queue.add(start);
        while (!queue.isEmpty()) {
            NodeParam nodeParam = queue.remove();
            List<EdgeParam> list = edgeMap.get(nodeParam.getId());
            if (null != list) {
                Comparator<EdgeParam> comparatorEdge = Comparator.comparing(EdgeParam::getSource).thenComparing(EdgeParam::getTarget);
                Collections.sort(list, comparatorEdge);
                sortEdges.addAll(list);
                for (EdgeParam edgeParam : list) {
                    NodeParam target = new NodeParam(edgeParam.getTarget());
                    target.setLevel(nodeParam.getLevel() + 1);
                    nodeParams.add(target);
                    queue.add(target);
                }
            }
        }
        return sortEdges;
    }

    public static List<ComboParam> sortComboParams(List<NodeParam> nodeParams, Map<Integer, ComboParam> comboParamMap) {
        List<ComboParam> comboParams = new ArrayList<>();
        for (NodeParam nodeParam : nodeParams) {
            ComboParam comboParam = comboParamMap.get(nodeParam.getComboId());
            if (null != comboParam && !comboParams.contains(comboParam)) {
                comboParams.add(comboParam);
            }
        }
        return comboParams;
    }

    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    public static Map<String, Object> JSONObjectToMap(Object map_key_value) {
        Map<String, Object> label = new HashMap<>();
        if (map_key_value == null) {
            return label;
        } else {
            JSONObject jsonObject = JSON.parseObject(map_key_value.toString());
            for (Map.Entry<String, Object> s : jsonObject.entrySet()) {
                label.put(s.getKey(), s.getValue());
            }
            return label;
        }
    }

    public static final String reg = "^(([0])|([1-9]+[0-9]*.{1}[0-9]+)|([0].{1}[1-9]+[0-9]*)|([1-9][0-9]*)|([0][.][0-9]+[1-9]+))$";

    public static final String scientificNotationRegex = "[-+]?[0-9]+(\\.[0-9]+)?[eE][-+]?[0-9]+";


    /**
     * 获取字符串是否是数字
     *
     * @param value
     * @return
     */
    public static boolean checkStrIsNumber(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        if (value.matches(reg) || value.matches(scientificNotationRegex)) {
            return true;
        }
        return false;
    }



    public static String getFormatDate(Long time) {
        Date date = new Date();
        date.setTime(time);
        SimpleDateFormat formatter = new SimpleDateFormat(DateConstant.NORM_DATETIME);
        return formatter.format(date);
    }
}
