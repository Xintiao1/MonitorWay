package cn.mw.monitor.report.service.impl;

import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.report.dto.MwAssetsFlowStatisDto;
import cn.mw.monitor.report.service.MwAssetsFlowStatisReportService;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.netflow.api.MwNetflowCommonService;
import cn.mw.monitor.service.netflow.entity.NetflowResult;
import cn.mw.monitor.service.netflow.param.NetflowSearchParam;
import cn.mw.monitor.user.service.MWUserService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author gengjb
 * @description 资产流量统计报表
 * @date 2023/8/28 15:32
 */
@Service
@Slf4j
public class MwAssetsFlowStatisReportServiceImpl implements MwAssetsFlowStatisReportService {

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private MWUserService userService;

    @Autowired
    private MwNetflowCommonService netflowCommonService;

    /**
     * 获取资产流量统计信息
     * @return
     */
    @Override
    public Reply getAssetsFlowInfo(NetflowSearchParam netflowSearchParam) {
        try {
            List<MwAssetsFlowStatisDto> assetsFlowStatisDtos = new ArrayList<>();
            //根据参数信息获取资产
            Map<String, MwTangibleassetsTable> assetsInfo = getAssetsInfo(netflowSearchParam);
            List<NetflowResult> netflowResult = netflowCommonService.getNetflowResult(netflowSearchParam);
            if(assetsInfo == null || CollectionUtils.isEmpty(netflowResult)){Reply.ok(assetsFlowStatisDtos);}
            for (NetflowResult result : netflowResult) {
                MwTangibleassetsTable mwTangibleassetsTable = assetsInfo.get(result.getAssetsId());
                MwAssetsFlowStatisDto assetsFlowStatisDto = new MwAssetsFlowStatisDto();
                assetsFlowStatisDto.extractFrom(mwTangibleassetsTable,result,netflowSearchParam);
                assetsFlowStatisDtos.add(assetsFlowStatisDto);
            }
            PageInfo pageInfo = new PageInfo<>(assetsFlowStatisDtos);
            //数据分页
            if(CollectionUtils.isNotEmpty(assetsFlowStatisDtos)){
                //根据分页信息分割数据
                Integer pageNumber = netflowSearchParam.getPageNumber();
                Integer pageSize = netflowSearchParam.getPageSize();
                int fromIndex = pageSize * (pageNumber -1);
                int toIndex = pageSize * pageNumber;
                if(toIndex > assetsFlowStatisDtos.size()){
                    toIndex = assetsFlowStatisDtos.size();
                }
                List<MwAssetsFlowStatisDto> dtos = assetsFlowStatisDtos.subList(fromIndex, toIndex);
                pageInfo.setPageSize(pageSize);
                pageInfo.setList(dtos);
                pageInfo.setPageNum(pageNumber);
            }
            return Reply.ok(pageInfo);
        }catch (Throwable e){
            log.error("MwAssetsFlowStatisReportServiceImpl{} getAssetsFlowInfo()",e);
            return Reply.fail("MwAssetsFlowStatisReportServiceImpl{} getAssetsFlowInfo()",e);
        }
    }



    /**
     * 获取资产数据
     * @param
     * @return
     */
    private Map<String,MwTangibleassetsTable> getAssetsInfo(NetflowSearchParam netflowSearchParam){
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setIsQueryAssetsState(true);
        assetsParam.setUserId(userService.getGlobalUser().getUserId());
        if(CollectionUtils.isNotEmpty(netflowSearchParam.getAssetsId())){
            assetsParam.setAssetsIds(netflowSearchParam.getAssetsId());
        }
        List<MwTangibleassetsTable> mwTangAssetses = mwAssetsManager.getAssetsTable(assetsParam);
        return assetsGroup(mwTangAssetses,netflowSearchParam);
    }

    /**
     * 资产分组
     * @return
     */
    private Map<String,MwTangibleassetsTable> assetsGroup(List<MwTangibleassetsTable> mwTangAssetses,NetflowSearchParam netflowSearchParam){
        Map<String,MwTangibleassetsTable> assetsGroupMap = new HashMap<>();
        if(CollectionUtils.isEmpty(mwTangAssetses)){return assetsGroupMap;}
        List<String> assetsIds = new ArrayList<>();
        for (MwTangibleassetsTable mwTangAssets : mwTangAssetses) {
            String assetsId = mwTangAssets.getId() == null?String.valueOf(mwTangAssets.getModelInstanceId()):mwTangAssets.getId();
            assetsIds.add(assetsId);
            assetsGroupMap.put(assetsId,mwTangAssets);
        }
        netflowSearchParam.setAssetsId(assetsIds);
        return assetsGroupMap;
    }

    @Override
    public void exportAssetsFlowInfo(NetflowSearchParam netflowSearchParam, HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            List<MwAssetsFlowStatisDto> assetsFlowStatisDtos = new ArrayList<>();
            netflowSearchParam.setPageSize(Integer.MAX_VALUE);
            //查询数据
            Reply reply = getAssetsFlowInfo(netflowSearchParam);
            if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS){
                PageInfo pageInfo = (PageInfo) reply.getData();
                if(CollectionUtils.isEmpty(pageInfo.getList()))return;
                assetsFlowStatisDtos = pageInfo.getList();
            }
            excelWriter = exportReportSetInfo("资产流量统计报表",response,MwAssetsFlowStatisDto.class);
            HashSet<String> includeColumnFiledNames = new HashSet<>();
            includeColumnFiledNames.add("assetsName");
            includeColumnFiledNames.add("ipAddress");
            includeColumnFiledNames.add("assetsStatus");
            includeColumnFiledNames.add("startTime");
            includeColumnFiledNames.add("endTime");
            includeColumnFiledNames.add("totalFlowIn");
            includeColumnFiledNames.add("totalFlowOut");
            includeColumnFiledNames.add("maxFlowIn");
            includeColumnFiledNames.add("maxFlowOut");
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet")
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(assetsFlowStatisDtos, sheet);
            log.info("导出成功");
        }catch (Throwable e){
            log.error("MwAssetsFlowStatisReportServiceImpl{} exportAssetsFlowInfo()",e);
        }finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }


    /**
     * 设置导出信息
     * @param name
     * @param response
     * @param dtoclass
     * @return
     * @throws IOException
     */
    private ExcelWriter exportReportSetInfo(String name,HttpServletResponse response,Class dtoclass) throws IOException {
        String fileName = System.currentTimeMillis()+""; //导出文件名
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
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
        // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
        HorizontalCellStyleStrategy horizontalCellStyleStrategy=new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
        //创建easyExcel写出对象
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), dtoclass).registerWriteHandler(horizontalCellStyleStrategy).build();
        return excelWriter;
    }
}
