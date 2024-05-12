package cn.mw.monitor.ipaddressmanage.ipconflict;

import cn.mw.monitor.api.common.UuidUtil;
import cn.mw.monitor.ipaddressmanage.dao.MwIpConfictTableDao;
import cn.mw.monitor.ipaddressmanage.dto.IpConflictHisTableDTO;
import cn.mw.monitor.service.scan.dto.IPInfoDTO;
import cn.mw.monitor.service.scan.model.IpInfo;
import cn.mw.monitor.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
public class IpConflictManage {
    private static final String PREFIX="IpConflictManage:";

    @Value("${ipmanage.ipConfict.expireTime}")
    private int expireTime;

    @Resource
    private MwIpConfictTableDao mwIpConfictTableDao;

    @Autowired
    private RedisUtils redisUtils;

    public void batchInsert(List<IPInfoDTO> ipInfoDTOList){
        List<IpConflictHisTableDTO> ipConflictHisList = new ArrayList<>();
        List<IpConflictHisTableDTO> ipConflictHisDetailList = new ArrayList<>();

        int i = 0;
        Date createTime = new Date();
        if(ipInfoDTOList.size() > 0) {
            for (IPInfoDTO ipInfoDTO : ipInfoDTOList) {
                int index = i % 10;
                String id = UuidUtil.getUid() + index;
                IpConflictHisTableDTO ipConflictHisTableDTO = new IpConflictHisTableDTO();
                ipConflictHisTableDTO.setId(id);
                ipConflictHisTableDTO.extractFrom(ipInfoDTO);
                ipConflictHisTableDTO.setCreateTime(createTime);
                ipConflictHisList.add(ipConflictHisTableDTO);

                for (IpInfo ipInfo : ipInfoDTO.getConflictsIp()) {
                    IpConflictHisTableDTO ipConflictDetail = new IpConflictHisTableDTO();
                    String detailId = UuidUtil.getUid() + index;
                    ipConflictDetail.setId(detailId);
                    ipConflictDetail.extractFrom(ipInfoDTO.getIp(), ipInfo);
                    ipConflictDetail.setConflictId(id);
                    ipConflictDetail.setCreateTime(createTime);
                    ipConflictHisDetailList.add(ipConflictDetail);
                }
                String key = PREFIX + ipInfoDTO.getIp();
                redisUtils.set(key ,ipInfoDTO ,expireTime);
            }
            mwIpConfictTableDao.batchInsertIpConflictHis(ipConflictHisList);
            mwIpConfictTableDao.batchInsertIpConflictHisDetail(ipConflictHisDetailList);

        }else{
            Set<String> keySet = redisUtils.keys(PREFIX);
            redisUtils.del(keySet);
        }
    }

    public List<IPInfoDTO> getCurrentIpConflictList(){
        List<IPInfoDTO> ret = new ArrayList<>();
        Set<String> keySet = redisUtils.keys(PREFIX);
        List<Object> datas = redisUtils.getValues(new ArrayList<>(keySet));
        for(Object obj : datas){
            ret.add((IPInfoDTO)obj);
        }
        return ret;
    }

    public Map<String ,IPInfoDTO> getCurrentIpConflictMap(){
        Map<String ,IPInfoDTO> ret = new HashMap<>();
        Set<String> keySet = redisUtils.keys(PREFIX);
        List<Object> datas = redisUtils.getValues(new ArrayList<>(keySet));
        for(Object obj : datas){
            IPInfoDTO ipInfoDTO = (IPInfoDTO)obj;
            ret.put(ipInfoDTO.getIp() ,ipInfoDTO);
        }
        return ret;
    }

    public IPInfoDTO getIpConflict(String ip){
        String key = PREFIX + ip;
        IPInfoDTO ret = (IPInfoDTO)redisUtils.get(key);
        return ret;
    }
}
