package cn.mw.monitor.api.controller;


import cn.mw.monitor.weixin.dao.MwWeixinTemplateDao;
import cn.mw.monitor.weixin.service.WxPortalService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RequestMapping("/mwapi")
@RestController
@Slf4j
@Api(value = "WeiXinPortal")
public class MWWxPortalController {

    private static final Logger logger = LoggerFactory.getLogger("control-" + MWWxPortalController.class.getName());

    @Autowired
    WxPortalService wxPortalService;

    /**
     *
     * @param signature 微信加密签名   signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
     * @param timestamp 时间戳    注意：微信的时间戳已秒为单位，Java毫秒
     * @param nonce 随机数
     * @param echostr 随机字符串
     * @return
     */
    @GetMapping(value="/weixin/wx",produces = "text/plain;charset=utf-8")
    public String authGet(@RequestParam(name = "signature", required = false) String signature,
                          @RequestParam(name = "timestamp", required = false) String timestamp,
                          @RequestParam(name = "nonce", required = false) String nonce,
                          @RequestParam(name = "echostr", required = false) String echostr) {
        try{
            logger.info("\n接收到来自微信服务器的认证消息：signature = [{}], timestamp = [{}], nonce = [{}], echostr = [{}]",
                    signature, timestamp, nonce, echostr);

            //根据微信提供的规则校验参数
            if (wxPortalService.check(timestamp, nonce, signature)) {
                return echostr;
            }
        }catch (Exception e){
            log.error(e.toString());
        }
        return "非法请求";
    }

    @PostMapping(value="/weixin/wx")
    public String post(@RequestBody String requestBody,
                       @RequestParam(name = "encrypt_type", required = false) String encType,
                       @RequestParam(name = "msg_signature", required = false) String msgSignature,
                       @RequestParam(name = "signature", required = false) String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce) {
        this.logger.info(
                "\n接收到来自微信服务器请求：[signature=[{}], encType=[{}], msgSignature=[{}],"
                        + " timestamp=[{}], nonce=[{}], requestBody=[\n{}\n] ",
                signature, encType, msgSignature, timestamp, nonce, requestBody);

        if (encType == null) {
            // 明文传输的消息
            Map<String, String> map = wxPortalService.parseRequest(requestBody);
            this.logger.info("\n接受到的信息RequestBody封装的map信息是：\n",map);

            String xml = wxPortalService.getRespose(map);

            System.err.println("回应微信服务器最终数据形式：");
            System.err.println(xml);
            return xml;
        } else if ("aes".equals(encType)) {
            // aes加密的消息
            return "";
        }

        return null;
    }




}
