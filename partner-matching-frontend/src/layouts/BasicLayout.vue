<script setup lang="ts">
import { ref } from 'vue';
import 'vant/lib/index.css'
import Index from "../pages/Index.vue";
import Team from "../pages/Team.vue";

  const onClickLeft = () => alert("左边");
  const onClickRight = () => alert("右边");
  const active = ref('home');
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

  <div id="content">
    <template v-if="active === 'home'">
      <Index />
    </template>
    <template v-if="active === 'team'">
      <Team />
    </template>
  </div>

  <van-tabbar v-model="active">
    <van-tabbar-item icon="home-o" name="home">主页</van-tabbar-item>
    <van-tabbar-item icon="search" name="team">队伍</van-tabbar-item>
    <van-tabbar-item icon="friends-o" name="person">个人</van-tabbar-item>
  </van-tabbar>

</template>

<style scoped>

</style>