package cn.joinhealth.monitor.zbx.service;

import cn.mwpaas.common.model.Reply;
import cn.joinhealth.monitor.zbx.dao.ZbxGraphDao;
import cn.joinhealth.monitor.zbx.dto.ZbxGraphDTO;
import cn.joinhealth.monitor.zbx.model.ZbxGraph;
import cn.joinhealth.monitor.zbx.servce.ZbxGraphService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
@Slf4j
public class ZbxGraphServiceImpl implements ZbxGraphService {
    private static final Logger logger = LoggerFactory.getLogger(ZbxGraphService.class);
    @Autowired
    private ZbxGraphDao zbxGraphDao;

    @Override
    public Reply getZbxGraphRegexp(String hostType,String modelName) {
        try {
            ZbxGraph beans = new ZbxGraph();
            beans.setHostType(hostType);
            beans.setModelName(modelName);
            List<ZbxGraph> list = zbxGraphDao.selectGraphRegexp(beans);
            Reply reply = new Reply();
            if(list.size()>0){
                List<ZbxGraphDTO> listd = new ArrayList<>();
                for(ZbxGraph bean:list){
                    ZbxGraphDTO dto = new ZbxGraphDTO();
                    BeanUtils.copyProperties(dto,bean);
                    listd.add(dto);
                }
                reply.setData(listd);
            }
            logger.info("ACCESS_LOG[]t_zbx_graph[]图表对应信息[]查询图表对应信息[]hostType:[]{}[]modelName:[]{}",hostType, modelName);
            return reply;
        }catch (Exception e){
            log.error("fail to update with hostType：{}[]modelName:[]{} cause:{}", hostType, modelName,e.getMessage());
            return Reply.fail(1,"查询图表对应信息失败");
        }
    }
}
