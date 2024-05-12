package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.IpV4Util;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.dataview.TopoLineView;
import cn.mw.monitor.api.dataview.TopoNodeView;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.server.service.impl.MwServerManager;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.rule.param.RelationParam;
import cn.mw.monitor.service.rule.param.TopoDropDownParam;
import cn.mw.monitor.service.scan.TopoAlertService;
import cn.mw.monitor.service.scan.model.TaskStatus;
import cn.mw.monitor.service.scan.param.*;
import cn.mw.monitor.service.server.api.MwServerService;
import cn.mw.monitor.service.server.api.dto.HistoryListDto;
import cn.mw.monitor.service.server.api.dto.HostPerformanceInfoDto;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.server.api.dto.NetListDto;
import cn.mw.monitor.service.server.param.AssetsIdsPageInfoParam;
import cn.mw.monitor.service.server.param.ItemLineParam;
import cn.mw.monitor.service.ssh.JavaSshService;
import cn.mw.monitor.service.ssh.TopoSshExecParam;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.snmp.param.scan.*;
import cn.mw.monitor.snmp.service.MWTopologyService;
import cn.mw.monitor.snmp.service.TopoScanUpdateAction;
import cn.mw.monitor.topology.*;
import cn.mw.monitor.topology.alert.RuleParameter;
import cn.mw.monitor.topology.model.*;
import cn.mw.monitor.topology.param.LineColorParam;
import cn.mw.monitor.topology.param.LinkMod;
import cn.mw.monitor.topology.param.QueryGraphParam;
import cn.mw.monitor.topology.param.TopoIfModParam;
import cn.mw.monitor.topology.setting.LineRuleManage;
import cn.mw.zbx.common.ZbxConstants;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * 拓扑查询
 * @auth dev
 * @desc
 * @date 2020/3/31
 */
@RequestMapping("/mwapi")
@Controller
@Api(value = "拓扑管理", tags = "拓扑管理")
@Slf4j(topic = "MWTopoLogger")
public class MWTopologyController extends BaseApiService implements InitializingBean {
    private static Set<String> topoProcessing = new HashSet<>();

    @Value("${scan.debug}")
    private boolean debug;

    @Value("${topology.appendAssetIp}")
    private boolean appendAssetIp;

    @Value("${topology.line.pageNumber}")
    private int pageNumber;

    @Value("${topology.line.pageSize}")
    private int pageSize;

    @Value("${topology.create.label.enable}")
    private Boolean topoLabelEnable;

    @Value("${topology.create.org.enable}")
    private Boolean topoOrgEnable;

    @Autowired
    private MWTopologyService mwtopologyService;

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MwServerManager mwServerManager;

    @Autowired
    private TopoRedisManage topoRedisManage;

    @Autowired
    private MwServerService mwServerService;

    @Autowired
    private TopoAlertService topoAlertService;

    @Autowired
    private NetFlowManage netFlowManage;

    @Autowired
    private TopoInfoManage topoInfoManage;

    @Autowired
    private LineRuleManage lineRuleManage;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private JavaSshService javaSshService;

    private AtomicInteger taskCount = new AtomicInteger(0);
    private int maxTaskCount = 1;
    private ExecutorService fixedThreadPool;
    private Map<String, String> itemNameMap = new HashedMap();

    @PostMapping("/topology/metaInfo/editor")
    @ResponseBody
    @ApiOperation(value = "拓扑信息保存", tags = "拓扑信息保存")
    public ResponseBase metaInfoSave(@RequestBody TopoMetaInfoParam param) {
        Reply reply = null;
        try {
            reply = mwtopologyService.metaInfoSave(param);
        }catch (Throwable e){
            log.error("metaInfoSave", e);
        }
        return setResultSuccess(reply.getData());
    }

    /**
     * 拓扑创建
     */
    @MwPermit(moduleName = "topo_manage")
    @PostMapping("/topology/create")
    @ResponseBody
    public ResponseBase createTopology(@RequestBody @Validated CreateTopoParam createTopoParam,
                                       HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = Reply.ok();
        try {
            synchronized (taskCount) {
                if (taskCount.get() > maxTaskCount-1) {
                    return setResultFail("有任务正在执行,请等待任务执行完毕", createTopoParam);
                }
                taskCount.incrementAndGet();
            }
            ThreadPoolExecutor threadPoolExecutor = ((ThreadPoolExecutor) fixedThreadPool);
            log.info("createTopology 1 threadPoolExecutor core:{} ,active:{}"
                    ,threadPoolExecutor.getCorePoolSize() ,threadPoolExecutor.getActiveCount());

            fixedThreadPool.execute(() -> {
                try {
                    mwtopologyService.discoverTopology(createTopoParam);
                    log.info("toplogy dicovery finished!");
                    updateTopoCacheInfo(createTopoParam.getId());
                    //更新标签数据
                    mwtopologyService.updateTopoLabel(createTopoParam.getId(),createTopoParam.getLabelList());
                    //资产标签数据
                    if (topoLabelEnable && DiscoveryType.Label.name().equals(createTopoParam.getType())) {
                        mwtopologyService.updateTopoAssetsLabel(createTopoParam.getId(), createTopoParam.getAssetsLabelList());
                    }
                }catch (Throwable throwable){
                    log.error("topology error:", throwable);
                }finally {
                    taskCount.decrementAndGet();
                }
            });

            log.info("createTopology 2 threadPoolExecutor core:{} ,active:{}"
                        , threadPoolExecutor.getCorePoolSize(), threadPoolExecutor.getActiveCount());

            //等待子线程创建topoId
            for(int i = 0 ;i < 3 ; i++){
                Thread.sleep(1000);
                if(StringUtils.isNotEmpty(createTopoParam.getId())){
                    reply = Reply.ok(createTopoParam.getId());
                }
            }

            log.info("createTopology 3 threadPoolExecutor core:{} ,active:{}"
                    , threadPoolExecutor.getCorePoolSize(), threadPoolExecutor.getActiveCount());
        } catch (Throwable throwable) {
            log.error("topology error:", throwable);
            return setResultFail("拓扑创建失败", createTopoParam);
        }

        return setResultSuccess(reply);

    }



    /**
     * 拓扑浏览
     */
    @MwPermit(moduleName = "topo_manage")
    @PostMapping("/topology/browse")
    @ResponseBody
    public ResponseBase browseTopology(HttpServletRequest request, RedirectAttributesModelMap model, @RequestBody QueryGraphParam param) {
        Reply reply = null;
        try {
            reply = mwtopologyService.browseTopology(param);

            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }

        } catch (Exception e) {
            log.error("toplogy browse error:{}", param.getTopoName());
            log.error("toplogy browse error:", e);
            return setResultFail(e.getMessage(), null);
        }

