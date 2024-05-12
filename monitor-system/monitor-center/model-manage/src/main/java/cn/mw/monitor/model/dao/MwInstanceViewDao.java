package cn.mw.monitor.model.dao;

import cn.mw.monitor.model.dto.MwInstanceViewDTO;

import java.util.List;

public interface MwInstanceViewDao {
    int insert(MwInstanceViewDTO view); //插入一条记录

    int deleteById(long id); //根据id删除记录

    int update(MwInstanceViewDTO view); //更新记录

    MwInstanceViewDTO findById(long id); //根据id查询记录

    List<MwInstanceViewDTO> findAll(MwInstanceViewDTO mwInstanceViewDTO); //查询所有记录
}
