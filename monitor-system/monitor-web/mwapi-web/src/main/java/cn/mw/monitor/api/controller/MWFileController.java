package cn.mw.monitor.api.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.server.service.MwMyMonitorService;
import cn.mw.monitor.server.service.impl.MwServerManager;
import cn.mw.monitor.service.server.api.MyMonitorCommons;
import cn.mw.monitor.service.server.api.dto.AssetsBaseDTO;
import cn.mw.monitor.service.server.api.dto.ExportChartParam;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.server.api.dto.LineChartDTO;
import cn.mw.monitor.service.server.api.dto.MWHistoryDTO;
import cn.mw.monitor.service.server.api.dto.MyMonitorExportParam;
import cn.mw.monitor.service.user.dto.SettingDTO;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.fastjson.JSONArray;
import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import okhttp3.*;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author syt
 * @Date 2020/8/21 15:39
 * @Version 1.0
 */
@RequestMapping("/mwapi/file")
@Controller
@Api(value = "知识库", tags = "文件")
public class MWFileController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("control-" + MWFileController.class.getName());
    //logo上传目录
    static final String MODULE = "file-upload";
    //厂商图标上传目录
    static final String MODULE1 = "vendor-upload";

    static final String NAME = "NAME";
    static final String TYPE = "TYPE";

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    //文件上传路径
    @Value("${file.url}")
    private String filePath;

    @Value("${basicUrl}")
    private String vendorFilePath;

    @Value("${script-manage.spider.python.url}")
    private String spiderUrl;

    @Autowired
    private MyMonitorCommons myMonitorCommons;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private MwServerManager mwServerManager;

    @PostMapping("/upload")
    @ResponseBody
    @ApiOperation(value = "单个文件上传")
    public ResponseBase upload(@RequestParam("file") MultipartFile multipartFile, @RequestParam(name="model",required=false) String modelParam) {
        Reply reply = new Reply();
        if (multipartFile.isEmpty()) {
            reply.setMsg("文件为空");
        }
        //获取文件名
        String fileName = multipartFile.getOriginalFilename();


        //获取文件的后缀名
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        //文件重命名，防止重复

        if (fileName.contains("-")||fileName.replace(suffixName,"").contains(".")) {
            return setResultFail("文件内不允许存在特殊符号",reply);
        }
        /*fileName = fileName.replace(suffixName,"-")+UUIDUtils.getUUID() + suffixName;*/
        //保持文件上传下载一致
        fileName = fileName.replace(suffixName,"")+ suffixName;
        String dateStr = df.format(new Date());
        String varPathUrl = MODULE + "/" + dateStr;
        String path = filePath + File.separator + MODULE + File.separator + dateStr;

        if(StringUtils.isNotEmpty(modelParam)){
            varPathUrl = modelParam + "/" + dateStr;
            path = filePath + File.separator + modelParam + File.separator + dateStr;
        }

        File file = new File(new File(path).getAbsolutePath() + File.separator + fileName);
        varPathUrl = varPathUrl + "/" + fileName;

        //检测是否存在目录
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            multipartFile.transferTo(file);
            //url访问文件名,分割符
            reply.setData(varPathUrl);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), multipartFile);
        }
        chmod(file);
        return setResultSuccess(reply);
    }

    @PostMapping("/upload-logo/create")
    @ResponseBody
    @ApiOperation(value = "logo文件上传")
    public ResponseBase uploadLogo(@RequestParam("file") MultipartFile multipartFile) {
        Reply reply = new Reply();
        if (multipartFile.isEmpty()) {
            reply.setMsg("文件为空");
        }
        //获取文件名
        String fileName = multipartFile.getOriginalFilename();
        //文件重命名，防止重复
        fileName = UUIDUtils.getUUID() + fileName;
        //设置放到数据库字段的值
        String fileNameInTable = fileName;
        File file = new File(new File(filePath).getAbsolutePath() + File.separator + MODULE + File.separator + fileName);
        //检测是否存在目录
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        SettingDTO logo = new SettingDTO();
        try {
            multipartFile.transferTo(file);
            logo.setLogoUrl(fileNameInTable);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), multipartFile);
        }
        reply.setData(logo);
        chmod(file);
        return setResultSuccess(reply);
    }

    @PostMapping("/vendor-upload")
    @ResponseBody
    @ApiOperation(value = "厂商图标上传")
    public ResponseBase vendorUpload(@RequestParam("file") MultipartFile multipartFile) {
        Reply reply = new Reply();
        if (multipartFile.isEmpty()) {
            reply.setMsg("文件为空");
        }
        //获取文件名
        String fileName = multipartFile.getOriginalFilename();
        //获取文件的后缀名
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        //设置放到数据库字段的值
        String fileNameInTable = UUIDUtils.getUUID() + suffixName;
//        String fileNameInTable = "999999" + suffixName;

        File file = new File(new File(filePath).getAbsolutePath()
                + File.separator + MODULE1 + File.separator + fileNameInTable);
        //检测是否存在目录
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            multipartFile.transferTo(file);
            reply.setData(fileNameInTable);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), multipartFile);
        }
        chmod(file);
        return setResultSuccess(reply);
    }

    @PostMapping("/user/icon/upload")
    @ResponseBody
    @ApiOperation(value = "用户头像上传")
    public ResponseBase userImageUpload(@RequestParam("file") MultipartFile multipartFile) {
        Reply reply = new Reply();
        if (multipartFile.isEmpty()) {
            reply.setMsg("文件为空");
            return setResultFail(reply.getMsg(),null);
        }
        if (multipartFile.getSize() > 100 * 1024 || multipartFile.getSize() < 1 * 1024){
            reply.setMsg("文件大小应在1K~100K");
            return setResultFail(reply.getMsg(),null);
        }
        //获取文件名
        String fileName = multipartFile.getOriginalFilename();
        //获取文件的后缀名
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        //设置放到数据库字段的值
        String fileNameInTable = UUIDUtils.getUUID() + suffixName;
        //转存文件
        File file = new File(new File(filePath).getAbsolutePath()
                + File.separator + MODULE + File.separator + fileNameInTable);
        //检测是否存在目录
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            multipartFile.transferTo(file);
            logger.info("userImageUpload:" + fileNameInTable);
            reply.setData(fileNameInTable);
        } catch (Exception e) {
            logger.error("上传图片失败",e);
            return setResultFail("上传失败", multipartFile);
        }
        chmod(file);
        return setResultSuccess(reply);
    }



    @PostMapping("/script-manage/file/upload")
    @ResponseBody
    @ApiOperation(value = "脚本管理文件上传")
    public ResponseBase scriptFileUpload(@RequestParam("file") MultipartFile multipartFile) {
        Reply reply = new Reply();
        if (multipartFile.isEmpty()) {
            reply.setMsg("文件为空");
            return setResultFail(reply.getMsg(), null);
        }
        if (multipartFile.getSize() > 1 * 1024 * 1024 || multipartFile.getSize() < 0) {
            reply.setMsg("文件大小应小于1G");
            return setResultFail(reply.getMsg(), null);
        }
        //获取文件名
        String fileName = multipartFile.getOriginalFilename();
        String faile = fileName.replaceAll(" ","");
        if (hasChinese(fileName)||faile.length()!=fileName.length()){
            reply.setMsg("文件名不含中文和空格等非法字符");
            return setResultFail(reply.getMsg(), null);
        }
        //转存文件
        File file = new File(new File(filePath).getAbsolutePath()
                + File.separator + MODULE + File.separator + fileName);
//        File file = new File("D:\\"+fileName);
        //检测是否存在目录
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            multipartFile.transferTo(file);
            logger.info("scriptFileUpload:" + fileName);
            String spiderFilePath = sendFileToSpider(file);
            if (StringUtils.isEmpty(spiderFilePath)) {
                return setResultFail("文件上传失败", null);
            }
            reply.setData(spiderFilePath);
        } catch (Exception e) {
            logger.error("上传文件失败", e);
            return setResultFail("上传文件失败", multipartFile);
        }
        chmod(file);
        return setResultSuccess(reply);
    }

    /**
     * 同步上传文件至spider
     * @param file 文件
     * @return 新的地址
     */
    private String sendFileToSpider( File file) {
        String newFilePath = "";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            okhttp3.RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("File", file.getName(),
                            okhttp3.RequestBody.create(MediaType.parse("application/octet-stream"),
                                    new File(file.getAbsolutePath())))
                    .build();
            Request request = new Request.Builder()
                    .url(spiderUrl + "/admin/uploader")
                    .method("POST", body)
                    .addHeader("X-Token", "mw64799c7760ef4bf796bce6b3a6051b58")
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                newFilePath = response.body().string();
                logger.error("spider file path is "+newFilePath);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return newFilePath;
    }

    private void chmod(File file){
//        String command = "chmod 644 " + file.getAbsolutePath();
//        ProcessBuilder processBuilder = new ProcessBuilder(command);
//        processBuilder.directory(file);
//        processBuilder.redirectInput(ProcessBuilder.Redirect.from(file));
//        processBuilder.redirectOutput(ProcessBuilder.Redirect.to(file));
////        Runtime runtime = Runtime.getRuntime();
//        try {
//            Process process = processBuilder.start();
//            InputStream inputStream = process.getInputStream();
//            OutputStream outputStream = process.getOutputStream();
////            Process process = runtime.exec(command);
//            int exitValue = process.waitFor();
////            int exitValue = process.exitValue();
//            if (exitValue != 0) {
//                logger.error("change file permission failed");
//            }
//        } catch (Exception e) {
//            logger.error("change file permission failed",e);
//        }
    }
    //
