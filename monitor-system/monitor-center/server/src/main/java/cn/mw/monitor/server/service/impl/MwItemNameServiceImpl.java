package cn.mw.monitor.server.service.impl;

import cn.mw.monitor.server.dao.ItemNameDao;
import cn.mw.monitor.server.param.UpdateItemNameParam;
import cn.mw.monitor.server.serverdto.ItemNameDto;
import cn.mw.monitor.server.service.MwItemNameService;
import cn.mwpaas.common.model.Reply;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author syt
 * @Date 2021/8/24 15:22
 * @Version 1.0
 */
@Service
@Slf4j
@Transactional
public class MwItemNameServiceImpl implements MwItemNameService {
    @Resource
    private ItemNameDao itemNameDao;


    @Override
    public Reply updateItemChName(UpdateItemNameParam param) {
        String name = param.getItemName();
        String ifName = "";
        if (name.indexOf("[") != -1) {
            ifName = name.substring(name.indexOf("["), name.indexOf("]") + 1);
//                中括号在前面
            if (name.indexOf("[") == 0) {
                name = name.substring(name.indexOf("]") + 1);
            }
//                中括号在后面
            if (name.indexOf("]") == (name.length() - 1)) {
                name = name.substring(0, name.indexOf("["));
            }
        }
        int i = itemNameDao.updateItemChName(name, param.getDescr());
        if (i == 0) {
            ItemNameDto nameDto = new ItemNameDto();
            nameDto.setDescr(param.getDescr());
            nameDto.setItemName(name);
            nameDto.setRequestName(name);
            itemNameDao.insertItemChName(nameDto);
        }
        return Reply.ok("更新成功");
    }
}
