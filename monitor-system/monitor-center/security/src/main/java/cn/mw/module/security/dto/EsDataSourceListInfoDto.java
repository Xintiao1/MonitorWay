package cn.mw.module.security.dto;

import lombok.Data;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qzg
 * @date 2021/12/21 16:55
 */
@Data
public class EsDataSourceListInfoDto {
    public  RestHighLevelClient client;
    //查询索引
    public  String queryEsIndex;
    //Id
    public  String id;

    //es名称
    public  String dataSourceName;

    public String dataSourceType;

}
