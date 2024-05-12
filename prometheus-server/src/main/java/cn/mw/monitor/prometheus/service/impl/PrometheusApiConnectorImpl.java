package cn.mw.monitor.prometheus.service.impl;

import cn.mw.monitor.TPServer.dto.MwTPServerDTO;
import cn.mw.monitor.prometheus.constants.QueryConstants;
import cn.mw.monitor.prometheus.service.IPrometheusApiConnector;
import cn.mw.monitor.prometheus.vo.PanelQueryParamVo;
import cn.mw.monitor.prometheus.vo.PrometheusResponseVo;
import cn.mwpaas.common.utils.HttpUtils;
import com.alibaba.fastjson.JSON;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class PrometheusApiConnectorImpl implements IPrometheusApiConnector {

    private MwTPServerDTO mwTPServerDTO;

    private static final String TEMPLATE_NAME = "SqlTemplate";
    private static final int ONE_HOUR_SECONDS = 60 * 60;
    private static final int STEP_PER_HOUR = 14;

    public PrometheusApiConnectorImpl(MwTPServerDTO mwTPServerDTO) {
        this.mwTPServerDTO = mwTPServerDTO;
    }

    @Override
    public PrometheusResponseVo doQuery(PanelQueryParamVo panelQueryParamVo) {
        String url = String.format(Locale.ENGLISH, "%s%s", mwTPServerDTO.getMonitoringServerUrl(), QueryConstants.QueryUrl.QUERY);
        String queryState = fillQueryState(panelQueryParamVo);
        String requestUrl = null;
        try {
            requestUrl = String.format(Locale.ENGLISH, "%s?query=%s", url, URLEncoder.encode(queryState, StandardCharsets.UTF_8.toString()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        log.info("doQuery url:{}",requestUrl);
        String result = HttpUtils.doGet(requestUrl);
        PrometheusResponseVo prometheusResponseVo= JSON.parseObject(result,PrometheusResponseVo.class);
        return prometheusResponseVo;
    }

    @Override
    public PrometheusResponseVo doQueryRange(PanelQueryParamVo panelQueryParamVo) {
        String url = String.format(Locale.ENGLISH, "%s%s", mwTPServerDTO.getMonitoringServerUrl(), QueryConstants.QueryUrl.QUERY_RANGE);
        String queryState = fillQueryState(panelQueryParamVo);
        String requestUrl= null;
        try {
            requestUrl = String.format(Locale.ENGLISH, "%s?query=%s&start=%s&end=%s&step=%s", url, URLEncoder.encode(queryState, StandardCharsets.UTF_8.toString())
                    , panelQueryParamVo.getStart(), panelQueryParamVo.getEnd(), calcStep(panelQueryParamVo.getStart(), panelQueryParamVo.getEnd()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        log.info("doQueryRange url:{}", requestUrl);
        String result = HttpUtils.doGet(requestUrl);
        PrometheusResponseVo prometheusResponseVo = JSON.parseObject(result, PrometheusResponseVo.class);
        return prometheusResponseVo;
    }

    private int calcStep(String start, String end) {
        BigDecimal startTime = new BigDecimal(start);
        BigDecimal endTime = new BigDecimal(end);
        // 按照每小时步进14秒计算
        return endTime.subtract(startTime).divide(new BigDecimal(ONE_HOUR_SECONDS), 2, RoundingMode.DOWN).multiply(new BigDecimal(STEP_PER_HOUR)).intValue();
    }

    /**
     * 填充查询条件
     *
     * @param panelQueryParamVo 查询参数
     * @return 查询语句
     */
    private String fillQueryState(PanelQueryParamVo panelQueryParamVo) {
        Map<String, String> params = new HashMap<>();
        params.put(QueryConstants.QueryParamName.NODE, appendStateFormList(panelQueryParamVo.getNodeList()));
        params.put(QueryConstants.QueryParamName.HOST_IP, appendStateFormList(panelQueryParamVo.getHostIpList()));
        params.put(QueryConstants.QueryParamName.NAMESPACE, appendStateFormList(panelQueryParamVo.getNamespaceList()));
        return fillParams(panelQueryParamVo.getQuery(), params);
    }

    private String appendStateFormList(List<String> stringList) {
        StringBuffer stringBuffer = new StringBuffer();
        if (CollectionUtils.isNotEmpty(stringList)) {
            for (int i = 0; i < stringList.size(); i++) {
                stringBuffer.append(stringList.get(i));
                if (i != stringList.size() - 1) {
                    stringBuffer.append("|");
                }
            }
        } else {
            stringBuffer.append(".*");
        }
        return stringBuffer.toString();
    }

    private String fillParams(String querySql, Map<String, String> params) {
        // 配置模板信息
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_29);
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        stringLoader.putTemplate(TEMPLATE_NAME, querySql);
        configuration.setTemplateLoader(stringLoader);
        // 获取模板
        Template template = null;
        try {
            template = configuration.getTemplate(TEMPLATE_NAME);
            // 填充模板参数
            StringWriter writer = new StringWriter();
            template.process(params, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("fillParams error!", e);
            throw new RuntimeException(e);
        }
    }
}
