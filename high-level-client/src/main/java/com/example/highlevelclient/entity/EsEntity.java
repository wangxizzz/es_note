package com.example.highlevelclient.entity;

import lombok.Data;

@Data
public class EsEntity<T> {
    private String id;
    private T data;
}
