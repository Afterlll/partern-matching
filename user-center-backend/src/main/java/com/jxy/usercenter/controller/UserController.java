package com.jxy.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jxy.usercenter.common.BaseResponse;
import com.jxy.usercenter.common.ErrorCode;
import com.jxy.usercenter.common.ResultUtils;
import com.jxy.usercenter.exception.BusinessException;
import com.jxy.usercenter.model.domain.User;
import com.jxy.usercenter.model.domain.request.UserLoginRequest;
import com.jxy.usercenter.model.domain.request.UserRegisterRequest;
import com.jxy.usercenter.service.UserService;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.jxy.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.jxy.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author 江喜原
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true", originPatterns = {"http://localhost:5173"})
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 校验
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户
     *
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        // TODO 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "缺少管理员权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    /**
     * 推荐用户
     * @param pageCurrent 请求第几页的数据
     * @param pageSize 一页多少条数据
     * @param request
     * @return
     */
    @GetMapping("/recommend")
    public BaseResponse<IPage<User>> recommendUsers(long pageCurrent, long pageSize, HttpServletRequest request) {
        return ResultUtils.success(userService.recommendUsers(pageCurrent, pageSize, request));
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }



    /**
     * 根据标签列表搜索用户
     *
     * @param tagNameList
     * @return
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList) {
        return ResultUtils.success(userService.searchUserByTags(tagNameList));
    }

    /**
     * 更改用户信息
     * @param user
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUserInfo(@RequestBody User user, HttpServletRequest request) {
        if (user == null || user.getId() == null || user.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.getLoginUser(request);
        return ResultUtils.success(userService.updateUserInfo(user, request));
    }

}
