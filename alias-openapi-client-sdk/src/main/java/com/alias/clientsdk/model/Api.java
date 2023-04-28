package com.alias.clientsdk.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Api implements Serializable {

    /**
     * 用户id
     */
    Long id;

    /**
     * 用户账号
     */
    String account;

    /**
     * 接口id
     */
    String interfaceId;

    /**
     * 接口地址
     */
    String url;

    /**
     * 请求体
     */
    Object body;

    /**
     * 请求方法
     */
    String method;
}