//    @PostMapping("/multiUpload")
//    @ResponseBody
//    @ApiOperation(value = "多个文件上传")
//    public ResponseBase multiUpload( @RequestParam("file") MultipartFile[] multipartFiles) {
//        Reply reply = new Reply();
//        if (multipartFiles.length == 0) {
//            reply.setMsg("请选择要上传的文件");
//        }
//        String attachmentUrl = "";
//        for(MultipartFile multipartFile : multipartFiles){
//            if(multipartFile.isEmpty()){
//                reply.setMsg("文件上传失败");
//            }
//            //获取文件名
//            String fileName = multipartFile.getOriginalFilename();
//            //获取文件的后缀名
//            String suffixName = fileName.substring(fileName.lastIndexOf("."));
//            //设置放到数据库字段的值
//            String fileNameInTable = UUIDUtils.getUUID() + "|" + fileName;
//            attachmentUrl = attachmentUrl + fileNameInTable;
//            //文件重命名，防止重复
//            fileName = UUIDUtils.getUUID() + fileName;
//            File file = new File(filePath+fileName);
//            //检测是否存在目录
//            if (!file.getParentFile().exists()) {
//                file.getParentFile().mkdirs();
//            }
//            try {
//                multipartFile.transferTo(file);
//
//            } catch (Exception e) {
//                logger.error(e.getMessage());
//                return setResultFail(e.getMessage(), multipartFile);
//            }
//        }
//        reply.setData(attachmentUrl);
//        return setResultSuccess(reply);
//    }
    @PostMapping("/download")
    @ResponseBody
    @ApiOperation(value = "文件下载")
    public ResponseBase download(@RequestParam String fileName, final HttpServletResponse response, final HttpServletRequest request) {
        Reply reply = new Reply();
        OutputStream os = null;
        InputStream is = null;
        try {
            //取得输出流
            os = response.getOutputStream();
            //清空输出流
            response.reset();
            response.setContentType("application/x-download;charset=GBK");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("utf-8")));
            //读取流
            File f = new File(filePath + File.separator + MODULE + File.separator + fileName);
            is = new FileInputStream(f);
            if (is == null) {
                logger.error("下载附件失败，请检查文件" + fileName + "是否存在");
                return setResultFail("下载附件失败，请检查文件" + fileName + "是否存在", fileName);
            }
            IOUtils.copy(is, response.getOutputStream());
        } catch (IOException e) {
            return setResultFail("下载附件失败，error:" + e.getMessage(), fileName);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
                return setResultFail(e.getMessage(), response);
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
                return setResultFail(e.getMessage(), response);
            }
        }
        return setResultSuccess(reply);
    }

    //将list集合数据按照指定大小分成好几个小的list
    public List<List<List<Object>>> getSubList(List<List<Object>> allData, int size) {
        List<List<List<Object>>> result = new ArrayList();
        for (int begin = 0; begin < allData.size(); begin = begin + size) {
            int end = (begin + size > allData.size() ? allData.size() : begin + size);
            result.add(allData.subList(begin, end));
        }
        return result;
    }

    @PostMapping("/excelOut")
    @ResponseBody
    @ApiOperation(value = "导出excel表格")
    public ResponseBase getExcel(@RequestBody ExportChartParam uParam, HttpServletResponse response, final HttpServletRequest request) {
        ExcelWriter excelWriter = null;
        Date startTime = null;
        Date endTime = null;
        try {
            //需要导出的数据
            if (uParam.getDateType() != null) {
                Calendar calendar = Calendar.getInstance();
                endTime = calendar.getTime();
                switch (uParam.getDateType()) {//1：hour 2:day 3:week 4:month
                    case 1:
                        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 1);
                        startTime = calendar.getTime();
                        break;
                    case 2:
                        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
                        startTime = calendar.getTime();
                        break;
                    case 3:
                        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 7);
                        startTime = calendar.getTime();
                        break;
                    case 4:
                        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
                        startTime = calendar.getTime();
                        break;
                    default:
                        break;
                }
            }
            int serverId = uParam.getMonitorServerId();
            List<List<MWHistoryDTO>> historys = new ArrayList<>();
            List<ItemApplication> items = uParam.getItemsList();
            boolean typeFlag = false;//记录是否有接口名称或者其他分区名称
            boolean webMonitor = false;
            if (!Strings.isNullOrEmpty(uParam.getWebName()) && !Strings.isNullOrEmpty(uParam.getTitleType())) {
                String key = null;
                if (uParam.getTitleType().equals("1")) {
                    key = "web.test.in[" + uParam.getWebName() + "," + uParam.getWebName() + "," + "bps]";
                } else if (uParam.getTitleType().equals("2")) {
                    key = "web.test.time[" + uParam.getWebName() + "," + uParam.getWebName() + "," + "resp]";
                }
                Assert.notNull(key, ErrorConstant.WEBMONITOR_MSG_301008);
                Assert.notNull(uParam.getAssetsId(), ErrorConstant.WEBMONITOR_MSG_301008);
                Assert.notNull(serverId, ErrorConstant.WEBMONITOR_MSG_301008);
                String hostId = uParam.getAssetsId();
                List<String> hostids = new ArrayList<>();
                hostids.add(hostId);
                MWZabbixAPIResult result = mwtpServerAPI.getWebItemId(serverId, hostids, key);
                items = JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class);
                webMonitor = true;
            }

            boolean avgFlag = false;
            if (items == null || items.size() <= 0) {
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbyFilter(serverId, uParam.getItemNames(), uParam.getAssetsId());
                //根据主机查询对应的监控项信息
                items = JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class);
                if (items == null || items.size() <= 0) {
                    MWZabbixAPIResult result1 = mwtpServerAPI.itemGetbySearch(serverId, uParam.getItemNames(), uParam.getAssetsId());
                    //根据主机查询对应的监控项信息
                    items = JSONArray.parseArray(String.valueOf(result1.getData()), ItemApplication.class);
                    if (items != null && items.size() > 0) {
                        items = items.stream().filter(item -> item.getName().indexOf("]") != -1).collect(Collectors.toList());
                        if (uParam.getItemNames().size() == 1) {
                            avgFlag = true;
                        }
                    }
                }

            }
            //记录监控项对应的信息
            Map<String, String> map = new HashMap<>();
            //颗粒度
            int n = 0;
            if (uParam.getDelay() > 0) {
                n = uParam.getDelay();
            }
            int maxSize = 0;
            for (int i = 0; i < items.size(); i++) {
                String itemid = items.get(i).getItemid();
                String name = items.get(i).getName();

                //记录监控项对应可能存在名称当且仅当有一个中括号的情况
                if (name.indexOf("[") != -1) {
                    typeFlag = true;
                    map.put(itemid + TYPE, name.substring(name.indexOf("[") + 1, name.indexOf("]")));
                }
                //记录监控项对应单位
                map.put(itemid, items.get(i).getUnits());
                //记录监控项对应中文名称
                map.put(itemid + NAME, mwServerManager.getChNameWithout(name));
                if (webMonitor) {
                    typeFlag = true;
                    map.put(itemid + TYPE, uParam.getWebName());
                    map.put(itemid + NAME, "1".equals(uParam.getTitleType()) ? "网站下载速度" : "相应时间");
                }
                String value_type = items.get(i).getValue_type();
                long time_from = 0l;
                long time_till = 0l;
                if (!Strings.isNullOrEmpty(uParam.getDateStart()) && !Strings.isNullOrEmpty(uParam.getDateEnd()) && uParam.getDateType() == 5) {
                    time_from = DateUtils.parse(uParam.getDateStart()).getTime() / 1000L;
                    time_till = DateUtils.parse(uParam.getDateEnd()).getTime() / 1000L;
                } else {
                    time_from = startTime.getTime() / 1000L;
                    time_till = endTime.getTime() / 1000L;
                    uParam.setDateStart(DateUtils.formatDateTime(startTime));
                    uParam.setDateEnd(DateUtils.formatDateTime(endTime));
                }
                List<MWHistoryDTO> history = myMonitorCommons
                        .getHistoryByItemId(serverId, itemid, time_from, time_till, Integer.parseInt(value_type),uParam.getIsTrend(),uParam.getValueType(),uParam.getDateType());
                historys.add(history);
                if (maxSize < history.size()) {
                    maxSize = history.size();
                }
            }

            if (avgFlag) {
                int len = historys.size();
                int sum = 0;
                ItemApplication itemApplication = items.get(0);
                String itemId = itemApplication.getItemid();
                items.clear();
                items.add(itemApplication);
                List<MWHistoryDTO> listInfo = new ArrayList<>();
                for (int x = 0; x < maxSize; x++) {
                    Double lastValue = 0.0;
                    Double valueDouble;
                    String dateTime = null;
                    sum = 0;
                    for (int y = 0; y < len; y++) {
                        if (historys.get(y).get(x) != null) {
                            sum++;
                            lastValue = lastValue + Double.valueOf(historys.get(y).get(x).getValue());
                        }
                        if (historys.get(y).get(x).getClock() != null) {
                            dateTime = historys.get(y).get(x).getClock();
                        }
                    }
                    valueDouble = Double.valueOf(lastValue / sum);
                    //只保留两位小数
                    Double values = new BigDecimal(valueDouble).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    MWHistoryDTO mwHistoryDTO = new MWHistoryDTO();
                    mwHistoryDTO.setLastValue(values);
                    mwHistoryDTO.setClock(dateTime);
                    mwHistoryDTO.setValue(String.valueOf(values));
                    mwHistoryDTO.setItemid(itemId);
                    listInfo.add(mwHistoryDTO);
                }
                historys.clear();
                historys.add(listInfo);
            }
            //将需要导出的数据分为50000一组(一个sheet最多只能放入65000左右条数据)
            List<List<Object>> resultList = new ArrayList<>();
            if (historys.size() > 0) {
                resultList = getDataList(historys, map, n,uParam.getHostName());

            }
            List<List<List<Object>>> li = getSubList(
                    resultList, 50000);

            //设置回复头一些信息
            String fileName = null; //导出文件名
            if (uParam.getName() != null && !uParam.getName().equals("")) {
                fileName = uParam.getName();
            } else {
                fileName = System.currentTimeMillis() + "";
            }

            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            logger.info("fileName: {}", fileName);

            // 头的策略
            WriteCellStyle headWriteCellStyle = new WriteCellStyle();

            WriteFont headWriteFont = new WriteFont();
            headWriteFont.setFontHeightInPoints((short) 11);
            headWriteCellStyle.setWriteFont(headWriteFont);
            // 内容的策略
            WriteCellStyle contentWriteCellStyle = new WriteCellStyle();

            WriteFont contentWriteFont = new WriteFont();
            // 字体大小
            contentWriteFont.setFontHeightInPoints((short) 12);
            contentWriteCellStyle.setWriteFont(contentWriteFont);
            //设置垂直居中
            contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            //设置垂直居中
            contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
            // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
            HorizontalCellStyleStrategy horizontalCellStyleStrategy =
                    new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);

            //创建easyExcel写出对象
            excelWriter = EasyExcel
                    .write(response.getOutputStream())
                    .head(head(uParam.getDateStart(), uParam.getDateEnd(), items.size(), typeFlag))
                    .registerWriteHandler(horizontalCellStyleStrategy).build();


            //计算sheet分页
            Integer sheetNum = resultList.size() % 50000 == 0 ? resultList.size() / 50000 : resultList.size() / 50000 + 1;
            for (int i = 0; i < sheetNum; i++) {
                WriteSheet sheet = EasyExcel.writerSheet(i, "sheet" + i).build();
                excelWriter.write(li.get(i), sheet);
            }
            if (sheetNum == 0) {
                WriteSheet sheet = EasyExcel.writerSheet(0, "sheet" + 0).build();
                excelWriter.write(resultList, sheet);
            }

            logger.info("导出成功");
        } catch (Exception e) {
            logger.error("导出失败", e);
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
        return setResultSuccess();
    }

    private List<List<String>> head(String dateStart, String dateEnd, int count, boolean typeFlag) {
        StringBuffer sb = new StringBuffer();
        String s = sb.append("开始时间：        ").append(dateStart).append("  ~  ").append("结束时间：        ").append(dateEnd).toString();
        List<List<String>> list = new ArrayList<List<String>>();
        if (count <= 0) {
            List<String> head = new ArrayList<>();
            head.add(s);
            list.add(head);
            return list;
        }
        for (int i = 0; i < count; i++) {
            if (typeFlag) {
                List<String> head = new ArrayList<>();
                head.add(s);
                head.add("名称");
                list.add(head);
            }
            List<String> head0 = new ArrayList<>();
            head0.add(s);
            head0.add("监控指标");
            List<String> head1 = new ArrayList<>();
            head1.add(s);
            head1.add("时间");
            List<String> head2 = new ArrayList<>();
            head2.add(s);
            head2.add("值");
            List<String> head3 = new ArrayList<>();
            head3.add(s);
            head3.add("单位");
            List<String> head4 = new ArrayList<>();
            head4.add(s);
            head4.add("");
            list.add(head0);
            list.add(head1);
            list.add(head2);
            list.add(head3);
            list.add(head4);
        }
        return list;
    }

    //

    /**
     * 将list集合数据转换成以对象中的list为首的新list
     *
     * @param historys 原始历史数据，一个监控项/多个监控项
     * @param map      监控项对应信息
     * @param n        隔几条取一条
     * @return
     */
    public List<List<Object>> getDataList(List<List<MWHistoryDTO>> historys, Map<String, String> map, int n,String hostName) {
        List<List<Object>> list = new ArrayList<>();
        int size = historys.get(0).size();
        if(historys.size() > 1){
            if(historys.get(1).size() < size){
                size = historys.get(1).size();
            }
        }
        List<Object> data = null;
        for (int j = 0; j < size; j = (n <= 1) ? j + 1 : (j + n)) {
            data = new ArrayList<>();
            MWHistoryDTO mwHistoryDTO = null;
            for (int i = 0; i < historys.size(); i++) {//将所有数据拼接成一行
                mwHistoryDTO = historys.get(i).get(j);
                String nameType = map.get(mwHistoryDTO.getItemid() + TYPE);
                if (nameType != null && StringUtils.isNotEmpty(nameType)) {
                    if(StringUtils.isNotBlank(hostName)){
                        //名称
                        data.add(hostName+"-"+nameType);
                    }else{
                        //名称
                        data.add(nameType);
                    }
                }
                //监控项名称
                data.add(map.get(mwHistoryDTO.getItemid() + NAME));
                //时间
                data.add(new Date(Long.valueOf(mwHistoryDTO.getClock()) * 1000L));
                //值
                data.add(mwHistoryDTO.getValue());
                //单位
                data.add(map.get(mwHistoryDTO.getItemid()));
                //空一格
                data.add(null);
            }
            list.add(data);
        }
        return list;
    }

    @Autowired
    private MwMyMonitorService myMonitorService;

    @PostMapping("/myMonitor/excelOut")
    @ResponseBody
    @ApiOperation(value = "导出excel表格")
    public ResponseBase getMyMonitorExcel(@RequestBody MyMonitorExportParam uParam, HttpServletResponse response, final HttpServletRequest request) {
        ExcelWriter excelWriter = null;
        try {
            LineChartDTO dto = myMonitorService.getLineChartDTO(uParam);
            AssetsBaseDTO assetsBaseDTO = dto.getAssetsBaseDTO();

            //需要导出的数据
            int serverId = assetsBaseDTO.getMonitorServerId();
            List<List<MWHistoryDTO>> historys = new ArrayList<>();
            List<ItemApplication> items = dto.getItemApplicationList();
            boolean typeFlag = false;//记录是否有接口名称或者其他分区名称

            boolean avgFlag = false;
            if (items == null || items.size() <= 0) {
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbyFilter(serverId, uParam.getItemNames(), assetsBaseDTO.getAssetsId());
                //根据主机查询对应的监控项信息
                items = JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class);
                if (items == null || items.size() <= 0) {
                    MWZabbixAPIResult result1 = mwtpServerAPI.itemGetbySearch(serverId, uParam.getItemNames(), assetsBaseDTO.getAssetsId());
                    //根据主机查询对应的监控项信息
                    items = JSONArray.parseArray(String.valueOf(result1.getData()), ItemApplication.class);
                    if (items != null && items.size() > 0) {
                        items = items.stream().filter(item -> item.getName().indexOf("]") != -1).collect(Collectors.toList());
                        if (uParam.getItemNames().size() == 1) {
                            avgFlag = true;
                        }
                    }
                }
            }
            //记录监控项对应的信息
            Map<String, String> map = new HashMap<>();
            //颗粒度
            int n = 0;
            if (uParam.getDelay() > 0) {
                n = uParam.getDelay();
            }
            int maxSize = 0;
            for (int i = 0; i < items.size(); i++) {
                String itemid = items.get(i).getItemid();
                String name = items.get(i).getName();

                //记录监控项对应可能存在名称当且仅当有一个中括号的情况
                if (name.indexOf("[") != -1) {
                    typeFlag = true;
                    map.put(itemid + TYPE, name.substring(name.indexOf("[") + 1, name.indexOf("]")));
                }
                //记录监控项对应单位
                map.put(itemid, items.get(i).getUnits());
                //记录监控项对应中文名称
                map.put(itemid + NAME, mwServerManager.getChNameWithout(name));
                String value_type = items.get(i).getValue_type();
                List<MWHistoryDTO> history = myMonitorCommons
                        .getHistoryByItemId(serverId, itemid, DateUtils.parse(uParam.getDateStart()).getTime() / 1000L, DateUtils.parse(uParam.getDateEnd()).getTime() / 1000L, Integer.parseInt(value_type),uParam.getIsTrend(),uParam.getValueType(),uParam.getDateType());
                historys.add(history);
                if (maxSize < history.size()) {
                    maxSize = history.size();
                }
            }

            if (avgFlag) {
                int len = historys.size();
                int sum = 0;
                ItemApplication itemApplication = items.get(0);
                String itemId = itemApplication.getItemid();
                items.clear();
                items.add(itemApplication);
                List<MWHistoryDTO> listInfo = new ArrayList<>();
                for (int x = 0; x < maxSize; x++) {
                    Double lastValue = 0.0;
                    Double valueDouble;
                    String dateTime = null;
                    sum = 0;
                    for (int y = 0; y < len; y++) {
                        if (historys.get(y).get(x) != null) {
                            sum++;
                            lastValue = lastValue + Double.valueOf(historys.get(y).get(x).getValue());
                        }
                        if (historys.get(y).get(x).getClock() != null) {
                            dateTime = historys.get(y).get(x).getClock();
                        }
                    }
                    valueDouble = Double.valueOf(lastValue / sum);
                    //只保留两位小数
                    Double values = new BigDecimal(valueDouble).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    MWHistoryDTO mwHistoryDTO = new MWHistoryDTO();
                    mwHistoryDTO.setLastValue(values);
                    mwHistoryDTO.setClock(dateTime);
                    mwHistoryDTO.setValue(String.valueOf(values));
                    mwHistoryDTO.setItemid(itemId);
                    listInfo.add(mwHistoryDTO);
                }
                historys.clear();
                historys.add(listInfo);
            }
            //将需要导出的数据分为50000一组(一个sheet最多只能放入65000左右条数据)

            List<List<Object>> resultList = getDataList(historys, map, n,uParam.getHostName());
            List<List<List<Object>>> li = getSubList(
                    resultList, 50000);

            //设置回复头一些信息
            String fileName = System.currentTimeMillis() + ""; //导出文件名

            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            logger.info("fileName: {}", fileName);

            // 头的策略
            WriteCellStyle headWriteCellStyle = new WriteCellStyle();

            WriteFont headWriteFont = new WriteFont();
            headWriteFont.setFontHeightInPoints((short) 11);
            headWriteCellStyle.setWriteFont(headWriteFont);
            // 内容的策略
            WriteCellStyle contentWriteCellStyle = new WriteCellStyle();

            WriteFont contentWriteFont = new WriteFont();
            // 字体大小
            contentWriteFont.setFontHeightInPoints((short) 12);
            contentWriteCellStyle.setWriteFont(contentWriteFont);
            //设置垂直居中
            contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            //设置垂直居中
            contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
            // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
            HorizontalCellStyleStrategy horizontalCellStyleStrategy =
                    new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);

            //创建easyExcel写出对象
            excelWriter = EasyExcel
                    .write(response.getOutputStream())
                    .head(head(uParam.getDateStart(), uParam.getDateEnd(), items.size(), typeFlag))
                    .registerWriteHandler(horizontalCellStyleStrategy).build();


            //计算sheet分页
            Integer sheetNum = resultList.size() % 50000 == 0 ? resultList.size() / 50000 : resultList.size() / 50000 + 1;
            for (int i = 0; i < sheetNum; i++) {
                WriteSheet sheet = EasyExcel.writerSheet(i, "sheet" + i).build();
                excelWriter.write(li.get(i), sheet);
            }
            if (sheetNum == 0) {
                WriteSheet sheet = EasyExcel.writerSheet(0, "sheet" + 0).build();
                excelWriter.write(resultList, sheet);
            }
            logger.info("导出成功");
        } catch (Exception e) {
            logger.error("导出失败", e);
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
        return setResultSuccess();
    }


    public static boolean hasChinese(CharSequence content) {
        if (null == content) {
            return false;
        }
        String regex = "[\u2E80-\u2EFF\u2F00-\u2FDF\u31C0-\u31EF\u3400-\u4DBF\u4E00-\u9FFF\uF900-\uFAFF\uD840\uDC00-\uD869\uDEDF\uD869\uDF00-\uD86D\uDF3F\uD86D\uDF40-\uD86E\uDC1F\uD86E\uDC20-\uD873\uDEAF\uD87E\uDC00-\uD87E\uDE1F]+";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(content).find();
    }
}
