package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.model.dto.rancher.MwModelRancherProjectUserDTO;
import cn.mw.monitor.model.dto.rancher.MwModelRancherUserDTO;
import cn.mw.monitor.model.dto.rancher.RancherCapacity;
import cn.mw.monitor.model.param.rancher.RancherInstanceParam;
import cn.mw.monitor.model.util.ModelOKHttpUtils;
import cn.mw.monitor.service.model.dto.rancher.MwModelRancherDataInfoDTO;
import cn.mw.monitor.service.scan.model.ProxyInfo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.mw.monitor.model.param.MatchModelTypeEnum.CLUSTER;

/**
 * @author qzg
 */
@Slf4j
@Service
public class RancherClientUtils {
    @Autowired
    private ProxySearch proxySearch;

    private static final String https = "https://";

    /**
     * 集群地址
     */
    private static final String rancherClusters = "/v3/clusters/";

    /**
     * 集群地址
     */
    private static final String rancherClusterId = "/v3/clusters/#clusterId/";

    /**
     * 项目地址
     */
    private static final String rancherProjects = "/v3/projects/";
    /**
     * 集群下项目
     */
    private static final String rancherProjectsByCluster = "/v3/cluster/#clusterId/projects";

    /**
     * 工作负载地址前缀
     */
    private static final String rancherWorkloads = "/workloads/";

    /**
     * 工作负载地址前缀
     */
    private static final String rancherNameSpace = "/v3/cluster/#clusterId/namespaces/";

    /**
     * 节点地址前缀
     */
    private static final String rancherNodes = "/v3/nodes/";
    /**
     * 集群下节点
     */
    private static final String rancherNodesByCluster = "/v3/cluster/#clusterId/nodes";

    private static final String rancherProjectUesr = "/v3/projectRoleTemplateBindings";

    private static final String rancherUsers = "/v3/users";


