package com.jxy.usercenter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxy.usercenter.common.ErrorCode;
import com.jxy.usercenter.exception.BusinessException;
import com.jxy.usercenter.mapper.TeamMapper;
import com.jxy.usercenter.mapper.UserTeamMapper;
import com.jxy.usercenter.model.domain.Team;
import com.jxy.usercenter.model.domain.User;
import com.jxy.usercenter.model.domain.UserTeam;
import com.jxy.usercenter.model.enums.TeamStatusEnum;
import com.jxy.usercenter.model.request.TeamAddRequest;
import com.jxy.usercenter.model.request.TeamJoinRequest;
import com.jxy.usercenter.model.request.TeamQueryRequest;
import com.jxy.usercenter.model.request.TeamUpdateRequest;
import com.jxy.usercenter.model.vo.TeamVo;
import com.jxy.usercenter.model.vo.UserTeamVo;
import com.jxy.usercenter.service.TeamService;
import com.jxy.usercenter.service.UserService;
import com.jxy.usercenter.service.UserTeamService;
import org.apache.poi.ss.formula.functions.T;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.crypto.Data;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author 13547
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-03-12 14:22:53
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private UserService userService;

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserTeamMapper userTeamMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addTeam(TeamAddRequest teamAddRequest, HttpServletRequest request) {
        // 1. 请求参数是否为空
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = teamAddRequest.getName();
        String description = teamAddRequest.getDescription();
        Integer maxNum = teamAddRequest.getMaxNum();
        Date expireTime = teamAddRequest.getExpireTime();
        Long userId = teamAddRequest.getUserId();
        Integer status = teamAddRequest.getStatus();
        String password = teamAddRequest.getPassword();

        // 2.是否登录，未登录不允许创建
        User loginUser = userService.getLoginUser(request);
        // 3.校验信息
//            1.队伍人数 >1且<= 20
        if (maxNum == null || maxNum <= 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数 <1 或 > 20");
        }
//            2.队伍标题 <= 20
        if (StrUtil.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题 > 20");
        }
//            3.描述 <= 512
        if (StrUtil.isBlank(description) || description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "描述 > 512");
        }
//            4.status 是否公开(int)不传默认为(公开)
        status = Optional.ofNullable(status).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }

//            5 .如果 status 是加密状态，一定要有密码，且密码<= 32
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StrUtil.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码 > 32");
            }
        }
//            6.超时时间 > 当前时间
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍的过期时间在当前时刻之前,不符合要求");
        }
