package cn.mw.monitor.assetsTemplate.service.impl;

import cn.mw.monitor.assetsTemplate.api.param.assetsTemplate.AddAssetsTemplateParam;
import cn.mw.monitor.assetsTemplate.api.param.assetsTemplate.QueryTemplateExportParam;
import cn.mw.monitor.assetsTemplate.dao.MwAseetstemplateTableDao;
import cn.mw.monitor.assetsTemplate.service.MwAssetsTemplateExportService;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.assetsTemplate.dto.MwAssetsTemplateDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mwpaas.common.model.Reply;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static cn.mw.monitor.assetsTemplate.utils.ExportExcel.createExcel;

/**
 * @author qzg
 * @date 2021/7/5
 */
@Service
@Slf4j
public class MwAssetsTemplateExportImpl implements MwAssetsTemplateExportService {
    private static final Logger logger = LoggerFactory.getLogger("MwAssetsTemplateExportImpl");
    @Resource
    private MwAseetstemplateTableDao mwAseetstemplateTableDao;
    @Resource
    private MwAssetsTemplateServiceImpl mwAssetsTemplateServiceImpl;
    // 总行数
    private static int totalRows = 0;
    // 总条数
    private static int totalCells = 0;
    // 错误信息接收器
    private static String errorMsg = "";

    @Autowired
    private ILoginCacheInfo loginCacheInfo;

    /**
     * 模板管理 导出模板
     *
     * @param qParam
     * @param response
     * @return
     */
    @Override
    public Reply templateInfoExport(QueryTemplateExportParam qParam, HttpServletResponse response) {
        try {
            Map criteria = PropertyUtils.describe(qParam);
            List<MwAssetsTemplateDTO> mwScanList = mwAseetstemplateTableDao.selectTepmplateTableList(criteria);
            Map map = new HashMap();
            Map ziduanMap = new HashMap();
            ziduanMap.put("assetsTypeName","资产类型");
            ziduanMap.put("subAssetsTypeName","子类型");
            ziduanMap.put("monitorModeName","监控方式");
            ziduanMap.put("specification","规格型号");
            ziduanMap.put("brand","厂商");
            ziduanMap.put("description","描述");
            ziduanMap.put("templateName","模板名称");
            ziduanMap.put("interfacesType","类型");
            ziduanMap.put("systemObjid","系统OID");
            map.put("ziDuan", ziduanMap);
            map.put("listData", mwScanList);
            createExcel("资产模板类型导出", map, response);
            logger.info("EngineManage_LOG[]EngineManage[]资产模板管理导出操作查询模板信息[]{}[]", mwScanList);
            return Reply.ok("导出成功！");
        } catch (Exception e) {
            log.error("fail to selectListEngineManage with qsParam={}, cause:{}", qParam, e);
            return Reply.fail(ErrorConstant.ASSETSTEMPLATECODE_280105, ErrorConstant.ASSETSTEMPLATE_MSG_280105);
        }
    }

    @Override
    public Reply templateInfoImport(MultipartFile file, HttpServletResponse response) {
        try {
            String fileName = file.getOriginalFilename();
            String insertMessage = "";
            int count = 0;
            int error = 0;
            int success = 0;

            if (null != fileName && (fileName.toLowerCase().endsWith(".xls") || fileName.toLowerCase().endsWith(".xlsx"))) {
                Map map = getExcelInfo(file);
                List<AddAssetsTemplateParam> list = (List<AddAssetsTemplateParam>) map.get("dataList");
                String errorMsg = map.get("errorMsg").toString();
                for (AddAssetsTemplateParam param : list) {
                    Reply insertReply = null;
                    try {
                        insertReply = mwAssetsTemplateServiceImpl.insert(param);
                        if (null != insertReply && insertReply.getRes() == 280102) {
                            //新增成功，但是
                            errorMsg += insertReply.getMsg() + "；";
                            success++;
                        } else {
                            //新增成功
                            success++;
                        }
                    } catch (RuntimeException e) {
                        //重复数据
                        count++;
                    }
                }
                if (list == null || list.size() == 0) {
                    return Reply.fail("数据导入失败。" + errorMsg);
                } else {
                    if (success > 0) {
                        //错误数据 ((totalRows-1) - success) > 0;
                        if (count > 0) {
                            return Reply.ok("部分数据导入成功，" + count + "条重复数据已被忽略。" + errorMsg);
                        }
                        if (success == (totalRows - 1)) {
                            return Reply.fail("数据导入成功。" + errorMsg);
                        } else {
                            return Reply.fail("部分数据导入成功。" + errorMsg);
                        }
                    } else {
                        if (count > 0) {
                            return Reply.ok("数据导入失败，" + count + "条重复数据。" + errorMsg);
                        }
                    }
                }
            } else {
                logger.error("没有传入正确的excel文件名", file);
            }
            return Reply.ok("导入成功！");
        } catch (Exception e) {
            logger.error("fail to templateInfoImport with MultipartFile={}, cause:{}", file, e);
            return Reply.fail(ErrorConstant.ASSETSTEMPLATECODE_280106, ErrorConstant.ASSETSTEMPLATE_MSG_280106);
        }
    }

