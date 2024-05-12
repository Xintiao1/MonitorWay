package cn.huaxing.service.impl;

import cn.huaxing.dao.TestDao;
import cn.huaxing.service.TestDemo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class TestDemoImpl implements TestDemo {

    @Resource
    private TestDao testDao;

    @Override
    public void test() {
        int count = testDao.select();
        log.info("{} count :{}" ,this.getClass().getCanonicalName() ,count);
    }
}
