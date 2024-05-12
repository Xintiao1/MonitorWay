package cn.mw.module.security.service;

import cn.mw.module.security.dto.DataSourceConfigureDTO;
import cn.mwpaas.common.model.Reply;

/**
 * @author qzg
 * @date 2021/12/8
 */
public interface DataSourceConfigureService {
    Reply creatDataSourceInfo(DataSourceConfigureDTO param);

    Reply getDataSourceInfo(DataSourceConfigureDTO param);

    Reply editorDataSourceInfo(DataSourceConfigureDTO param);

    Reply deleteDataSourceInfo(DataSourceConfigureDTO param);

    Reply dataSourceDropDown(String type);

    Reply dropDownByInsertSelect();

    Reply initConfig(DataSourceConfigureDTO param);

    Reply shutDownConfig(DataSourceConfigureDTO param);

    Reply fuzzSearchAllFiledData();

    Reply getTopicField();
}
