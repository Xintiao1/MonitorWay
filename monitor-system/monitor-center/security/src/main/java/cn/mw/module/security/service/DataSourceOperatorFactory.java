package cn.mw.module.security.service;

import cn.mw.module.security.common.DataSourceEnum;
import cn.mw.module.security.service.impl.OperateClickHouseDataSource;
import cn.mw.module.security.service.impl.OperateEsDataSource;
import cn.mw.module.security.service.impl.OperateKafkaDataSource;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceOperatorFactory {

    static Map<Integer, OperateDataSource> dataSourceOperationMap = new HashMap<>();
    static {
        dataSourceOperationMap.put(DataSourceEnum.Elasticsearch.getValue(),new OperateEsDataSource());
        dataSourceOperationMap.put(DataSourceEnum.Kafka.getValue(),new OperateKafkaDataSource());
        dataSourceOperationMap.put(DataSourceEnum.ClickHouse.getValue(),new OperateClickHouseDataSource());
    }

    public static OperateDataSource dataSourceOperate(Integer dataSourceType){
        return dataSourceOperationMap.get(dataSourceType);
    }
}
