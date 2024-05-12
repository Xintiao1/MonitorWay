package cn.mw.monitor.prometheus.constants;

public interface QueryConstants {

    class QueryUrl {
        public static final String QUERY = "query";
        public static final String QUERY_RANGE = "query_range";
    }

    class Query {
        public static final String QUERY_NAMESPACE = "kube_namespace_labels";

        public static final String QUERY_POD = "kube_pod_info";

        public static final String QUERY_POD_CONTAINER = "kube_pod_container_info";
    }

    class QueryParamName {
        public static final String NODE = "node";
        public static final String HOST_IP = "hostIp";
        public static final String NAMESPACE = "namespace";
    }
}
