package cn.mw.monitor.assetsSubType.service.impl;

import cn.mw.monitor.service.dropdown.param.DropdownDTO;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.assetsSubType.dto.TypeTreeDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.assetsSubType.api.param.AssetsSubType.QueryAssetsSubTypeParam;
import cn.mw.monitor.assetsSubType.dao.MwAssetsSubTypeTableDao;
import cn.mw.monitor.assetsSubType.model.MwAssetsGroupTable;
import cn.mw.monitor.assetsSubType.model.MwAssetsSubTypeTable;
import cn.mw.monitor.assetsSubType.model.MwAssetsSubTypeTableParam;
import cn.mw.monitor.assetsSubType.service.MwAssetsSubTypeService;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWTPServerProxy;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mw.zbx.MWZabbixApi;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by baochengbin on 2020/3/17.
 */
@Service
@Slf4j
@Transactional
@Component
public class MwAssetsSubTypeServiceImpl implements MwAssetsSubTypeService {

    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/AssetsSubType");

    @Autowired
    private MwAssetsSubTypeTableParam basePids;

    //    @Autowired
    private MWZabbixApi mwZabbixApi;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Resource
    private MwAssetsSubTypeTableDao mwAssetssubtypeTableDao;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    /**
     * 根据资产ID取资产信息
     *
     * @param id 自增序列ID
     * @return
     */
    @Override
    public Reply selectById(Integer id) {
        try {
            MwAssetsSubTypeTable mwAssetssubtypeTable = mwAssetssubtypeTableDao.selectById(id);

            logger.info("AssetsSubType_LOG[]AssetsSubType[]子资产类型管理[]根据自增序列ID取子资产类型[]{}", id);
            return Reply.ok(mwAssetssubtypeTable);
        } catch (Exception e) {
            log.error("fail to selectById with id={}, cause:{}", id, e.getMessage());
            return Reply.fail(ErrorConstant.ASSETSSUBTEOYCODE_270101, ErrorConstant.ASSETSSUBTEOY_MSG_270101);
        }
    }

    /**
     * 分页查询子资产类型
     *
     * @param qsParam
     * @return
     */
    @Override
    public Reply selectList(QueryAssetsSubTypeParam qsParam) {
        try {

            List<MwAssetsSubTypeTable> mwScanList = mwAssetssubtypeTableDao.selectList(qsParam);

            logger.info("AssetsSubType_LOG[]AssetsSubType[]子资产分类管理[]分页查询子资产分类信息[]{}[]", qsParam);

            return Reply.ok(mwScanList);

        } catch (Exception e) {
            log.error("fail to selectListAssetsSubType with qsParam={}, cause:{}", qsParam, e.getMessage());
            return Reply.fail(ErrorConstant.ASSETSSUBTEOYCODE_270104, ErrorConstant.ASSETSSUBTEOY_MSG_270104);
        }
    }


