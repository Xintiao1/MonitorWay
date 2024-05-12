package cn.mw.monitor.logManage.service.impl;

import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.logManage.param.LogAnalysisParam;
import cn.mw.monitor.logManage.service.MwLogManageService;
import cn.mw.monitor.logManage.utils.OkHttpUtils;
import cn.mw.monitor.logManage.vo.TableNameInfo;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MwLogManageServiceImpl implements MwLogManageService {

    private static final String URL_PREFIX = "http://127.0.0.1:8088/mwapi/logManage/logAnalysis/";

    private static final String USER_SPLIT = "Ω";

    @Autowired
    private MWUserService mwUserService;

    @Override
    public List<TableNameInfo> getTables() {
        String url = URL_PREFIX + "getTables";
        String result = OkHttpUtils.httpGet(url);
        JSONObject jsonObject = JSON.parseObject(result);
        Object data = jsonObject.get("data");
        Integer rtnCode = (Integer) jsonObject.get("rtnCode");
        if (Constants.HTTP_RES_CODE_200.equals(rtnCode)) {
            List<TableNameInfo> list = JSON.parseArray(JSON.toJSONString(data), TableNameInfo.class);
            return list;
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> getColumnByTable(String tableName) {

        String url = URL_PREFIX + "getColumnByTable/" + tableName;
        String result = OkHttpUtils.httpGet(url);
        JSONObject jsonObject = JSON.parseObject(result);
        Object data = jsonObject.get("data");
        Integer rtnCode = (Integer) jsonObject.get("rtnCode");
        if (Constants.HTTP_RES_CODE_200.equals(rtnCode)) {
            List<Map<String, Object>> mapList = JSONObject.parseObject(JSON.toJSONString(data), new TypeReference<List<Map<String, Object>>>() {
            });
            return mapList;
        }
        return null;
    }

    @Override
    public Object list(LogAnalysisParam logAnalysisParam) {
        String url = URL_PREFIX + "list";

        String userIdOrUserName = getUserIdOrUserName();
        logAnalysisParam.setUserIdOrName(userIdOrUserName);

        String result = OkHttpUtils.httpPostJson(url, JSON.toJSONString(logAnalysisParam));
        JSONObject jsonObject = JSON.parseObject(result);
        Object data = jsonObject.get("data");
        Integer rtnCode = (Integer) jsonObject.get("rtnCode");
        if (Constants.HTTP_RES_CODE_200.equals(rtnCode)) {
            return data;
        }
        return data;
    }

    @Override
    public Object logAnalysisChar(LogAnalysisParam logAnalysisParam) {
        String url = URL_PREFIX + "chat/browse";
        // 获取用户
        String userIdOrUserName = getUserIdOrUserName();
        logAnalysisParam.setUserIdOrName(userIdOrUserName);
        String result = OkHttpUtils.httpPostJson(url, JSON.toJSONString(logAnalysisParam));
        JSONObject jsonObject = JSON.parseObject(result);
        Object data = jsonObject.get("data");
        Integer rtnCode = (Integer) jsonObject.get("rtnCode");
        if (Constants.HTTP_RES_CODE_200.equals(rtnCode)) {
            return data;

        }
        return data;
    }

    @Override
    public String saveSelectedColumns(String columnsParam) {
        log.info("保存字段接口入参为：{}", columnsParam);
        JSONObject jsonParam = JSONObject.parseObject(columnsParam);
        String paramStr = (String) jsonParam.get("columnsParam");
        if (StringUtils.isEmpty(paramStr)) {
            return "保存失败,参数为空";
        }
        String userIdOrUserName = getUserIdOrUserName();
        if (StringUtils.isEmpty(userIdOrUserName)) {
            log.error("用户未登录");
            return "用户未登录";
        }
        paramStr = paramStr + USER_SPLIT + userIdOrUserName;
        String url = URL_PREFIX + "saveSelectedColumns";
        String result = OkHttpUtils.httpPostJson(url, JSON.toJSONString(paramStr));
        JSONObject jsonObject = JSON.parseObject(result);
        Integer rtnCode = (Integer) jsonObject.get("rtnCode");
        if (Constants.HTTP_RES_CODE_200.equals(rtnCode)) {
            return "success";
        }
        return "保存失败";
    }

    @Override
    public Object getLogAnalysisCacheInfo() {
        String url = URL_PREFIX + "getLogAnalysisCacheInfo";
        String userIdOrUserName = getUserIdOrUserName();
        String result = OkHttpUtils.httpPostJson(url, JSON.toJSONString(userIdOrUserName));
        JSONObject jsonObject = JSON.parseObject(result);
        Object data = jsonObject.get("data");
        Integer rtnCode = (Integer) jsonObject.get("rtnCode");
        if (Constants.HTTP_RES_CODE_200.equals(rtnCode)) {
            return data;
        }
        return data;
    }

    public String getUserIdOrUserName() {
        GlobalUserInfo user = mwUserService.getGlobalUser();
        if (ObjectUtils.isNotEmpty(user)) {
            if (ObjectUtils.isNotEmpty(user.getUserId())) {
                return String.valueOf(user.getUserId());
            }
            if (StringUtils.isNotEmpty(user.getUserName())) {
                return user.getUserName();
            }
        }
        throw new RuntimeException("未找到登录用户");
    }
}
