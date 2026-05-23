<template>
  <div class="reds-modal login-modal" role="presentation">
    <i tabindex="-1" class="reds-mask" aria-label="弹窗遮罩"></i>
    <div class="login-container">
      <div class="icon-btn-wrapper close-button" @click="close">
        <Close style="width: 1em; height: 1em" />
      </div>
      <div class="left">
        <div class="login-reason">欢迎来到好物社!来到属于你的世界</div>
        <div class="qrcode">
          <img
            class="qrcode-img"
            src="E:\FinshWork\hongshu-ui-master\hongshu-ui-master\hongshu-web\public\favicon.ico"
          />
          <div class="status" style="display: none"></div>
        </div>
        <div class="tip">请使用手机号作为账号登陆哦~</div>
      </div>
      <div class="right">
        <div class="title">手机号登录</div>
        <div class="input-container">
          <form onsubmit="return false">
            <label class="phone">
              <span class="country-code"></span>
              <input placeholder="请输入手机号哦~" type="text" name="blur" v-model="userLogin.phone" />
              <svg class="reds-icon clear" width="24" height="24" fill="#xhs-pc-web-phone" style="display: none">
                <use xlink:href="#clear"></use>
              </svg>
            </label>
            <div style="height: 16px"></div>
            <label class="auth-code">
              <input type="password" placeholder="请输入密码哦~" autocomplete="false" v-model="userLogin.password" />
            </label>
            <div class="err-msg"></div>
            <button class="submit" @click="loginMethod">登录</button>
          </form>
        </div>
        <div class="agreements">
          <label class="agreement-label">
            <input type="checkbox" v-model="agreedToTerms" class="agreement-checkbox" />
            <span class="checkmark"></span>
          </label>
          <span class="agree-text">我已阅读并同意</span>
          <a class="links" target="_blank" href="https://agree.xiaohongshu.com/h5/terms/ZXXY20220331001/-1">
            《用户协议》
          </a>
          <a class="links" target="_blank" href="https://agree.xiaohongshu.com/h5/terms/ZXXY20220509001/-1">
            《隐私政策》
          </a>
          <br />
          <a
            class="links"
            target="_blank"
            href="https://oa.xiaohongshu.com/h5/terms/ZXXY20220516001/-1"
            style="margin-left: 25px"
            >《儿童/青少年个人信息保护规则》
          </a>
        </div>
        <div class="oauth-tip">
          <span class="oauth-tip-line">新用户可直接免验证登录</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { Close } from "@element-plus/icons-vue";
import type { UserLogin } from "@/type/user";
import { login } from "@/api/user";
import { ref } from "vue";
import { storage } from "@/utils/storage";
import { useUserStore } from "@/store/userStore";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";

const userStore = useUserStore();
const router = useRouter();

const agreedToTerms = ref(false);

const userLogin = ref<UserLogin>({
  phone: "",
  email: "",
  code: "",
  username: "",
  password: "",
});

const emit = defineEmits(["clickChild"]);
const close = () => {
  //传递给父组件
  emit("clickChild", false);
};
const phoneRegex = /^1[3-9]\d{9}$/; // 正则表达式校验手机号格式

const loginMethod = () => {
  if (!agreedToTerms.value) {
    // 如果未勾选协议，则显示提示信息
    ElMessage.warning("请先阅读并同意用户协议、隐私政策和个人信息保护规则");
    return;
  }

  const phoneNumber = userLogin.value.phone;
  if (!phoneNumber) {
    ElMessage.warning("请输入手机号");
    return;
  }
  if (!phoneRegex.test(phoneNumber)) {
    ElMessage.warning("手机号格式不正确");
    return;
  }
  if (!userLogin.value.password) {
    ElMessage.warning("请输入密码");
    return;
  }
  login(userLogin.value)
    .then((res: any) => {
      const { data } = res;
      const currentUser = data.userInfo;
      storage.set("accessToken", data.accessToken);//向浏览器存储设置token
      storage.set("refreshToken", data.refreshToken);
      userStore.setUserInfo(currentUser);
      router.push({ path: "/", query: { date: Date.now() } });
      emit("clickChild", false);
    })
    .catch((error: any) => {
      console.log(error);
      ElMessage.error("登录失败");
    });
};
</script>

