package cn.mw.module.security.service.impl;

import cn.mw.module.security.common.AlertFieldEnum;
import cn.mw.module.security.common.DataSourceEnum;
import cn.mw.module.security.dto.*;
import cn.mw.module.security.service.DataSourceConfigureService;
import cn.mw.module.security.service.DataSourceOperatorFactory;
import cn.mw.module.security.service.OperateDataSource;
import cn.mw.module.security.util.ElasticsearchConfig;
import cn.mw.monitor.security.dao.DataSourceConfigureDao;
import cn.mw.monitor.util.IDModelType;
import cn.mw.monitor.util.ModuleIDManager;
import cn.mw.monitor.util.RSAUtils;
import cn.mwpaas.common.model.Reply;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.common.Node;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author qzg
 * @date 2021/12/8
 */
@Service
@Slf4j
public class DataSourceConfigureServiceImpl implements DataSourceConfigureService {
    @Resource
    private DataSourceConfigureDao dataSourceConfigureDao;

    @Autowired
    DataSourceOperatorFactory dataSourceOperatorFactory;

    //数据源操作对象
    OperateDataSource operateDataSource;

    @Override
    @Transactional
    public Reply creatDataSourceInfo(DataSourceConfigureDTO param) {
        //数据源id处理
        ModuleIDManager moduleIDManager = new ModuleIDManager();
        param.setId(String.valueOf(moduleIDManager.getID(IDModelType.DataSource)));
        //数据源操作对象
        operateDataSource = dataSourceOperatorFactory.dataSourceOperate(param.getDataSourceType());
        //创建数据源
        int i = operateDataSource.creatDataSource(dataSourceConfigureDao,param);
        if(i > 0){
            return Reply.ok();
        }
        return Reply.fail("数据源创建失败");
    }

    @Override
    public Reply getDataSourceInfo(DataSourceConfigureDTO param) {
        List<DataSourceConfigureDTO> resultList = new ArrayList<>();
        PageHelper.startPage(param.getPageNumber(), param.getPageSize());

        //查询数据列
        System.out.println("入参："+param);
        List<DataSourceConfigureDTO> list = dataSourceConfigureDao.getDataSourceInfo(param);
        log.info("数据源个数"+list.size());
        //数据源列表展示
        if("".equals(param.getDataSourceType()) || param.getDataSourceType() == null){
            for(DataSourceConfigureDTO datasource:list){
                //创建数据源操作对象
                log.info("开始处理数据源"+datasource.getIp());
                operateDataSource = dataSourceOperatorFactory.dataSourceOperate(datasource.getDataSourceType());
                //数据源详细信息处理
                DataSourceConfigureDTO data = operateDataSource.getDataSourceInfo(dataSourceConfigureDao,datasource);
                log.info("将{}数据源添加到展示列表",datasource.getIp());
                resultList.add(data);
            }
        }else {
            //数据源详细信息展示
            DataSourceConfigureDTO dataSource = list.get(0);
            operateDataSource = dataSourceOperatorFactory.dataSourceOperate(dataSource.getDataSourceType());
            DataSourceConfigureDTO data = operateDataSource.getDataSourceInfo(dataSourceConfigureDao,dataSource);
            resultList.add(data);
        }

        PageInfo pageInfo = new PageInfo<>(resultList);
        pageInfo.setList(resultList);
        return Reply.ok(pageInfo);
    }

    @Override
    @Transactional
    public Reply editorDataSourceInfo(DataSourceConfigureDTO param) {
        //数据源操作对象
        operateDataSource = dataSourceOperatorFactory.dataSourceOperate(param.getDataSourceType());
        //创建数据源
        int i = operateDataSource.editDataSourceInfo(dataSourceConfigureDao,param);
        if(i > 0){
            return Reply.ok();
        }
        return Reply.fail("数据源编辑失败");
    }

    @Override
    @Transactional
    public Reply deleteDataSourceInfo(DataSourceConfigureDTO param) {
        if (param.getIds() != null && param.getIds().size() > 0) {
            dataSourceConfigureDao.deleteDataSourceInfo(param.getIds());
        }
        return Reply.ok();
    }

    @Override
    public Reply dataSourceDropDown(String type) {
        List<Map> list = new ArrayList<>();
        //数据源类型下拉数据
        if ("dataSource".equals(type)) {
            list = dataSourceConfigureDao.dataSourceDropDownByDataSourceType();
        }
        //连接类型下拉数据
        if ("connection".equals(type)) {
            list = dataSourceConfigureDao.dataSourceDropDownByConnectionType();
        }
        return Reply.ok(list);
    }

