package com.zealzhangz.custommavenproperty.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Created by ao.zhang/ao.zhang@iluvatar.ai.<br/>
 * @version Version: 0.0.1
 * @date DateTime: 2019/07/05 19:50:00<br/>
 */
@RestController
public class TestController {
    @Value("${signingKey}")
    private String signingKey;

    @GetMapping("/test")
    public Map<String,String> getSigningKey(){
        Map<String,String> res = new HashMap<>(1);
        res.put("signingKey",signingKey);
        return res;
    }
}
