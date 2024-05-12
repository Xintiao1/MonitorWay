package cn.mw.xiangtai.plugin.service.impl.detailimpl;

import cn.mw.xiangtai.plugin.domain.dto.AttackAddressDataDTO;
import cn.mw.xiangtai.plugin.domain.dto.LongitudeLatitudeDTO;
import cn.mw.xiangtai.plugin.domain.dto.XIangtaiMapAreaDto;
import cn.mw.xiangtai.plugin.domain.dto.XiangtaiDeviceDto;
import cn.mw.xiangtai.plugin.domain.param.XiangtaiVisualizedParam;
import cn.mw.xiangtai.plugin.domain.vo.PositionMapVO;
import cn.mw.xiangtai.plugin.monitor.dao.XiangtaiLogVisualizedMapper;
import cn.mw.xiangtai.plugin.service.SyslogAlertService;
import cn.mw.xiangtai.plugin.service.XiangtaiVisualizedModule;
import cn.mw.xiangtai.plugin.utils.XtIpUtil;
import cn.mwpaas.common.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gengjb
 * @description 攻击位置地图
 * @date 2023/10/19 14:53
 */
@Service
@Slf4j
public class XiangTaiAttackPositionMapModule implements XiangtaiVisualizedModule {

    @Autowired
    private SyslogAlertService syslogAlertService;

    @Value("${file.url}")
    private String filePath;

    static final String MODULE = "ipAddressdb";

    private final String UNKOWN = "未知";

    @Autowired
    private XiangtaiLogVisualizedMapper visualizedMapper;

    @Autowired
    private XtIpUtil ipUtil;

    @Override
    public int[] getType() {
        return new int[]{116};
    }

    @Override
    public Object getData(XiangtaiVisualizedParam visualizedParam) {
        try {
            List<AttackAddressDataDTO> attackDataForTheDay = syslogAlertService.getAttackDataForTheDay();
//            List<PositionMapVO> list = handlerIpAddress(attackDataForTheDay);
//            if (list == null) {
//                list = new ArrayList<>();
//            }
            List<PositionMapVO> vos = new ArrayList<>();
            List<AttackAddressDataDTO> dataDTOS = getIpAreaAddress(attackDataForTheDay, vos);
            if(CollectionUtils.isEmpty(dataDTOS)){return vos;}
            List<PositionMapVO> mapVOS = handlerIpAddress(dataDTOS);
            if(CollectionUtils.isNotEmpty(mapVOS)){
                vos.addAll(mapVOS);
            }
            return vos;
        } catch (Throwable e) {
            log.error("XiangTaiAttackPositionMapModule{} getData() ERROR::", e);
            return null;
        }
    }

