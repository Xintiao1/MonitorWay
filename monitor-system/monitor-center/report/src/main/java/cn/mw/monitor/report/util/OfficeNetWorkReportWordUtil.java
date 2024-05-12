package cn.mw.monitor.report.util;

import cn.mw.monitor.report.dto.PatrolInspectionDeviceDto;
import cn.mw.monitor.report.dto.PatrolInspectionRunStatusDto;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @ClassName OfficeNetWorkReportWordUtil
 * @Description 办公网巡检报告导出word工具类
 * @Author gengjb
 * @Date 2022/12/22 10:48
 * @Version 1.0
 **/
@Slf4j
public class OfficeNetWorkReportWordUtil {

    /**
     * 根据参数，模板生成word文件
     * @param param 参数集合
     * @param template 模板路径
     * @return
     */
    public static XWPFDocument generateWord(Map<String,Object> param, String template){
        XWPFDocument document = null;
        try {
            OPCPackage pack = POIXMLDocument.openPackage(template);
            document = new XWPFDocument(pack);
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            //处理占位符数据
            handlePlaceholder(paragraphs,param,document);
            //处理表格数据
            handleTemplateTable(document,param);
        }catch (Throwable e){
            log.error("办公网巡检报告生成word模板失败，失败信息:"+e.getMessage());
        }
        return document;
    }

    /**
     * 处理模板中的占位符
     * @param paragraphs
     * @param param
     * @param document
     */
    public static void handlePlaceholder(List<XWPFParagraph> paragraphs,Map<String,Object> param,XWPFDocument document){
        if(CollectionUtils.isEmpty(paragraphs))return;
        for (XWPFParagraph paragraph : paragraphs) {
            List<XWPFRun> runs = paragraph.getRuns();
            for (XWPFRun run : runs) {
                String text = run.getText(0);
                if(StringUtils.isBlank(text))continue;
                for (Map.Entry<String, Object> entry : param.entrySet()) {
                    String key = entry.getKey();
                    if(text.contains(key)){
                        Object value = entry.getValue();
                        text = text.replace(key, value.toString());
                    }
                }
                run.setText(text,0);
            }
        }
    }

    public static void handleTemplateTable(XWPFDocument document,Map<String,Object> param) throws Exception {
        List<PatrolInspectionRunStatusDto> runStatusDtos = (List<PatrolInspectionRunStatusDto>) param.get("runStatus");
        Map<String, PatrolInspectionRunStatusDto> runStatusMap = handleRunStatus(runStatusDtos);
        // 处理表格
        Iterator<XWPFTable> it = document.getTablesIterator();
        //表格索引
        int i = 0;
        while (it.hasNext()) {
            XWPFTable table = it.next();
            int size = table.getRows().size() - 1;
            XWPFTableRow row2 = table.getRow(size);
            List<XWPFTableRow> rows = table.getRows();
            List<PatrolInspectionDeviceDto> deviceDtos;
            int _row = 0;
            for (XWPFTableRow row : rows) {
                List<XWPFTableCell> cells = row.getTableCells();
                for (XWPFTableCell cell : cells) {
                    List<XWPFParagraph> paragraphListTable = cell.getParagraphs();
                    processParagraphs(paragraphListTable, param, document);
                }
                String templateName;
                switch (i){
                    case 0://运行状态数据
                        if(CollectionUtils.isEmpty(runStatusDtos))continue;
                        templateName = row.getCell(0).getText();
                        for (PatrolInspectionRunStatusDto runStatusDto : runStatusDtos) {
                            String name = runStatusDto.getName();
                            //名称相同,设置值
                            if(name.equals(templateName)){
                                row.getCell(1).setText(runStatusDto.getAssetsTotal().toString());//总资产数量
                                row.getCell(2).setText(runStatusDto.getNormalAssetsCount().toString());//正常资产数量
                                row.getCell(3).setText(runStatusDto.getAbnormalAssetsCount().toString());//异常资产数量
                            }
                        }
                        break;
                    case 1://佛山分行办公网设备
                        deviceDtos = (List<PatrolInspectionDeviceDto>) param.get("foShanBranch");
                        handleDeviceStatusData(deviceDtos,row,runStatusMap.get("佛山分行办公网"));
                        break;
                    case 2://卡中心办公网设备
                        deviceDtos = (List<PatrolInspectionDeviceDto>) param.get("cardCore");
                        handleDeviceStatusData(deviceDtos,row,runStatusMap.get("卡中心办公网"));
                        break;
                    case 3://总行办公网设备
                        deviceDtos = (List<PatrolInspectionDeviceDto>) param.get("headOfficeNetWork");
                        handleDeviceStatusData(deviceDtos,row,runStatusMap.get("总行办公网"));
                        break;
                    case 4://总行办公网核心设备
                        deviceDtos = (List<PatrolInspectionDeviceDto>) param.get("headOfficeNetWorkCore");
                        log.info("总行办公网核心设备"+deviceDtos);
                        handleDeviceStatusData(deviceDtos,row,runStatusMap.get("总行办公网核心"));
                        break;
                }
                _row++;
            }
            i++;
        }
    }

