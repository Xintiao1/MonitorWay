package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.model.dao.MwModelJudgeDao;
import cn.mw.monitor.model.dto.MwModelJudgeDTO;
import cn.mw.monitor.model.param.MwModelIdListParam;
import cn.mw.monitor.model.param.MwModelJudgeParam;
import cn.mw.monitor.model.service.MwModelJudgeService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.DateUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;
import static cn.mwpaas.common.enums.DateUnitEnum.YEAR;

@Service
@Slf4j
public class MwModelJudgeServiceImpl implements MwModelJudgeService {
    @Resource
    private MwModelJudgeDao mwModelJudgeDao;
    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;
    public static final int ADMIN = 106;
    /**
     *
     */
    @Override
    public Reply insertJudgeMessage(MwModelJudgeParam param) {
        try {
            //获取当前登录用户ID
            String loginName = "admin";
            Integer userId = 106;
            if (iLoginCacheInfo != null && iLoginCacheInfo.getLoginName() != null &&
                    iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()) != null) {
                loginName = iLoginCacheInfo.getLoginName();
                userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
            }
            param.setUserId(userId);
            param.setUserName(loginName);
            mwModelJudgeDao.insertModelJudgeInfo(param);
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to insertJudgeMessage cause:{}", e);
            return Reply.fail(500, "新增评价数据失败");
        }
    }

    /**
     *
     */
    @Override
    public Reply checkJudgeCycle(MwModelJudgeParam param) {
        try {

            Integer judgeCycle = param.getJudgeCycle();
            Boolean isCheck = false;
            //评价限制：2为无限制
            if(intValueConvert(judgeCycle) == 2){
                isCheck = true;
                return Reply.ok(isCheck);
            }
            MwModelJudgeDTO judgeDTO = mwModelJudgeDao.checkModelJudgeTime(param);
            //根据实例Id查询评价数据，
            if (judgeDTO != null && intValueConvert(judgeDTO.getId()) != 0) {
                if (judgeDTO.getJudgeTime() != null && intValueConvert(judgeCycle) == 1) {
                    long timeNum = DateUtils.between(judgeDTO.getJudgeTime(), new Date(), YEAR);
                    if (timeNum >= 1) {
                        isCheck = true;
                    }
                }
            } else {
                isCheck = true;
            }
            return Reply.ok(isCheck);
        } catch (Exception e) {
            log.error("fail to checkJudgeCycle cause:{}", e);
            return Reply.fail(500, "评价数据校验失败");
        }
    }

    /**
     *
     */
    @Override
    public Reply selectJudgeMessage(MwModelJudgeParam param) {
        try {
            PageHelper.startPage(param.getPageNumber(), param.getPageSize());
            List<MwModelJudgeDTO> judgeDTOS = mwModelJudgeDao.selectModelJudgeInfo(param);
            PageInfo pageInfo = new PageInfo(judgeDTOS);
            pageInfo.setList(judgeDTOS);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to selectJudgeMessage cause:{}", e);
            return Reply.fail(500, "查询评价信息失败");
        }
    }

    @Override
    public Reply deleteJudgeMessage(MwModelIdListParam param) {
        try {
            //获取当前登录用户ID
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
            if(userId == ADMIN){
                mwModelJudgeDao.batchDeleteModelJudge(param.getIds());
            }else{
                return Reply.warn("请联系管理员删除");
            }
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to deleteJudgeMessage cause:{}", e);
            return Reply.fail(500, "删除评价信息失败");
        }
    }

}