    /**
     * 获取IP地址对应的区域信息
     */
    private List<AttackAddressDataDTO> getIpAreaAddress(List<AttackAddressDataDTO> attackDataForTheDay, List<PositionMapVO> positionMapVOS){
        List<AttackAddressDataDTO> attackAddressDataDTOS = new ArrayList<>();
        if(CollectionUtils.isEmpty(attackDataForTheDay)){return attackDataForTheDay;}
        //查询目的设备IP信息
        List<XiangtaiDeviceDto> xiangtaiDeviceDtos = visualizedMapper.selectXiangtaiDeviceMappingInfo();
        if(CollectionUtils.isEmpty(xiangtaiDeviceDtos)){return attackDataForTheDay;}
        for (AttackAddressDataDTO addressDataDTO : attackDataForTheDay) {
            String dstIp = addressDataDTO.getDstIp();
            if(StringUtils.isNotBlank(dstIp)){
                String ipArea = checkIpAddressSegment(dstIp, xiangtaiDeviceDtos);
                addressDataDTO.setDstIpArea(ipArea);
            }
            String srcIp = addressDataDTO.getSrcIp();
            if(StringUtils.isNotBlank(srcIp)){
                String ipArea = checkIpAddressSegment(srcIp, xiangtaiDeviceDtos);
                addressDataDTO.setSrcIpArea(ipArea);
            }
        }
        List<String> areas = new ArrayList<>();
        //根据区域信息查询经纬度信息
        List<String> srcIpAreas = attackDataForTheDay.stream().filter(item -> StringUtils.isNotBlank(item.getSrcIpArea())).map(AttackAddressDataDTO::getSrcIpArea).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(srcIpAreas)){
            areas.addAll(srcIpAreas);
        }
        List<String> dstIpAreas = attackDataForTheDay.stream().filter(item -> StringUtils.isNotBlank(item.getDstIpArea())).map(AttackAddressDataDTO::getDstIpArea).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(dstIpAreas)){
            areas.addAll(dstIpAreas);
        }
        if(CollectionUtils.isEmpty(areas)){return attackDataForTheDay;}
        log.info("XiangTaiAttackPositionMapModule{} getIpAreaAddress() attackDataForTheDay::"+attackDataForTheDay);
        //查询区域
        List<XIangtaiMapAreaDto> areaDtos = visualizedMapper.selectIpAddressAreaInfo(areas);
        log.info("XiangTaiAttackPositionMapModule{} getIpAreaAddress() areaDtos::"+areaDtos);
        if(CollectionUtils.isEmpty(areaDtos)){return attackDataForTheDay;}
        for (AttackAddressDataDTO attackAddressDataDTO : attackDataForTheDay) {
            PositionMapVO positionMapVO = new PositionMapVO();
            String srcIpArea = attackAddressDataDTO.getSrcIpArea();
            String dstIpArea = attackAddressDataDTO.getDstIpArea();
            log.info("XiangTaiAttackPositionMapModule{} getIpAreaAddress() srcIpArea::"+srcIpArea);
            log.info("XiangTaiAttackPositionMapModule{} getIpAreaAddress() dstIpArea::"+dstIpArea);
            for (XIangtaiMapAreaDto areaDto : areaDtos) {
                String wholeName = areaDto.getWholeName();
                if(StringUtils.isNotBlank(srcIpArea) && wholeName.contains(srcIpArea)){
                    LongitudeLatitudeDTO srcAddressObj = new LongitudeLatitudeDTO();
                    srcAddressObj.setCity(areaDto.getWholeName().split("，")[0]+areaDto.getWholeName().split("，")[1]);
                    srcAddressObj.setLongitude(Double.parseDouble(areaDto.getLon()));
                    srcAddressObj.setLatitude(Double.parseDouble(areaDto.getLat()));
                    positionMapVO.setSrcAddressObj(srcAddressObj);
                }
                if(StringUtils.isNotBlank(dstIpArea) && wholeName.contains(dstIpArea)){
                    LongitudeLatitudeDTO dstAddressObj = new LongitudeLatitudeDTO();
                    dstAddressObj.setCity(areaDto.getWholeName().split("，")[0]+areaDto.getWholeName().split("，")[1]);
                    dstAddressObj.setLongitude(Double.parseDouble(areaDto.getLon()));
                    dstAddressObj.setLatitude(Double.parseDouble(areaDto.getLat()));
                    positionMapVO.setDstAddressObj(dstAddressObj);
                }
            }
            if(positionMapVO.getDstAddressObj() != null && positionMapVO.getSrcAddressObj() != null){
                positionMapVOS.add(positionMapVO);
                continue;
            }
            attackAddressDataDTOS.add(attackAddressDataDTO);
        }
        log.info("XiangTaiAttackPositionMapModule{} getIpAreaAddress() attackAddressDataDTOS::"+attackAddressDataDTOS);
        return attackAddressDataDTOS;
    }

    private String checkIpAddressSegment(String ipAddress,List<XiangtaiDeviceDto> xiangtaiDeviceDtos){
        try {
            for (XiangtaiDeviceDto xiangtaiDeviceDto : xiangtaiDeviceDtos) {
                String ipAddressSegment = xiangtaiDeviceDto.getIpAddressSegment();
                if(StringUtils.isBlank(ipAddressSegment)){continue;}
                if(ipAddressSegment.contains("-")){
                    String[] ips = ipAddressSegment.split("-");
                    InetAddress ip = InetAddress.getByName(ipAddress);
                    InetAddress startIp = InetAddress.getByName(ips[0]);
                    InetAddress endIp = InetAddress.getByName(ips[1]);
                    if(isIPInRange(ip,startIp,endIp)){
                        return xiangtaiDeviceDto.getIpArea();
                    }
                }
                if(ipAddressSegment.contains("/")){
                    SubnetUtils subnetUtils = new SubnetUtils(ipAddressSegment);
                    if(subnetUtils.getInfo().isInRange(ipAddress)){
                        return xiangtaiDeviceDto.getIpArea();
                    }
                }
            }
        }catch (Throwable e){
            log.error("XiangTaiAttackPositionMapModule{} checkIpAddressSegment() ERROR::",e);
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
     * 获取IP地址的城市
     *
     * @param attackDataForTheDay
     */
    private List<PositionMapVO> handlerIpAddress(List<AttackAddressDataDTO> attackDataForTheDay) {
        List<PositionMapVO> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(attackDataForTheDay)) {
            return null;
        }
        for (AttackAddressDataDTO attackAddressDataDTO : attackDataForTheDay) {
            PositionMapVO vo = new PositionMapVO();
            try {
                LongitudeLatitudeDTO srcAddressInfo = getAddressInfo(attackAddressDataDTO.getSrcIp());
                LongitudeLatitudeDTO dstAddressInfo = getAddressInfo(attackAddressDataDTO.getDstIp());
                vo.setSrcAddressObj(srcAddressInfo);
                vo.setDstAddressObj(dstAddressInfo);
                if (ObjectUtils.isEmpty(srcAddressInfo) || ObjectUtils.isEmpty(dstAddressInfo)) {
                    vo.setStatus(UNKOWN);
                }
            } catch (Throwable e) {
                vo.setStatus(UNKOWN);
                log.error("XiangTaiAttackPositionMapModule{} handlerIpAddress() 地址解析出错::", e);
            }
            list.add(vo);
        }

        return list;
    }

    private LongitudeLatitudeDTO getAddressInfo(String ipaddress) {
        try {
            LongitudeLatitudeDTO srcLongitudeLatitudeDTO = new LongitudeLatitudeDTO();
            boolean isInner = ipUtil.ipIsInner(ipaddress);
            if (isInner) {
                srcLongitudeLatitudeDTO = new LongitudeLatitudeDTO();
                srcLongitudeLatitudeDTO.setCounty("中国");
                srcLongitudeLatitudeDTO.setProvince("江苏省");
                srcLongitudeLatitudeDTO.setCity("泰州市");
                srcLongitudeLatitudeDTO.setLongitude(119.91);
                srcLongitudeLatitudeDTO.setLatitude(32.48);
                return srcLongitudeLatitudeDTO;
            }

            srcLongitudeLatitudeDTO.setLongitude(ipUtil.getLongitude(ipaddress));
            srcLongitudeLatitudeDTO.setLatitude(ipUtil.getLatitude(ipaddress));
            srcLongitudeLatitudeDTO.setCounty(ipUtil.getCountry(ipaddress));
            String province = ipUtil.getProvince(ipaddress);
            if (StringUtils.isBlank(province)) {
                province = ipUtil.getProvinceBySearcher(ipaddress);
            }
            srcLongitudeLatitudeDTO.setProvince(province);

            String city = ipUtil.getCity(ipaddress);
            if (StringUtils.isBlank(city)) {
                city = ipUtil.getCityBySearcher(ipaddress);
            }
            srcLongitudeLatitudeDTO.setCity(city);
            return srcLongitudeLatitudeDTO;
        } catch (Exception e) {
            return null;
        }
    }
}
