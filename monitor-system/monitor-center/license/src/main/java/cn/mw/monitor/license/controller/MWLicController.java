package cn.mw.monitor.license.controller;

import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.license.config.MWLicenseConfigLoad;
import cn.mw.monitor.license.service.impl.UpdateLicenseProp;
import cn.mw.monitor.service.license.service.CheckLicenseService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.util.HostUtils;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.UUIDUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

/**
 * 许可管理
 */
@RequestMapping("/mwapi")
@Controller
@Api(value = "许可管理", tags = "许可管理")
@Slf4j
public class MWLicController extends BaseApiService {
    @Resource
    CheckLicenseService checkLicenseService;
    @Resource
    UpdateLicenseProp updateLicenseProp;

    @Value("${mwProduce.company}")
    private  String company;

    //文件上传路径
    @Value("${mwProduce.filePath}")
    private String filePath;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${mwProduce.genSecurity}")
    private boolean genSecurity;

    @PostMapping("/sn/browse")
    @ResponseBody
    @ApiOperation(value = "机器码查询")
    public ResponseBase getSn()  {
        Reply reply=new Reply();
        try {
            String sn = HostUtils.getSn(genSecurity).toUpperCase();
            reply.setData(sn);
            return setResultSuccess(reply);
        }catch (Exception e){
            log.error("getSn",e);
            return setResultFail("获取机器码失败",null);
        }
    }


    @PostMapping("/license/browse")
    @ResponseBody
    @ApiOperation(value = "即将过期证书查询")
    public ResponseBase getLicense()  {
        try {
            Reply reply = checkLicenseService.queryLicenseInfo();
            return setResultSuccess(reply);
        }catch (Exception e){
            log.error("getLicense",e);
            return setResultFail("获取数据失败",null);
        }
    }

    @PostMapping("/licenseDetail/browse")
    @ResponseBody
    @ApiOperation(value = "许可管理页面查询")
    public ResponseBase getLicenseDetail()  {
        try {
            Reply reply = checkLicenseService.queryLicenseList();
            return setResultSuccess(reply);
        }catch (Exception e){
            log.error("getLicenseDetail",e);
            return setResultFail("获取数据失败",null);
        }
    }
    @PostMapping("/licenseDetail/update")
    @ResponseBody
    @ApiOperation(value = "许可管理页面更新")
    public ResponseBase updateLicenseDetail()  {
        try {
            updateLicenseProp.updateProp();
            return setResultSuccess("许可更新成功");
        }catch (Exception e){
            log.error("updateLicenseDetail",e);
            return setResultFail("更新失败",null);
        }
    }

    @PostMapping("/company/browse")
    @ResponseBody
    @ApiOperation(value = "许可管理页面公司名称查询")
    public ResponseBase getLicenseCompany()  {
        Reply reply=new Reply();
        try {
            reply.setData(MWLicenseConfigLoad.company);
            return setResultSuccess(reply);
        }catch (Exception e){
            log.error("getLicenseCompany",e);
            return setResultFail("获取数据失败",null);
        }
    }

    @PostMapping("/license/upload")
    @ResponseBody
    @ApiOperation(value = "单个文件上传")
    public ResponseBase upload(@RequestParam("file") MultipartFile multipartFile) {
        Reply reply = new Reply();
        if (multipartFile.isEmpty()) {
            reply.setMsg("文件为空");
        }
        //获取文件名
        String fileName = multipartFile.getOriginalFilename();
       /* //获取文件的后缀名
        String suffixName = fileName.substring(fileName.lastIndexOf("."));*/
        //设置放到数据库字段的值
        String fileNameInTable = fileName;
        /*//文件重命名，防止重复
        fileName = UUIDUtils.getUUID() + fileName;*/
        File file = new File(new File(filePath + File.separator).getAbsolutePath() + File.separator + fileName);
        //检测是否存在目录
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            multipartFile.transferTo(file);
            reply.setData(fileNameInTable);
        } catch (Exception e) {
            log.error("错误:{}",e);
            return setResultFail("接口报错!", multipartFile);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/user/operation")
    @ResponseBody
    @ApiOperation(value = "用户关闭走马灯操作记录")
    public ResponseBase userOperation()  {
        Reply reply=new Reply();
        try {
            String loginName = iLoginCacheInfo.getLoginName();
            if(!redisTemplate.hasKey(loginName + "_operation")){
                //距离凌晨剩余时间
                long now = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                long overTime = (now - (sdf.parse(sdf.format(now)).getTime()));
                long timeNextDay = 24*60*60*1000 - overTime;
                redisTemplate.opsForValue().set(loginName + "_operation", "1",timeNextDay/1000, TimeUnit.SECONDS);
            }
            return setResultSuccess(reply);
        }catch (Exception e){
            log.error("getLicenseCompany",e);
            return setResultFail("获取数据失败",null);
        }
    }

}

