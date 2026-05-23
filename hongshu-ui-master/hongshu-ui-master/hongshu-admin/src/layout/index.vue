<template>
  <div :class="classObj" class="app-wrapper" :style="{ '--current-color': theme }">
    <div v-if="device === 'mobile' && sidebar.opened" class="drawer-bg" @click="handleClickOutside"/>
    <sidebar v-if="!sidebar.hide" class="sidebar-container" />
    <div :class="{ hasTagsView: needTagsView, sidebarHide: sidebar.hide }" class="main-container">
      <div :class="{ 'fixed-header': fixedHeader }">
        <navbar @setLayout="setLayout" />
        <tags-view v-if="needTagsView" />
      </div>
      <app-main />
      <settings ref="settingRef" />
    </div>
  </div>
</template>

<script setup>
import { useWindowSize } from '@vueuse/core'
import Sidebar from './components/Sidebar/index.vue'
import { AppMain, Navbar, Settings, TagsView } from './components'
import defaultSettings from '@/settings'

import useAppStore from '@/store/modules/app'
import useSettingsStore from '@/store/modules/settings'

const settingsStore = useSettingsStore()
const theme = computed(() => settingsStore.theme);
const sideTheme = computed(() => settingsStore.sideTheme);
const sidebar = computed(() => useAppStore().sidebar);
const device = computed(() => useAppStore().device);
const needTagsView = computed(() => settingsStore.tagsView);
const fixedHeader = computed(() => settingsStore.fixedHeader);

const classObj = computed(() => ({
  hideSidebar: !sidebar.value.opened,
  openSidebar: sidebar.value.opened,
  withoutAnimation: sidebar.value.withoutAnimation,
  mobile: device.value === 'mobile'
}))

const { width, height } = useWindowSize();
const WIDTH = 992;

watchEffect(() => {
  if (device.value === 'mobile' && sidebar.value.opened) {
    useAppStore().closeSideBar({ withoutAnimation: false })
  }
  if (width.value - 1 < WIDTH) {
    useAppStore().toggleDevice('mobile')
    useAppStore().closeSideBar({ withoutAnimation: true })
  } else {
    useAppStore().toggleDevice('desktop')
  }
})

function handleClickOutside() {
  useAppStore().closeSideBar({ withoutAnimation: false })
}

const settingRef = ref(null);
function setLayout() {
  settingRef.value.openSetting();
}
</script>

<style lang="scss" scoped>
@mixin clearfix {
  &:after {
    content: "";
    display: table;
    clear: both;
  }
}

.app-wrapper {
  @include clearfix;
  position: relative;
  height: 100%;
  width: 100%;
  background-color: #f5f7fa;

  &.mobile.openSidebar {
    position: fixed;
    top: 0;
  }
}

.drawer-bg {
  background: #000;
  opacity: 0.3;
  width: 100%;
  top: 0;
  height: 100%;
  position: absolute;
  z-index: 999;
}

.fixed-header {
  position: fixed;
  top: 0;
  right: 0;
  z-index: 9;
  width: calc(100% - 220px);
  transition: width 0.28s;
  background: linear-gradient(135deg, #ffffff 0%, #fffafb 100%); // 极淡红渐变
  border-bottom: 2px solid #ff2442; // 头部：鲜艳小红书红底边线
  box-shadow: 0 2px 8px rgba(255, 36, 66, 0.08); // 红色调阴影

  // 头部顶部细红线点缀
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, #ff2442 0%, #ff657a 50%, #ff2442 100%);
  }
}

.hideSidebar .fixed-header {
  width: calc(100% - 54px);
}

.sidebarHide .fixed-header {
  width: 100%;
}

.mobile .fixed-header {
  width: 100%;
}
</style>

