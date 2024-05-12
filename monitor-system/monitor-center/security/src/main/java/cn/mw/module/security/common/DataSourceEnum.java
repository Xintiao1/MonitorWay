package cn.mw.module.security.common;

public enum DataSourceEnum {
    /**
     * es数据源
     */
    Elasticsearch(1),
    /**
     * kafka数据源
     */
    Kafka(2),
    ClickHouse(3);

    private final int value;

    DataSourceEnum(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }
}
