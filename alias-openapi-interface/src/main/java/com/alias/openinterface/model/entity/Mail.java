package com.alias.openinterface.model.entity;

import lombok.Data;

@Data
public class Mail {

    private String fromMail;

    private String toMail;

    private String content;
}
