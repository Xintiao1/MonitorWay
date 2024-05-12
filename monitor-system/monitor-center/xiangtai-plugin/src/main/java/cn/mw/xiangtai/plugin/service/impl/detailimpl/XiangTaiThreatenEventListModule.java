package cn.mw.xiangtai.plugin.service.impl.detailimpl;

import cn.mw.xiangtai.plugin.domain.dto.XiangtaiDeviceDto;
import cn.mw.xiangtai.plugin.domain.entity.SyslogAlertEntity;
import cn.mw.xiangtai.plugin.domain.entity.XiangtaiIPMSEntity;
import cn.mw.xiangtai.plugin.domain.param.XiangtaiVisualizedParam;
import cn.mw.xiangtai.plugin.monitor.dao.XiangtaiIPMSMapper;
import cn.mw.xiangtai.plugin.monitor.dao.XiangtaiLogVisualizedMapper;
import cn.mw.xiangtai.plugin.service.SyslogAlertService;
import cn.mw.xiangtai.plugin.service.XiangtaiVisualizedModule;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.util.SubnetUtils;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author gengjb
 * @description 威胁事件列表
 * @date 2023/10/19 14:55
 */
@Service
@Slf4j
public class XiangTaiThreatenEventListModule implements XiangtaiVisualizedModule {

    @Autowired
    private SyslogAlertService syslogAlertService;


    @Value("${file.url}")
    private String filePath;

    static final String MODULE = "ipAddressdb";

    private final String UNKOWN = "未知";

    @Autowired
    private XiangtaiLogVisualizedMapper visualizedMapper;

    @Autowired
    private XiangtaiIPMSMapper xiangtaiIPMSMapper;

    @Override
    public int[] getType() {
        return new int[]{117};
    }

    @Override
    public Object getData(XiangtaiVisualizedParam visualizedParam) {
        try {
            List<SyslogAlertEntity> alertEntityList = syslogAlertService.getThreatEventListV1();
            log.info("XiangTaiThreatenEventListModule{} getData() alertEntityList::"+alertEntityList);
//            handlerIpAddress(alertEntityList);
            getDstDeviceInfo(alertEntityList);
            //获取对应IP的部门信息
            getIpmsInfo(alertEntityList);
            return alertEntityList;
        } catch (Throwable e) {
            log.error("XiangTaiThreatenEventListModule{} getData() ERROR::", e);
            return null;
        }
    }


    private void getIpmsInfo(List<SyslogAlertEntity> alertEntityList){
        if(CollectionUtils.isEmpty(alertEntityList)){return;}
        List<String> ips = alertEntityList.stream().filter(item -> StringUtils.isNotBlank(item.getDstIp())).map(SyslogAlertEntity::getDstIp).collect(Collectors.toList());
        QueryWrapper<XiangtaiIPMSEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("ip_address",ips);
        List<XiangtaiIPMSEntity> ipmsEntities = xiangtaiIPMSMapper.selectList(queryWrapper);
        if(CollectionUtils.isEmpty(ipmsEntities)){return;}
        Map<String, XiangtaiIPMSEntity> ipmsEntityMap = ipmsEntities.stream().collect(Collectors.toMap(XiangtaiIPMSEntity::getIpAddress, item -> item, (existring, replacement) -> replacement));
        for (SyslogAlertEntity syslogAlertEntity : alertEntityList) {
            String dstIp = syslogAlertEntity.getDstIp();
            XiangtaiIPMSEntity xiangtaiIPMSEntity = ipmsEntityMap.get(dstIp);
            if(xiangtaiIPMSEntity == null){continue;}
            syslogAlertEntity.setIpmsInfo(xiangtaiIPMSEntity);
        }
    }

