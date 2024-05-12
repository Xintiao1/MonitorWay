package cn.mw.xiangtai.plugin.service.impl.detailimpl;

import cn.mw.xiangtai.plugin.domain.dto.AttackSourceDTO;
import cn.mw.xiangtai.plugin.domain.dto.XiangtaiDeviceDto;
import cn.mw.xiangtai.plugin.domain.param.XiangtaiVisualizedParam;
import cn.mw.xiangtai.plugin.domain.vo.ExternalAttackSourceVO;
import cn.mw.xiangtai.plugin.monitor.dao.XiangtaiLogVisualizedMapper;
import cn.mw.xiangtai.plugin.service.XiangtaiVisualizedModule;
import cn.mw.xiangtai.plugin.service.XiangtaiVisualizedService;
import cn.mw.xiangtai.plugin.utils.XtIpUtil;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * @author gengjb
 * @description 祥泰攻击源topN组件
 * @date 2023/10/17 9:41
 */
@Service
@Slf4j
public class XiangtaiAttackSourceTopNModule implements XiangtaiVisualizedModule {

    @Autowired
    private XiangtaiVisualizedService visualizedService;

    @Value("${file.url}")
    private String filePath;

    static final String MODULE = "ipAddressdb";

    private final String UNKOWN = "未知";

    @Autowired
    private XtIpUtil ipUtil;

    @Autowired
    private XiangtaiLogVisualizedMapper visualizedMapper;

    @Override
    public int[] getType() {
        return new int[]{108};
    }

    @Override
    public Object getData(XiangtaiVisualizedParam visualizedParam) {
        try {
            log.info("filePath"+filePath);
            ExternalAttackSourceVO attackSourceVO = visualizedService.attackSourceTopN(visualizedParam.getTopN());
//            handlerIpAddress(attackSourceVO.getAttackSourceList());
            getDstDeviceInfo(attackSourceVO.getAttackSourceList());
            return attackSourceVO;
        }catch (Throwable e){
            log.error("XiangtaiAttackSourceTopNModule{} getData() ERROR::",e);
            return null;
        }
    }



    private void getDstDeviceInfo(List<AttackSourceDTO> attackSourceList){
        if(CollectionUtils.isEmpty(attackSourceList)){return;}
        //查询目的设备IP信息
        List<XiangtaiDeviceDto> xiangtaiDeviceDtos = visualizedMapper.selectXiangtaiDeviceMappingInfo();
        if(CollectionUtils.isEmpty(xiangtaiDeviceDtos)){return;}
        for (AttackSourceDTO attackSourceDTO : attackSourceList) {
            String ip = attackSourceDTO.getIp();
            if(StringUtils.isNotBlank(ip)){
                XiangtaiDeviceDto xiangtaiDeviceDto = XtIpUtil.checkIpAddressSegment(ip, xiangtaiDeviceDtos);
               if(xiangtaiDeviceDto != null && StringUtils.isNotBlank(xiangtaiDeviceDto.getAddressInfo())){
                   attackSourceDTO.setCountry(xiangtaiDeviceDto.getAddressInfo());
               }else{
                   attackSourceDTO.setCountry(UNKOWN);
               }
            }
        }
    }

    /**
     * 获取IP地址的国家与城市
     * @param attackSourceList
     */
    private void handlerIpAddress(List<AttackSourceDTO> attackSourceList){
        try {
            for (AttackSourceDTO attackSourceDTO : attackSourceList) {
                try {
                    String country = ipUtil.getCountry(attackSourceDTO.getIp());
                    String province = ipUtil.getProvince(attackSourceDTO.getIp());
                    String city = ipUtil.getCity(attackSourceDTO.getIp());
                    String address = "";
                    if(StringUtils.isNotBlank(country)){
                        address = country;
                    }
                    if(StringUtils.isNotBlank(province)){
                        address = address+"-"+province;
                    }
                    if(StringUtils.isNotBlank(city)){
                        address = address+"-"+city;
                    }

                    if(StringUtils.isNotBlank(address)){
                        attackSourceDTO.setCountry(address);
                    }else{
                        attackSourceDTO.setCountry(UNKOWN);
                    }
                }catch (Throwable e){
                    String ipAddressInfo = getIpAddressInfo(attackSourceDTO);
                    if(StringUtils.isNotBlank(ipAddressInfo)){
                        attackSourceDTO.setCountry(ipAddressInfo);
                    }else{
                        attackSourceDTO.setCountry(UNKOWN);
                        log.error("XiangtaiAttackSourceTopNModule{} handlerIpAddress() 地址解析出错::",e);
                    }
                }
            }
        }catch (Throwable e){
            log.error("XiangtaiAttackSourceTopNModule{} handlerIpAddress() ERROR::",e);
        }
    }

    /**
     * 如果第一次获取IP地址异常，通过国内的IP信息获取
     */
    private String getIpAddressInfo(AttackSourceDTO attackSourceDTO){
        String countryAndProvince = null;
        try {
            String temPath = new File(filePath + File.separator + MODULE).getAbsolutePath() + File.separator+"ip2region.xdb";
            File file = new File(temPath);
            InputStream inputStream = new FileInputStream(file);
            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
            Searcher searcher = Searcher.newWithBuffer(bytes);
            String search = searcher.search(attackSourceDTO.getIp());
            if(StringUtils.isNotBlank(search)){
                String[] ipAddress = search.split("\\|");
                if(ipAddress.length >= 3){
                    countryAndProvince = ipAddress[0]+"-"+ipAddress[2];
                }else{
                    countryAndProvince = ipAddress[0];
                }
            }
        }catch (Throwable e){
            log.info("XiangtaiAttackSourceTopNModule{} getIpAddressInfo() ip2region地址解析出错::");
        }
        return countryAndProvince;
    }
}