//            7.校验用户最多创建 5 个队伍
        // todo 有 bug，可能同时创建 100 个队伍(高并发)
        if (count(new QueryWrapper<Team>().eq("userId", loginUser.getId())) >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建 5 个队伍");
        }
        // 4，插入队伍信息到队伍表
        Team team = new Team();
        BeanUtil.copyProperties(teamAddRequest, team);
        team.setUserId(loginUser.getId());
        if (!save(team)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        // 5.插入用户 =>队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(loginUser.getId());
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(new Date());

        if (!userTeamService.save(userTeam)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return team.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteTeam(Long id, HttpServletRequest request) {
        Long userId = isLoginAndTeamOwnerAndAdmin(id, request);
        // 1. 先删除关联关系
        boolean a = userTeamService.remove(new QueryWrapper<UserTeam>().eq("teamId", id));
        // 2. 再删除队伍
        boolean b = removeById(id);
        return a && b;
    }

    /**
     * 判断当前用户是否登录并且是否是该队伍的队长或者管理员
     *
     * @param id
     * @param request
     * @return userId
     */
    private Long isLoginAndTeamOwnerAndAdmin(Long id, HttpServletRequest request) {
        // 需要登录且是队长
        User user = userService.getLoginUser(request);
        Long userId = user.getId();
        Team team = teamMapper.selectById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Long teamUserId = team.getUserId();
        if (!userId.equals(teamUserId)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        } else if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return user.getId();
    }

    @Override
    public Boolean updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamUpdateRequestId = teamUpdateRequest.getId();
        isLoginAndTeamOwnerAndAdmin(teamUpdateRequestId, request);
        // 查询队伍是否存在
        if (teamUpdateRequestId != null && teamUpdateRequestId > 0) {
            Team team = teamMapper.selectById(teamUpdateRequestId);
            if (team == null) {
                throw new BusinessException(ErrorCode.NULL_ERROR);
            }
            // 查看更换的值是否与旧值一致

        }
        Team team = new Team();
        // 更换队伍状态为加密时，必须带上密码
        Integer status = teamUpdateRequest.getStatus();
        if (status != null && status == TeamStatusEnum.SECRET.getValue()) {
            if (teamUpdateRequest.getPassword() == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "修改房间状态为加密时必须锁上密码");
            }
        }
        BeanUtil.copyProperties(teamUpdateRequest, team);
        return updateById(team);
    }

    @Override
    public TeamVo getTeamById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamMapper.selectById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        TeamVo teamVo = new TeamVo();
        BeanUtil.copyProperties(team, teamVo);
        return teamVo;
    }

    @Override
    public List<TeamVo> getTeamList(String searchText, Integer pageNum, Integer status, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
        }
        List<Team> teamList = teamMapper.selectList(new QueryWrapper<Team>().orderByDesc("createTime"));
        return teamList.stream().map(team -> {
            TeamVo teamVo = new TeamVo();
            BeanUtil.copyProperties(team, teamVo);
            return teamVo;
        }).collect(Collectors.toList());
    }

    @Override
    public IPage<TeamVo> getTeamListPage(TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 需要登录
        User loginUser = userService.getLoginUser(request);

        Long id = teamQueryRequest.getId();
        String name = teamQueryRequest.getName();
        String description = teamQueryRequest.getDescription();
        Integer maxNum = teamQueryRequest.getMaxNum();
        Long userId = teamQueryRequest.getUserId();
        Integer status = teamQueryRequest.getStatus();
        int pageSize = teamQueryRequest.getPageSize();
        int pageNum = teamQueryRequest.getPageNum();
        String searchText = teamQueryRequest.getSearchText();
        Date expireTime = teamQueryRequest.getExpireTime();
        String password = teamQueryRequest.getPassword();
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();

        if (id != null && id > 0) {
            queryWrapper.eq("id", id);
        }
        if (StrUtil.isNotBlank(name)) {
            queryWrapper.like("name", name);
        }
        if (StrUtil.isNotBlank(description)) {
            queryWrapper.like("description", description);
        }
        if (maxNum != null && maxNum > 1) {
            queryWrapper.ge("maxNum", maxNum);
        }
        if (userId != null && userId > 0) {
            queryWrapper.eq("userId", userId);
        }
        // 公开状态可以直接搜索出来
        if (status != null && TeamStatusEnum.PUBLIC.getValue() == status) {
            queryWrapper.eq("status", status);
        }
        // 搜索条件
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
        }
        // 过滤掉已过期的数据
        // todo 定时任务清理已过期的数据（逻辑删除）
        queryWrapper.and(qw -> qw.isNull("expireTime").or().gt("expireTime", new Date()));

        // 只有管理员才能查看加密还有非公开的房间
        if (status != null && status >= 0 && TeamStatusEnum.PUBLIC.getValue() == status && userService.isAdmin(request)) {
            queryWrapper.eq("status", status);
        }
        if (status != null && TeamStatusEnum.SECRET.getValue() == status) {
            if (userService.isAdmin(request)) {
                // 管理员
                queryWrapper.eq("status", status);
            } else if (StrUtil.isBlank(password)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间需要输入密码");
            }
            // 普通用户，有密码
            queryWrapper.eq("password", password);
        }

        Page<Team> page = page(new Page<>(pageNum, pageSize), queryWrapper);
        Page<TeamVo> pageTeamVo = new Page<>();
        BeanUtil.copyProperties(page, pageTeamVo);
        pageTeamVo.setRecords(
            page.getRecords().stream().map(team -> {
                TeamVo teamVo = new TeamVo();
                BeanUtil.copyProperties(team, teamVo);

                // 关联查询出队长信息
                teamVo.setTeamCaptainId(team.getUserId());
                // 关联查询出其他队员信息
                List<UserTeam> userTeamList = userTeamMapper.selectList(new QueryWrapper<UserTeam>().eq("teamId", team.getId()));
                teamVo.setTeamMember(userTeamList.stream()
                        .map(UserTeam::getUserId)
                        .map(utId -> userService.getById(utId))
                        .map(user -> userService.getSafetyUser(user))
                        .collect(Collectors.toList()));

                teamVo.setHasJoin(false);
                if (teamVo.getTeamCaptainId().equals(loginUser.getId())) {
                    teamVo.setHasJoin(true);
                } else {
                    // 加入 用户 是否加入队伍的标识
                    List<User> teamMember = teamVo.getTeamMember();
                    teamVo.setHasJoin(false);
                    if (teamMember != null) {
                        for (User user : teamMember) {
                            if (user.getId().equals(loginUser.getId())) {
                                teamVo.setHasJoin(true);
                                break;
                            }
                        }
                    }
                }

                return teamVo;
            }).collect(Collectors.toList()));

        return pageTeamVo;
    }

    /**
     * 用户加入队伍
     *
     * @param teamJoinRequest
     * @param request
     * @return
     */
    @Override
    public Boolean joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 用户最多加入 5 个队伍
        Long userId = loginUser.getId();
        if (userTeamService.count(new QueryWrapper<UserTeam>().eq("userId", userId)) >= 5) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户最多加入 5 个队伍");
        }
        // 队伍必须存在，只能加入未满、未过期的队伍
        Team team = teamMapper.selectById(teamJoinRequest.getTeamId());
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (new Date().after(team.getExpireTime())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "该队伍已过期");
        }
        // 不能加入自己的队伍
        if (userId.equals(team.getUserId())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不能加入自己的队伍");
        }
        // 查询出已加入该队伍的人数
        if (userTeamService.count(new QueryWrapper<UserTeam>().eq("teamId", teamJoinRequest.getTeamId())) >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "该队伍人数已满");
        }
        // 不能重复加入已加入的队伍（幂等性）
        if (userTeamService.getOne(new QueryWrapper<UserTeam>().eq("userId", userId).eq("teamId", team.getId())) != null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不能重复加入已加入的队伍");
        }
        // 禁止加入私有的队伍
        Integer status = team.getStatus();
        if (status != null && TeamStatusEnum.PRIVATE.getValue() == status) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "禁止加入私有的队伍");
        }
        // 如果加入的队伍是加密的，必须密码匹配才可以
        String password = teamJoinRequest.getPassword();
        if (status != null && TeamStatusEnum.SECRET.getValue() == status) {
            if (password == null || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "密码不正确");
            }
        }

        RLock lock = redissonClient.getLock("partner:matching:join:lock");
        while (true) {
            try {
                if (lock.tryLock(0, -1, TimeUnit.MICROSECONDS)) {
                    // 新增队伍 - 用户关联信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamJoinRequest.getTeamId());
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                // 只能释放自己的锁
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean quitTeam(Long teamId, HttpServletRequest request) {
        if (teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验队伍是否存在
        User loginUser = userService.getLoginUser(request);
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        // 校验我是否已加入队伍
        Long userId = loginUser.getId();
        if (userTeamService.count(new QueryWrapper<UserTeam>().eq("teamId", teamId).eq("userId", userId)) == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "你未加入该队伍");
        }
        long teamNum = userTeamService.count(new QueryWrapper<UserTeam>().eq("teamId", teamId));
        Boolean f = false;
        if (teamNum == 1) {
            // 只剩一人，队伍解散
            f = deleteTeam(teamId, request);
        } else if (teamNum > 1) {
            // 队长，权限转移给第二早加入的用户
            int i = 1;
            if (team.getUserId().equals(userId)) {
                List<UserTeam> userTeamList = userTeamMapper.selectList(new QueryWrapper<UserTeam>().eq("teamId", teamId).last("order by id asc limit 2"));
//                        .orderByAsc("joinTime"));
                UserTeam userTeam = userTeamList.get(1);
                Team team1 = new Team();
                team1.setId(teamId);
                team1.setUserId(userTeam.getUserId());
                i = teamMapper.updateById(team1);
            }
            // 队员，直接退出
            f = userTeamService.remove(new QueryWrapper<UserTeam>().eq("userId", userId).eq("teamId", teamId)) && i > 0;
        }
        return f;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disbandTeam(Long teamId, HttpServletRequest request) {
        if (teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验队伍是否存在
        User loginUser = userService.getLoginUser(request);
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (!team.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "你不是队长，无权解散队伍");
        }
        // 删除队伍及其关联关系
        return deleteTeam(teamId, request);
    }

    @Override
    public List<UserTeamVo> listMyJoin(TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        IPage<TeamVo> page = getTeamListPage(teamQueryRequest, request);
        return page.getRecords().stream().filter(teamVo -> {
                    // 是队长
                    if (teamVo.getTeamCaptainId().equals(userId)) {
                        return false;
                    }
                    for (User user : teamVo.getTeamMember()) {
                        if (user.getId().equals(userId)) {
                            return true;
                        }
                    }
                    return false;
                }).map(team -> {
                    UserTeamVo userTeamVo = new UserTeamVo();
                    BeanUtil.copyProperties(team, userTeamVo);
                    return userTeamVo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<UserTeamVo> listMyCreate(TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        IPage<TeamVo> page = getTeamListPage(teamQueryRequest, request);
        return page.getRecords().stream()
                .filter(teamVo -> teamVo.getTeamCaptainId().equals(userId))
                .map(team -> {
                    UserTeamVo userTeamVo = new UserTeamVo();
                    BeanUtil.copyProperties(team, userTeamVo);
                    return userTeamVo;
                })
                .collect(Collectors.toList());
//        List<Team> teamList = teamMapper.selectList(new QueryWrapper<Team>().eq("userId", userId).gt("expireTime", new Date()));
//        return teamList.stream()
//                .map(team -> {
//                    UserTeamVo userTeamVo = new UserTeamVo();
//                    BeanUtil.copyProperties(team, userTeamVo);
//                    return userTeamVo;
//                })
//                .collect(Collectors.toList());
    }


}