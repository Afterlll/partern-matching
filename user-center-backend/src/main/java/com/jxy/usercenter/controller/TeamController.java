package com.jxy.usercenter.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jxy.usercenter.common.BaseResponse;
import com.jxy.usercenter.common.ResultUtils;
import com.jxy.usercenter.model.request.TeamAddRequest;
import com.jxy.usercenter.model.request.TeamJoinRequest;
import com.jxy.usercenter.model.request.TeamQueryRequest;
import com.jxy.usercenter.model.request.TeamUpdateRequest;
import com.jxy.usercenter.model.vo.TeamVo;
import com.jxy.usercenter.model.vo.UserTeamVo;
import com.jxy.usercenter.service.TeamService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/*
* 队伍控制器请求
*/
@RestController
@RequestMapping("/team")
@CrossOrigin(allowCredentials = "true", originPatterns = {"http://localhost:5173"})
public class TeamController {

    @Resource
    private TeamService teamService;

    /**
     * 添加队伍
     *
     * @param teamAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        return ResultUtils.success(teamService.addTeam(teamAddRequest, request));
    }

    /**
     * 根据id删除队伍
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(Long id, HttpServletRequest request) {
        return ResultUtils.success(teamService.deleteTeam(id, request));
    }

    /**
     * 修改队伍
     *
     * @param teamUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        return ResultUtils.success(teamService.updateTeam(teamUpdateRequest, request));
    }

    /**
     * 根据 id 获取 队伍信息
     *
     * @param id
     * @return
     */
    @GetMapping("/get/{id}")
    public BaseResponse<TeamVo> getTeamById(@PathVariable Long id) {
        return ResultUtils.success(teamService.getTeamById(id));
    }

    /**
     * 获取所有队伍信息
     *
     * @param request
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamVo>> getTeamList(HttpServletRequest request) {
        return ResultUtils.success(teamService.getTeamList(request));
    }

    /**
     * 分页获取所有队伍信息
     *
     * @param teamQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<IPage<TeamVo>> getTeamListPage(@RequestBody TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        return ResultUtils.success(teamService.getTeamListPage(teamQueryRequest, request));
    }

    /**
     * 用户加入队伍
     *
     * @param teamJoinRequest
     * @param request
     * @return
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        return ResultUtils.success(teamService.joinTeam(teamJoinRequest, request));
    }

    /**
     * 用户退出队伍
     *
     * @param teamId
     * @param request
     * @return
     */
    @GetMapping("/quit/{teamId}")
    public BaseResponse<Boolean> quitTeam(@PathVariable Long teamId, HttpServletRequest request) {
        return ResultUtils.success(teamService.quitTeam(teamId, request));
    }

    /**
     * 队长解散队伍
     *
     * @param teamId
     * @param request
     * @return
     */
    @GetMapping("/disband/{teamId}")
    public BaseResponse<Boolean> disbandTeam(@PathVariable Long teamId, HttpServletRequest request) {
        return ResultUtils.success(teamService.disbandTeam(teamId, request));
    }

    /**
     * 获取当前用户已加入的队伍
     *
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<UserTeamVo>> listMyJoin(HttpServletRequest request) {
        return ResultUtils.success(teamService.listMyJoin(request));
    }

    /**
     * 获取当前用户创建的队伍
     *
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<UserTeamVo>> listMyCreate(HttpServletRequest request) {
        return ResultUtils.success(teamService.listMyCreate(request));
    }

}