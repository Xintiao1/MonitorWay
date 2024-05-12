package cn.huaxing.controller;

import cn.huaxing.param.DemoParam;
import cn.huaxing.service.TestDemo;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mwpaas.common.model.Reply;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mwapi/huaxing/demo")
@Slf4j
public class DemoController extends BaseApiService {

    @Autowired
    private TestDemo testDemo;
    @PostMapping("/test")
    @ResponseBody
    public ResponseBase test(@RequestBody DemoParam demoParam) {
        testDemo.test();
        return setResultSuccess(Reply.ok());
    }
}
