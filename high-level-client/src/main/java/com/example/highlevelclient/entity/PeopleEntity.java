package com.example.highlevelclient.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeopleEntity {
    private Integer crowdId;
    private Integer userId;
    private String name;
    private String city;
    private String userSex;
}
