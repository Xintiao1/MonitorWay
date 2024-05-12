package cn.mw.xiangtai.plugin.utils;

import cn.mw.xiangtai.plugin.constants.XiangtaiConstants;
import cn.mw.xiangtai.plugin.domain.dto.XiangtaiDeviceDto;
import com.maxmind.geoip2.DatabaseReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class XtIpUtil {

    @Value("${file.url}")
    private String filePath;

    private static final String MODULE = "ipAddressdb";

    private DatabaseReader reader = null;

    private Searcher searcher = null;


    public DatabaseReader getReader() {
        if (ObjectUtils.isEmpty(this.reader)) {
            String temPath = new File(filePath + File.separator + MODULE).getAbsolutePath() + File.separator;
            log.info("初始化GeoLite2, 地址为：{}", temPath);
            File file = new File(temPath + "GeoLite2-City.mmdb");
            DatabaseReader build = null;
            try {
                build = new DatabaseReader.Builder(file).build();
            } catch (IOException e) {
                throw new RuntimeException("加载GeoLite2-City.mmdb异常: ", e);
            }
            this.reader = build;
            return this.reader;
        }
        return this.reader;
    }

    public Searcher getSearcher() throws Exception {
        if (ObjectUtils.isEmpty(this.searcher)) {
            String temPath = new File(filePath + File.separator + MODULE).getAbsolutePath() + File.separator + "ip2region.xdb";
            log.info("ip2region, 地址为：{}", temPath);
            File file = new File(temPath);
            InputStream inputStream = Files.newInputStream(file.toPath());
            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
            Searcher searcher = Searcher.newWithBuffer(bytes);
            this.searcher = searcher;
            return searcher;
        }
        return this.searcher;
    }

    public String getProvinceBySearcher(String ipaddress) throws Exception {
        String searchedProvince = getSearcher().search(ipaddress);
        String province = "";
        if (StringUtils.isNotBlank(searchedProvince)) {
            String[] ipAddress = searchedProvince.split("\\|");
            if (ipAddress.length >= 2) {
                province = ipAddress[2];
            }
        }
        return province;
    }

    public String getCityBySearcher(String ipaddress) throws Exception {
        String searchedProvince = getSearcher().search(ipaddress);
        String province = "";
        if (StringUtils.isNotBlank(searchedProvince)) {
            String[] ipAddress = searchedProvince.split("\\|");
            if (ipAddress.length >= 3) {
                province = ipAddress[3];
            }
        }
        return province;
    }

    /**
     * 获取经度
     *
     * @param ip ip地址
     * @return
     * @throws Exception
     */
    public Double getLongitude(String ip) throws Exception {
        return getReader().city(InetAddress.getByName(ip)).getLocation().getLongitude();
    }

    /**
     * 获取维度
     *
     * @param ip ip地址
     * @return
     * @throws Exception
     */
    public Double getLatitude(String ip) throws Exception {
        return getReader().city(InetAddress.getByName(ip)).getLocation().getLatitude();
    }

    /**
     * 获取国家
     *
     * @param ip
     * @return
     */
    public String getCountry(String ip) throws Exception {
        return getReader().city(InetAddress.getByName(ip)).getCountry().getNames().get(XiangtaiConstants.ZH_CN);
    }

    /**
     * 省份
     *
     * @param ip
     * @return
     * @throws Exception
     */
    public String getProvince(String ip) throws Exception {
        return getReader().city(InetAddress.getByName(ip)).getMostSpecificSubdivision().getNames().get(XiangtaiConstants.ZH_CN);
    }

    /**
     * 城市
     *
     * @param ip
     * @return
     * @throws Exception
     */
    public String getCity(String ip) throws Exception {
        return getReader().city(InetAddress.getByName(ip)).getCity().getNames().get(XiangtaiConstants.ZH_CN);
    }

    /**
     * 判断IP是否内网IP
     *
     * @param ip
     * @Title: ipIsInner
     * @return: boolean
     */
    public boolean ipIsInner(String ip) {
        boolean isInnerIp = false;
        for (Pattern tmp : ipFilterRegexList) {
            Matcher matcher = tmp.matcher(ip);
            if (matcher.find()) {
                isInnerIp = true;
                break;
            }
        }
        return isInnerIp;
    }

    /**
     * 私有IP：
     * A类  10.0.0.0-10.255.255.255
     * B类  172.16.0.0-172.31.255.255
     * C类  192.168.0.0-192.168.255.255
     * <p>
     * 127这个网段是环回地址
     * localhost
     */
    static List<Pattern> ipFilterRegexList = new ArrayList<>();

    static {
        Set<String> ipFilter = new HashSet<>();
        ipFilter.add("^10\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])" + "\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])$");
        // B类地址范围: 172.16.0.0---172.31.255.255
        ipFilter.add("^172\\.(1[6789]|2[0-9]|3[01])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])$");
        // C类地址范围: 192.168.0.0---192.168.255.255
        ipFilter.add("^192\\.168\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])$");
        ipFilter.add("127.0.0.1");
        ipFilter.add("0.0.0.0");
        ipFilter.add("localhost");
        for (String tmp : ipFilter) {
            ipFilterRegexList.add(Pattern.compile(tmp));
        }
    }


    public static XiangtaiDeviceDto checkIpAddressSegment(String ipAddress,List<XiangtaiDeviceDto> xiangtaiDeviceDtos){
        try {
            for (XiangtaiDeviceDto xiangtaiDeviceDto : xiangtaiDeviceDtos) {
                String ipAddressSegment = xiangtaiDeviceDto.getIpAddressSegment();
                if(cn.mwpaas.common.utils.StringUtils.isBlank(ipAddressSegment)){continue;}
                if(ipAddressSegment.contains("-")){
                    String[] ips = ipAddressSegment.split("-");
                    InetAddress ip = InetAddress.getByName(ipAddress);
                    InetAddress startIp = InetAddress.getByName(ips[0]);
                    InetAddress endIp = InetAddress.getByName(ips[1]);
                    if(isIPInRange(ip,startIp,endIp)){
                        return xiangtaiDeviceDto;
                    }
                }
                if(ipAddressSegment.contains("/")){
                    SubnetUtils subnetUtils = new SubnetUtils(ipAddressSegment);
                    if(subnetUtils.getInfo().isInRange(ipAddress)){
                        return xiangtaiDeviceDto;
                    }
                }
            }
        }catch (Throwable e){
            log.error("XtIpUtil{} checkIpAddressSegment() ERROR::",e);
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
}
