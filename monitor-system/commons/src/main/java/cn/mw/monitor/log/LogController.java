package cn.mw.monitor.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.log.param.LogLeverlParam;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequestMapping("/mwapi")
@Slf4j
@Controller
@Api(value = "系统日志文件控制")
public class LogController extends BaseApiService {

    private Pattern pattern = Pattern.compile("^(https{0,1}://.+/mwapi).+$");

    @Value("${scheduling.taskurl}")
    private String taskUrl;

    @Value("${scheduling.enabled}")
    private boolean isTimer;

    @Value("${mwmonitor.systemId}")
    private String systemId;

    @Value("${mwmonitor.timerId}")
    private String timerId;

    @Value("${scheduling.hasTask}")
    private Boolean hasTask;

    @PostMapping(value = "/logLevel")
    @ResponseBody
    public ResponseBase changeLogLevel(@RequestBody LogLeverlParam logLeverlParam) {
        try {
            String[] params = logLeverlParam.getParamString().split(":");
            if(null != params && StringUtils.isNotEmpty(params[0])) {
                    if (systemId.equals(params[0])) {
                        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
                        Logger logger = loggerContext.getLogger("root");
                        logger.setLevel(Level.valueOf(params[1]));
                    } else if (timerId.equals(params[0])) {
                        if (isTimer) {
                            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
                            loggerContext.getLogger("root").setLevel(Level.valueOf(params[1]));
                        } else {
                            Matcher matcher = pattern.matcher(taskUrl);
                            if (matcher.find()) {
                                String taskRequestUrl = matcher.group(1);

                                CloseableHttpClient httpClient = HttpClients.custom().build();
                                if (hasTask) {
                                    String reqUrl = taskRequestUrl + "/logLevel";
                                    String request = JSONObject.toJSONString(logLeverlParam);
                                    HttpUriRequest httpRequest = RequestBuilder.post().setUri(reqUrl)
                                            .addHeader("Content-Type", "application/json")
                                            .setEntity(new StringEntity(request, ContentType.APPLICATION_JSON))
                                            .build();

                                    CloseableHttpResponse response = httpClient.execute(httpRequest);
                                    HttpEntity entity = response.getEntity();
                                    JsonNode result = new ObjectMapper().readTree(entity.getContent());

                                    if (result.has("error")) {
                                        return setResultFail("log level update error!", params[0]);
                                    }
                                }
                            }
                        }

                }
            }
        } catch (Exception e) {
            log.error("动态修改日志级别出错", e);
            return setResultFail("log level update error!",logLeverlParam);

        }

        return setResultSuccess("refresh log level success!");
    }
}
