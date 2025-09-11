package com.sunboat.adminWeb.business.controller;

import com.sunboat.common.core.annotation.AntiShake;
import com.sunboat.common.core.annotation.Idempotent;
import com.sunboat.common.core.result.RtnResult;
import com.sunboat.common.core.utils.RedisTemplateUtils;
import com.sunboat.common.core.utils.RedissonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
//
    @Autowired
    private RedissonUtils redissonUtils;

    @Autowired
    private RedisTemplateUtils redisTemplateUtils;

    @GetMapping("/test")
    public List<Map<String, Object>> testDatabaseConnection() {

        return jdbcTemplate.queryForList("SELECT 1");
    }

    @GetMapping("/connectRedis")
    public RtnResult<HashMap<String,Object>> testRedisTemplateUtils() {
        HashMap<String,Object> stringObjectHash = null;
        Integer testNum = redisTemplateUtils.getString("sunboat");
        stringObjectHash = new HashMap<>();
        stringObjectHash.put("sunboat",testNum);
        return RtnResult.success(stringObjectHash);
    }

    @GetMapping("/idempotent")
    @Idempotent(
            key = "#orderId",
            expire = 60,
            message = "该订单已处理，请勿重复回调"
    )
    public RtnResult<String> testIdempotent(String orderId,String userName) {

        try {
            // 让当前线程休眠10秒（10000毫秒）
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // 处理中断异常
            Thread.currentThread().interrupt(); // 恢复中断状态
            e.printStackTrace();
        }

        return RtnResult.success("firstPass");
    }

    /**
     * 确认支付接口（实现防抖）
     * 防抖：防止3秒内重复提交（用户误操作）
     */

    @AntiShake(
            timeout = 3,
            key = "#orderId + '-' + #userId"
    )
    public RtnResult<Void> testAntiShake(String orderId,String userId) {

        return RtnResult.success();
    }


}