    /**
     * 新增修改页面下拉数据
     *
     * @return
     */
    @Override
    public Reply dropDownByInsertSelect() {
        Map map = new HashMap();
        List<Map> listDataSourceType = dataSourceConfigureDao.dataSourceDropDownByDataSourceType();
        List<Map> listConnectionType = dataSourceConfigureDao.dataSourceDropDownByConnectionType();
        List<Map> listAuthType = dataSourceConfigureDao.dataSourceDropDownByAuthType();
        if (listDataSourceType != null && listConnectionType.size() > 0) {
            map.put("dataSourceType", listDataSourceType);
        } else {
            map.put("dataSourceType", new ArrayList<>());
        }
        if (listConnectionType != null && listConnectionType.size() > 0) {
            map.put("connectionType", listConnectionType);
        } else {
            map.put("connectionType", new ArrayList<>());
        }
        if (listAuthType != null && listAuthType.size() > 0) {
            map.put("authType", listAuthType);
        } else {
            map.put("authType", new ArrayList<>());
        }
        return Reply.ok(map);
    }

    @Override
    public Reply initConfig(DataSourceConfigureDTO param) {
        int sum = 0;
        if (param.getIdsAndTypes() != null && param.getIdsAndTypes().size() > 0) {
            for (String id:param.getIdsAndTypes().keySet()) {
                param.setId(id);
                param.setState(1);
                //数据源操作对象
                operateDataSource = dataSourceOperatorFactory.dataSourceOperate(param.getIdsAndTypes().get(id));
                //关闭数据源启动状态
                int i = operateDataSource.initDataSource(dataSourceConfigureDao,param);
                sum = sum+i;
            }
        }
        if(sum == param.getIdsAndTypes().size()){
            return Reply.ok();
        }
        return Reply.fail(500, "数据源初始化应用失败");
    }

    /**
     * 禁用 数据源配置
     *
     * @param param
     * @return
     */
    @Override
    public Reply shutDownConfig(DataSourceConfigureDTO param) {
        int sum = 0;
        if (param.getIdsAndTypes() != null && param.getIdsAndTypes().size() > 0) {
            for (String id:param.getIdsAndTypes().keySet()) {
                param.setId(id);
                param.setState(0);
                //数据源操作对象
                operateDataSource = dataSourceOperatorFactory.dataSourceOperate(param.getIdsAndTypes().get(id));
                //关闭数据源启动状态
                int i = operateDataSource.shutDownDataSource(dataSourceConfigureDao,param);
                sum = sum+i;
            }
        }
        if(sum == param.getIdsAndTypes().size()){
            return Reply.ok("禁用成功");
        }
        return Reply.fail("禁用数据源配置失败");
    }

    /**
     * 模糊搜索所有字段联想
     *
     * @return
     */
    @Override
    public Reply fuzzSearchAllFiledData() {
        //根据值模糊查询数据
        List<Map<String, Object>> fuzzSeachAllFileds = dataSourceConfigureDao.fuzzSearchAllFiled();
        Set<String> fuzzSeachData = new HashSet<>();
        if (!cn.mwpaas.common.utils.CollectionUtils.isEmpty(fuzzSeachAllFileds)) {
            for (Map<String, Object> fuzzSeachAllFiled : fuzzSeachAllFileds) {
                fuzzSeachAllFiled.forEach((k, v) -> {
                    String value = "";
                    if (v != null) {
                        value = String.valueOf(v);
                    }
                    fuzzSeachData.add(value);
                });
            }
        }
        fuzzSeachData.stream().sorted(Comparator.reverseOrder());
        Map<String, Set<String>> fuzzyQuery = new HashMap<>();
        fuzzyQuery.put("fuzzyQuery", fuzzSeachData);
        return Reply.ok(fuzzyQuery);
    }

    /**
     * kafka新建topic字段展示接口
     * @param
     * @return
     */
    @Override
    public Reply getTopicField() {
        //获取字段信息
        List<TopicFieldDTO> fieldList = dataSourceConfigureDao.getTopicField();
        List<FieldModelDTO> modelList = new ArrayList<>();
        //处理字段信息
        for(TopicFieldDTO topicField:fieldList){
            if(AlertFieldEnum.事件时间.getValue().equals(topicField.getFieldCode())){
                modelList = dataSourceConfigureDao.getTimeModel();
            }
            if(AlertFieldEnum.事件来源.getValue().equals(topicField.getFieldCode())){
                modelList = dataSourceConfigureDao.getEventSources();
            }
            topicField.setFieldModel(modelList);
        }
        return Reply.ok(fieldList);
    }
}