    /**
     * 获取目的设备信息
     */
    private void getDstDeviceInfo(List<SyslogAlertEntity> alertEntityList) throws Exception {
        if(CollectionUtils.isEmpty(alertEntityList)){return;}
        List<String> dstIps = alertEntityList.stream().filter(item->StringUtils.isNotBlank(item.getDstIp())).map(SyslogAlertEntity::getDstIp).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(dstIps)){return;}
        //查询目的设备IP信息
        List<XiangtaiDeviceDto> xiangtaiDeviceDtos = visualizedMapper.selectXiangtaiDeviceMappingInfo();
        if(CollectionUtils.isEmpty(xiangtaiDeviceDtos)){return;}
        for (SyslogAlertEntity syslogAlertEntity : alertEntityList) {
            String dstIp = syslogAlertEntity.getDstIp();
            if(StringUtils.isNotBlank(dstIp)){
                String ipChnAddress = checkIpAddressSegment(dstIp, xiangtaiDeviceDtos);
                syslogAlertEntity.setDstDevice(ipChnAddress);
            }
            String srcIp = syslogAlertEntity.getSrcIp();
            if(StringUtils.isNotBlank(srcIp)){
                String ipChnAddress = checkIpAddressSegment(srcIp, xiangtaiDeviceDtos);
                syslogAlertEntity.setCountry(ipChnAddress);
            }
        }
    }


    private String checkIpAddressSegment(String dstIp,List<XiangtaiDeviceDto> xiangtaiDeviceDtos){
        try {
            for (XiangtaiDeviceDto xiangtaiDeviceDto : xiangtaiDeviceDtos) {
                String ipAddressSegment = xiangtaiDeviceDto.getIpAddressSegment();
                if(StringUtils.isBlank(ipAddressSegment)){continue;}
                if(ipAddressSegment.contains("-")){
                    String[] ips = ipAddressSegment.split("-");
                    InetAddress ip = InetAddress.getByName(dstIp);
                    InetAddress startIp = InetAddress.getByName(ips[0]);
                    InetAddress endIp = InetAddress.getByName(ips[1]);
                    if(isIPInRange(ip,startIp,endIp)){
                        return xiangtaiDeviceDto.getAddressInfo();
                    }
                }
                if(ipAddressSegment.contains("/")){
                    SubnetUtils subnetUtils = new SubnetUtils(ipAddressSegment);
                    if(subnetUtils.getInfo().isInRange(dstIp)){
                        return xiangtaiDeviceDto.getAddressInfo();
                    }
                }
            }
        }catch (Throwable e){
            log.error("XiangTaiThreatenEventListModule{} checkIpAddressSegment() ERROR::",e);
        }
        return null;
    }

    public static boolean isIPInRange(InetAddress ip, InetAddress startIp, InetAddress endIp) {
        long target = ipToLong(ip);
        long start = ipToLong(startIp);
        long end = ipToLong(endIp);
        return (target >= start) && (target <= end);
    }

    public static long ipToLong(InetAddress ip) {
        byte[] octets = ip.getAddress();
        long result = 0;
        for (byte octet : octets) {
            result <<= 8;
            result |= octet & 0xff;
        }
        return result;
    }

    /**
     * 获取IP地址的国家与城市
     * @param alertEntityList
     */
    private void handlerIpAddress(List<SyslogAlertEntity> alertEntityList){
        try {
            String temPath = new File(filePath + File.separator + MODULE).getAbsolutePath() + File.separator;
            File file = new File(temPath + "GeoLite2-City.mmdb");
            DatabaseReader reader = new DatabaseReader.Builder(file).build();
            for (SyslogAlertEntity syslogAlertEntity : alertEntityList) {
                try {
                    InetAddress ipAddress = InetAddress.getByName(syslogAlertEntity.getSrcIp());
                    CityResponse response = reader.city(ipAddress);
                    String country = response.getCountry().getNames().get("zh-CN");
                    String subdivision = response.getSubdivisions().get(0).getNames().get("zh-CN");
                    String address = null;
                    if(StringUtils.isNotBlank(country)){
                        address = country;
                    }
                    if(StringUtils.isNotBlank(subdivision)){
                        address = address+"-"+subdivision;
                    }
                    if(StringUtils.isNotBlank(address)){
                        syslogAlertEntity.setCountry(address);
                    }else{
                        syslogAlertEntity.setCountry(UNKOWN);
                    }
                }catch (Throwable e){
                    String ipAddressInfo = getIpAddressInfo(syslogAlertEntity);
                    if(StringUtils.isNotBlank(ipAddressInfo)){
                        syslogAlertEntity.setCountry(ipAddressInfo);
                    }else{
                        syslogAlertEntity.setCountry(UNKOWN);
                        log.error("XiangTaiThreatenEventListModule{} handlerIpAddress() 地址解析出错::",e);
                    }
                }
            }
        }catch (Throwable e){
            log.error("XiangTaiThreatenEventListModule{} handlerIpAddress() ERROR::",e);
        }
    }

    /**
     * 如果第一次获取IP地址异常，通过国内的IP信息获取
     */
    private String getIpAddressInfo(SyslogAlertEntity syslogAlertEntity){
        String countryAndProvince = null;
        try {
            String temPath = new File(filePath + File.separator + MODULE).getAbsolutePath() + File.separator+"ip2region.xdb";
            File file = new File(temPath);
            InputStream inputStream = new FileInputStream(file);
            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
            Searcher searcher = Searcher.newWithBuffer(bytes);
            String search = searcher.search(syslogAlertEntity.getSrcIp());
            if(StringUtils.isNotBlank(search)){
                String[] ipAddress = search.split("\\|");
                if(ipAddress.length >= 3){
                    countryAndProvince = ipAddress[0]+"-"+ipAddress[2];
                }else{
                    countryAndProvince = ipAddress[0];
                }
            }
        }catch (Throwable e){
            log.info("XiangTaiThreatenEventListModule{} getIpAddressInfo() ip2region地址解析出错::");
        }
        return countryAndProvince;
    }
}
