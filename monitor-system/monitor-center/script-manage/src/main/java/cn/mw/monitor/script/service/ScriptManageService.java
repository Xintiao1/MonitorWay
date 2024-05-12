package cn.mw.monitor.script.service;

import cn.mw.monitor.script.entity.MwHomeworkAlert;
import cn.mw.monitor.script.param.*;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mwpaas.common.model.Reply;
import com.alibaba.fastjson.JSONArray;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * @author gui.quanwang
 * @className ScriptManageService
 * @description 脚本管理服务
 * @date 2022/4/8
 */
public interface ScriptManageService {

    /**
     * 增加脚本数据
     *
     * @param param 脚本数据
     * @return
     */
    Reply addScript(ScriptManageParam param);


    /**
     * 更新脚本数据
     *
     * @param param 脚本数据
     * @return
     */
    Reply updateScript(ScriptManageParam param);

    /**
     * 删除脚本数据
     *
     * @param param 脚本数据
     * @return
     */
    Reply deleteScript(ScriptManageParam param);

    /**
     * 获取脚本列表
     *
     * @param scriptManageParam 脚本参数
     * @return
     */
    Reply getScriptList(ScriptManageParam scriptManageParam);

    /**
     * 获取脚本信息
     *
     * @param scriptManageParam 脚本参数
     * @return
     */
    Reply getScriptInfo(ScriptManageParam scriptManageParam);

    /**
     * 执行脚本
     *
     * @param scriptManageParam 脚本参数
     * @return
     */
    Reply execScript(ScriptManageParam scriptManageParam);

    /**
     * 更新执行结果
     *
     * @param map 结果数据
     * @return
     */
    Reply updateExecScript(HashMap map);

    /**
     * 获取执行列表数据
     *
     * @param scriptManageParam
     * @return
     */
    Reply getExecList(ScriptManageParam scriptManageParam);

    /**
     * 获取执行详情数据
     *
     * @param scriptManageParam
     * @return
     */
    Reply getExecDetail(ScriptManageParam scriptManageParam);

    /**
     * 获取历史脚本执行列表数据
     *
     * @param scriptManageParam
     * @return
     */
    Reply getExecHistoryList(ScriptManageParam scriptManageParam);

    /**
     * 获取模糊查询数据
     *
     * @param type 类别
     * @return
     */
    Reply getFuzzList(String type);

    /**
     * 增加账户
     *
     * @param param
     * @return
     */
    Reply addAccount(ScriptAccountParam param);

    /**
     * 更新账户信息
     *
     * @param param
     * @return
     */
    Reply updateAccount(ScriptAccountParam param);

    /**
     * 删除账户信息
     *
     * @param param
     * @return
     */
    Reply deleteAccount(ScriptAccountParam param);

    /**
     * 获取单个账户数据
     *
     * @param param
     * @return
     */
    Reply browseAccount(ScriptAccountParam param);

    /**
     * 获取账户列表数据
     *
     * @param param
     * @return
     */
    Reply getAccountList(ScriptAccountParam param);

    /**
     * 获取账户下拉列表数据
     *
     * @param param
     * @return
     */
    Reply getAccountDropList(ScriptAccountParam param);

    /**
     * 分发文件
     *
     * @param param 分发数据
     * @return
     */
    Reply distributeFile(FileTransParam param);

    /**
     * 增加作业
     *
     * @param param 作业信息
     * @return
     */
    Reply addHomework(HomeworkParam param);

    /**
     * 删除作业
     *
     * @param param 作业信息
     * @return
     */
    Reply deleteHomework(HomeworkParam param);

    /**
     * 修改作业
     *
     * @param param 作业信息
     * @return
     */
    Reply updateHomework(HomeworkParam param);

    /**
     * 查询作业
     *
     * @param param 作业信息
     * @return
     */
    Reply browseHomework(HomeworkParam param);

    /**
     * 查询作业列表
     *
     * @param param 作业信息
     * @return
     */
    Reply browseHomeworkList(HomeworkParam param);

    /**
     * 执行作业
     *
     * @param param 作业信息
     * @return
     */
    Reply performHomework(HomeworkParam param) throws Exception;

    /**
     * 查询作业步骤详情
     *
     * @param param 作业信息
     * @return
     */
    Reply browseHomeworkStep(HomeworkParam param);

    /**
     * 查询作业步骤详情
     *
     * @param param 作业信息
     * @return
     */
    Reply getExecResult(HomeworkParam param);

    /**
     * 更新作业执行结果
     *
     * @param map 结果数据
     * @return
     */
    Reply updateHomeworkExecScript(HashMap map);

    /**
     * 执行作业
     *
     * @param param 作业信息
     * @return
     */
    Reply rePerformHomework(HomeworkParam param);

    void syncAccount(JSONArray jsonArray, GlobalUserInfo userInfo, SynAccount param);

    void removAssets(List<Integer> ids);

    Reply accountCreate(CreateAssets param);

    Reply editorAccount(CreateAssets param);

    Reply getAllAlertBrowse(MwHomeworkAlert mwHomeworkAlert);

    Reply alertCreate(MwHomeworkAlert mwHomeworkAlert);

    Reply alertEditor(MwHomeworkAlert mwHomeworkAlert);

    Reply alertDelete(List<MwHomeworkAlert> mwHomeworkAlert);

    Reply alertHomework(String alertExeHomework);

    Reply homeworkList(ListParamString listParamString);

    Reply HisBrowse(HomeworkHis param);

    Reply HisBrowseCheck(HomeworkHis param);

    Reply downById(HomeworkHis param, HttpServletResponse response);

    void downZip(HomeworkHis param, HttpServletResponse response);

    Reply getConText(ScriptManageParam param);

    void exeportModel(HttpServletResponse response) throws IOException;

    void exeportAccount(MultipartFile file, HttpServletResponse response, GlobalUserInfo userInfo);

    Reply deleteExecHistoryList(List<ScriptManageParam> param);
}
