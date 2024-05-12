package cn.mw.monitor.api.controller;

import cn.mw.monitor.user.service.MWUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/mwapi")
@Controller
@Slf4j
public class MWTestController {

    @Autowired
    private MWUserService userService;

    @GetMapping("/test")
    public String login() {
        userService.getGlobalUser();
        return "test";
    }

}