    /**
     * 更新分类信息
     *
     * @param auParam
     * @return
     */
    @Override
    public Reply update(MwAssetsSubTypeTable auParam) {
        try {
            auParam.setModifier(iLoginCacheInfo.getLoginName());
            Integer id = auParam.getId();
            String str = basePids.getIds();
            String[] strs = str.split(",");
            //基本16主机组不能修改和删除
            int[] ints = Arrays.stream(strs).mapToInt(Integer::parseInt).toArray();

            boolean flag = true;
            for (int i : ints) {
                if (i == id) {
                    flag = false;
                }
            }
            if (flag) {
                //前端未传pid,自己去数据库查；
                Integer di = auParam.getId();
                MwAssetsSubTypeTable dip = mwAssetssubtypeTableDao.selectById(di);
                List<MwAssetsGroupTable> groupTables = mwAssetssubtypeTableDao.selectGroupServerMap(dip.getId());
                if (groupTables != null && groupTables.size() > 0) {
                    for (MwAssetsGroupTable groupTable : groupTables) {
                        Integer pid = dip.getPid();
                        if (0 == pid) {
                            Integer groupid = Integer.parseInt(groupTable.getGroupId());
                            String name = auParam.getTypeName();

                            //修改之前查看数据是否存在
                            List<String> idlists = new ArrayList<>();
                            idlists.add(String.valueOf(groupid));
                            MWZabbixAPIResult resultData = mwtpServerAPI.hostgroupGetById(groupTable.getMonitorServerId(), idlists);
                            List<String> retunnIdlists = new ArrayList<>();
                            if (0 == resultData.code) {
                                JsonNode map = (JsonNode) resultData.getData();
                                if (map.size() > 0) {
                                    map.forEach(data -> {
                                        JsonNode a = data.get("groupid");
                                        String s = a.textValue();
                                        retunnIdlists.add(s);
                                    });
                                }
                                //如果进入到下面的if,说明Zabbix存在主机组数据，则调用修改方法，不存在则只修改本模块数据。
                                if (groupid == Integer.parseInt(retunnIdlists.get(0))) {
                                    MWZabbixAPIResult resultData2 = mwtpServerAPI.hostgroupUpdate(groupTable.getMonitorServerId(), String.valueOf(groupid), "[分组]" + name);
                                    List<String> retunnIdlists2 = new ArrayList<>();
                                    if (0 == resultData2.getCode()) {
                                        JsonNode map2 = (JsonNode) resultData.getData();
                                        if (map2.size() > 0) {
                                            map2.forEach(data -> {
                                                JsonNode a = data.get("groupid");
                                                String s = a.textValue();
                                                retunnIdlists2.add(s);
                                            });
                                        }
                                        if (groupid == Integer.parseInt(retunnIdlists2.get(0))) {
                                            //修改成功，接着修改本模块数据
                                            mwAssetssubtypeTableDao.updateById(auParam);
                                        }
                                    }
                                } else {
                                    mwAssetssubtypeTableDao.updateById(auParam);
                                }
                            }
                        } else {
                            mwAssetssubtypeTableDao.updateById(auParam);
                        }
                    }
                } else {
                    mwAssetssubtypeTableDao.updateById(auParam);
                }
            } else {
                mwAssetssubtypeTableDao.updateById(auParam);
            }
            return Reply.ok("更新成功！");
        } catch (Exception e) {
            log.error("fail to updateAssetsSubType with auParam={}, cause:{}", auParam, e.getMessage());
            return Reply.fail(ErrorConstant.ASSETSSUBTEOYCODE_270103, ErrorConstant.ASSETSSUBTEOY_MSG_270103);
        }
    }

    /**
     * 新增分类信息
     *
     * @param auParam
     * @return
     */
    @Override
    public Reply insert(MwAssetsSubTypeTable auParam) {
        try {
            auParam.setCreator(iLoginCacheInfo.getLoginName());
            auParam.setModifier(iLoginCacheInfo.getLoginName());

            List<Integer> autoIncrment = mwAssetssubtypeTableDao.selectAutoIncrment();

            Integer pid = auParam.getPid();
            if (0 == pid) {
                auParam.setNodes(",0," + autoIncrment.get(0).toString() + ',');
            } else {
                MwAssetsSubTypeTable mwAssetsSubTypeTable = mwAssetssubtypeTableDao.selectById(auParam.getPid());
                auParam.setNodes(mwAssetsSubTypeTable.getNodes() + autoIncrment.get(0).toString() + ',');
                mwAssetssubtypeTableDao.insert(auParam);
            }

            //如果pid==0调用Zabbix接口创建主机组，并将返回的groupids保存到对象里.
            //新增多Zabbix的添加保存
            List<MwAssetsGroupTable> groupTables = new ArrayList<>();
            if (0 == pid) {
                mwAssetssubtypeTableDao.insert(auParam);
                List<MWTPServerAPI> mwtpServerAPIS = MWTPServerProxy.getMWTPServerAPIList();
                for (MWTPServerAPI mwtpServerAPI : mwtpServerAPIS) {
                    MWZabbixAPIResult resultData = mwtpServerAPI.hostgroupCreate(mwtpServerAPI.getServerId(), "[分组]" + auParam.getTypeName());
                    if (resultData.getCode() == 0) {
                        JsonNode node = (JsonNode) resultData.getData();
                        if (node.size() > 0) {
                            String groupid = "";
                            if (node.size() > 0) {
                                JsonNode a2 = node.get("groupids");
                                groupid = a2.get(0).asText();
                            }
                            MwAssetsGroupTable groupTable = new MwAssetsGroupTable();
                            groupTable.setAssetsSubtypeId(autoIncrment.get(0));
                            groupTable.setMonitorServerId(mwtpServerAPI.getServerId());
                            groupTable.setGroupId(groupid);
                            groupTables.add(groupTable);
                        } else {
                            throw new Exception("创建主机群组失败：" + mwtpServerAPI.getServerId());
                        }
                    }
                }
            }

            //建立gorupid与多Zabbix对应关系
            if (groupTables.size() > 0) {
                mwAssetssubtypeTableDao.batCreateGroupServerMap(groupTables);
            }

            return Reply.ok("新增成功！");
        } catch (Exception e) {
            log.error("fail to insertAssetsSubType with auParam={}, cause:{}", auParam, e.getMessage());
            return Reply.fail(ErrorConstant.ASSETSSUBTEOYCODE_270102, ErrorConstant.ASSETSSUBTEOY_MSG_270102);
        }
    }

