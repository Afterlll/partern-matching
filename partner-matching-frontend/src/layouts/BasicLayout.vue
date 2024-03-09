<script setup lang="ts">
import 'vant/lib/index.css'
import {useRouter} from "vue-router";
  const router = useRouter()
  const onClickLeft = () => {
    router.push({path: "/"})
  };
  const onClickRight = () => {
    router.push({path: "/search"})
  };
</script>

<template>
  <van-nav-bar
      title="标题"
      left-arrow
      @click-left="onClickLeft"
      @click-right="onClickRight"
  >
    <template #right>
      <van-icon name="search" size="18" />
    </template>
  </van-nav-bar>

  <router-view />

  <van-tabbar route>
    <van-tabbar-item icon="home-o" to="/"  name="home">主页</van-tabbar-item>
    <van-tabbar-item icon="search" to="/team" name="team">队伍</van-tabbar-item>
    <van-tabbar-item icon="friends-o" to="/user"  name="user">个人</van-tabbar-item>
  </van-tabbar>

</template>

<style scoped>

</style>