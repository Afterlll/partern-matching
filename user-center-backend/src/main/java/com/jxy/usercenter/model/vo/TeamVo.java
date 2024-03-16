package com.jxy.usercenter.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.jxy.usercenter.model.domain.User;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 队伍响应给前端字段
 */
@Data
public class TeamVo implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 队长 id
     */
    private Long teamCaptainId;

    /*
    * 是否在队伍中
     */
    private Boolean hasJoin;

    /**
     * 队伍成员
     */
    private List<User> teamMember;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