    public Integer getMonitorModeIdByName(String name) {
        Integer monitorModeId = mwAseetstemplateTableDao.getMonitorModeId(name);
        return monitorModeId;
    }

    public Integer getAssetsTypeIdByName(String name, Integer type) {
        Integer assetsTypeId = mwAseetstemplateTableDao.getAssetsType(name, type);
        return assetsTypeId;
    }

    public Integer getAssetsSubTypeIdByName(String typeSubName, Integer type,String typeName) {
        Integer assetsTypeId = mwAseetstemplateTableDao.getAssetsSubType(typeSubName, type,typeName);
        return assetsTypeId;
    }

    /**
     * 读EXCEL文件，获取信息集合
     *
     * @return
     */
    public Map getExcelInfo(MultipartFile mFile) {
        String fileName = mFile.getOriginalFilename();// 获取文件名
        try {
            if (!validateExcel(fileName)) {// 验证文件名是否合格
                return null;
            }
            boolean isExcel2003 = true;// 根据文件名判断文件是2003版本还是2007版本
            if (isExcel2007(fileName)) {
                isExcel2003 = false;
            }
            return createExcelImport(mFile.getInputStream(), isExcel2003);
        } catch (Exception e) {
            logger.error("fail to getExcelInfo with MultipartFile={}, cause:{}", mFile, e);
        }
        return null;
    }

    /**
     * 根据excel里面的内容读取信息
     *
     * @param is          输入流
     * @param isExcel2003 excel是2003还是2007版本
     * @return
     * @throws IOException
     */
    public Map createExcelImport(InputStream is, boolean isExcel2003) {
        try {
            Workbook wb = null;
            if (isExcel2003) {// 当excel是2003时,创建excel2003
                wb = new HSSFWorkbook(is);
            } else {// 当excel是2007时,创建excel2007
                wb = new XSSFWorkbook(is);
            }
            return readExcelValue(wb);// 读取Excel里面客户的信息
        } catch (IOException e) {
            logger.error("fail to createExcelImport, cause:{}", e);
        }
        return null;
    }

    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("^[+]?[\\d]*$");
        ;
    }

    /**
     * 读取Excel里面客户的信息
     *
     * @param wb
     * @return
     */
    private Map readExcelValue(Workbook wb) {
        String loginName = loginCacheInfo.getLoginName();
        int num = wb.getNumberOfSheets();
        List<AddAssetsTemplateParam> dtoList = new ArrayList<AddAssetsTemplateParam>();
        Map map = new HashMap();
        Pattern pattern = Pattern.compile("^[+]?[\\d]*$");
        //从第四个sheet表开始
        errorMsg = "";
        for (int x = 0; x < num; x++) {
            Sheet sheet = wb.getSheetAt(x);
            // 得到Excel的行数
            totalRows = sheet.getPhysicalNumberOfRows();
            // 得到Excel的列数(前提是有行数)
            if (totalRows > 1 && sheet.getRow(0) != null) {
                totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
            }

            // 循环Excel行数
            for (int r = 1; r < totalRows; r++) {
                AddAssetsTemplateParam dto = new AddAssetsTemplateParam();
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                boolean isflag = true;
                for (int c = 0; c < totalCells; c++) {
                    Cell cell = row.getCell(c);
                    if (null != cell) {
                        if (c == 0) {
                            //资产子类型
                            if (Strings.isNullOrEmpty(cell.getStringCellValue())) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，资产子类型不能为空；";
                                isflag = false;
                                continue;
                            }
                            Cell cell2 = row.getCell(1);
                            Integer subAssetsTypeId;
                            if(cell2 != null){
                                subAssetsTypeId = getAssetsSubTypeIdByName(cell.getStringCellValue(),2,cell2.getStringCellValue());
                            }else{
                                subAssetsTypeId = getAssetsTypeIdByName(cell.getStringCellValue(), 2);
                            }
                            if (subAssetsTypeId == null) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，资产子类型输入信息有误，数据库中没有对应该信息；";
                                isflag = false;
                                continue;
                            }
                            dto.setSubAssetsTypeId(subAssetsTypeId);
                        } else if (c == 1) {
                            //资产类型
                            if (Strings.isNullOrEmpty(cell.getStringCellValue())) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，资产类型不能为空；";
                                isflag = false;
                                continue;
                            }
                            Integer assetsTypeId = getAssetsTypeIdByName(cell.getStringCellValue(), 1);
                            if (assetsTypeId == null) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，资产类型输入信息有误，数据库中没有对应该信息；";
                                isflag = false;
                                continue;
                            }
                            dto.setAssetsTypeId(assetsTypeId);
                        } else if (c == 2) {
                            //监控方式
                            if (Strings.isNullOrEmpty(cell.getStringCellValue())) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，监控方式不能为空；";
                                isflag = false;
                                continue;
                            }
                            Integer monitorMod = getMonitorModeIdByName(cell.getStringCellValue());
                            if (monitorMod == null) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，监控方式输入信息有误，数据库中没有对应该信息；";
                                isflag = false;
                                continue;
                            }
                            dto.setMonitorMode(monitorMod);
                        } else if (c == 3) {
                            //类型
                            int type = 0;
                            if (cell.getCellType().getCode() == 0) {
                            //if (cell.getCellType().equals(CellType.NUMERIC)) {//单元格为数字
                                double douVal = cell.getNumericCellValue();
                                Integer integer = (int) douVal;
                                //判断是否是数字。
                                boolean isNum = pattern.matcher(integer + "").matches();
                                if (!isNum) {
                                    errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，类型输入信息有误，只能是0和正整数；";
                                    isflag = false;
                                    continue;
                                } else {
                                    dto.setInterfacesType(integer);
                                }
                            }
                            if (cell.getCellType().getCode() == 1) {
                            //if (cell.getCellType().equals(CellType.STRING)) {//单元格为字符串
                                if (Strings.isNullOrEmpty(cell.getStringCellValue())) {
                                    type = 0;
                                    dto.setInterfacesType(type);
                                } else {
                                    //判断是否是数字。
                                    boolean isNum = pattern.matcher(cell.getStringCellValue()).matches();
                                    if (!isNum) {
                                        errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，类型输入信息有误，只能是0和正整数；";
                                        isflag = false;
                                        continue;
                                    } else {
                                        dto.setInterfacesType(Integer.valueOf(cell.getStringCellValue()));
                                    }
                                }
                            }

                        } else if (c == 4) {
                            //模板名称
                            if (Strings.isNullOrEmpty(cell.getStringCellValue())) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，模板名称不能为空；";
                                isflag = false;
                                continue;
                            }
                            dto.setTemplateName(cell.getStringCellValue());
                        } else if (c == 5) {
                            if ("SNMP".equals(row.getCell(2).getStringCellValue())) {
                                //系统OID
                                if (Strings.isNullOrEmpty(cell.getStringCellValue())) {
                                    errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，SNMP监控方式下，系统OID不能为空；";
                                    isflag = false;
                                    continue;
                                }
                                dto.setSystemObjid(cell.getStringCellValue());
                            }
                        } else if (c == 6) {
                            //规格型号
                            if (Strings.isNullOrEmpty(cell.getStringCellValue())) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，规格型号不能为空；";
                                isflag = false;
                                continue;
                            }
                            dto.setSpecification(cell.getStringCellValue());
                        } else if (c == 7) {
                            //描述
                            dto.setDescription(cell.getStringCellValue());
                        } else if (c == 8) {
                            //厂商
                            if (Strings.isNullOrEmpty(cell.getStringCellValue())) {
                                errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，厂商不能为空；";
                                isflag = false;
                                continue;
                            }
                            dto.setBrand(cell.getStringCellValue());
                        }
