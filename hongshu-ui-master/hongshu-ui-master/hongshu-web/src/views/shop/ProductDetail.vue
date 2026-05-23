<template>
  <transition name="fade">
    <div class="product-detail-mask" v-if="visible" @click.self="handleClose">
      <div class="product-detail-container animate__animated animate__zoomIn">
        <div class="close-btn" @click="handleClose">
          <el-icon><Close /></el-icon>
        </div>

        <div class="detail-content" v-loading="loading">
          <div class="left-media">
            <el-image :src="formatUrl(product.cover)" fit="cover" class="main-image">
              <template #error><div class="image-slot">暂无图片</div></template>
            </el-image>
          </div>

          <div class="right-info">
            <div class="seller-header">
              <el-avatar :size="40" :src="formatUrl(product.sellerAvatar)" />
              <div class="seller-info-text">
                <span class="seller-name">{{ product.sellerName || '匿名卖家' }}</span>
                <span class="post-date">发布于商城</span>
              </div>
            </div>

            <div class="info-scroll-area">
              <template v-if="isEditing">
                <el-input v-model="product.productName" placeholder="修改商品名称" class="edit-input" />
              </template>
              <h1 v-else class="product-title">{{ product.productName }}</h1>

              <div class="price-tag">
                <span class="currency">￥</span>
                <template v-if="isEditing">
                  <el-input-number v-model="product.price" :precision="2" :step="1" size="small" />
                </template>
                <span v-else class="amount">{{ product.price }}</span>
              </div>

              <div class="stock-info">
                <template v-if="isEditing">
                  <span style="font-size: 14px; color: #666; margin-right: 8px;">库存:</span>
                  <el-input-number v-model="product.stock" :min="0" size="small" />
                </template>
                <el-tag v-else :type="product.stock > 0 ? 'success' : 'info'">
                  {{ product.stock > 0 ? `库存: ${product.stock}` : '已售罄' }}
                </el-tag>
              </div>

              <div class="desc-card">
                <el-input v-if="isEditing" type="textarea" v-model="product.description" :rows="4" />
                <p v-else class="product-desc">{{ product.description || '暂无描述' }}</p>
              </div>
            </div>

            <div class="action-footer">
              <div class="interaction-placeholder"></div>

              <template v-if="isMyProduct">
                <template v-if="isEditing">
                  <el-button @click="isEditing = false">取消</el-button>
                  <el-button type="success" round :loading="saveLoading" @click="handleUpdateSubmit">保存修改</el-button>
                </template>
                <template v-else>
                  <el-button type="primary" round @click="isEditing = true">修改商品</el-button>
                  <el-button type="danger" plain round @click="handleDelete">下架商品</el-button>
                </template>
              </template>

              <template v-else>
                <el-input-number
                    v-if="product.stock > 0"
                    v-model="buyQuantity"
                    :min="1"
                    :max="product.stock"
                    size="default"
                    controls-position="right"
                    style="margin-right: 15px; width: 100px;"
                />

                <el-button
                    type="danger"
                    round
                    class="buy-btn"
                    :disabled="product.stock <= 0"
                    :loading="buyLoading"
                    @click="handleBuy"
                >
                  {{ product.stock > 0 ? '立即购买' : '已售罄' }}
                </el-button>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { Close } from '@element-plus/icons-vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
// 注意：请根据你的实际路径引入 API
// import { getProductById, buyProduct } from '@/api/product';

const props = defineProps({
  visible: Boolean,
  productId: [String, Number]
});

const emit = defineEmits(['update:visible','refresh']);
const product = ref<any>({});
const loading = ref(false);
const router = useRouter();
const buyLoading = ref(false);

const isEditing = ref(false);
const saveLoading = ref(false);
const buyQuantity = ref(1); // 购买数量

// 格式化图片地址
const formatUrl = (url: string) => {
  if (!url) return '';
  if (url.startsWith('http')) return url;
  return '/dev-api' + url;
};

// 获取 Token 头
const uploadHeaders = computed(() => {
  let token = localStorage.getItem("accessToken") || "";
  token = token.replace(/^"|"$/g, '').trim();
  return { "Authorization": "Bearer " + token, "accessToken": token };
});

// 获取当前用户ID
const currentUserId = computed(() => {
  const userInfo = localStorage.getItem("userInfo");
  if (userInfo) {
    try { return JSON.parse(userInfo).id; } catch (e) { return null; }
  }
  return null;
});

// 判断是否为自己的商品
const isMyProduct = computed(() => {
  return currentUserId.value && product.value.sellerId && String(currentUserId.value) === String(product.value.sellerId);
});

// 详情获取
const fetchProductDetail = async (id: any) => {
  loading.value = true;
  isEditing.value = false;
  buyQuantity.value = 1; // 重置购买数量
  try {
    const res = await axios.get(`/dev-api/web/product/get/${id}`, { headers: uploadHeaders.value });
    if (res.data.code === 200) {
      product.value = res.data.data;
    }
  } catch (error) {
    ElMessage.error("获取详情失败");
  } finally {
    loading.value = false;
  }
};

