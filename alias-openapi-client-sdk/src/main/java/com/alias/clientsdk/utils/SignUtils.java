package com.alias.clientsdk.utils;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 签名工具
 */
@Slf4j
public class SignUtils {
    /**
     * 生成签名
     * @param body
     * @param secretKey
     * @return
     */
    public static String genSign(String body, String secretKey) {
        Digester md5 = new Digester(DigestAlgorithm.SHA256);
        String content;
        if (!StringUtils.isAnyBlank(body)) {
            content = body + secretKey;
        } else {
            content = "blank_value" + secretKey;
        }
        log.info("genSign...body: {}", body);
        log.info("genSign...content: {}", content);
        return md5.digestHex(content);
    }
}