        return setResultSuccess(reply);

    }

    /**
     * 拓扑取消创建
     */
    @MwPermit(moduleName = "topo_manage")
    @PostMapping("/topology/cancel")
    @ResponseBody
    public ResponseBase cancelTopoCreate(@RequestBody CreateTopoParam createTopoParam) {
        Reply reply = Reply.ok();
        try {
            mwtopologyService.shutdownScan(createTopoParam.getId());
        }catch (Exception e) {
            log.error("toplogy cancel error:", e);
            return setResultFail("取消失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 查看拓扑创建状态
     */
    @MwPermit(moduleName = "topo_manage")
    @PostMapping("/topology/create/status")
    @ResponseBody
    public ResponseBase topoCreateStatus(@RequestBody TopoStatusParam topoStatusParam) {
        Reply reply = null;
        try {
            reply = mwtopologyService.topologyStatus(topoStatusParam);
        }catch (Exception e) {
            log.error("toplogy status error:", e);
            return setResultFail("查看状态失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 拓扑查看
     */
    @MwPermit(moduleName = "topo_manage")
    @PostMapping("/topology/editor")
    @ResponseBody
    public ResponseBase editTopology(@RequestBody EditorTopoParam param, HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;

        if(debug){
            log.info(param.toString());
        }

        if(StringUtils.isEmpty(param.getId())){
            return setResultSuccess(Reply.ok());
        }

        try {
            //获取原始的拓扑图,并转换
            reply = mwtopologyService.editTopology(param.getId());
            if (null == reply || reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }

            NewTopoView newTopoView = new NewTopoView();
            MwTopoGraphDTO mwTopoGraphDTO = (MwTopoGraphDTO) reply.getData();
            //设置拓扑相关属性
            refreshTopoViewProperties(mwTopoGraphDTO ,newTopoView);

            //获取图形数据,并且同步index
            Graph graph = mwtopologyService.genGraph(mwTopoGraphDTO);

            //如果不显示孤立节点,则移除
            if(!mwTopoGraphDTO.getShowIsolatedNode()){
                graph.removeIsolatedNode();
            }

            //如果是空白拓扑,则直接返回
            if(null == graph.getGroup() || graph.getGroup().size() == 0){
                reply.setData(newTopoView);
                return setResultSuccess(reply);
            }

            pageNumber = (0 != param.getPageNumber()?param.getPageNumber():pageNumber);
            pageSize = (0 != param.getPageSize()?param.getPageSize():pageSize);

            Reply assetsReply = mwModelViewCommonService.findTopoModelAssets(MwTangibleassetsTable.class);
            List<MwTangibleassetsTable> assetsList = null;
            if (assetsReply.getRes() == PaasConstant.RES_SUCCESS) {
                assetsList = (List)assetsReply.getData();
            }

            ConnectLevel connectLevel = ConnectLevel.ALL;
            try {
                connectLevel = ConnectLevel.valueOf(param.getConnectLevel().toUpperCase());
            }catch (Exception e){
                log.warn("ERROR ConnectLevel:{}",param.getConnectLevel());
            }

            log.info("getTopoView");
            newTopoView = getTopoView(graph ,mwTopoGraphDTO.getEdited() ,pageNumber ,pageSize
                    ,assetsList ,connectLevel,getAssetsTypeSet(mwTopoGraphDTO.getAssetType(),graph)
                    );
            refreshTopoViewProperties(mwTopoGraphDTO ,newTopoView);

            log.info("topoAlertService.getTerminalEnable");
            boolean terminalEnable = topoAlertService.getTerminalEnable(mwTopoGraphDTO.getId());
            newTopoView.setTerminalEnable(terminalEnable);

            if(StringUtils.isNotEmpty(mwTopoGraphDTO.getScene())){
                Scene scene = JSON.parseObject(mwTopoGraphDTO.getScene(), Scene.class);
                newTopoView.setScene(scene);
            }

            if(StringUtils.isNotEmpty(mwTopoGraphDTO.getStages())){
                Stages stages = JSON.parseObject(mwTopoGraphDTO.getStages(), Stages.class);
                newTopoView.setStages(stages);
            }

            //更新流量节点信息
            log.info("netFlowManage update");
            NetFlowInfo netFlowInfo = netFlowManage.addOrRemoveNetFlowInfo(newTopoView.getId() ,newTopoView ,graph);
            netFlowManage.saveNetFlowInfo(newTopoView.getId() ,netFlowInfo);

            //转换坐标成整形,缩短坐标长度,便于前端显示
            newTopoView.formateData();

            //拓扑视图暂存缓存,初始化拓扑index
            updateTopoViewInfo(newTopoView);

            //设置是否显示连线
            if(!mwTopoGraphDTO.getShowLine()){
                newTopoView.setLink(null);
            }

            reply.setData(newTopoView);

            if(debug){
                log.info(newTopoView.toString());
            }

        } catch (Exception e) {
            log.error("editTopology error:{}" ,param.getId());
            log.error("editTopology error:", e);
            return setResultFail("拓扑查看失败", null);
        }

        return setResultSuccess(reply);

    }

    private Set<Integer> getAssetsTypeSet(String assetType, Graph graph) {
        Set<Integer> set = new HashSet<>();
        if (StringUtils.isEmpty(assetType)) {
            for (Group group : graph.getGroup()) {
                for (Node node : group.getNodes().getNode()) {
                    if (node.getAssetTypeId() > 0) {
                        set.add(node.getAssetTypeId());
                    }
                }
            }
            return set;
        }
        String[] array = assetType.split(",");
        for (String str : array) {
            set.add(Integer.parseInt(str));
        }
        return set;
    }

    private void refreshTopoViewProperties(MwTopoGraphDTO mwTopoGraphDTO ,NewTopoView newTopoView){
        newTopoView.setId(mwTopoGraphDTO.getId());
        newTopoView.setVisible(mwTopoGraphDTO.getIfVisible());
    }

    private NewTopoView getTopoView(Graph graph ,boolean isEdited ,int pageNumber ,int pageSize
            ,List<MwTangibleassetsTable> assetsList ,ConnectLevel connectLevel,Set<Integer> assetsTypeSet){

        Set<String> assetsIpSet = new HashSet<>();
        Map<String, MwTangibleassetsTable> assetsMap = new HashMap<>();
        if(null != assetsList && assetsList.size() > 0){
            for(MwTangibleassetsTable asset: assetsList){
                if (assetsTypeSet != null){
                    if (assetsTypeSet.contains(asset.getAssetsTypeId())){
                        assetsIpSet.add(asset.getInBandIp());
                        assetsMap.put(asset.getInBandIp(), asset);
                    }
                }else {
                    assetsIpSet.add(asset.getInBandIp());
                    assetsMap.put(asset.getInBandIp(), asset);
                }
            }
        }

        NewTopoView newTopoView = new NewTopoView();

        //生成拓扑视图
        TopoFilterContext topoFilterContext = new TopoFilterContext();

        //如果编辑过,则直接返回数据库中拓扑图
        if(isEdited){
            newTopoView.setType("editor");

            //不自动生成text节点
            topoFilterContext.setGenTextNode(false);
        }

        topoFilterContext.setPageNumber(pageNumber);
        topoFilterContext.setPageSize(pageSize);

        topoFilterContext.setAssetsIpSet(assetsIpSet);
        topoFilterContext.setConnectLevel(connectLevel);

        TopoContext topoContext = newTopoView.extractFromMwTopoGraphDTO(graph, assetsMap ,topoFilterContext);
        topoContext.setDebug(debug);
        topoContext.setAssetsMap(assetsMap);
        topoContext.setAppendAssetIp(appendAssetIp);

        //未编辑过的图才计算节点坐标
        if(!isEdited) {
            log.info("getTopoView coordinateCal");
            newTopoView.coordinateCal(topoContext);
        }

        //过滤接口数据,按页数返回数据
        newTopoView.filterData(topoFilterContext);

        //更新额外信息节点
        newTopoView.refreshInfoNode(topoContext);

        //设置默认字体颜色
        topoInfoManage.initTextColor(newTopoView);

        return newTopoView;
    }

    private NewTopoView getTopo(String topoId) throws Exception{
        return getTopo(topoId ,null);
    }

    private NewTopoView getTopo(String topoId ,Graph graph) throws Exception{

        Graph graphD = graph;
        Reply reply = mwtopologyService.editTopology(topoId);
        if (reply.getRes() != PaasConstant.RES_SUCCESS) {
            return null;
        }
        MwTopoGraphDTO mwTopoGraphDTO = (MwTopoGraphDTO) reply.getData();
        NewTopoView newTopoView = new NewTopoView();
        newTopoView.setId(topoId);

        if(null == graphD) {
            graphD = mwtopologyService.genGraph(mwTopoGraphDTO);
        }

        //未编辑过需要计算节点坐标
        if(!mwTopoGraphDTO.getEdited()) {
            TopoFilterContext topoFilterContext = new TopoFilterContext();
            topoFilterContext.setGenTextNode(false);
            TopoContext topoContext = newTopoView.extractFromMwTopoGraphDTO(graphD, null ,topoFilterContext);
            newTopoView.coordinateCal(topoContext);
        }else{
            newTopoView.extractFromGraph(graphD);
        }

        if(StringUtils.isNotEmpty(mwTopoGraphDTO.getNetFlowInfo())){
            NetFlowInfo netFlowInfo = JSON.parseObject(mwTopoGraphDTO.getNetFlowInfo(), NetFlowInfo.class);
            newTopoView.setNetFlowInfo(netFlowInfo);
        }
        return newTopoView;
    }

    /**
     * 拓扑删除
     */
    @MwPermit(moduleName = "topo_manage")
    @PostMapping("/topology/delete")
    @ResponseBody
    public ResponseBase deleteTopology(@RequestBody DeleteTopoParam deleteTopoParam, HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwtopologyService.deleteTopology(deleteTopoParam.getId());

            //删除告警相关信息
            topoAlertService.topoDel(deleteTopoParam.getId());
        } catch (Exception e) {
            log.error("toplogy delete error:", e);
            return setResultFail("拓扑删除失败", null);
        }

        return setResultSuccess(Reply.ok());

    }

    /**
     * 拓扑编辑后保存
     */
    @PostMapping("/topology/editorSave")
    @ResponseBody
    public ResponseBase saveTopology(@RequestBody TopoModParam topoParam, HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        boolean processing = false;

        //防止用户在页面上反复点保存
        synchronized (topoProcessing) {
            if (!topoProcessing.contains(topoParam.getId())) {
                topoProcessing.add(topoParam.getId());
                processing = true;
            }
        }

        try {
            if(processing){
                //合并相同的连接
                topoParam.mergeSameLink();
                reply = mwtopologyService.saveTopology(topoParam);
                mwtopologyService.removeTopoId(topoParam.getId());
                //通知告警模块
                topoAlertService.notifyTopoUpdate(topoParam.getId());

                //更新拓扑缓存
                updateTopoCacheInfo(topoParam.getId());
            }
        } catch (Exception e) {
            log.error("saveTopology error:", e);
            return setResultFail("拓扑保存失败", null);
        }finally {
            if(processing){
                topoProcessing.remove(topoParam.getId());
            }
        }

        if(!processing){
            return setResultFail("重复保存:" + topoParam.getId(), null);
        }

        return setResultSuccess(reply);

    }

    /**
     * 拓扑节点信息查看
     */
    @MwPermit(moduleName = "topo_manage")
    @PostMapping("/topology/node/browse")
    @ResponseBody
    public ResponseBase viewTopoNode(@RequestBody ViewTopoNodeParam viewTopoNodeParam,
                                     HttpServletRequest request, RedirectAttributesModelMap model) {
        try {
            if (StringUtils.isNotEmpty(viewTopoNodeParam.getIp())) {
                //获取资产信息
                MwTangibleassetsDTO assetInfo = mwModelViewCommonService.selectByIp(viewTopoNodeParam.getIp());
                TopoNodeView nodeView = new TopoNodeView();
                nodeView.setIp(viewTopoNodeParam.getIp());
                if (null != assetInfo ){
                    if(null != assetInfo.getMonitorServerId()) {
                        //获取节点性能数据
                        HostPerformanceInfoDto pInfo = mwServerManager.getHostPerformanceInfo(assetInfo.getMonitorServerId()
                                , assetInfo.getAssetsId());
                        nodeView.setAssetId(assetInfo.getId());
                        nodeView.setAssetName(assetInfo.getAssetsName());
                        nodeView.setCpuUsage(pInfo.getCpuUnitilization());
                        nodeView.setMemUsage(pInfo.getMemoryUtilization());
                        nodeView.setDelay(pInfo.getDelayed());
                    }else{
                        log.warn("viewTopoNode no monitorServerid ip:{},assetName:{}" ,assetInfo.getInBandIp() ,assetInfo.getAssetsName());
                    }
                }
                return setResultSuccess(Reply.ok(nodeView));
            }
        } catch (Exception e) {
            log.warn("view node warn:", e);

        }
        return setResultFail("view node fail", viewTopoNodeParam);
    }

    /**
     * 拓扑链路信息查看
     */
    @MwPermit(moduleName = "topo_manage")
    @PostMapping("/topology/line/browse")
    @ResponseBody
    public ResponseBase viewTopoLine(@RequestBody ViewTopoLineParam viewTopoLineParam,
                                     HttpServletRequest request, RedirectAttributesModelMap model) {

        if(debug){
            log.info(viewTopoLineParam.toString());
        }

        TopoLineView topoLineView = new TopoLineView();
        topoLineView.setUpGraphIndex(viewTopoLineParam.getUpGraphIndex());
        topoLineView.setDownGraphIndex(viewTopoLineParam.getDownGraphIndex());
        topoLineView.setPageNumber(viewTopoLineParam.getPageNumber());
        topoLineView.setPageSize(viewTopoLineParam.getPageSize());
        List<ViewTopoIfParam> viewTopoIfParamList = viewTopoLineParam.getIfLinkViewList();

        if(StringUtils.isEmpty(viewTopoLineParam.getTopoId())){
            return setResultFail("topoId is null", viewTopoLineParam);
        }

        Map<Integer, NewTopoNodeView> nodeIndexMap = null;

        try {
            //获取拓扑图
            NewTopoView newTopoView = getTopo(viewTopoLineParam.getTopoId());
            nodeIndexMap = newTopoView.genNodeIndexMap();
            if(null == viewTopoIfParamList || viewTopoIfParamList.size() == 0){
                viewTopoIfParamList = new ArrayList<>();
                //获取对应连线的接口列表
                List<IfLinkView> ifLinkViewList = newTopoView.getIfLinkViewList(viewTopoLineParam.getUpGraphIndex()
                        ,viewTopoLineParam.getDownGraphIndex() ,viewTopoLineParam.getConLevel());

                topoLineView.setTotal(ifLinkViewList.size());

                //获取指定页数据
                ifLinkViewList = newTopoView.getIfLinkViewByPage(ifLinkViewList ,topoLineView.getPageNumber() ,topoLineView.getPageSize());

                if(null != ifLinkViewList && ifLinkViewList.size() >0) {
                    for(IfLinkView ifLinkView : ifLinkViewList){
                        ViewTopoIfParam viewTopoIfParam = new ViewTopoIfParam();
                        viewTopoIfParam.setId(ifLinkView.getId());
                        viewTopoIfParam.setUpIfName(ifLinkView.getUpIfName());
                        viewTopoIfParam.setDownIfName(ifLinkView.getDownIfName());
                        viewTopoIfParam.setConLevel(ifLinkView.getConLevel());
                        viewTopoIfParamList.add(viewTopoIfParam);
                    }

                }
            }

            Map<Integer ,List<String>> serverHostIdMap = new HashMap<>();
            Map<Integer ,List<String>> serverIfMap = new HashMap<>();

            if(null != nodeIndexMap) {
                NewTopoNodeView upNode = nodeIndexMap.get(viewTopoLineParam.getUpGraphIndex());
                if (null != upNode && StringUtils.isNotEmpty(upNode.getTangibleId())) {
                    //获取资产信息
                    Reply reply = mwModelViewCommonService.selectById(upNode.getTangibleId());
                    if (null != reply && PaasConstant.RES_SUCCESS == reply.getRes()) {
                        MwTangibleassetsTable upassetInfo = (MwTangibleassetsTable) reply.getData();
                        if (null != upassetInfo) {
                            topoLineView.setUpIp(upassetInfo.getInBandIp());
                            topoLineView.setUpDevice(upassetInfo.getAssetsName());

                            List<String> hostids = serverHostIdMap.get(upassetInfo.getMonitorServerId());
                            if (null == hostids) {
                                hostids = new ArrayList<>();
                                serverHostIdMap.put(upassetInfo.getMonitorServerId(), hostids);
                            }
                            hostids.add(upassetInfo.getAssetsId());
                            topoLineView.setUpHostId(upassetInfo.getAssetsId());
                            topoLineView.setUpMonitorServerId(upassetInfo.getMonitorServerId());

                            List<String> ifNames = serverIfMap.get(upassetInfo.getMonitorServerId());
                            if (null == ifNames) {
                                ifNames = new ArrayList<>();
                                serverIfMap.put(upassetInfo.getMonitorServerId(), ifNames);
                            }

                            for (ViewTopoIfParam viewTopoIfParam : viewTopoIfParamList) {
                                ifNames.add(viewTopoIfParam.getUpIfName());
                            }
                        }
                    }
                }

                NewTopoNodeView downNode = nodeIndexMap.get(viewTopoLineParam.getDownGraphIndex());
                if (null != downNode && StringUtils.isNotEmpty(downNode.getTangibleId())) {
                    //获取资产信息
                    Reply reply = mwModelViewCommonService.selectById(downNode.getTangibleId());
                    if (null != reply && PaasConstant.RES_SUCCESS == reply.getRes()) {
                        MwTangibleassetsTable downassetInfo = (MwTangibleassetsTable) reply.getData();

                        if (null != downassetInfo) {
                            topoLineView.setDownIp(downassetInfo.getInBandIp());
                            topoLineView.setDownDevice(downassetInfo.getAssetsName());
                            topoLineView.setDownMonitorServerId(downassetInfo.getMonitorServerId());

                            List<String> hostids = serverHostIdMap.get(downassetInfo.getMonitorServerId());
                            if (null == hostids) {
                                hostids = new ArrayList<>();
                                serverHostIdMap.put(downassetInfo.getMonitorServerId(), hostids);
                            }
                            hostids.add(downassetInfo.getAssetsId());
                            topoLineView.setDownHostId(downassetInfo.getAssetsId());

                            List<String> ifNames = serverIfMap.get(downassetInfo.getMonitorServerId());
                            if (null == ifNames) {
                                ifNames = new ArrayList<>();
                                serverIfMap.put(downassetInfo.getMonitorServerId(), ifNames);
                            }
                            for (ViewTopoIfParam viewTopoIfParam : viewTopoIfParamList) {
                                ifNames.add(viewTopoIfParam.getDownIfName());
                            }
                        }
                    }
                }
            }

            List<HostPerformanceInfoDto> list = new ArrayList<>();
            for(Integer monitorServerId : serverHostIdMap.keySet()){
                List<String> hostIds = serverHostIdMap.get(monitorServerId);
                List<String> ifNames = serverIfMap.get(monitorServerId);
                List<HostPerformanceInfoDto> ret = mwServerManager.getHostPerformanceInfo(monitorServerId ,hostIds ,ifNames);
                list.addAll(ret);
            }

            if(null != list) {
                NetFlowInfo netFlowInfo = newTopoView.getNetFlowInfo();
                Map<Integer, List<NetFlowEntry>> netFlowMap = new HashMap<>();
                if(null != netFlowInfo) {
                    netFlowMap = netFlowInfo.findLineMap();
                }
                topoLineView.extractData(list, viewTopoIfParamList ,netFlowMap);

                LineColorParam lineColorParam = lineRuleManage.getLineColorParam(viewTopoLineParam.getTopoId()
                ,viewTopoLineParam.getUpGraphIndex() ,viewTopoLineParam.getDownGraphIndex());

                topoLineView.setLinecolor(lineColorParam);
            }
            log.info(topoLineView.toString());
            return setResultSuccess(Reply.ok(topoLineView));
        } catch (Exception e) {
            log.error("topoview line error:", e);

        }
        return setResultFail("view line fail", viewTopoLineParam);
    }


    @PostMapping("/topology/searchUpAssisted")
    @ResponseBody
    @ApiOperation(value = "查询上联资产", tags = "查询上联资产")
    public ResponseBase searchUpAssisted(@RequestBody SearchAssets searchAssets) {
        SearchAssistedResponse searchAssistedResponse = new SearchAssistedResponse();
        searchAssistedResponse.setHaveMwTangibleassetsTable(false);
        try {
            Reply reply = mwModelViewCommonService.selectById(searchAssets.getTangibleId());
            if (null != reply && PaasConstant.RES_SUCCESS == reply.getRes()) {
                MwTangibleassetsTable mwTangibleassetsTable = (MwTangibleassetsTable)reply.getData();
                searchAssistedResponse.setHaveMwTangibleassetsTable(true);
                searchAssistedResponse.setMwTangibleassetsTable(mwTangibleassetsTable);
            }
        } catch (Exception e) {
            log.error("topoview line error:", e);

        }
        return setResultSuccess(Reply.ok(searchAssistedResponse));
    }


    @PostMapping("/topology/searchdownAssisted")
    @ResponseBody
    @ApiOperation(value = "查询下联资产", tags = "查询下联资产")
    public ResponseBase searchdownAssisted(@RequestBody SearchAssets searchAssets) {

        List<MwTangibleassetsTable> mwTangibleassetsDTOs = new ArrayList<>();
        try {
            Reply reply = mwModelViewCommonService.selectById(searchAssets.getTangibleId());
            if (null != reply && PaasConstant.RES_SUCCESS == reply.getRes()) {
                MwTangibleassetsTable mwTangibleassetsDTO = (MwTangibleassetsTable)reply.getData();
                mwTangibleassetsDTOs.add(mwTangibleassetsDTO);
            }

        } catch (Exception e) {
            log.error("topoview line error:", e);

        }
        return setResultSuccess(mwTangibleassetsDTOs);
    }


    @PostMapping("/topology/searchIfHisData")
    @ResponseBody
    @ApiOperation(value = "查询接口图形数据", tags = "查询接口图形数据")
    public ResponseBase searchIfHisData(@RequestBody IfHisDataParam ifHisDataParam) {

        List<String> hostIds = new ArrayList<>();
        hostIds.add(ifHisDataParam.getAssetsId());

        List<String> itemNames = new ArrayList<>();
        Map<String, List<ItemApplication>> itemMap = new HashedMap();
        if(StringUtils.isEmpty(ifHisDataParam.getItemType())){
            for(String key : itemNameMap.keySet()){
                String itemName = "[" + ifHisDataParam.getItemName() + "]" + key;
                itemNames.add(itemName);
                itemMap.put(itemName, new ArrayList<>());
            }
        }else if(itemMap.keySet().contains(ifHisDataParam.getItemType())) {
            String itemName = "[" + ifHisDataParam.getItemName() + "]" + ifHisDataParam.getItemType();
            itemNames.add(itemName);
            itemMap.put(itemName, new ArrayList<>());
        }

        List<ItemApplication> itemApplications = mwServerService.itemsGetByNames(ifHisDataParam.getMonitorServerId() ,hostIds ,itemNames);
        //根据itemid,查询历史数据
        List<ItemLineParam> itemLineParams = new ArrayList<>();
        if(null != itemApplications) {
            for(ItemApplication itemApplication : itemApplications){
                ItemLineParam itemLineParam = new ItemLineParam();
                itemLineParam.setMonitorServerId(ifHisDataParam.getMonitorServerId());
                itemLineParam.setItemName(itemApplication.getName());
                itemLineParam.setItemId(itemApplication.getItemid());
                itemLineParam.setUnits(itemApplication.getUnits());
                itemLineParam.setHistory(3);
                itemLineParam.setValue_type(MwServerService.NUMERAL);
                itemLineParams.add(itemLineParam);
            }
        }

        Reply reply = mwServerService.getHistoryByItemIds(itemLineParams ,ifHisDataParam.getMonitorServerId());
        if (null != reply && reply.getRes() == PaasConstant.RES_SUCCESS) {
            Map<String, HistoryListDto> datasMap = new HashedMap();
            List<HistoryListDto> dtos = (List) reply.getData();
            for(HistoryListDto historyListDto : dtos){
                for(String itemName : itemNameMap.keySet()){
                    if(historyListDto.getTitleName().indexOf(itemName) != -1){
                        String key = itemNameMap.get(itemName);
                        datasMap.put(key ,historyListDto);
                        break;
                    }
                }
            }
            return setResultSuccess(datasMap);
        }

        return setResultSuccess(Reply.ok());
    }

    @PostMapping("/topology/canAdd")
    @ResponseBody
    @ApiOperation(value = "判断是否可以新增节点", tags = "判断是否可以新增节点")
    public ResponseBase canAdd(@RequestBody CanAddTopoView param, HttpServletRequest request, RedirectAttributesModelMap model) {

        Reply reply = null;
        try {
            String topoId = param.getTopoId();
            String nodeId = param.getId();
            if(!topoRedisManage.isExistTopo(topoId)){
                NewTopoView newTopoView = getTopo(topoId);
                topoRedisManage.refreshTopo(newTopoView);
            }
            //获取编辑过的拓扑并返回
            NewTopoView newTopoView1 = mwtopologyService.selectEditTopology(param.getTopoId(), param.isAssetFilter());
            newTopoView1.setType("editor");
            if (!topoRedisManage.isExistNode(topoId, nodeId)) {
                reply.setData("true");
            } else {
                reply.setData("false");
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("canAdd error:{}", param.getTopoId());
            log.error("canAdd error:", e);
            return setResultFail("新增节点失败", null);
        }
    }

    @PostMapping("/topology/updateLineAndNode")
    @ResponseBody
    @ApiOperation(value = "修改节点和线", tags = "修改节点和线")
    public ResponseBase updateLineAndNode(@RequestBody TopoIfModParam param, HttpServletRequest request, RedirectAttributesModelMap model) {
        List<NetFlowViewAction> list = new ArrayList<>();
        try {
            if(debug){
                log.info(param.toString());
            }

            String topoId = param.getId();
            if(null != param.getLinks() && param.getLinks().size() > 0) {
                String str = topoRedisManage.getTopoView(topoId);
                TopoCacheView topoCacheView = null;
                if (StringUtils.isNotEmpty(str)) {
                    topoCacheView = TopoCacheView.getTopoCacheView(str);
                }else{
                    topoCacheView = updateTopoCacheInfo(topoId);
                }

                Map<Integer ,LineInfo> lineInfoMap = topoRedisManage.getLineInfoMap(topoId);
                if(null != lineInfoMap) {
                    for (LinkMod linkMod : param.getLinks()) {
                        LineInfo lineInfo = lineInfoMap.get(linkMod.getId());
                        if (null != lineInfo) {
                            linkMod.extractFrom(lineInfo);
                            topoRedisManage.modLink(topoId, linkMod);
                        }
                    }
                }

                Map<Integer, TopoCacheNodeView> nodeIndexMap = topoCacheView.genAllNodeIndexMap();
                //更新流量信息
                list = netFlowManage.updateNetFlowInfo(param ,nodeIndexMap);

                //更新节点颜色
                topoInfoManage.initTextColor(list);

                //拓扑连线流量规则
                lineRuleManage.updateSubLineRule(param ,nodeIndexMap ,topoRedisManage);

            }else{
                return setResultFail("接口不能为空", "false");
            }

            return setResultSuccess(Reply.ok(list));
        } catch (Exception e) {
            log.error("updateLineAndNode error:", e);
            return setResultFail("修改失败", "false");
        }
    }

    private TopoCacheView updateTopoCacheInfo(String topoId) throws Exception{
        NewTopoView newTopoView = getTopo(topoId);
        topoRedisManage.refreshTopo(newTopoView);
        TopoCacheView topoCacheView = new TopoCacheView();
        topoCacheView.extractFrom(newTopoView);
        topoRedisManage.saveTopoCacheInfo(newTopoView);
        return topoCacheView;
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/topology/getNetDataList/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前hostid的所有网络接口列表的数据")
    public ResponseBase getNetDataList(@RequestBody AssetsIdsPageInfoParam param) {
        Reply reply;

        try {
            String topoId = param.getTopoId();
            if(!topoRedisManage.isExistTopo(topoId) || !topoRedisManage.isExistTopoLine(topoId)){
                NewTopoView newTopoView = getTopo(topoId);
                topoRedisManage.refreshTopo(newTopoView);
            }

            // 验证内容正确性
            reply = mwServerService.getNetDataList(param);
             PageInfo pageInfo= (PageInfo)reply.getData();
            List<NetListDto> listDtos =pageInfo.getList();
            List<NetListDto> list = new ArrayList<>();
            Set<String> strings = topoRedisManage.getInterfaces(topoId, param.getGraphIndex());
            for (int i = 0 ; i<listDtos.size();i++) {
                NetListDto d=listDtos.get(i);
                d.setState("未占用");
                if (d.getInterfaceName()!=null&&d.getInterfaceName()!=""&&strings.contains(d.getInterfaceName())){
                    d.setState("已占用");
                }
                list.add(d);
            }

            Collections.sort(list, new Comparator<NetListDto>(){
                @Override
                public int compare(NetListDto u1, NetListDto u2) {
                    return u1.getInterfaceName().compareTo(u2.getInterfaceName());
                }
            });

            pageInfo.setList(list);
            reply=Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("getNetDataList error:{}", param.getTopoId());
            log.error("getNetDataList error:", e);
            return setResultFail("获取接口列表失败", null);
        }

        return setResultSuccess(reply);
    }

    @PostMapping("/topology/deviceTopo/editor")
    @ResponseBody
    @ApiOperation(value = "更新网络设备拓扑关系表mw_topo_tree", tags = "更新网络设备拓扑关系")
    public ResponseBase updateDeviceTopo(HttpServletRequest request) {
        try {
            mwtopologyService.updateDeviceTopo();
        }catch (Exception e){
            log.error("updateDeviceTopo", e);
            return setResultFail("更新网络设备拓扑失败", "");
        }
        return setResultSuccess();
    }

    @PostMapping("/topology/refreshIPScanTable/editor")
    @ResponseBody
    @ApiOperation(value = "重置ip地址管理扫描关系表mw_ipscan_asset", tags = "重置ip地址管理扫描关系表mw_ipscan_asset")
    public ResponseBase refreshIPScanTable(HttpServletRequest request) {
        try {
            mwtopologyService.refreshIPScanTable();
        }catch (Throwable e){
            log.error("updateDeviceTopo", e);
            return setResultFail("更新网络设备拓扑失败", "");
        }
        return setResultSuccess();
    }

    @PostMapping("/topology/rule/dropdown")
    @ResponseBody
    @ApiOperation(value = "高级配置下拉框", tags = "高级配置下拉框")
    public ResponseBase getRuleDropDown(@RequestBody TopoDropDownParam topoDropDownParam) {
        List<String> list = new ArrayList<>();
        try {
            list = topoAlertService.getDropDownList(topoDropDownParam.getKey());
        }catch (Throwable e){
            log.error("getRuleDropDown", e);
            return setResultFail("更新网络设备拓扑失败", "");
        }
        return setResultSuccess(list);
    }

    @PostMapping("/topology/rule/relation")
    @ResponseBody
    @ApiOperation(value = "高级配置关系下拉框", tags = "高级配置关系下拉框")
    public ResponseBase getRuleRelation(@RequestBody RelationParam param) {
        List<Map<String, String>> list = new ArrayList<>();
        try {
            list = topoAlertService.getRelationList(param.isGroup());
        }catch (Throwable e){
            log.error("getRuleRelation", e);
            return setResultFail("更新网络设备拓扑失败", "");
        }
        return setResultSuccess(list);
    }

    @PostMapping("/topology/rule/save")
    @ResponseBody
    @ApiOperation(value = "高级配置保存", tags = "高级配置保存")
    public ResponseBase ruleSave(@RequestBody RuleParameter param) {
        String ret = "";
        try {
            ret = topoAlertService.saveRule(param);
        }catch (Throwable e){
            log.error("ruleSave", e);
            return setResultFail("高级配置保存失败", "");
        }
        return setResultSuccess(ret);
    }

    @PostMapping("/topology/rule/editor")
    @ResponseBody
    @ApiOperation(value = "查看高级配置", tags = "查看高级配置")
    public ResponseBase ruleEditor(@RequestBody RuleParameter param) {
        RuleParameter ret = null;
        try {
            ret = (RuleParameter)topoAlertService.editRule(param);
        }catch (Throwable e){
            log.error("ruleSave", e);
        }
        return setResultSuccess(ret);
    }

    @PostMapping("/topology/listNotifyEvent")
    @ResponseBody
    @ApiOperation(value = "获取拓扑告警信息", tags = "获取拓扑告警信息")
    public ResponseBase getNotifyEvent(@RequestBody TopoNotifyParam param) {
        Reply reply = Reply.ok();
        try {
            if(null != topoAlertService){
                reply = topoAlertService.getNotifyEvent(param);
            }
        }catch (Throwable e){
            log.error("getNotifyEvent", e);
        }
        return setResultSuccess(reply.getData());
    }

    @PostMapping("/topology/terminalScan/editor")
    @ResponseBody
    @ApiOperation(value = "设备终端扫描", tags = "设备终端扫描")
    public ResponseBase startTerminalScan(@RequestBody TopoTerminalScanParam param) {
        if(StringUtils.isEmpty(param.getTopoId())){
            String msg = Reply.replaceMsg(ErrorConstant.COMMON_MSG_200001, new String[]{"拓扑id"});
            return setResultFail(msg ,param);
        }

        try {
            Reply reply = mwtopologyService.getTopologyInfoById(param.getTopoId());
            if(null != reply && PaasConstant.RES_SUCCESS == reply.getRes()){
                MwTopoGraphDTO mwTopoGraphDTO = (MwTopoGraphDTO)reply.getData();

                boolean ret = topoAlertService.setTerminalEnable(param.getTopoId() ,mwTopoGraphDTO.getTopoName() ,param.isEnable());
                if(!ret){
                    List<TaskStatus> taskStatuses = topoAlertService.getTaskStatus();
                    if(null != taskStatuses){
                        for(TaskStatus taskStatus : taskStatuses){
                            if(taskStatus.getTerminalScanEnable().get()){
                                String msg = Reply.replaceMsg("[#{1}]任务正在执行,请先关闭", new String[]{taskStatus.getTopoName()});
                                return setResultFail(msg ,param);
                            }
                        }
                    }
                }
            }
        }catch (Throwable e){
            log.error("startTerminalScan", e);
        }

        String msg = "成功关闭终端扫描";
        if(param.isEnable()){
            msg = "成功开启终端扫描";
        }
        return setResultSuccess(Reply.ok(msg));
    }

    @PostMapping("/topology/analyseLine/create")
    @ResponseBody
    @ApiOperation(value = "节点连线分析", tags = "节点连线分析")
    public ResponseBase analyseLine(@RequestBody TopoModParam topoParam) {

        if(debug){
            log.info(topoParam.toString());
        }
        Reply reply = null;
        try {
            reply = mwtopologyService.analyseLine(topoParam);
        }catch (Exception e){
            log.error("analyseLine" ,e);
            reply = Reply.fail("节点连线分析失败");
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/topology/graphIndex/create")
    @ResponseBody
    @ApiOperation(value = "获取拓扑图索引", tags = "获取拓扑图索引")
    public ResponseBase getGraphIndex(@RequestBody TopoIndexParam topoIndexParam) {
        int ret = mwtopologyService.getGraphIndex(topoIndexParam);
        return setResultSuccess(Reply.ok(ret));
    }

    @PostMapping("/topology/scanUpdate/create")
    @ResponseBody
    @ApiOperation(value = "拓扑扫描更新", tags = "拓扑扫描更新")
    public ResponseBase topoScanUpdate(@RequestBody TopoIndexParam topoIndexParam) {
        Reply reply = Reply.ok(topoIndexParam.getTopoId());

        //防止两个拓扑扫描任务同时执行,会导致拓扑状态获取异常
        int count = taskCount.incrementAndGet();
        if (count > maxTaskCount) {
            log.info("taskCount:{}" ,count);
            taskCount.decrementAndGet();
            return setResultFail("有任务正在执行,请等待任务执行完毕", topoIndexParam);
        }

        ThreadPoolExecutor threadPoolExecutor = ((ThreadPoolExecutor) fixedThreadPool);
        log.info("topoScanUpdate 1 threadPoolExecutor core:{} ,active:{}"
                    ,threadPoolExecutor.getCorePoolSize() ,threadPoolExecutor.getActiveCount());

        CreateTopoParam createTopoParam = new CreateTopoParam();
        try {
            //查询拓扑信息
            Reply serviceReply = mwtopologyService.editTopology(topoIndexParam.getTopoId());
            if (null == serviceReply || serviceReply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("拓扑查询失败" ,topoIndexParam.getTopoId());
            }
            MwTopoGraphDTO mwTopoGraphDTO = (MwTopoGraphDTO) serviceReply.getData();

            if(!mwTopoGraphDTO.getEdited()){
                return setResultFail("请先点编辑,并保存" ,topoIndexParam.getTopoId());
            }

            createTopoParam.extractFrom(mwTopoGraphDTO);

            //重新扫描,并获取当前拓扑信息
            DiscoveryType type = DiscoveryType.valueOf(createTopoParam.getType());
            if(DiscoveryType.Empty == type){
                return setResultFail("不支持空拓扑创建类型" ,topoIndexParam.getTopoId());
            }

            fixedThreadPool.execute(() -> {
                boolean isSuccess = doTopoScanUpdate(topoIndexParam ,createTopoParam , type);
                try {
                    if(isSuccess) {
                        updateTopoCacheInfo(topoIndexParam.getTopoId());
                    }
                }catch (Throwable throwable){
                    log.error("topoScanUpdate" ,throwable);
                }
            });

        }catch (Throwable throwable){
            log.error("topoScanUpdate" ,throwable);
            return setResultFail("处理失败" ,topoIndexParam.getTopoId());
        }finally {
            int num = taskCount.decrementAndGet();
            log.info("taskCount:{}" ,num);
        }

        return setResultSuccess(reply);
    }

    private boolean doTopoScanUpdate(TopoIndexParam topoIndexParam ,CreateTopoParam createTopoParam ,final DiscoveryType type ){

        try{
            mwtopologyService.topoScanUpdate(TopoScanUpdateAction.START ,createTopoParam);

            //扫描网络拓扑
            Graph graph = mwtopologyService.discoveryGraph(createTopoParam ,type);

            log.info("doTopoScanUpdate redicovery finished!");
            //获取当前拓扑信息
            Reply assetsReply = mwModelViewCommonService.findTopoModelAssets(MwTangibleassetsTable.class);
            List<MwTangibleassetsTable> assetsList = null;
            if (assetsReply.getRes() == PaasConstant.RES_SUCCESS) {
                assetsList = (List)assetsReply.getData();
            }

            NewTopoView newTopoView = getTopoView(graph ,true ,1 ,Integer.MAX_VALUE
                    ,assetsList ,ConnectLevel.ALL,null);

            NewTopoView oldTopoView = getTopo(topoIndexParam.getTopoId());

            //合并新旧拓扑信息
            TopoUpdateManage topoUpdateManage = new TopoUpdateManage(oldTopoView ,mwtopologyService);
            topoUpdateManage.mergeTopoView(newTopoView);
            topoUpdateManage.save(iLoginCacheInfo.getLoginName());

        }catch (Throwable throwable){
            log.error("topoScanUpdate" ,throwable);
        }finally {
            mwtopologyService.topoScanUpdate(TopoScanUpdateAction.END ,createTopoParam);
        }

        return true;
    }

    @PostMapping("/topology/lock/perform")
    @ResponseBody
    @ApiOperation(value = "拓扑锁定", tags = "拓扑锁定")
    public ResponseBase topoLock(@RequestBody LockInfoParam lockInfoParam) {
        Reply reply = null;
        try {
            reply = mwtopologyService.topoLock(lockInfoParam);
            if(null != reply && PaasConstant.RES_SUCCESS == reply.getRes()){
                return setResultSuccess(lockInfoParam);
            }
        }catch (Throwable throwable){
            log.error("topoLock" ,throwable);
        }

        return setResultFail("设置失败" ,lockInfoParam);
    }

    @PostMapping("/topology/config/create")
    @ResponseBody
    @ApiOperation(value = "拓扑属性设置", tags = "拓扑属性设置")
    public ResponseBase topoConfigUpdate(@RequestBody TopoConfigParam topoConfigParam) {
        Reply reply = null;
        try {
            reply = mwtopologyService.topoConfigUpdate(topoConfigParam);
            if(null != reply && PaasConstant.RES_SUCCESS == reply.getRes()){
                return setResultSuccess(topoConfigParam);
            }
        }catch (Throwable throwable){
            log.error("topoConfigUpdate" ,throwable);
        }

        return setResultFail("设置失败" ,topoConfigParam);
    }

    @PostMapping("/topology/command/perform")
    @ResponseBody
    @ApiOperation(value = "拓扑执行ssh命令", tags = "拓扑执行ssh命令")
    public ResponseBase topoSshExecute(@RequestBody TopoSshExecParam topoSshExecParam) {
        Reply reply = Reply.fail(topoSshExecParam);
        try {
         /*   if (topoSshExecParam.getAssetTypeId()==1){*/
                /*reply = sshService.execute(topoSshExecParam);*/
    /*        }else {*/
                reply = javaSshService.execute(topoSshExecParam);
        /*    }*/
            if(null != reply && PaasConstant.RES_SUCCESS == reply.getRes()){
                return setResultSuccess(reply);
            }
        }catch (Throwable throwable){
            log.error("topoSshExecute" ,throwable);
        }

        String message = null;
        if(StringUtils.isNotEmpty(reply.getMsg())){
            message = reply.getMsg();
        }else{
            message = "拓扑执行ssh命令失败";
        }
        return setResultFail(message ,reply);
    }


    @PostMapping("/topology/command/close")
    @ResponseBody
    @ApiOperation(value = "关闭连接接口", tags = "关闭连接接口")
    public ResponseBase topoSshClose(@RequestBody TopoSshExecParam topoSshExecParam) {
        Reply reply = Reply.fail(topoSshExecParam);
       /*     if (topoSshExecParam.getAssetTypeId()==1){
               *//* reply = sshService.execute(topoSshExecParam);*//*
                return setResultSuccess(Reply.ok("成功"));
            }else {*/
                reply = javaSshService.closeRun(topoSshExecParam);
           /* }*/
            if(null != reply && PaasConstant.RES_SUCCESS == reply.getRes()){
                return setResultSuccess(reply);
            }
        return setResultFail("指令失效" ,reply);
    }



    @PostMapping("/topology/command/result/browse")
    @ResponseBody
    @ApiOperation(value = "拓扑查看ssh命令结果", tags = "拓扑查看ssh命令结果")
    public ResponseBase topoSshResult(@RequestBody TopoSshExecParam topoSshExecParam) {
        Reply reply = Reply.fail(topoSshExecParam);;
        try {
          /*  if (topoSshExecParam.getAssetTypeId()==1) {
                reply = sshService.getResponse(topoSshExecParam);
            }else {*/
                reply = javaSshService.getOut(topoSshExecParam);
          /*  }*/
            if(null != reply && PaasConstant.RES_SUCCESS == reply.getRes()){
                return setResultSuccess(reply);
            }
        }catch (Throwable throwable){
            log.error("topoSshResult" ,throwable);
        }

        return setResultFail("拓扑查看ssh命令结果" ,reply);
    }

    @PostMapping("/topology/label/browse")
    @ResponseBody
    @ApiOperation(value = "拓扑标签信息查看", tags = "拓扑标签信息查看")
    public ResponseBase browseTopoLabel(@RequestBody TopoMetaInfoParam param) {
        Reply reply = null;
        try {
            reply = mwtopologyService.browseTopoLabel(param);
        } catch (Throwable e) {
            log.error("browseTopoLabel", e);
        }
        return setResultSuccess(reply.getData());
    }

    @PostMapping("/topology/addTopoToGraphDB")
    @ResponseBody
    @ApiOperation(value = "绑定拓扑到图形数据库", tags = "加入拓扑到图形数据库")
    public ResponseBase addTopoToGraphDB(@RequestBody TopoIndexParam topoIndexParam) {
        Reply reply = null;
        try {
            reply = mwtopologyService.addTopoToGraphDB(topoIndexParam);
        } catch (Throwable e) {
            log.error("addTopoToGraphDB", e);
        }
        return setResultSuccess(reply.getData());
    }

    @PostMapping("/topology/listGraphDB")
    @ResponseBody
    @ApiOperation(value = "查看图形数据库", tags = "查看图形数据库")
    public ResponseBase listTopoToGraphDB() {
        Reply reply = null;
        try {
            reply = mwtopologyService.listTopoToGraphDB();
        } catch (Throwable e) {
            log.error("addTopoToGraphDB", e);
        }
        return setResultSuccess(reply.getData());
    }

    @GetMapping("/topology/getSimplifyLabel")
    @ResponseBody
    @ApiOperation(value = "创建拓扑时，获取资产标签数据", tags = "创建拓扑时，获取资产标签数据")
    public ResponseBase getSimplifyLabel() {
        Reply reply = null;
        try {
            reply = mwtopologyService.getSimplifyLabel();
        } catch (Throwable e) {
            log.error("addTopoToGraphDB", e);
        }
        return setResultSuccess(reply.getData());
    }

    @GetMapping("/topology/getAssetsLabelEnable")
    @ResponseBody
    @ApiOperation(value = "获取当前系统是否支持拓扑获取资产标签")
    public ResponseBase getAssetsLabelEnable() {
        Reply reply = null;
        try {
            reply = Reply.ok(topoLabelEnable);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            return setResultFail(e.getMessage(), "");
        }
        return setResultSuccess(reply);
    }

    @GetMapping("/topology/getAssetsOrgEnable")
    @ResponseBody
    @ApiOperation(value = "获取当前系统是否支持拓扑根据机构创建")
    public ResponseBase getAssetsOrgEnable() {
        Reply reply = null;
        try {
            reply = Reply.ok(topoOrgEnable);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            return setResultFail("信息获取失败", "");
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/topology/assetsLabel/browse")
    @ResponseBody
    @ApiOperation(value = "拓扑资产标签信息查看", tags = "拓扑资产标签信息查看")
    public ResponseBase browseTopoAssetsLabel(@RequestBody TopoMetaInfoParam param) {
        Reply reply = null;
        try {
            reply = mwtopologyService.browseTopoAssetsLabel(param);
        } catch (Throwable e) {
            log.error("browseTopoAssetsLabel", e);
        }
        return setResultSuccess(reply.getData());
    }

    @PostMapping("/topology/line/switch")
    @ResponseBody
    @ApiOperation(value = "是否显示拓扑连线", tags = "连线")
    public ResponseBase showTopoLine(@RequestBody TopoLineSwithParam param ,HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            //设置连线开关
            mwtopologyService.updLineSwitch(param);
            EditorTopoParam editorTopoParam = new EditorTopoParam();
            editorTopoParam.setId(param.getTopoId());
            return editTopology(editorTopoParam ,request ,model);
        } catch (Throwable e) {
            log.error("showTopoLine", e);
        }

        return setResultFail("设置失败" ,param);
    }


    /**
     * 拓扑坐标计算调试
     */
    @MwPermit(moduleName = "topo_manage")
    @PostMapping("/topology/debug/coordinateGenTest")
    @ResponseBody
    @ApiOperation(value = "拓扑坐标计算调试")
    public ResponseBase coordinateGenTest(@RequestBody TopoCoordinateDebugParam param) {
        Reply reply = null;
        try{
            String ret = mwtopologyService.debugCordinate(param);
            return setResultSuccess(ret);
        } catch (Throwable e) {
            log.error("coordinateGenTest", e);
        }

        return setResultFail("调试失败" ,param);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        itemNameMap.put(ZbxConstants.MW_INTERFACE_IN_DROPPED ,"pickLoss");
        itemNameMap.put(ZbxConstants.MW_INTERFACE_OUT_DROPPED ,"sendLoss");
        itemNameMap.put(ZbxConstants.MW_INTERFACE_IN_TRAFFIC ,"in");
        itemNameMap.put(ZbxConstants.MW_INTERFACE_OUT_TRAFFIC ,"out");
        fixedThreadPool = new ThreadPoolExecutor(0, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    private void updateTopoViewInfo(NewTopoView newTopoView){
        //拓扑视图暂存缓存
        log.info("updateTopoViewInfo topoRedisManage save redis");
        topoRedisManage.saveTopoCacheInfo(newTopoView);

        //初始化拓扑index
        int maxIndex = newTopoView.getLatestMaxIndex();
        log.info("updateTopoViewInfo init topo index");
        mwtopologyService.initGraphIndex(newTopoView.getId() ,maxIndex);
    }

}