    /*
     * @Description 获取项目和用户关系
     * @Author qzg
     * @Date 2023/04/10 14:14
     */
    public List<MwModelRancherProjectUserDTO> getRancherProjectUser(RancherInstanceParam param) throws Exception {
        List<MwModelRancherProjectUserDTO> rancherProjectUserList = new ArrayList<>();
        try {
            JSONObject json = readJsonFromUrl(https + param.getIp() + rancherProjectUesr, param);
            if (json != null) {
                JSONArray dataArr = json.getJSONArray("data");
                if (dataArr != null) {
                    for (int i = 0; i < dataArr.size(); i++) {
                        JSONObject dataObj = dataArr.getJSONObject(i);
                        MwModelRancherProjectUserDTO rancherUserDTO = JSONObject.parseObject(JSONObject.toJSONString(dataObj), MwModelRancherProjectUserDTO.class);
                        rancherProjectUserList.add(rancherUserDTO);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("getRancherProjectUesr to fail:" + ex.getMessage());
            throw ex;
        }
        return rancherProjectUserList;
    }

    /*
     * @Description 获取rancher所有的user信息
     * @Author qzg
     * @Date 2023/04/10 14:14
     */
    public List<MwModelRancherUserDTO> getRancherUsers(RancherInstanceParam param) throws Exception {
        List<MwModelRancherUserDTO> rancherUserList = new ArrayList<>();
        try {
            JSONObject json = readJsonFromUrl(https + param.getIp() + rancherUsers, param);
            if (json != null) {
                JSONArray dataArr = json.getJSONArray("data");
                if (dataArr != null) {
                    for (int i = 0; i < dataArr.size(); i++) {
                        JSONObject dataObj = dataArr.getJSONObject(i);
                        MwModelRancherUserDTO rancherUserDTO = JSONObject.parseObject(JSONObject.toJSONString(dataObj), MwModelRancherUserDTO.class);
                        if (Strings.isNullOrEmpty(rancherUserDTO.getUsername()) && dataObj.get("name") != null) {
                            rancherUserDTO.setUsername(dataObj.get("name").toString());
                        }
                        rancherUserList.add(rancherUserDTO);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("getRancherUsers to fail:" + ex.getMessage());
            throw ex;
        }
        return rancherUserList;
    }


    /*
     * @Description 获取指定集群下的指定命名空间
     * @Author qzg
     * @Date 2023/04/10 14:14
     */
    public List<MwModelRancherDataInfoDTO> getNameSpacesById(String clusterId, RancherInstanceParam param) throws Exception {
        List<MwModelRancherDataInfoDTO> rancherNameSpaceList = new ArrayList<>();
        String rancherNameSpaceStr = rancherNameSpace.replaceAll("#clusterId", clusterId);
        try {
            JSONObject json = readJsonFromUrl(https + param.getIp() + rancherNameSpaceStr + param.getId(), param);
            if (json != null) {
                MwModelRancherDataInfoDTO rancherNameSpace = nameSpaceExtractFrom(json);
                if (Strings.isNullOrEmpty(rancherNameSpace.getPId())) {
                    rancherNameSpace.setPId(clusterId);
                }
                rancherNameSpace.setClusterId(clusterId);
                rancherNameSpaceList.add(rancherNameSpace);
            }
        } catch (Exception ex) {
            log.error("getNameSpacesByCluster to fail:" + ex.getMessage());
            throw ex;
        }
        return rancherNameSpaceList;
    }


    /*
     * @Description 获取集群下的命名空间
     * @Author qzg
     * @Date 2023/04/10 14:14
     */
    public List<MwModelRancherDataInfoDTO> getNameSpacesByCluster(String clusterId, RancherInstanceParam param) throws Exception {
        List<MwModelRancherDataInfoDTO> rancherNameSpaceList = new ArrayList<>();
        String rancherNameSpaceStr = rancherNameSpace.replaceAll("#clusterId", clusterId);
        try {
            JSONObject json = readJsonFromUrl(https + param.getIp() + rancherNameSpaceStr, param);
            if (json != null) {
                JSONArray dataArr = json.getJSONArray("data");
                if (dataArr != null) {
                    for (int i = 0; i < dataArr.size(); i++) {
                        JSONObject dataObj = dataArr.getJSONObject(i);
                        MwModelRancherDataInfoDTO rancherNameSpace = nameSpaceExtractFrom(dataObj);
                        if (Strings.isNullOrEmpty(rancherNameSpace.getPId())) {
                            rancherNameSpace.setPId(clusterId);
                        }
                        rancherNameSpace.setClusterId(clusterId);
                        rancherNameSpaceList.add(rancherNameSpace);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("getNameSpacesByCluster to fail:" + ex.getMessage());
            throw ex;
        }
        return rancherNameSpaceList;
    }

    private MwModelRancherDataInfoDTO nameSpaceExtractFrom(JSONObject dataObj) throws Exception {
        MwModelRancherDataInfoDTO ranchernameSpace = JSONObject.parseObject(JSONObject.toJSONString(dataObj), MwModelRancherDataInfoDTO.class);
        if (dataObj.get("projectId") != null) {
            ranchernameSpace.setPId(dataObj.get("projectId").toString());
        }
        return ranchernameSpace;
    }

    /*
     * @Description 根据NodeId获取node信息
     * @Author qzg
     * @Date 2023/04/10 14:14
     */
    public List<MwModelRancherDataInfoDTO> getNodeInfoById(RancherInstanceParam param) throws Exception {
        List<MwModelRancherDataInfoDTO> nodesList = new ArrayList<>();
        try {
            JSONObject json = readJsonFromUrl(https + param.getIp() + rancherNodes + param.getId(), param);
            if (json != null) {
                MwModelRancherDataInfoDTO rancherNodes = nodesExtractFrom(json);
                nodesList.add(rancherNodes);
            }
        } catch (Exception ex) {
            log.error("getNodeInfoById to fail:" + ex.getMessage());
        }
        return nodesList;
    }

    /*
     * @Description 获取所有Nodes
     * @Author qzg
     * @Date 2023/04/10 14:14
     */
    public List<MwModelRancherDataInfoDTO> getNodes(RancherInstanceParam param) throws Exception {
        List<MwModelRancherDataInfoDTO> nodesList = new ArrayList<>();
        try {
            JSONObject json = null;
            if (CLUSTER.getType().equals(param.getType())) {
                //获取指定cluster下的nodes
                String rancherNodesByClusterstr = rancherNodesByCluster.replaceAll("#clusterId", param.getId());
                json = readJsonFromUrl(https + param.getIp() + rancherNodesByClusterstr, param);
            } else {
                json = readJsonFromUrl(https + param.getIp() + rancherNodes, param);
            }
            if (json != null) {
                JSONArray dataArr = json.getJSONArray("data");
                if (dataArr != null) {
                    for (int i = 0; i < dataArr.size(); i++) {
                        JSONObject dataObj = dataArr.getJSONObject(i);
                        MwModelRancherDataInfoDTO rancherNodes = nodesExtractFrom(dataObj);
                        nodesList.add(rancherNodes);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("getNodes to fail:", ex);
        }
        return nodesList;
    }

    private MwModelRancherDataInfoDTO nodesExtractFrom(JSONObject dataObj) throws Exception {
        MwModelRancherDataInfoDTO rancherNodes = JSONObject.parseObject(JSONObject.toJSONString(dataObj), MwModelRancherDataInfoDTO.class);
        if (dataObj.get("clusterId") != null) {
            rancherNodes.setPId(dataObj.get("clusterId").toString());
        }
        if (dataObj.get("requestedHostname") != null) {
            rancherNodes.setName(dataObj.get("requestedHostname").toString());
        }
        if (dataObj.get("info") != null) {
            Map infoMap = (Map) dataObj.get("info");
            if (infoMap.size() > 0 && infoMap.get("kubernetes") != null) {
                Map kubernetesMap = (Map) infoMap.get("kubernetes");
                if (kubernetesMap.size() > 0 && kubernetesMap.get("kubeletVersion") != null) {
                    rancherNodes.setKubeProxyVersion(kubernetesMap.get("kubeletVersion").toString());
                }
            }
            if (infoMap.size() > 0 && infoMap.get("os") != null) {
                Map osMap = (Map) infoMap.get("os");
                if (osMap.size() > 0 && osMap.get("dockerVersion") != null) {
                    String dockerVersion = osMap.get("dockerVersion").toString();
                    rancherNodes.setDockerVersion(dockerVersion.indexOf("//") != -1 ? dockerVersion.substring(dockerVersion.indexOf("//") + 2) : dockerVersion);
                }
            }
        }
        RancherCapacity capacity = JSONObject.parseObject(JSONObject.toJSONString(dataObj.get("capacity")), RancherCapacity.class);
        RancherCapacity requested = JSONObject.parseObject(JSONObject.toJSONString(dataObj.get("requested")), RancherCapacity.class);
        if (capacity != null) {
            rancherNodes.setCpu(capacity.getCpu());
            rancherNodes.setMemory(capacity.getMemory());
            rancherNodes.setPods(capacity.getPods());
        }
        if (requested != null) {
            rancherNodes.setRequestedCpu(requested.getCpu());
            rancherNodes.setRequestedMemory(requested.getMemory());
            rancherNodes.setRequestedPods(requested.getPods());
        }
        return rancherNodes;
    }


    /**
     * @Description 根据projectId获取项目
     * @Author qzg
     * @Date 2023/04/10 14:14
     */
    public List<MwModelRancherDataInfoDTO> getProjectById(RancherInstanceParam param) throws Exception {
        List<MwModelRancherDataInfoDTO> rancherProjectList = new ArrayList<>();
        try {
            JSONObject json = readJsonFromUrl(https + param.getIp() + rancherProjects + param.getId(), param);
            if (json != null) {
                MwModelRancherDataInfoDTO rancherProject = projectExtractFrom(json);
                rancherProjectList.add(rancherProject);
            }
        } catch (Exception ex) {
            log.error("getProjectById to fail:" + ex.getMessage());
        }
        return rancherProjectList;

    }


    /**
     * @Description 获取所有项目
     * @Author qzg
     * @Date 2023/04/10 14:14
     */
    public List<MwModelRancherDataInfoDTO> getProjects(RancherInstanceParam param) throws Exception {
        List<MwModelRancherDataInfoDTO> rancherProjectList = new ArrayList<>();
        try {
            JSONObject json = null;
            if (CLUSTER.getType().equals(param.getType())) {
                //获取指定cluster下的Project
                String rancherProjectsByClusterStr = rancherProjectsByCluster.replaceAll("#clusterId", param.getId());
                json = readJsonFromUrl(https + param.getIp() + rancherProjectsByClusterStr, param);
            } else {
                json = readJsonFromUrl(https + param.getIp() + rancherProjects, param);
            }
            if (json != null) {
                JSONArray dataArr = json.getJSONArray("data");
                if (dataArr != null) {
                    for (int i = 0; i < dataArr.size(); i++) {
                        JSONObject dataObj = dataArr.getJSONObject(i);
                        MwModelRancherDataInfoDTO rancherProject = projectExtractFrom(dataObj);
                        rancherProjectList.add(rancherProject);
                    }
                }
            }

        } catch (Exception ex) {
            log.error("getProjects to fail{}:", ex);
        }
        return rancherProjectList;

    }

    /**
     * @Description 更加集群Id获取集群信息
     * @Author qzg
     * @Date 2023/04/10 14:14
     */
    public List<MwModelRancherDataInfoDTO> getClusterById(RancherInstanceParam param) throws Exception {
        List<MwModelRancherDataInfoDTO> clusterList = new ArrayList<>();
        try {
            String rancherClusterIdStr = rancherClusterId.replaceAll("#clusterId", param.getId());
            JSONObject json = readJsonFromUrl(https + param.getIp() + rancherClusterIdStr, param);
            if (json != null) {
                MwModelRancherDataInfoDTO rancherCluster = clustersExtractFrom(json);
                rancherCluster.setPId(param.getModelInstanceId().toString());
                clusterList.add(rancherCluster);
            }
        } catch (Exception ex) {
            log.error("get:" + param.getIPAdress() + "getClusterById to fail;" + ex.getMessage());
            throw ex;
        }
        return clusterList;
    }


    /**
     * @Description 获取所有集群
     * @Author qzg
     * @Date 2023/04/10 14:14
     */
    public List<MwModelRancherDataInfoDTO> getClusters(RancherInstanceParam param) throws Exception {
        List<MwModelRancherDataInfoDTO> clusterList = new ArrayList<>();
        try {
            JSONObject json = readJsonFromUrl(https + param.getIp() + rancherClusters, param);
            if (json != null) {
                JSONArray dataArr = json.getJSONArray("data");
                if (dataArr != null) {
                    for (int i = 0; i < dataArr.size(); i++) {
                        JSONObject dataObj = dataArr.getJSONObject(i);
                        MwModelRancherDataInfoDTO rancherCluster = clustersExtractFrom(dataObj);
                        rancherCluster.setPId(param.getModelInstanceId().toString());
                        clusterList.add(rancherCluster);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("get:" + param.getIp() + "rancherClusters to fail;" + ex.getMessage());
            throw ex;
        }
        return clusterList;
    }

    private MwModelRancherDataInfoDTO projectExtractFrom(JSONObject dataObj) throws Exception {
        MwModelRancherDataInfoDTO rancherProject = JSONObject.parseObject(JSONObject.toJSONString(dataObj), MwModelRancherDataInfoDTO.class);
        if (dataObj.get("clusterId") != null) {
            rancherProject.setPId(dataObj.get("clusterId").toString());
        }
        return rancherProject;
    }

    private MwModelRancherDataInfoDTO clustersExtractFrom(JSONObject dataObj) throws Exception {
        MwModelRancherDataInfoDTO rancherCluster = JSONObject.parseObject(JSONObject.toJSONString(dataObj), MwModelRancherDataInfoDTO.class);
        RancherCapacity capacity = JSONObject.parseObject(JSONObject.toJSONString(dataObj.get("capacity")), RancherCapacity.class);
        rancherCluster.setCpu(capacity.getCpu());
        rancherCluster.setMemory(capacity.getMemory());
        rancherCluster.setPods(capacity.getPods());
        RancherCapacity requested = JSONObject.parseObject(JSONObject.toJSONString(dataObj.get("requested")), RancherCapacity.class);
        rancherCluster.setRequestedCpu(requested.getCpu());
        rancherCluster.setRequestedMemory(requested.getMemory());
        rancherCluster.setRequestedPods(requested.getPods());
        if (dataObj.get("version") != null) {
            Map versionMap = (Map) dataObj.get("version");
            String gitVersion = versionMap.get("gitVersion") != null ? versionMap.get("gitVersion").toString() : "";
            rancherCluster.setGitVersion(gitVersion);
        }
        return rancherCluster;
    }


    /**
     * @Description 读取记录
     * @Author qzg
     * @Date 2023/04/10 14:15
     */
    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }


    /**
     * 从url地址中获取json数据
     */
    public JSONObject readJsonFromUrl(String url, RancherInstanceParam param) throws Exception {
        InputStream is;
        JSONObject json = null;
        try {
            //将完整的路径存入IpAdress，分布式发送时使用
            param.setIPAdress(url);
            log.info("rancher 查询url:" + url);
            //查询代理服务器
            List<ProxyInfo> proxyInfos = new ArrayList<>();
            json = proxySearch.doProxySearch(JSONObject.class, proxyInfos, param.getModelInstanceId()
                    , "mwRancherService", "readJsonFromUrl", param, null);
            if (proxyInfos.size() > 0) {
                return json;
            }
            String jsonText = ModelOKHttpUtils.builder().url(url)
                    .addHeader("Authorization", "Bearer " + param.getTokens())
                    .get()
                    .sync();
            json = JSONObject.parseObject(jsonText);
            log.info("readJsonFromUrl获取数据:" + json);
            return json;
        } catch (Exception ex) {
            log.error(url + "readJsonFromUrl获取数据失败");
        }
        return null;
    }

    /**
     * @Description 通过put请求设置数据
     * @Author qzg
     * @Date 2023/04/10 14:16
     */
//    public JSONObject putDataGetJson(String url, JSONObject data) throws Exception {
//        try {
//            // Create a trust manager that does not validate certificate chains
//            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
//                public X509Certificate[] getAcceptedIssuers() {
//                    return null;
//                }
//
//                public void checkClientTrusted(X509Certificate[] certs, String authType) {
//                }
//
//                public void checkServerTrusted(X509Certificate[] certs, String authType) {
//                }
//            }
//            };
//            // Install the all-trusting trust manager
//            SSLContext sc = SSLContext.getInstance("SSL");
//            sc.init(null, trustAllCerts, new java.security.SecureRandom());
//            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//
//            URLConnection connection = new URL(url).openConnection();
//            HttpsURLConnection httpConn = (HttpsURLConnection) connection;
//
//            httpConn.setRequestMethod("PUT");
//            httpConn.setDoOutput(true);
//            httpConn.setRequestProperty("Authorization", "Bearer " + rancherBearerToken);
//            httpConn.setRequestProperty("Accept", "application/json");
//            httpConn.setRequestProperty("Content-Type", "application/json");
//
//            OutputStreamWriter osw = new OutputStreamWriter(httpConn.getOutputStream());
//            osw.write(data.toString());
//            osw.flush();
//            osw.close();
//            InputStream is = httpConn.getInputStream();
//            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
//            String jsonText = readAll(rd);
//            JSONObject json = JSONObject.parseObject(jsonText);
//            is.close();
//            return json;
//        } catch (Exception ex) {
//            log.error("putDataGetJson to fail:" + ex.getMessage());
//            throw ex;
//        }
//    }


    /**
     * @Description 通过post请求设置数据
     * @Author qzg
     * @Date 2023/04/10 14:16
     */
//    public JSONObject postDataGetJson(String url, JSONObject data) throws Exception {
//        InputStream is = null;
//        try {
//            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
//                public X509Certificate[] getAcceptedIssuers() {
//                    return null;
//                }
//
//                public void checkClientTrusted(X509Certificate[] certs, String authType) {
//                }
//
//                public void checkServerTrusted(X509Certificate[] certs, String authType) {
//                }
//            }
//            };
//            SSLContext sc = SSLContext.getInstance("SSL");
//            sc.init(null, trustAllCerts, new java.security.SecureRandom());
//            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//
//            URLConnection connection = new URL(url).openConnection();
//            HttpsURLConnection httpConn = (HttpsURLConnection) connection;
//
//            httpConn.setRequestMethod("POST");
//            httpConn.setDoOutput(true);
//            httpConn.setRequestProperty("Authorization", "Bearer " + rancherBearerToken);
//            httpConn.setRequestProperty("Accept", "application/json");
//            httpConn.setRequestProperty("Content-Type", "application/json");
//
//            OutputStreamWriter osw = new OutputStreamWriter(httpConn.getOutputStream());
//            osw.write(data.toString());
//            osw.flush();
//            osw.close();
//            is = httpConn.getInputStream();
//
//            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
//            String jsonText = readAll(rd);
//            JSONObject strInfoJson = JSONObject.parseObject(jsonText);
//            return strInfoJson;
//        } catch (Exception ex) {
//            log.error("postDataGetJson to fail:" + ex.getMessage());
//            throw ex;
//        } finally {
//            is.close();
//        }
//    }


}
