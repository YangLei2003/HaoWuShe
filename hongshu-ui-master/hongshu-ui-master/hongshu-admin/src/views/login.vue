<template>
  <div class="login-container" :style="{ backgroundImage: 'url(' + bgImg + ')' }">
    <div class="login-weapper">
      <el-form
          ref="loginRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
      >
        <div class="logo-container">
          <img class="logo" src="@/assets/logo/logo.png" />
          <h3 class="title">好物社</h3>
          <p class="subtitle">后台管理系统</p>
        </div>

        <el-form-item prop="username">
          <el-input
              v-model="loginForm.username"
              type="text"
              size="large"
              auto-complete="off"
              placeholder="账号 / 手机号"
          >
            <template #prefix>
              <svg-icon icon-class="user" class="el-input__icon input-icon" />
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="loginForm.password"
              type="password"
              size="large"
              auto-complete="off"
              placeholder="密码"
              @keyup.enter="handleLogin"
          >
            <template #prefix>
              <svg-icon icon-class="password" class="el-input__icon input-icon" />
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="code" v-if="captchaEnabled">
          <el-input
              v-model="loginForm.code"
              size="large"
              auto-complete="off"
              placeholder="验证码"
              class="code-input"
              @keyup.enter="handleLogin"
          >
            <template #prefix>
              <svg-icon icon-class="validCode" class="el-input__icon input-icon" />
            </template>
          </el-input>
          <div class="login-code">
            <img :src="codeUrl" @click="getCode" class="login-code-img" />
          </div>
        </el-form-item>

        <div class="form-options">
          <el-checkbox
              v-model="loginForm.rememberMe"
              class="remember-me"
          >
            记住密码
          </el-checkbox>
          <div class="register-link-container" v-if="register">
            <router-link class="link-type register-link" :to="'/register'">
              立即注册
            </router-link>
          </div>
        </div>

        <el-form-item style="width: 100%">
          <el-button
              :loading="loading"
              size="large"
              type="primary"
              class="login-btn"
              @click.prevent="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登 录 中...</span>
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="el-login-footer">
      <span>Copyright © 2024 HongShu All Rights Reserved.</span>
    </div>
  </div>
</template>

<script setup>
// --- JS 部分完全未动，保持原样 ---
import { getCodeImg } from "@/api/login";
import Cookies from "js-cookie";
import { encrypt, decrypt } from "@/utils/jsencrypt";
import useUserStore from "@/store/modules/user";
// 这里引入您指定的背景图，不影响登录功能
import bgImg from '@/assets/images/img.png'

const userStore = useUserStore();
const route = useRoute();
const router = useRouter();
const { proxy } = getCurrentInstance();

const loginForm = ref({
  username: "admin",
  password: "admin123",
  rememberMe: false,
  code: "",
  uuid: "",
});

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }],
};

const codeUrl = ref("");
const loading = ref(false);
// 验证码开关
const captchaEnabled = ref(true);
// 注册开关
const register = ref(false);
const redirect = ref(undefined);

watch(
    route,
    (newRoute) => {
      redirect.value = newRoute.query && newRoute.query.redirect;
    },
    { immediate: true }
);

function handleLogin() {
  proxy.$refs.loginRef.validate((valid) => {
    if (valid) {
      loading.value = true;
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30 });
        Cookies.set("password", encrypt(loginForm.value.password), {
          expires: 30,
        });
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 });
      } else {
        // 否则移除
        Cookies.remove("username");
        Cookies.remove("password");
        Cookies.remove("rememberMe");
      }
      // 调用action的登录方法
      userStore
          .login(loginForm.value)
          .then(() => {
            const query = route.query;
            const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
              if (cur !== "redirect") {
                acc[cur] = query[cur];
              }
              return acc;
            }, {});
            router.push({ path: redirect.value || "/", query: otherQueryParams });
          })
          .catch(() => {
            loading.value = false;
            // 重新获取验证码
            if (captchaEnabled.value) {
              getCode();
            }
          });
    }
  });
}

function getCode() {
  getCodeImg().then((res) => {
    captchaEnabled.value =
        res.captchaEnabled === undefined ? true : res.captchaEnabled;
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img;
      loginForm.value.uuid = res.uuid;
    }
  });
}

function getCookie() {
  const username = Cookies.get("username");
  const password = Cookies.get("password");
  const rememberMe = Cookies.get("rememberMe");
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password:
        password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe),
  };
}

getCode();
getCookie();
</script>

<style lang="scss" scoped>
// --- 全新的 CSS，模仿小红书风格 ---

