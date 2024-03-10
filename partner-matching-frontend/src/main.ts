import { createApp } from 'vue'
import App from './App.vue'
import routes from "./config/route.ts";
import * as VueRouter from 'vue-router';
import Vant from 'vant'
import 'vant/lib/index.css';

const router = VueRouter.createRouter({
    history: VueRouter.createWebHashHistory(),
    routes,
})

const app = createApp(App);
app.use(Vant)
app.use(router)

app.mount('#app')