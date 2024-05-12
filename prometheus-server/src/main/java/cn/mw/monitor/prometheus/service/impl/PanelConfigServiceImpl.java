package cn.mw.monitor.prometheus.service.impl;

import cn.mw.monitor.prometheus.dao.*;
import cn.mw.monitor.prometheus.dto.LayoutConfigDto;
import cn.mw.monitor.prometheus.dto.PanelConfigDto;
import cn.mw.monitor.prometheus.dto.PanelQueryRelDto;
import cn.mw.monitor.prometheus.dto.QueryConfigDto;
import cn.mw.monitor.prometheus.service.IPanelConfigService;
import cn.mw.monitor.prometheus.utils.PrometheusApiConnectorFactory;
import cn.mw.monitor.prometheus.vo.PanelQueryParamVo;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PanelConfigServiceImpl implements IPanelConfigService {

    @Resource
    private PanelConfigDao panelConfigDao;

    @Resource
    private QueryConfigDao queryConfigDao;

    @Resource
    private LayoutConfigDao layoutConfigDao;

    @Resource
    private PanelQueryRelDao panelQueryRelDao;

    @Resource
    private PrometheusPropertyDao prometheusPropertyDao;

    @Override
    public Reply getAllPanelConfigs(Integer userId) {
        // 查询所有布局
        List<LayoutConfigDto> layoutConfigDtoList = layoutConfigDao.getAllLayoutConfigsByUserId(userId);
        if (!layoutConfigDtoList.isEmpty()) {
            List<Integer> layoutIds = layoutConfigDtoList.stream().map(LayoutConfigDto::getId).collect(Collectors.toList());
            List<PanelConfigDto> panelConfigDtoList = panelConfigDao.getAllPanelConfigsByLayoutIds(layoutIds);
            if (!panelConfigDtoList.isEmpty()) {
                List<Integer> panelIds = panelConfigDtoList.stream().map(PanelConfigDto::getId).collect(Collectors.toList());
                List<PanelQueryRelDto> panelQueryRelDtoList = panelQueryRelDao.getPanelQueryRelListByPanelIds(panelIds);
                Map<Integer, List<PanelQueryRelDto>> panelQueryRelMap = panelQueryRelDtoList.stream().collect(Collectors.groupingBy(PanelQueryRelDto::getPanelId));
                panelConfigDtoList.forEach(item -> {
                    if (panelQueryRelMap.containsKey(item.getId())) {
                        item.setColumnList(panelQueryRelMap.get(item.getId()));
                    }
                });
                Map<Integer, List<PanelConfigDto>> layoutPanelMap = panelConfigDtoList.stream().collect(Collectors.groupingBy(PanelConfigDto::getLayoutId));
                for (LayoutConfigDto layoutConfigDto : layoutConfigDtoList) {
                    if (layoutPanelMap.containsKey(layoutConfigDto.getId())) {
                        layoutConfigDto.setPanelConfigDtoList(layoutPanelMap.get(layoutConfigDto.getId()));
                    }
                }
            }
        }
        return Reply.ok(layoutConfigDtoList);
    }

    @Override
    public Reply getPanelData(PanelQueryParamVo panelQueryParamVo) throws Exception {
        PrometheusApiConnectorImpl prometheusApiConnector = PrometheusApiConnectorFactory.createConnector(panelQueryParamVo.getServiceId());
        if (prometheusApiConnector != null) {
            if (panelQueryParamVo.isQueryRange()) {
                return Reply.ok(prometheusApiConnector.doQueryRange(panelQueryParamVo));
            } else {
                return Reply.ok(prometheusApiConnector.doQuery(panelQueryParamVo));
            }
        }
        return Reply.fail("prometheusApiConnector is null!");
    }

    @Override
    @Transactional
    public Reply insertPanelData(PanelConfigDto panelConfigDto) {
        if (CollectionUtils.isNotEmpty(panelConfigDto.getColumnList())) {
            panelQueryRelDao.batchInsertPanelQueryRelList(panelConfigDto.getColumnList());
        }
        return Reply.ok(panelConfigDao.insertPanelConfig(panelConfigDto));
    }

    @Override
    public Reply updatePanelData(PanelConfigDto panelConfigDto) {
        return Reply.ok(panelConfigDao.updatePanelConfig(panelConfigDto));
    }

    @Override
    public Reply deletePanelData(Integer panelId) {
        return Reply.ok(panelConfigDao.deletePanelConfig(panelId));
    }

    @Override
    @Transactional
    public Reply insertLayoutConfig(LayoutConfigDto layoutConfigDto) {
        if (layoutConfigDto.getId() != null) {
            deleteLayoutConfig(layoutConfigDto.getId());
        }
        layoutConfigDao.insertLayoutConfig(layoutConfigDto);
        if (CollectionUtils.isNotEmpty(layoutConfigDto.getPanelConfigDtoList())) {
            layoutConfigDto.getPanelConfigDtoList().forEach(item -> {
                item.setLayoutId(layoutConfigDto.getId());
                item.setCreator(layoutConfigDto.getCreator());
                panelConfigDao.insertPanelConfig(item);
                if (CollectionUtils.isNotEmpty(item.getColumnList())) {
                    item.getColumnList().forEach(col -> col.setPanelId(item.getId()));
                    panelQueryRelDao.batchInsertPanelQueryRelList(item.getColumnList());
                }
            });
        }
        return Reply.ok(layoutConfigDto.getId());
    }

    @Override
    public Reply getAllQuerySql() {
        return Reply.ok(queryConfigDao.getAllQueryConfigs());
    }

    @Override
    public Reply getAllPrometheusProperties() {
        return Reply.ok(prometheusPropertyDao.getAllPrometheusProperties());
    }

    @Override
    @Transactional
    public Reply deleteLayoutConfig(Integer id) {
        panelConfigDao.deletePanelConfigByLayoutId(id);
        panelQueryRelDao.deletePanelQueryRelByLayoutId(id);
        return Reply.ok(layoutConfigDao.deleteLayoutConfig(id));
    }
}
