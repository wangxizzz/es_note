package com.example.highlevelclient.entity;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class User {
    private Long userId;
    private String username;
    private String city;
    private String userSex;
    private int userAge;
    private Date birthday;
    private List<Long> crowdIds;
}
