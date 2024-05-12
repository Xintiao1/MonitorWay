package cn.mw.monitor.dropDown.service.impl;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.dropDown.dao.MwDropdownTableDao;
import cn.mw.monitor.dropDown.dto.MwDropdownDTO;
import cn.mw.monitor.dropDown.dto.SelectCharDropDto;
import cn.mw.monitor.dropDown.dto.SelectNumDropDto;
import cn.mw.monitor.dropDown.service.MwDropDownService;
import cn.mw.monitor.service.common.ServiceException;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class MwDropDownServiceImpl implements MwDropDownService {

    @Resource
    private MwDropdownTableDao mwDropdownTableDao;

    /**
     * 根据下拉框Code查询下拉框信息
     */
    @Override
    public Reply selectDropdownByCode(String dropCode) {
        try {
            List<MwDropdownDTO> valueDto = mwDropdownTableDao.selectByCode(dropCode);
            //去除重复值
            List<MwDropdownDTO> dropdownDTOS = new ArrayList<>();
            Map<String,MwDropdownDTO> dropMap = new HashMap<>();
            if(!CollectionUtils.isEmpty(valueDto)){
                for (MwDropdownDTO dropdownDTO : valueDto) {
                    String dropValue = dropdownDTO.getDropValue();
                    if(dropMap.get(dropValue) != null){
                        continue;
                    }else{
                        dropdownDTOS.add(dropdownDTO);
                        dropMap.put(dropValue,dropdownDTO);
                    }
                }
            }
            log.info("DROPDOWN_LOG[]DROPDOWN[]下拉框管理[]根据名称取下拉框信息[]{}", dropCode);
            return Reply.ok(dropdownDTOS);
        } catch (Exception e) {
            log.error("fail to selectDropdownByCode with dropCode={}, cause:{}", dropCode, e.getMessage());
            throw new ServiceException(Reply.fail(ErrorConstant.DROPDOWNCODE_230101, ErrorConstant.DROPDOWN_MSG_230101));
        }
    }

    @Override
    public Reply pageSelectNumUrl(String type) {
        List<SelectNumDropDto> valueDto = mwDropdownTableDao.pageSelectNumUrl(type);
        return Reply.ok(valueDto);
    }

    @Override
    public Reply pageSelectCharUrl(String type) {
        List<SelectCharDropDto> valueDto = mwDropdownTableDao.pageSelectCharUrl(type);
        return Reply.ok(valueDto);
    }

    @Override
    public Reply selectDropdown(String fieldName, String tableName) {

        try {
            List<Object> list = mwDropdownTableDao.selectDropDown(fieldName, tableName);
            list = list.stream().filter(x-> StringUtils.isNotEmpty(x.toString())).collect(Collectors.toList());
            log.info("DROPDOWN_LOG[]selectDropdown[]下拉框管理[]根据数据库表字段、表名取下拉框信息[]{}", fieldName + "---" + tableName);
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to selectDropdown with param={}, cause:{}", fieldName + "---" + tableName, e);
            throw new ServiceException(Reply.fail(ErrorConstant.DROPDOWNCODE_230101, ErrorConstant.DROPDOWN_MSG_230101));
        }
    }


}
