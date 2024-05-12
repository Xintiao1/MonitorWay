package cn.mw.module.security.dto;

import lombok.Data;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * @author qzg
 * @date 2021/12/21 16:55
 */
public enum MwDataSourceType {
    ELASTICSEARCH("Elasticsearch"),
    KAFKA("Kafka");
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    MwDataSourceType(String type) {
        this.type = type;
    }
}
