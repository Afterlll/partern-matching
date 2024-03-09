<script setup lang="ts">
import {ref} from "vue";

const activeIds = ref([]);
const activeIndex = ref(0);
const searchValue = ref('')

const originTagList = [{
  text: '性别',
  children: [
    {text: '男', id: '男'},
    {text: '女', id: '女'},
  ],
},
  {
    text: '年级',
    children: [
      {text: '大一', id: '大一'},
      {text: '大二', id: '大二'},
      {text: '大3', id: '大3'},
      {text: '大4', id: '大4'},
      {text: '大5', id: '大5aaaaaaa'},
      {text: '大6', id: '大6aaaaaaa'},
    ],
  },
]

let tagList = ref(originTagList)

/**
 * 移除标签
 * @param tag 所要移除标签的 id
 */
const doClose = (tag) => {
  activeIds.value = activeIds.value.filter(item => item !== tag)
}

/**
 * 搜索过滤
 */
const onSearch = () => {
  tagList.value = originTagList.map(parentTag => {
    const tempChildren = [...parentTag.children];
    const tempParentTag = {...parentTag};
    tempParentTag.children = tempChildren.filter(item => item.text.includes(searchValue.value))
    return tempParentTag
  })
}

const onCancel = () => {
  tagList.value = originTagList
}

/**
 * 点击标签的父级，刷新出所有子标签
 */
const click = () => {
  tagList.value = originTagList
}

</script>

<template>
  <van-search
      v-model="searchValue"
      shape="round"
      background="#fb7299"
      placeholder="请输入要检索的标签"
      @search="onSearch"
      @cancel="onCancel"
  />

  <van-divider>已选标签</van-divider>
<!--  <van-highlight v-if="activeIds.length === 0" keywords="请选择标签" source-string="请选择标签" />-->
  <div v-if="activeIds.length === 0">请选择标签</div>
  <van-row gutter="16" style="padding: 0 16px">
    <van-col v-for="tag in activeIds">
      <van-tag closeable size="small" type="primary" @close="doClose(tag)">
        {{ tag }}
      </van-tag>
    </van-col>
  </van-row>


  <van-divider>选择标签</van-divider>

  <van-tree-select
      v-model:active-id="activeIds"
      v-model:main-active-index="activeIndex"
      :items="tagList"
      @click="click"
  />


</template>

<style scoped>

</style>