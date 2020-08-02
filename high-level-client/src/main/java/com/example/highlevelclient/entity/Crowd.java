package com.example.highlevelclient.entity;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 *
 * @author wangxi created on 2020/8/2 3:07 PM
 * @version v1.0
 */
@Data
@Builder
public class Crowd {
    private Long crowdId;
    private Long userId;

    private Date insertTime;

    public String getId() {
        return crowdId + "_" + userId;
    }
}
