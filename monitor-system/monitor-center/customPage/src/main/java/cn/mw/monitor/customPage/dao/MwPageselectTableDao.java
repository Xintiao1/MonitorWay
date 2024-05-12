package cn.mw.monitor.customPage.dao;

import cn.mw.monitor.customPage.model.MwMultiPageselectTable;
import cn.mw.monitor.customPage.model.MwPageselectTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MwPageselectTableDao {

    List<MwPageselectTable> selectByPageId(@Param("pageId")Integer pageId);

    List<MwMultiPageselectTable> selectByMultiPageId(List pageIds);

}