package cn.mw.monitor.api.controller;

import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.UUIDUtils;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.service.topo.api.MwAssetsLogoService;
import cn.mw.monitor.service.topo.model.MwAssetsLogoDTO;
import cn.mw.monitor.service.topo.param.InsertAssetsLogoParam;
import cn.mw.monitor.service.topo.param.QueryAssetsLogoParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Iterator;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "拓扑图例")
public class MwAssetsLogoController extends BaseApiService {

    //文件上传路径
    @Value("${file.url}")
    private String filePath;

    @Autowired
    MwAssetsLogoService mwAssetsLogoService;

    @PostMapping("/topology-alert/browse")
    @ResponseBody
    @ApiOperation(value = "资产图标列表查看")
    public ResponseBase queryList(@RequestBody QueryAssetsLogoParam qParam,
                                  HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply = null;
        try{
            reply = mwAssetsLogoService.selectList(qParam);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            log.error("queryList", e);
            return setResultFail(e.getMessage(), qParam);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/topology-alert/editor")
    @ResponseBody
    @ApiOperation(value = "资产图标查看")
    public ResponseBase editor(@RequestBody QueryAssetsLogoParam qParam,
                             HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply = null;
        try{
            reply = mwAssetsLogoService.selectById(qParam.getId());
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), qParam);
        }
        return setResultSuccess(reply.getData());
    }

    @PostMapping("/topology-alert/create")
    @ResponseBody
    @ApiOperation(value = "资产图标新增")
    public ResponseBase create(@RequestBody InsertAssetsLogoParam insertAssetsLogoParam,
                               HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply = null;
        try{
            if(0 == insertAssetsLogoParam.getId()) {
                reply = mwAssetsLogoService.insert(insertAssetsLogoParam);
            }else{
                MwAssetsLogoDTO mwAssetsLogoDTO = new MwAssetsLogoDTO();
                CopyUtils.copyObj(insertAssetsLogoParam,mwAssetsLogoDTO);
                mwAssetsLogoService.update(mwAssetsLogoDTO);
            }
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), insertAssetsLogoParam);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/topology-alert/delete")
    @ResponseBody
    @ApiOperation(value = "资产图标删除")
    public ResponseBase create(@RequestBody QueryAssetsLogoParam qParam,
                               HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply = null;
        try{
            mwAssetsLogoService.setLogoDir(this.filePath + File.separator + MwAssetsLogoService.MODULE);
            reply = mwAssetsLogoService.delete(qParam.getId());
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), qParam);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/topology-alert/uploads/create")
    @ResponseBody
    @ApiOperation(value = "上传资产图标")
    public ResponseBase upload(HttpServletRequest request, RedirectAttributesModelMap model){
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Reply reply = new Reply();
        Iterator<String> fileNames = multipartRequest.getFileNames();
        while (fileNames.hasNext()){
            String key = fileNames.next();
            MultipartFile multipartFile = multipartRequest.getFile(key);
            String fileName = multipartFile.getOriginalFilename();
            int index = fileName.lastIndexOf(".");
            String suffix = fileName.substring(index);
            //设置放到数据库字段的值
            String uuid = UUIDUtils.getUUID();
            StringBuffer fileNameInTable = new StringBuffer(Constants.UPLOAD_BASE_URL)
                    .append(MwAssetsLogoService.MODULE).append("/").append(uuid).append(suffix);
            //文件重命名，防止重复
            fileName = uuid + suffix;
            File file = new File(new File(filePath).getAbsolutePath()
                    + File.separator + MwAssetsLogoService.MODULE
                    + File.separator + fileName);
            //检测是否存在目录
            try {

                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                    file.getParentFile().setReadable(true, false);
                    file.getParentFile().setWritable(true, false);
                    file.getParentFile().setExecutable(true, false);

                    file.getParentFile().getParentFile().setReadable(true, false);
                    file.getParentFile().getParentFile().setWritable(true, false);
                    file.getParentFile().getParentFile().setExecutable(true, false);

                }

                multipartFile.transferTo(file);
                file.setReadable(true, false);
                file.setWritable(true, false);

                reply.setData(fileNameInTable.toString());
            } catch (Exception e) {
                log.error("upload",e);
                return setResultFail(e.getMessage(), multipartFile);
            }
        }
        return setResultSuccess(reply);
    }
}
