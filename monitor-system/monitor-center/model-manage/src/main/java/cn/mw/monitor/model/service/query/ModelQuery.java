package cn.mw.monitor.model.service.query;

import org.elasticsearch.index.query.QueryBuilder;

public interface ModelQuery {
    QueryBuilder genQuery();
}
