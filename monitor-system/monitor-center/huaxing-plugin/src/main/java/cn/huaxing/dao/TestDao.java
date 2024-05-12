package cn.huaxing.dao;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestDao {
    Integer select();
}
