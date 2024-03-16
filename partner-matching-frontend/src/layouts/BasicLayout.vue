<script setup lang="ts">
import 'vant/lib/index.css'
import {useRouter} from "vue-router";
import {ref} from "vue";
import routes from "../config/route.ts";
  const router = useRouter()
const DEFAULT_TITLE = '伙伴匹配';
const title = ref(DEFAULT_TITLE);

/**
 * 根据路由切换标题
 */
router.beforeEach((to, from) => {
  const toPath = to.path;
  const route = routes.find((route) => {
    return toPath == route.path;
  })
  title.value = route?.title ?? DEFAULT_TITLE;
})

  const onClickLeft = () => {
    // router.push({path: "/"})
    router.back()
  };
  const onClickRight = () => {
    router.push("/search")
  };
</script>

<template>
  <van-nav-bar
      :title="title"
      left-arrow
      @click-left="onClickLeft"
      @click-right="onClickRight"
  >
    <template #right>
      <van-icon name="search" size="18" />
    </template>
  </van-nav-bar>

  <div id="content">
    <router-view/>
  </div>

  <van-tabbar route>
    <van-tabbar-item icon="home-o" to="/"  name="home">主页</van-tabbar-item>
    <van-tabbar-item icon="search" to="/team" name="team">队伍</van-tabbar-item>
    <van-tabbar-item icon="friends-o" to="/user"  name="user">个人</van-tabbar-item>
  </van-tabbar>

</template>

<style scoped>
#content {
  padding-bottom: 80px;
}
</style>