<style lang="scss">
// ========== 侧边栏：用温润红（不同层次） ==========
.sidebar-container {
  background: linear-gradient(180deg, #ffffff 0%, #fff8f9 100%) !important; // 温润红渐变
  border-right: 1px solid #ffd4dc !important; // 柔红边框
  position: relative;

  // 侧边栏左侧装饰条
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4px;
    background: linear-gradient(180deg, #ff657a 0%, #ff2442 50%, #ff657a 100%);
    z-index: 10;
  }
}

// 菜单项样式
.el-menu {
  border-right: none !important;
  background: transparent !important;

  .el-menu-item,
  .el-sub-menu__title {
    border-radius: 8px;
    margin: 4px 8px;
    width: calc(100% - 16px);
    transition: all 0.2s ease;
    color: #555555;

    &:hover {
      background: linear-gradient(90deg, #fff0f2 0%, #ffe8eb 100%) !important; // 柔红悬停
      color: #ff2442 !important;
    }
  }

  .el-menu-item.is-active {
    background: linear-gradient(90deg, #ffeff2 0%, #ffe4e8 100%) !important;
    color: #ff2442 !important;
    font-weight: 500;
    position: relative;

    // 激活项左侧红条（与侧边栏装饰条呼应）
    &::before {
      content: '';
      position: absolute;
      left: -8px;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 24px;
      background-color: #ff2442;
      border-radius: 0 2px 2px 0;
    }

    &::after {
      content: '';
      position: absolute;
      right: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 20px;
      background-color: #ff657a; // 右侧用浅一点的红
      border-radius: 2px 0 0 2px;
    }
  }
}

// 子菜单
.el-sub-menu {
  .el-sub-menu__title {
    &:hover {
      background: linear-gradient(90deg, #fff0f2 0%, #ffe8eb 100%) !important;
    }
  }

  .el-menu-item {
    padding-left: 50px !important;
  }

  &.is-opened {
    > .el-sub-menu__title {
      color: #ff2442 !important;
      font-weight: 500;
    }
  }
}

// 主内容区
.main-container {
  .app-main {
    background-color: #f5f7fa !important;
    min-height: calc(100vh - 50px);
  }
}

// TagsView 红色元素
.tags-view-container {
  border-bottom: 1px solid #ffd4dc !important;
  background: linear-gradient(180deg, #ffffff 0%, #fffafb 100%) !important;

  .tags-view-item {
    border: 1px solid #ffe0e4 !important;
    background-color: #ffffff !important;

    &.active {
      background: linear-gradient(135deg, #fff0f2 0%, #ffe8eb 100%) !important;
      border-color: #ff2442 !important;
      color: #ff2442 !important;

      &::before {
        background-color: #ff2442 !important;
      }
    }

    &:hover {
      color: #ff2442 !important;
      border-color: #ffb3bd !important;
    }
  }
}

// 头部 navbar 红色元素
// 强制覆盖 navbar 组件的背景色
.navbar {
  background: linear-gradient(135deg, #ffffff 0%, #fffafb 100%) !important;
  border-bottom: 2px solid #ff2442 !important;
  position: relative;

  // 头部顶部细红线点缀
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, #ff2442 0%, #ff657a 50%, #ff2442 100%);
    z-index: 10;
  }
}

// 如果 navbar 内部有包裹层，也要覆盖
.navbar .navbar-content,
.navbar .header-container,
.navbar .el-row {
  background: transparent !important;
}

// 强制覆盖 fixed-header 的背景（防止被内部组件覆盖）
.fixed-header {
  background: linear-gradient(135deg, #ffffff 0%, #fffafb 100%) !important;
  border-bottom: 2px solid #ff2442 !important;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, #ff2442 0%, #ff657a 50%, #ff2442 100%);
    z-index: 10;
  }
}

// 汉堡菜单按钮
.hamburger-container {
  &:hover {
    background: linear-gradient(135deg, #fff0f2 0%, #ffe8eb 100%) !important;
    color: #ff2442 !important;
  }
}

// Logo 区域红色（如果有）
.logo-container {
  background: linear-gradient(135deg, #fff5f6 0%, #fff0f2 100%) !important;
  border-bottom: 1px solid #ffd4dc !important;

  .logo-title {
    color: #ff2442 !important;
    font-weight: 600;
  }
}

// 滚动条红色调（可选）
::-webkit-scrollbar-thumb {
  background-color: #ffb3bd !important;
  border-radius: 4px;

  &:hover {
    background-color: #ff657a !important;
  }
}
</style>