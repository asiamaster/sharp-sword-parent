package com.dili.ss.idempotent.controller;

import com.dili.ss.idempotent.dto.TokenPair;
import com.dili.ss.idempotent.service.IdempotentTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.*;

/**
 * 幂等token控制器
 */
@RestController
@RequestMapping("/idempotentToken")
@ConditionalOnExpression("'${idempotent.enable}'=='true'")
public class IdempotentTokenController {
//    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);
    @Autowired
    private IdempotentTokenService idempotentTokenService;

    /**
     * 根据url获取TokenPair
     * @param url
     * @return
     */
    @ResponseBody
    @GetMapping("/getToken.api")
    public TokenPair getToken(@RequestParam("url") String url) {
        return idempotentTokenService.getToken(url);
    }


}