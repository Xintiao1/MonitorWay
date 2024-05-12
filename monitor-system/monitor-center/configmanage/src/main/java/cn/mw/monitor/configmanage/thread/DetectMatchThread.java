package cn.mw.monitor.configmanage.thread;

import cn.mw.monitor.configmanage.common.ConfigType;
import cn.mw.monitor.configmanage.common.DetectType;
import cn.mw.monitor.configmanage.dao.MwConfigManageTableDao;
import cn.mw.monitor.configmanage.entity.RuleManage;
import cn.mw.monitor.configmanage.service.EncryptUtil;
import cn.mw.monitor.weixinapi.DelFilter;
import cn.mw.monitor.weixinapi.MessageContext;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author gui.quanwang
 * @className DetectMatchThread
 * @description 扫描匹配线程
 * @date 2021/12/31
 */
@Slf4j
public class DetectMatchThread implements Runnable {

    /**
     * 执行失败
     */
    private final int EXEC_FAIL = 3;
    /**
     * 执行结束
     */
    private final int EXEC_FINISH = 2;
    /**
     * 匹配失败，无法识别
     */
    private final int MATCH_ERROR = 0;
    /**
     * 匹配成功
     */
    private final int MATCH_SUCCESS = 1;

    /**
     * 配置管理数据库操作
     */
    private MwConfigManageTableDao configManageTableDao;

    /**
     * 资产ID
     */
    private String assetsId;

    /**
     * 执行日志ID
     */
    private int logId;

    /**
     * 规则数据
     */
    private RuleManage rule;

    /**
     * 配置类别
     */
    private ConfigType configType;

    public DetectMatchThread(MwConfigManageTableDao configManageTableDao, String assetsId, int logId, RuleManage rule, ConfigType configType) {
        this.configManageTableDao = configManageTableDao;
        this.assetsId = assetsId;
        this.logId = logId;
        this.rule = rule;
        this.configType = configType;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        //匹配状态
        boolean matchStatus = false;
        //获取当前资产是否存在配置数据
        String fileName = configManageTableDao.getLastFileName(assetsId, configType.getDataBaseValue());
        String content = showTxt(fileName);
        if (StringUtils.isEmpty(content)) {
            log.error("【DetectMatchThread】获取数据失败，查询数据为" + this.toString());
            updateExecLog(logId, EXEC_FAIL, MATCH_ERROR);
            return;
        }
        try {
            //如果是普通匹配方式
            if (0 == rule.getSeniorType()) {
                //如果为字符串匹配
                if (0 == rule.getMatchContentType()) {
                    matchStatus = content.contains(rule.getRuleMatchContent());
                    matchStatus = matchStatus == (0 == rule.getRuleMatchType());
                }
                //如果为正则匹配
                if (1 == rule.getMatchContentType()) {
                    matchStatus = content.matches(rule.getRuleMatchContent());
                    matchStatus = matchStatus == (0 == rule.getRuleMatchType());
                }
                //如果为高级检索
            } else if (1 == rule.getSeniorType()) {
                matchStatus = seniorMatch(content, rule.getId());
            }
        } catch (Exception e) {
            log.error("【DetectMatchThread】处理数据失败，查询数据为" + this.toString(), e);
            updateExecLog(logId, EXEC_FAIL, MATCH_ERROR);
            return;
        }
        if (matchStatus) {
            updateExecLog(logId, EXEC_FINISH, MATCH_SUCCESS);
        } else {
            updateExecLog(logId, EXEC_FINISH, MATCH_ERROR);
        }
    }

    /**
     * 高级匹配
     *
     * @param content 匹配数据
     * @param ruleId  规则ID
     * @return true:匹配成功  false :匹配失败
     */
    private boolean seniorMatch(String content, int ruleId) {
        boolean result = false;
        //获取高级配置数据
        List<MwRuleSelectParam> ruleSelectList = configManageTableDao.selectMwAlertRuleSelect(DetectType.RULE.getDetectName() + "-" + ruleId);
        if (CollectionUtils.isEmpty(ruleSelectList)) {
            return result;
        }
        List<MwRuleSelectParam> ruleSelectParams = new ArrayList<>();
        for (MwRuleSelectParam s : ruleSelectList) {
            if (s.getParentKey().equals("root")) {
                ruleSelectParams.add(s);
            }
        }
        for (MwRuleSelectParam s : ruleSelectParams) {
            s.setConstituentElements(getChild(s.getKey(), ruleSelectList));
        }
        HashMap<String, Object> assetsMap = new HashMap<>();
        assetsMap.put("文本", content);
        MessageContext messageContext = new MessageContext();
        messageContext.setKey(assetsMap);
        result = DelFilter.delFilter(ruleSelectParams, messageContext, ruleSelectList);
        return result;
    }

    /**
     * @param logId        记录ID
     * @param updateStatus 2：处理结束  3：处理失败
     * @param matchStatus  匹配结果（0：匹配失败，1：匹配成功）
     */
    private void updateExecLog(int logId, int updateStatus, int matchStatus) {
        configManageTableDao.updateExecLogStatus(logId, updateStatus, matchStatus);
    }

    /**
     * 获取配置数据
     *
     * @param filename 文件名称
     * @return
     */
    private String showTxt(String filename) {
        if (StringUtils.isEmpty(filename)) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        String str = "";
        try {
            br = new BufferedReader(new FileReader(filename));
            String s = null;
            while ((s = br.readLine()) != null) {
                sb.append(System.lineSeparator() + s);
            }
            String result = sb.toString();
            str = EncryptUtil.decrypt(result);
        } catch (Exception e) {
            log.error("【DetectMatchThread】获取文本数据失败，查询数据为" + this.toString(), e);
            return null;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                log.error("【DetectMatchThread】获取文本数据失败，查询数据为" + this.toString(), e);
            }
        }
        return str;
    }

    /**
     * 获取子节点数据
     *
     * @param key      节点
     * @param rootList 根数据
     * @return
     */
    private static List<MwRuleSelectParam> getChild(String key, List<MwRuleSelectParam> rootList) {
        List<MwRuleSelectParam> childList = new ArrayList<>();
        for (MwRuleSelectParam s : rootList) {
            if (s.getParentKey().equals(key)) {
                childList.add(s);
            }
        }
        for (MwRuleSelectParam s : childList) {
            s.setConstituentElements(getChild(s.getKey(), rootList));
        }
        if (childList.size() == 0) {
            return null;
        }
        return childList;

    }

    @Override
    public String toString() {
        return "DetectMatchThread{" +
                "assetsId='" + assetsId + '\'' +
                ", logId=" + logId +
                ", rule=" + rule +
                ", configType=" + configType +
                '}';
    }
}
