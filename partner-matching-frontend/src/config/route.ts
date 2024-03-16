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
    { path: '/team', component: Team },
    { path: '/team/add', component: TeamAddPage },
    { path: '/team/update', component: TeamUpdatePage },
    { path: '/user', component: UserPage },
    { path: '/user/edit', component: UserEditPage },
    { path: '/user/update', component: UserUpdate },
    { path: '/user/team/create', component: UserTeamCreatePage },
    { path: '/user/team/join', component: UserTeamJoinPage },
    { path: '/search', component: Search },
    { path: '/search/result', component: SearchResult },
    { path: '/user/login', component: UserLoginPage },
]

export default routes;