//                        else if (c == 9) {
//                            // 发现日期
//                            if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
//                                String time = String.valueOf(cell.getStringCellValue());
////                                int i = time.indexOf(".");
////                                int y = time.indexOf("-");
////                                try {
////                                    if (i > 0) {
////                                        defect.setFindDate(DateUtil.convertStringToDate("yyyy.MM.dd", time));
////                                    }
////                                    if (y > 0) {
////                                        defect.setFindDate(DateUtil.convertStringToDate("yyyy-MM-dd", time));
////                                    }
////                                } catch (ParseException e) {
////                                }
//                            } else {
//                                cell.getDateCellValue();
//                            }
//                        }
                    } else {
                        errorMsg += "第" + (r + 1) + "行第" + (c + 1) + "列，输入不能为空；";
                        isflag = false;
                        continue;
                    }
                }
                if (isflag) {//导入数据有错误，就不添加
                    dto.setCreator(loginName);
                    dto.setModifier(loginName);
                    dtoList.add(dto);
                }
            }
            map.put("dataList", dtoList);
            map.put("errorMsg", errorMsg);
        }
        return map;
    }

    /**
     * 验证EXCEL文件
     *
     * @param filePath
     * @return
     */
    public static boolean validateExcel(String filePath) {
        if (filePath == null || !(isExcel2003(filePath) || isExcel2007(filePath))) {
            errorMsg = "文件名不是excel格式";
            return false;
        }
        return true;
    }

    // @描述：是否是2003的excel，返回true是2003
    public static boolean isExcel2003(String filePath) {
        return filePath.matches("^.+\\.(?i)(xls)$");
    }

    // @描述：是否是2007的excel，返回true是2007
    public static boolean isExcel2007(String filePath) {
        return filePath.matches("^.+\\.(?i)(xlsx)$");
    }

}