    /**
     * 删除分类信息
     *
     * @param ids
     * @return
     */
    @Override
    public Reply delete(List<Integer> ids) {
        try {
            String str = basePids.getIds();
            String[] strs = str.split(",");
            //基本16主机组不能修改和删除
            int[] ints = Arrays.stream(strs).mapToInt(Integer::parseInt).toArray();

            List<Integer> idList = new ArrayList<>();
            for (Integer i : ids) {
                boolean flag = true;
                for (int d : ints) {
                    if (d == i) {
                        flag = false;
                    }
                }
                if (flag) {
                    MwAssetsSubTypeTable mwAssetssubtypeTable = mwAssetssubtypeTableDao.selectById(i);
                    List<MwAssetsGroupTable> groupTables = mwAssetssubtypeTableDao.selectGroupServerMap(i);
                    if (0 == mwAssetssubtypeTable.getPid()) {
                        //判断该类型是否有资产使用，有的话则不删除，
                        List<Integer> typeIds = mwAssetssubtypeTableDao.getAssetsUseType();
                        int id = mwAssetssubtypeTable.getId();
                        if (typeIds.contains(id)) {
                            return Reply.fail("该类型已被资产使用不允许删除");
                        }


                        if (groupTables == null || groupTables.size() == 0) {
                            ArrayList<Integer> b = new ArrayList<>();
                            b.add(i);
                            mwAssetssubtypeTableDao.delete(b);
                        }

                        for (MwAssetsGroupTable groupTable : groupTables) {
                            List<String> groupisList = new ArrayList<>();
                            groupisList.add(groupTable.getGroupId());
                            MWZabbixAPIResult resultData = mwtpServerAPI.hostgroupGetById(groupTable.getMonitorServerId(), groupisList);
                            List<String> retunnIdlists = new ArrayList<>();
                            if (0 == resultData.code) {
                                JsonNode map = (JsonNode) resultData.getData();
                                System.err.println(map);
                                if (map.size() > 0) {
                                    map.forEach(data -> {
                                        JsonNode a = data.get("groupid");
                                        String s = a.textValue();
                                        retunnIdlists.add(s);
                                    });
                                }
                            }

                            //如果进入下面方法说明Zabbix存在该主机组可删除
                            if (retunnIdlists.get(0).equals(groupTable.getGroupId())) {
                                ArrayList<String> a = new ArrayList<>();
                                a.add(String.valueOf(groupTable.getGroupId()));
                                MWZabbixAPIResult resultData2 = mwtpServerAPI.hostgroupDelete(groupTable.getMonitorServerId(), a);
                                //删除本模块的数据
                                if (0 == resultData2.code) {
                                    ArrayList<Integer> b = new ArrayList<>();
                                    b.add(i);
                                    mwAssetssubtypeTableDao.delete(b);
                                }
                            }
                        }

                        //删除关系映射表
                        if (groupTables != null && groupTables.size() > 0) {
                            mwAssetssubtypeTableDao.deleteGroupIds(groupTables);
                        }
                    } else {
                        List<Integer> typeIds = mwAssetssubtypeTableDao.getAssetsUseSubType();
                        int id = mwAssetssubtypeTable.getId();
                        if (typeIds.contains(id)) {
                            return Reply.fail("该类型已被资产使用不允许删除");
                        }
                        idList.add(i);
                        mwAssetssubtypeTableDao.delete(idList);
                    }
                } else {
                    return Reply.fail("该类型不允许删除");
                }
            }
            return Reply.ok("删除成功");
        } catch (Exception e) {
            log.error("fail to deleteAssetsSubType with ids={}, cause:{}", ids, e.getMessage());
            return Reply.fail(ErrorConstant.ASSETSSUBTEOYCODE_270105, ErrorConstant.ASSETSSUBTEOY_MSG_270105);
        }
    }

    @Override
    public Reply selectDorpdownList(QueryAssetsSubTypeParam qsParam) {
        try {
            List<MwAssetsSubTypeTable> mworges = mwAssetssubtypeTableDao.selectList(qsParam);
            logger.info("ACCESS_LOG[]org[]子分类信息[]查询子分类信息[]{}[]");
            return Reply.ok(mworges);
        } catch (Exception e) {
            log.error("fail to selectDorpdownList , cause:{}", null, e.getMessage());
            return Reply.fail(ErrorConstant.ASSETSSUBTEOYCODE_270104, ErrorConstant.ASSETSSUBTEOY_MSG_270104);
        }
    }

