package com.jxy.usercenter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jxy.usercenter.model.domain.Team;
import com.jxy.usercenter.model.request.TeamAddRequest;
import com.jxy.usercenter.model.request.TeamJoinRequest;
import com.jxy.usercenter.model.request.TeamQueryRequest;
import com.jxy.usercenter.model.request.TeamUpdateRequest;
import com.jxy.usercenter.model.vo.TeamVo;
import com.jxy.usercenter.model.vo.UserTeamVo;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 13547
 * @description 针对表【team(队伍)】的数据库操作Service
 * @createDate 2024-03-12 14:22:53
 */
public interface TeamService extends IService<Team> {

    Long addTeam(TeamAddRequest teamAddRequest, HttpServletRequest request);

    Boolean deleteTeam(Long id, HttpServletRequest request);

    Boolean updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request);

    TeamVo getTeamById(Long id);

    List<TeamVo> getTeamList(String searchText, Integer pageNum, Integer status, HttpServletRequest request);

    IPage<TeamVo> getTeamListPage(TeamQueryRequest teamQueryRequest, HttpServletRequest request);

    Boolean joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request);

    Boolean quitTeam(Long teamId, HttpServletRequest request);

    Boolean disbandTeam(Long teamId, HttpServletRequest request);

    List<UserTeamVo> listMyJoin(TeamQueryRequest teamQueryRequest, HttpServletRequest request);

    List<UserTeamVo> listMyCreate(TeamQueryRequest teamQueryRequest, HttpServletRequest request);
}