/* 定义颜色变量 (小红书红) */
$xhs-red: #ff2442;
$xhs-red-hover: #e0203d;
$xhs-gray-text: #8c8c8c;
$xhs-gray-border: #e8e8e8;

/* 全局容器 */
.login-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh; /* 撑满屏幕 */
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* 登录表单包裹器，用于定位和可能的毛玻璃效果 */
.login-weapper {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  padding-bottom: 50px; /* 为底部预留空间 */
}

/* 核心登录表单卡片 */
.login-form {
  border-radius: 16px;
  background: #ffffff;
  width: 560px;               /* 从 380px 拉长到 460px，适配大屏 */
  padding: 50px 45px 40px 45px; /* 增加内边距，让内容不拥挤 */
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid $xhs-gray-border;
  transition: all 0.3s ease;

  /* Logo 和标题区域 */
  .logo-container {
    text-align: center;
    margin-bottom: 45px;

    .logo {
      width: 60px; /* 调整尺寸 */
      height: auto; /* 保持比例 */
      margin: 0 auto 10px auto; /* 居中 */
      display: block;
    }

    .title {
      margin: 0;
      font-size: 28px;
      font-weight: 600;
      color: #333;
      letter-spacing: 2px;
    }

    .subtitle {
      margin: 5px 0 0 0;
      font-size: 14px;
      color: $xhs-gray-text;
      font-weight: 400;
    }
  }

  /* Element Plus 输入框美化 */
  :deep(.el-form-item) {
    margin-bottom: 22px; /* 增加项之间的间距 */
  }

  :deep(.el-input__wrapper) {
    background-color: #f6f6f6; /* 小红书风格的灰色输入框背景 */
    box-shadow: none !important; /* 移除默认阴影 */
    border: 1px solid transparent;
    border-radius: 8px; /* 圆角 */
    padding: 0 15px;
    height: 48px; /* 增加高度 */
    transition: all 0.2s ease;

    &.is-focus {
      border-color: $xhs-red; /* 聚焦时显示小红书红 */
      background-color: #fff; /* 聚焦时背景变白 */
    }
  }

  :deep(.el-input__inner) {
    color: #333;
    font-size: 15px;

    &::placeholder {
      color: #bfbfbf;
    }
  }

  /* 输入框图标 */
  .input-icon {
    height: 100%;
    width: 16px;
    color: #999;
  }

  /* 验证码样式调整 */
  .code-input {
    width: 63%;
    display: inline-block;
    vertical-align: top;
  }

  .login-code {
    width: 33%;
    height: 48px; /* 匹配输入框高度 */
    float: right;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid $xhs-gray-border;
    cursor: pointer;

    .login-code-img {
      height: 100%;
      width: 100%;
      object-fit: cover; /* 保证验证码图片填充 */
    }
  }

  /* 选项区域 (记住密码和注册) */
  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 25px;

    .remember-me {
      margin: 0;
      :deep(.el-checkbox__label) {
        color: $xhs-gray-text;
        font-size: 13px;
      }
      :deep(.el-checkbox__inner) {
        border-radius: 4px;
        &:hover {
          border-color: $xhs-red;
        }
      }
      :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
        background-color: $xhs-red;
        border-color: $xhs-red;
      }
    }

    .register-link {
      font-size: 13px;
      color: $xhs-gray-text;
      text-decoration: none;
      transition: color 0.2s;

      &:hover {
        color: $xhs-red-hover;
      }
    }
  }

  /* 登录按钮美化 */
  /* 登录按钮美化 - 居中精致版 */
  .login-btn {
    height: 52px;             /* 高度同步增加 */
    width: 220px;             /* 按钮宽度也相应拉长 */
    display: block;
    margin: 20px auto 10px auto;
    border-radius: 26px;
    font-size: 17px;
    letter-spacing: 4px;      /* “登 录”两个字拉开 */
    background-color: $xhs-red;
    border-color: $xhs-red;
    box-shadow: 0 4px 12px rgba(255, 36, 66, 0.2); /* 淡淡的红色阴影 */
    transition: all 0.3s ease;

    &:hover, &:focus {
      background-color: $xhs-red-hover;
      border-color: $xhs-red-hover;
      transform: translateY(-1px); /* 悬浮微动感 */
      box-shadow: 0 6px 15px rgba(255, 36, 66, 0.3);
    }

    &:active {
      transform: translateY(0);
    }
  }
}

/* 底部美化 */
.el-login-footer {
  height: auto;
  line-height: 1.5;
  position: fixed;
  bottom: 20px;
  width: 100%;
  text-align: center;
  color: rgba(255, 255, 255, 0.7); /* 半透明白色，适配任何深色背景图 */
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 0.5px;
}
</style>