<style lang="less" scoped>
a {
  text-decoration: none;
  background-color: transparent;
}
.login-reason {
  height: 48px;
  padding: 0 20px;
  background: rgba(61, 138, 245, 0.1);
  color: #3d8af5;
  border-radius: 999px;
  font-size: 16px;
  font-weight: 600;
  line-height: 120%;
  margin-top: 40px;
  margin-bottom: -40px;
  max-width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.agreements {
  /* 其他样式保持不变 */

  /* 新增样式 */
  .agreement-label {
    position: relative;
    cursor: pointer;
    display: inline-block;
    margin-right: 20px;
  }

  .agreement-checkbox {
    position: absolute;
    opacity: 0;
    cursor: pointer;
  }

  .checkmark {
    position: absolute;
    top: -12px;
    left: 0;
    height: 15px;
    width: 15px;
    background-color: #eee;
    border: 1px solid #ccc;
    border-radius: 3px;
  }

  /* 显示选中样式 */
  .agreement-checkbox:checked ~ .checkmark:after {
    content: "";
    position: absolute;
    display: block;
    left: 4px;
    top: -1px;
    width: 5px;
    height: 10px;
    border: solid #ff2442;
    border-width: 0 2px 2px 0;
    transform: rotate(45deg);
  }

  .agree-text {
    /* 其他样式保持不变 */
    font-size: 12px;
    color: var(--color-tertiary-label);
  }
}

.login-modal {
  z-index: 100010;
  flex-direction: column;
}

.reds-modal {
  display: flex;
  align-items: center;
  justify-content: center;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1000002;
  box-sizing: border-box;
  visibility: visible;
  opacity: 1;
  transition:
    opacity 0.2s,
    visibility 0.2s;

  .reds-mask {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.25);
    z-index: -1;
  }

  .login-container {
    display: flex;
    position: relative;
    width: 800px;
    height: 480px;
    background: #fff;
    border-radius: 16px;
    box-shadow:
      0 4px 32px 0 rgba(0, 0, 0, 0.08),
      0 1px 4px 0 rgba(0, 0, 0, 0.04);
    transition: all 0.2s;

    .close-button {
      position: absolute;
      right: 20px;
      top: 20px;
      cursor: pointer;
      color: rgba(51, 51, 51, 0.8);
    }

    .left {
      width: 400px;
      display: flex;
      align-items: center;
      flex-direction: column;
      border-right: 1px solid rgba(0, 0, 0, 0.08);

      img {
        border-style: none;
      }

      .logo {
        margin-top: 70px;
        width: 80px;
        height: 35px;
        -webkit-user-select: none;
        user-select: none;
        pointer-events: none;
      }

      .course-video {
        margin-top: 20px;
        width: 160px;
        height: 240px;
        overflow: hidden;
        margin-bottom: -1px;

        video {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .qrcode {
        position: relative;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-top: 28px;
        width: 192px;
        height: 192px;
        border: 2px solid var(--color-shadow-border);
        border-radius: 12px;
        background: var(--color-white);

        .qrcode-img {
          width: 160px;
          height: 160px;
        }

        .status {
          display: flex;
          align-items: center;
          justify-content: center;
          position: absolute;
          left: 0;
          top: 0;
          width: 100%;
          height: 100%;
          flex-direction: column;
          background: #fff;
          opacity: 0.95;
          -webkit-backdrop-filter: blur(2.5px);
          backdrop-filter: blur(2.5px);
          border-radius: 10px;
        }
      }

      .tip {
        color: #333;
        font-weight: 600;
        font-size: 12px;
        margin-top: 16px;
        line-height: 120%;
      }

      .course {
        width: 64px;
        height: 28px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 999px;
        margin-top: 22px;
        font-weight: 400;
        font-size: 14px;
        color: rgba(51, 51, 51, 0.8);
        cursor: pointer;
      }
    }

    .right {
      width: 400px;
      display: flex;
      align-items: center;
      flex-direction: column;
      margin-top: 48px;

      .title {
        font-size: 18px;
        color: rgba(51, 51, 51, 0.8);
        font-weight: 600;
        line-height: 120%;
      }

      .input-container {
        margin-top: 24px;
        width: 304px;
        display: flex;
        flex-direction: column;

        .auth-code {
          margin-right: 45px;
          justify-content: center;
        }

        .auth-code,
        .phone {
          display: flex;
          align-items: center;
          font-size: 16px;
          line-height: 22px;
          color: rgba(51, 51, 51, 0.8);
          height: 48px;
          background: #fff;
          border-radius: 999px;
          transition: border-color 0.2s;
          border-bottom: 0.5px solid rgba(0, 0, 0, 0.08);

          .country-code {
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
            width: 29px;
            height: 100%;
            font-weight: 400;
            margin-left: 16px;
            margin-right: 20px;
            color: #333;
          }

          .country-code:after {
            position: absolute;
            left: 41px;
            content: "";
            height: 24px;
            width: 1px;
            background-color: #fff;
          }
        }

        .auth-code input,
        .phone input {
          font-size: 16px;
          width: 130px;
          height: 100%;
          caret-color: #ff2442;
          color: #333;
        }

        .code-button {
          font-size: 16px;
          color: #ff2442;
          cursor: pointer;
          opacity: 0.5;
        }

        .err-msg {
          margin-top: 9.5px;
          height: 10px;
          line-height: 10px;
          color: var(--color-red);
          font-size: 14px;
          font-weight: 400;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .submit {
          margin-top: 24px;
          height: 48px;
          background: #ff2442;
          color: #fff;
          opacity: 1;
          border-radius: 999px;
          font-size: 16px;
          font-weight: 600;
          cursor: pointer;
          transition: all 0.2s;
          width: 100%;
        }

        .submit:hover {
          opacity: 1;
        }

        .submit:active {
          transform: scale(0.98);
        }

        form {
          display: block;
          margin-top: 0em;
        }
      }

      .agreements {
        width: 280px;
        position: relative;
        margin-top: 16px;
        padding-left: 18px;
        font-size: 12px;
        color: var(--color-tertiary-label);
        line-height: 120%;

        .agree-icon {
          display: inline-block;
          width: 10px;
          height: 10px;
          border-radius: 100%;
          background-color: #fff;
          border: 1px solid rgba(0, 0, 0, 0.183);
        }

        a {
          color: #13386c;
        }

        .links {
          font-weight: 400;
        }
      }

      .oauth-tip:after,
      .oauth-tip:before {
        margin-top: 20px;
        display: block;
        width: 50px;
        height: 1px;
        content: "";
        background-color: rgba(0, 0, 0, 0.08);
      }

      .oauth-tip-line {
        margin: 0 12px;
        margin-top: 20px;
      }

      .oauth-tip {
        margin-top: 29px;
        display: flex;
        align-items: center;
        font-weight: 400;
        font-size: 14px;
        color: rgba(51, 51, 51, 0.6);
      }

      .login {
        display: flex;
        margin-top: 16px;

        .login-common {
          width: 144px;
          height: 40px;
          border: 1px solid rgba(0, 0, 0, 0.08);
          border-radius: 999px;
          color: rgba(51, 51, 51, 0.8);
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          margin-right: 10px;

          .reg {
            margin-left: 16px;
          }
        }
      }
    }
  }
}
</style>
