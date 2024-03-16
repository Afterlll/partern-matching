import { createApp } from 'vue'
import App from './App.vue'
import routes from "./config/route.ts";
import * as VueRouter from 'vue-router';
import Vant from 'vant'
import 'vant/lib/index.css';
import moment from 'moment'
import "./style.css"

const router = VueRouter.createRouter({
    history: VueRouter.createWebHistory(),
    routes,
})


const app = createApp(App);
app.config.globalProperties.$moment = moment
app.use(Vant)
app.use(router)

app.mount('#app')