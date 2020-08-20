package com.dbapp.app.ai.rpc;

import com.dbapp.app.ai.api.AiApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Auther: shawn.xie
 * @Date: 2020/8/19 17:00
 * @Description:
 */
@FeignClient(name = "ai-app",path = "/ai")
public interface AiClient extends AiApi {
}
