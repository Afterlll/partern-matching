package com.jxy.usercenter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jxy.usercenter.common.BaseResponse;
import com.jxy.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 *
 * @author 江喜原
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    List<User> searchUserByTags(List<String> tagNameList);

    boolean isAdmin(HttpServletRequest request);

    int updateUserInfo(User user, HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    IPage<User> recommendUsers(long pageCurrent, long pageSize, HttpServletRequest request);

    /**
     * 获取用户推荐缓存可以
     * @param userId
     * @return
     */
    String getRecommendKey(long userId);

}
