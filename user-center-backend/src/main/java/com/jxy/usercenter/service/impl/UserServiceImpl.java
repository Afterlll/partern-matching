package com.jxy.usercenter.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxy.usercenter.common.BaseResponse;
import com.jxy.usercenter.common.ErrorCode;
import com.jxy.usercenter.common.ResultUtils;
import com.jxy.usercenter.contant.UserConstant;
import com.jxy.usercenter.exception.BusinessException;
import com.jxy.usercenter.model.domain.User;
import com.jxy.usercenter.service.UserService;
import com.jxy.usercenter.mapper.UserMapper;
import com.jxy.usercenter.utils.DistinctUtil;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.swing.event.MenuKeyListener;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.jxy.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.jxy.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author 江喜原
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "jxy";

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return 新用户 id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户（SQL查询）
     * @param tagNameList
     * @return
     */
    @Deprecated
    public List<User> searchUserBySQL(List<String> tagNameList) {
        if (CollUtil.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String tag : tagNameList) {
            queryWrapper.like("tags", tag);
        }
        return userMapper.selectList(queryWrapper).stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 根据标签搜索用户（内存过滤）
     *
     * @param tagNameList
     * @return
     */
    public List<User> searchUserByTags(List<String> tagNameList) {
        Set<String> tagSet = new HashSet<>(tagNameList);

        // 1. 先查询出所有的用户
        List<User> userList = userMapper.selectList(null);
        // 2. 进行内存查询
        return userList.stream().filter(user -> {
            // 根据标签进行过滤
            String tags = user.getTags();
            if (StrUtil.isBlank(tags)) {
                return false;
            }
            JSONArray jsonArray = JSONUtil.parseArray(tags);
            List<String> tagList = JSONUtil.toList(jsonArray, String.class);
            for (String tag : tagList) {
                if (tagSet.contains(tag)) {
                    return true;
                }
            }
            return false;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 获取登录用户信息
     */
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }

    @Override
    public IPage<User> recommendUsers(long pageCurrent, long pageSize, HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        // 1. 缓存命中
        String recommendKey = getRecommendKey(loginUser.getId());
        String redisValue = stringRedisTemplate.opsForValue().get(recommendKey);
        if (StrUtil.isNotBlank(redisValue)) {
            return new Page<User>().setRecords(JSONUtil.toBean(redisValue, new TypeReference<List<User>>(){}, false));
        }
        // 2， 缓存未命中
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        IPage<User> userList = page(new Page<>(pageCurrent, pageSize), queryWrapper);
        List<User> list = userList.getRecords().stream().map(this::getSafetyUser).collect(Collectors.toList());
        // 异步写入缓存
        CompletableFuture.runAsync(() -> {
            stringRedisTemplate.opsForValue().set(recommendKey, JSONUtil.toJsonStr(list), 1, TimeUnit.DAYS);
        });
        return new Page<User>().setRecords(list);
    }

    /**
     * 获取推荐用户的缓存 key
     *
     * @param userId
     * @return
     */
    public String getRecommendKey(long userId) {
        return String.format("partner:matching:user:recommend:%s", userId);
    }

    @Override
    public List<User> matchUsers(long num, HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        List<String> loginUserTagList = JSONUtil.toBean(loginUser.getTags(), new TypeReference<List<String>>() {
        }, false);
        // 大根堆，每次剔除编辑距离较大的值
        PriorityQueue<Pair<Long, Integer>> usersQueue = new PriorityQueue<>((int) num, (o1, o2) -> o2.getValue() - o1.getValue());
        // 查询出所有的用户列表
        List<User> userList = list(new QueryWrapper<User>().select("id", "tags").ne("tags", "[]").ne("id", loginUser.getId()));
        for (User user : userList) {
            int distance = DistinctUtil.minDistanceTags(loginUserTagList,
                    JSONUtil.toBean(user.getTags(), new TypeReference<List<String>>() {}, false));
            if (usersQueue.size() < num) {
                usersQueue.add(new Pair<>(user.getId(), distance));
            } else if (usersQueue.element().getValue() > distance) {
                usersQueue.remove();
                usersQueue.add(new Pair<>(user.getId(), distance));
            }
        }
        List<Long> ids = new ArrayList<>();
        while (!usersQueue.isEmpty()) {
            ids.add(usersQueue.remove().getKey());
        }
        Collections.reverse(ids);
        /*
        SELECT * FROM your_table
        WHERE id IN (1, 3, 2)
        ORDER BY FIELD(id, 1, 3, 2);
        使用以上SQL语句保证从数据库查出来的数据不被打乱
         */
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        String jsonStr = JSONUtil.toJsonStr(ids);
        queryWrapper.last("ORDER BY FIELD(id, " + jsonStr.substring(1, jsonStr.length() - 1) + ")");
        return userMapper.selectList(queryWrapper).stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public int updateUserInfo(User user, HttpServletRequest request) {
        Long userId = user.getId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = getLoginUser(request);
        // 如果是管理员，允许更新任意用户
        // 如果不是管理员，只允许更新当前（自己的）信息
        if (!isAdmin(request) && !userId.equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

}