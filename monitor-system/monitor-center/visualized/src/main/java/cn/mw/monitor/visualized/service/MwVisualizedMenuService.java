package cn.mw.monitor.visualized.service;

import cn.mw.monitor.visualized.dto.MwVisualizedAssetsDto;
import cn.mw.monitor.visualized.dto.MwVisualizedChartDto;
import cn.mw.monitor.visualized.dto.MwVisualizedIndexDto;
import cn.mwpaas.common.model.Reply;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @ClassName MwVisualizedMenuService
 * @Description 可视化分区类型接口
 * @Author gengjb
 * @Date 2022/4/14 9:58
 * @Version 1.0
 **/
public interface MwVisualizedMenuService {

    /**
     * 查询所有分区与类型
     * @return
     */
    Reply selectVisualizedChart();


    /**
     * 查询可视化维度数据
     * @return
     */
    Reply selectVisualizedDimension();

    /**
     * 查询可视化指标数据
     * @return
     */
    Reply selectVisualizedIndex(MwVisualizedIndexDto dto);

    /**
     * 可视化增加图形图片上传
     * @param multipartFile 图片信息
     * @return
     */
    Reply addVisualizedImageUpload(MultipartFile multipartFile,Integer id);


    /**
     * 添加分区或者类型
     * @return
     */
    Reply addVisualizedChart(MwVisualizedChartDto visualizedChartDto);

//    /**
//     * 修改分区或者类型
//     * @param visualizedChartDto
//     * @return
//     */
//    Reply updateVisualizedChart(MwVisualizedChartDto visualizedChartDto);

    /**
     * 可视化图文区上传内容
     * @param file
     * @return
     */
    Reply visualizedImageAndTextAreaUpload(MultipartFile file);
}
