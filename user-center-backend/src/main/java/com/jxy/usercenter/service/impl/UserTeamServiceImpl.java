package com.jxy.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxy.usercenter.mapper.UserTeamMapper;
import com.jxy.usercenter.model.domain.UserTeam;
import com.jxy.usercenter.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author 13547
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-03-12 14:20:51
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}