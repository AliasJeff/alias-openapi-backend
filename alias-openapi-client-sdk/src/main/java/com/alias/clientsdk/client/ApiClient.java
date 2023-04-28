package com.alias.clientsdk.client;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alias.clientsdk.model.Api;

import java.util.HashMap;
import java.util.Map;

import static com.alias.clientsdk.utils.SignUtils.genSign;

public class ApiClient {

    private Integer appId;

    private String accessKey;

    private String secretKey;

    private String url = "http://localhost:8090/api/main";

    public ApiClient() {
    }

    public ApiClient(Integer appId, String accessKey, String secretKey) {
        this.appId = appId;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public String getResult(Api api) {
        String json = JSONUtil.toJsonStr(api.getBody());
        if ("get".equals(api.getMethod()) || "GET".equals(api.getMethod())) {
            return HttpRequest.get(url)
                    .header("Accept", "application/json;charset=UTF-8")
                    .addHeaders(getHeaders(api.getUrl(), api.getInterfaceId(), json, api.getId(), api.getAccount()))
                    .charset("UTF-8")
                    .body(json)
                    .execute().body();
        } else {
            return HttpRequest.post(url)
                    .header("Accept", "application/json;charset=UTF-8")
                    .addHeaders(getHeaders(api.getUrl(), api.getInterfaceId(), json, api.getId(), api.getAccount()))
                    .charset("UTF-8")
                    .body(json)
                    .execute().body();
        }
    }

    private Map<String, String> getHeaders(String url, String interfaceId, String body, Long userId, String account) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", String.valueOf(userId));
        map.put("account", account);
        map.put("interfaceId", interfaceId);
        map.put("url", url);
        map.put("appId", String.valueOf(appId));
        map.put("accessKey", accessKey);
//        map.put("secretKey", secretKey);
        map.put("body", body);
        map.put("timestamp", String.valueOf(DateUtil.date(System.currentTimeMillis())));
        map.put("nonce", RandomUtil.randomNumbers(4));
        map.put("sign", genSign(body, secretKey));
        return map;
    }

}