    @Override
    public Reply selectGroupServerMapList() {
        List<MwAssetsGroupTable> mwAssetsGroupTables = mwAssetssubtypeTableDao.selectGroupServerMap(null);
        logger.info("selectGroupServerMapList");
        return Reply.ok(mwAssetsGroupTables);
    }

    @Override
    public Reply selectTypeTrees(TypeTreeDTO typeTreeDTO) {
        List<TypeTreeDTO> typeTreeDTOS = mwAssetssubtypeTableDao.selectTypeTrees(typeTreeDTO);
        if (typeTreeDTOS.size() > 0) {
            TypeTreeDTO treeDTO = new TypeTreeDTO();
            for (int i = 0; i < typeTreeDTOS.size(); i++) {
                treeDTO.setPid(typeTreeDTOS.get(i).getId());
                List<TypeTreeDTO> child = mwAssetssubtypeTableDao.selectTypeTrees(treeDTO);
                typeTreeDTOS.get(i).setChildren(child);
            }
        }
        logger.info("selectTypeTrees");
        return Reply.ok(typeTreeDTOS);
    }

    @Override
    public Reply selectDorpdownList(boolean subTypeFlag, Integer classify) {
        try {
            List<DropdownDTO> list = new ArrayList<>();
            List<DropdownDTO> dtos = mwAssetssubtypeTableDao.selectTypeList(0, classify);
            if (subTypeFlag) {
                if (dtos != null && dtos.size() > 0) {
                    for (DropdownDTO pid : dtos) {
                        list.addAll(mwAssetssubtypeTableDao.selectTypeList(pid.getDropKey(), classify));
                    }
                }
            } else {
                list = dtos;
            }
            logger.info("ACCESS_LOG[]org[]子分类信息[]查询子分类信息[]{}[]");
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to selectDorpdownList , cause:{}", null, e.getMessage());
            return Reply.fail(ErrorConstant.ASSETSSUBTEOYCODE_270104, ErrorConstant.ASSETSSUBTEOY_MSG_270104);
        }
    }

    public Reply updateAssetsGroupId() {
        try {
            //1: 清空映射表
            mwAssetssubtypeTableDao.cleanGroupServerMap();

            //2： 查询多Zabbix和资产类型类型表
            List<MwAssetsSubTypeTable> groupNames = mwAssetssubtypeTableDao.selectGroupNames();
            List<MWTPServerAPI> mwtpServerAPIS = MWTPServerProxy.getMWTPServerAPIList();
            List<MwAssetsGroupTable> groupServerTables = new ArrayList<>();
            Boolean flag = false;

            //3 ：根据资产类型表中的类型名称取对应Zabbix中查询对应的groupid来保存
            if (groupNames.size() > 0 && mwtpServerAPIS.size() > 0) {
                //将数据加入线程池中
                int threadSize = 5;
                ExecutorService executorService = Executors.newFixedThreadPool(threadSize);
                List<Future<Boolean>> futureList = new ArrayList<>();
                for (MWTPServerAPI mwtpServerAPI : mwtpServerAPIS) {
                    groupNames.forEach(groupName -> {
                        MwSaveGroupIdMap item = new MwSaveGroupIdMap(mwtpServerAPI, groupName.getTypeName(), groupName.getId(), groupServerTables);
                        Future<Boolean> f = executorService.submit(item);
                        futureList.add(f);
                    });
                }

                //判断有无失败的子线程
                for (Future<Boolean> f : futureList) {
                    try {
                        //是否出错
                        Boolean aBoolean = f.get(10, TimeUnit.SECONDS);
                        if (aBoolean) {
                            flag = true;
                        }
                    } catch (Exception e) {
                        log.error("updateGroupId", e);
                        f.cancel(true);
                    }
                }
                executorService.shutdown();
            }

            //保存映射数据
            if (groupServerTables.size() > 0) {
                mwAssetssubtypeTableDao.insertBatchGroupServerMap(groupServerTables);
            }

            if (!flag) {
                return Reply.ok("修改成功");
            } else {
                return Reply.fail("修改失败");
            }
        } catch (Exception e) {
            log.error("fail to updateAssetsGroupId, cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.ASSETSSUBTEOYCODE_270106, ErrorConstant.ASSETSSUBTEOY_MSG_270106);
        }
    }

}
