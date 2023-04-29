package com.alias.openapicommon.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserAuthVO implements Serializable {

    private String accessKey;

    private String secretKey;
}
