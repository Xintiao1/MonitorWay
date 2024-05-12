package cn.mw.monitor.model.service.impl;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class RancherAPI {
    private final String RANCHER_API_BASE_URL = "https://10.180.5.184/v3/";
    private final String RANCHER_ACCESS_KEY = "your-rancher-access-key";
    private final String RANCHER_SECRET_KEY = "your-rancher-secret-key";

    private OkHttpClient client;
    private String authHeader;
    public RancherAPI() {
        client = new OkHttpClient();
        authHeader = Credentials.basic(RANCHER_ACCESS_KEY, RANCHER_SECRET_KEY);
    }
    public void getClusterProjectNamespace() throws IOException {
        Request request = new Request.Builder()
                .url(RANCHER_API_BASE_URL + "clusters")
                .addHeader("Authorization", authHeader)
                .build();
        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();

// 解析集群数据
// clusters = ...
//        for (Cluster cluster : clusters) {
//            String clusterId = cluster.getId();
//            Request requestProjects = new Request.Builder()
//                    .url(RANCHER_API_BASE_URL + "projects?clusterId=" + clusterId)
//                    .addHeader("Authorization", authHeader)
//                    .build();
//            Response responseProjects = client.newCall(requestProjects).execute();
//            String responseBodyProjects = responseProjects.body().string();

// 解析项目数据
// projects = ...
//            for (Project project : projects) {
//                String projectId = project.getId();
//                Request requestNamespaces = new Request.Builder()
//                        .url(RANCHER_API_BASE_URL + "namespaces?projectId=" + projectId)
//                        .addHeader("Authorization", authHeader)
//                        .build();
//                Response responseNamespaces = client.newCall(requestNamespaces).execute();
//                String responseBodyNamespaces = responseNamespaces.body().string();
//
//// 解析命名空间数据
//// namespaces = ...
//            }
//        }
    }
}