// 立即购买
const handleBuy = async () => {
  if (product.value.stock <= 0) return;
  buyLoading.value = true;
  try {
    // 这里如果没封装 API 就直接用 axios.post，传 params
    const res = await axios.post(`/dev-api/web/product/buy/${product.value.id}`, null, {
      params: { quantity: buyQuantity.value },
      headers: uploadHeaders.value
    });

    if (res.data.code === 200) {
      router.push({
        path: '/pay',
        query: {
          orderNo: res.data.data.orderNo,
          price: (product.value.price * buyQuantity.value).toFixed(2), // 计算总价
          qrCode: res.data.data.qrCode,
          sellerName: product.value.sellerName,
          sellerAvatar: product.value.sellerAvatar
        }
      });
      handleClose();
    } else {
      ElMessage.error(res.data.msg || "下单失败");
    }
  } catch (error) {
    ElMessage.error("服务器繁忙");
  } finally {
    buyLoading.value = false;
  }
};

// 更新商品信息
const handleUpdateSubmit = async () => {
  saveLoading.value = true;
  try {
    const res = await axios.put('/dev-api/web/product/update', product.value, { headers: uploadHeaders.value });
    if (res.data.code === 200) {
      ElMessage.success("修改成功");
      isEditing.value = false;
      emit('refresh');
    }
  } catch (error) {
    ElMessage.error("更新失败");
  } finally {
    saveLoading.value = false;
  }
};

// 下架商品
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要下架该商品吗？', '提示', { type: 'warning' });
    const res = await axios.post(`/dev-api/web/product/offShelf/${product.value.id}`, {}, { headers: uploadHeaders.value });
    if (res.data.code === 200) {
      ElMessage.success("商品已下架");
      emit('refresh', product.value.id);
      handleClose();
    }
  } catch (error) { if (error !== 'cancel') ElMessage.error("下架失败"); }
};

watch(() => [props.visible, props.productId], ([newVisible, newId]) => {
  if (newVisible && newId) fetchProductDetail(newId);
}, { immediate: true });

const handleClose = () => emit('update:visible', false);
</script>

<style scoped lang="less">
/* 样式保持不变 */
.product-detail-mask { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0, 0, 0, 0.6); z-index: 2000; display: flex; justify-content: center; align-items: center; backdrop-filter: blur(4px); }
.product-detail-container { position: relative; width: 940px; height: 80vh; background: #fff; border-radius: 24px; display: flex; overflow: hidden; }
.close-btn { position: absolute; top: 16px; right: 16px; width: 36px; height: 36px; background: rgba(0, 0, 0, 0.05); color: #999; border-radius: 50%; display: flex; justify-content: center; align-items: center; cursor: pointer; z-index: 100; transition: all 0.3s; &:hover { background: rgba(0, 0, 0, 0.1); color: #333; } }
.detail-content { display: flex; width: 100%; height: 100%; }
.left-media { flex: 1.2; height: 100%; background: #222; overflow: hidden; border-radius: 24px 0 0 24px; .main-image { width: 100%; height: 100%; } }
.right-info { flex: 0.8; min-width: 360px; padding: 32px; display: flex; flex-direction: column; border-left: 1px solid #f0f0f0; background: #fff; border-radius: 0 24px 24px 0; }
.seller-header { display: flex; align-items: center; gap: 12px; margin-bottom: 24px;
  .seller-info-text { flex: 1; display: flex; flex-direction: column; .seller-name { font-weight: 700; font-size: 16px; color: #333; } .post-date { font-size: 12px; color: #999; } }
}
.info-scroll-area { flex: 1; overflow-y: auto; padding-right: 10px; &::-webkit-scrollbar { width: 6px; } &::-webkit-scrollbar-thumb { background: #f0f0f0; border-radius: 3px; }
  .product-title { font-size: 24px; font-weight: 800; margin-bottom: 12px; color: #111; }
  .edit-input { margin-bottom: 12px; }
  .price-tag { color: #ff2442; margin-bottom: 15px; .amount { font-size: 36px; font-weight: 900; } }
  .desc-card { background: #fcfcfc; border: 1px solid #f9f9f9; border-radius: 12px; padding: 16px; .product-desc { font-size: 15px; color: #666; line-height: 1.7; white-space: pre-wrap; } }
}
.buy-btn { width: 140px; height: 48px; font-size: 16px; font-weight: 700; border-radius: 24px; background-color: #ff2442; border: none; }
.action-footer { padding-top: 24px; border-top: 1px solid #eee; margin-top: auto; display: flex; justify-content: flex-end; align-items: center; gap: 10px; position: relative; }
</style>