import Index from "../pages/Index.vue";
import Team from "../pages/Team.vue";
import User from "../pages/User.vue";
import Search from "../pages/Search.vue";
import UserEditPage from "../pages/UserEditPage.vue";
import SearchResult from "../pages/SearchResult.vue";
import UserLoginPage from "../pages/UserLoginPage.vue";

const routes = [
    { path: '/', component: Index },
    { path: '/team', component: Team },
    { path: '/user', component: User },
    { path: '/user/edit', component: UserEditPage },
    { path: '/search', component: Search },
    { path: '/search/result', component: SearchResult },
    { path: '/user/login', component: UserLoginPage },
]

export default routes;