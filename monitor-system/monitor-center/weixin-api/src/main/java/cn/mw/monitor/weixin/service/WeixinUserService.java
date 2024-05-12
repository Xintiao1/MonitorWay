package cn.mw.monitor.weixin.service;


import cn.mw.monitor.weixin.dao.MwWeixinUserDao;
import cn.mw.monitor.weixin.entity.MwWeixinUserTable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class WeixinUserService {

    @Resource
    private MwWeixinUserDao mwWeixinUserDao;

    public int insert(MwWeixinUserTable userEntity) {
        return mwWeixinUserDao.insert(userEntity);
    }

    public int delete(String openid) {
        return mwWeixinUserDao.delete(openid);
    }

    public int updateById(MwWeixinUserTable userEntity) {
        return mwWeixinUserDao.updateById(userEntity);
    }

    public MwWeixinUserTable selectOne(String openid) {
        return mwWeixinUserDao.selectOne(openid);
    }

    public List<MwWeixinUserTable> selectList() {
        return mwWeixinUserDao.selectList();
    }

    public MwWeixinUserTable selectOneByMwLoginName(String loginName) {
        return mwWeixinUserDao.selectOneByMwLoginName(loginName);
    }

}