    public static void handleDeviceStatusData(List<PatrolInspectionDeviceDto> deviceDtos,XWPFTableRow row,PatrolInspectionRunStatusDto runStatusDto){
        if(CollectionUtils.isEmpty(deviceDtos))return;
        String templateName = row.getCell(0).getText();
        if(StringUtils.isBlank(templateName) && runStatusDto != null){
            //设置资产数量
            row.getCell(0).setText(runStatusDto.getAssetsTotal() != null?runStatusDto.getAssetsTotal().toString():"0");
            row.getCell(1).setText(runStatusDto.getAssetsTotal() != null?runStatusDto.getNormalAssetsCount().toString():"0");
            row.getCell(2).setText(runStatusDto.getAssetsTotal() != null?runStatusDto.getAbnormalAssetsCount().toString():"0");
            return;
        }
        for (PatrolInspectionDeviceDto deviceDto : deviceDtos) {
            String inspectionContent = deviceDto.getInspectionContent();
            if(StringUtils.isBlank(inspectionContent))continue;
            //名称相同,设置值
            if(templateName.contains(inspectionContent) || inspectionContent.contains(templateName)){
                row.getCell(2).setText(deviceDto.getInspectionResult());
                row.getCell(3).setText(deviceDto.getMessage());
            }
            if(templateName.contains("查看框试设备系统稳定性") && inspectionContent.contains("查看框试设备系统稳定性")){
                row.getCell(2).setText(deviceDto.getInspectionResult());
                row.getCell(3).setText(deviceDto.getMessage());
            }
        }
    }

    /**
     * 处理段落
     */
    public static void processParagraphs(List<XWPFParagraph> paragraphList, Map<String, Object> param, XWPFDocument doc) throws Exception {
        if (paragraphList != null && paragraphList.size() > 0) {
            for (XWPFParagraph paragraph : paragraphList) {
                List<XWPFRun> runs = paragraph.getRuns();
                for (XWPFRun run : runs) {
                    String text = run.getText(0);
                    if (text != null) {
                        boolean isSetText = false;
                        for (Map.Entry<String, Object> entry : param.entrySet()) {
                            String key = entry.getKey();
                            if (text.contains(key)) {
                                isSetText = true;
                                Object value;
                                if (entry.getValue() != null) {
                                    value = entry.getValue();
                                } else {
                                    value = "";
                                }
                                // 文本替换
                                if (value instanceof String) {
                                    // 处理答案中的回车换行
                                    if (((String) value).contains("n")) {
                                        String[] lines = ((String) value).split("n");
                                        if (lines.length > 0) {
                                            text = text.replace(key, lines[0]);
                                            for (int j = 1; j < lines.length; j++) {
                                                run.addCarriageReturn();
                                                run.setText(lines[j]);
                                            }
                                        }
                                    } else {
                                        text = text.replace(key, value.toString());
                                    }
                                }
                            }
                        }
                        if (isSetText) {
                            run.setText(text, 0);
                        }
                    }
                }
            }
        }
    }

    /**
     * 将运行状态数据转换为MAP
     * @param runStatusDtos
     */
    public static  Map<String, PatrolInspectionRunStatusDto> handleRunStatus(List<PatrolInspectionRunStatusDto> runStatusDtos){
        Map<String,PatrolInspectionRunStatusDto> map = new HashMap<>();
        if(CollectionUtils.isEmpty(runStatusDtos))return map;
        for (PatrolInspectionRunStatusDto runStatusDto : runStatusDtos) {
            map.put(runStatusDto.getName(),runStatusDto);
        }
        return map;
    }

}
