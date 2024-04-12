package com.hmall.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

@FeignClient("cart-service") // 根据传入的item-service的服务名去注册中心拉取实例
public interface CartClient {
    @DeleteMapping("/carts")
    public void deleteCartItemByIds(@RequestParam("ids") Collection<Long> ids);
}
