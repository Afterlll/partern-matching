package com.jxy.usercenter.model.request;

import com.jxy.usercenter.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 加入队伍请求类
 */
@Data
public class TeamJoinRequest implements Serializable {
    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * team id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;

}