import Index from "../pages/Index.vue";
import Team from "../pages/team/Team.vue";
import User from "../pages/user/UserUpdate.vue";
import Search from "../pages/search/Search.vue";
import UserEditPage from "../pages/user/UserEditPage.vue";
import SearchResult from "../pages/search/SearchResult.vue";
import UserLoginPage from "../pages/user/UserLoginPage.vue";
import TeamAddPage from "../pages/TeamAddPage.vue";
import UserPage from "../pages/user/UserPage.vue";
import UserUpdate from "../pages/user/UserUpdate.vue";
import UserTeamCreatePage from "../pages/user/UserTeamCreatePage.vue";
import UserTeamJoinPage from "../pages/user/UserTeamJoinPage.vue";
import TeamUpdatePage from "../pages/team/TeamUpdatePage.vue";

const routes = [
    { path: '/', component: Index },
    { path: '/team', title: '找队伍', component: Team },
    { path: '/team/add', title: '创建队伍', component: TeamAddPage },
    { path: '/team/update', title: '更新队伍',  component: TeamUpdatePage },
    { path: '/user',  title: '个人信息', component: UserPage },
    { path: '/user/edit', title: '编辑信息', component: UserEditPage },
    { path: '/user/update', title: '更新信息',  component: UserUpdate },
    { path: '/user/team/create',  title: '创建队伍', component: UserTeamCreatePage },
    { path: '/user/team/join', title: '加入队伍', component: UserTeamJoinPage },
    { path: '/search', title: '找伙伴',  component: Search },
    { path: '/search/result', component: SearchResult },
    { path: '/user/login', title: '登录',  component: UserLoginPage },
]

export default routes;