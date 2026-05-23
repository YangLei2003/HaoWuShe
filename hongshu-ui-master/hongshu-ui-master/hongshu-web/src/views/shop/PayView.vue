<template>
  <div class="pay-container">
    <div class="pay-card animate__animated animate__fadeInUp">
      <div class="pay-header">
        <el-avatar :size="50" :src="formatUrl(payInfo.sellerAvatar)">
          {{ payInfo.sellerName?.charAt(0) }}
        </el-avatar>
        <div class="product-info">
          <p class="pay-target-title">向 <span>{{ payInfo.sellerName }}</span> 付款</p>
          <p class="order-no">订单号: {{ payInfo.orderNo }}</p>
        </div>
      </div>

      <div class="pay-body">
        <p class="pay-tip">待支付金额</p>
        <div class="price-tag-small">
          <span class="currency">￥</span>
          <span class="amount">{{ payInfo.price }}</span>
        </div>

        <div class="qr-code-wrapper-max">
          <el-image
              :src="payInfo.qrCode"
              fit="contain"
              class="qr-code-img"
          >
            <template #error>
              <div class="qr-error">
                找不到图片: WeChatMoneyImg.png <br/>
                请检查 src/assets/ 目录
              </div>
            </template>
          </el-image>
        </div>

        <p class="pay-method">
          <el-icon color="#1aad19" :size="20"><CircleCheck /></el-icon>
          微信支付安全加密
        </p>
      </div>

      <div class="pay-footer">
        <el-button type="primary" round class="confirm-btn" :loading="confirmLoading" @click="handleConfirmPay">
          我已付款
        </el-button>
        <el-button round class="cancel-btn" @click="handleCancel">
          取消支付
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { CircleCheck } from '@element-plus/icons-vue';
import axios from 'axios';

// ✅ 【重点】直接引入你指定的本地二维码
import WeChatPayImg from '@/assets/WeChatMoneyImg.png';

const route = useRoute();
const router = useRouter();
const confirmLoading = ref(false);

const payInfo = ref({
  orderNo: String(route.query.orderNo || ''),
  price: String(route.query.price || '0.30'),
  qrCode: WeChatPayImg, // ✅ 绑定本地图片
  sellerName: String(route.query.sellerName || '商家'),
  sellerAvatar: String(route.query.sellerAvatar || '')
});

const formatUrl = (url: string) => {
  if (!url) return '';
  if (url.includes('assets') || url.startsWith('data:')) return url;
  if (url.startsWith('http')) return url;
  return '/dev-api' + url;
};

const handleConfirmPay = async () => {
  if (!payInfo.value.orderNo) return;
  confirmLoading.value = true;
  try {
    let token = (localStorage.getItem("accessToken") || "").replace(/^"|"$/g, '').trim();
    const res = await axios.post(`/dev-api/web/product/confirmPay/${payInfo.value.orderNo}`, {}, {
      headers: { "Authorization": "Bearer " + token, "accessToken": token }
    });
    if (res.data.code === 200) {
      ElMessage.success("支付成功！");
      router.push('/shop');
    } else {
      ElMessage.error(res.data.data || "核销失败");
    }
  } catch (error) {
    ElMessage.error("网络异常");
  } finally {
    confirmLoading.value = false;
  }
};

const handleCancel = () => router.back();

onMounted(() => {
  if (!payInfo.value.orderNo) router.push('/shop');
});
</script>

<style scoped lang="less">
.pay-container { padding-top: 5vh; min-height: 100vh; background-color: #f6f6f6; display: flex; justify-content: center; }
.pay-card { width: 480px; background: #fff; border-radius: 32px; box-shadow: 0 20px 60px rgba(0,0,0,0.05); padding: 40px; display: flex; flex-direction: column; align-items: center; }
.pay-header { width: 100%; display: flex; align-items: center; gap: 12px; padding-bottom: 20px; border-bottom: 1px solid #f8f8f8;
  .pay-target-title { font-size: 16px; color: #333; margin: 0; span { font-weight: 800; } }
  .order-no { font-size: 12px; color: #ccc; }
}
.pay-body { width: 100%; padding: 30px 0; display: flex; flex-direction: column; align-items: center;
  .pay-tip { font-size: 13px; color: #999; margin-bottom: 5px; }
  /* ✅ 金额样式缩小 */
  .price-tag-small { color: #333; margin-bottom: 25px; display: flex; align-items: baseline;
    .currency { font-size: 18px; font-weight: bold; }
    .amount { font-size: 40px; font-weight: 900; }
  }
  /* ✅ 二维码容器占大头 */
  .qr-code-wrapper-max {
    position: relative; width: 320px; height: 320px; /* 🚀 巨型尺寸 */
    border: 1px solid #eee; padding: 10px; border-radius: 16px; margin-bottom: 25px; background: #fff;
    .qr-code-img { width: 100%; height: 100%; }
    .qr-logo-float { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); background: #fff; padding: 5px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
    .qr-error { font-size: 12px; color: #f56c6c; text-align: center; margin-top: 140px; }
  }
  .pay-method { font-size: 14px; color: #1aad19; display: flex; align-items: center; gap: 6px; }
}
.pay-footer { width: 100%; display: flex; flex-direction: column; gap: 12px;
  .confirm-btn { width: 100%; height: 52px; font-size: 17px; font-weight: 700; background: #ff2442; border: none; }
  .cancel-btn { width: 100%; height: 50px; color: #999; border: none; }
}
</style>