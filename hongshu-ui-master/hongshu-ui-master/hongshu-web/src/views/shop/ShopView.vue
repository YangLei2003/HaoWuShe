<template>
  <div class="shop-container">
    <div class="shop-header">
      <div class="header-left">
        <h2 class="shop-title">好物集市</h2>
        <p class="shop-subtitle">发现全世界的好物</p>
      </div>

      <div class="header-actions">
        <el-input
            v-model="searchQuery"
            placeholder="搜索好物名称..."
            :prefix-icon="Search"
            clearable
            class="search-bar"
        />
        <el-button type="danger" round @click="openAddDialog" :icon="Plus">
          发布我的好物
        </el-button>
      </div>
    </div>

    <div v-loading="loading" element-loading-text="正在加载好物...">
      <div v-if="filteredGoodsList.length > 0" class="goods-grid">
        <div
            v-for="item in filteredGoodsList"
            :key="item.id"
            class="goods-card"
            @click="handleGoodsClick(item)"
        >
          <div class="goods-image">
            <img :src="formatUrl(item.cover)" alt="商品图片" />
            <div v-if="item.stock === 0" class="stock-mask"><span>已售罄</span></div>
          </div>
          <div class="goods-info">
            <h3 class="goods-title">{{ item.productName }}</h3>
            <div class="goods-footer">
              <span class="price-value">￥{{ item.price }}</span>
              <span class="stock-count">库存: {{ item.stock }}</span>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else-if="!loading" :description="searchQuery ? '未找到相关宝贝' : '商城空空如也'" />
    </div>

    <el-dialog v-model="dialogVisible" title="发布好物" width="500px" destroy-on-close>
      <el-form :model="productForm" label-width="80px" ref="formRef">
        <el-form-item label="商品图片">
          <el-upload
              class="avatar-uploader"
              action="/dev-api/web/product/upload"
              name="file"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :before-upload="beforeUpload"
          >
            <img v-if="productForm.cover" :src="formatUrl(productForm.cover)" class="preview-img" />
            <el-icon v-else class="uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="productForm.productName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品价格">
          <el-input-number v-model="productForm.price" :precision="2" :step="5" :min="0" />
        </el-form-item>
        <el-form-item label="库存数量">
          <el-input-number v-model="productForm.stock" :min="1" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="productForm.description" type="textarea" placeholder="描述一下你的宝贝吧" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitProduct" :loading="submitting">确认发布</el-button>
        </span>
      </template>
    </el-dialog>

    <ProductDetail
        v-model:visible="detailVisible"
        :productId="currentProductId"
        @refresh="fetchGoodsList"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { Plus, Search } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import ProductDetail from './ProductDetail.vue';

// --- 状态控制 ---
const loading = ref(true);
const submitting = ref(false);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const searchQuery = ref(""); // ✅ 搜索词
const currentProductId = ref("");
const goodsList = ref<any[]>([]);

// --- 核心逻辑：过滤上架商品 + 搜索词匹配 ---
const filteredGoodsList = computed(() => {
  return goodsList.value.filter(item => {
    const isAvailable = item.status === 1;
    const isMatch = item.productName.toLowerCase().includes(searchQuery.value.toLowerCase());
    return isAvailable && isMatch;
  });
});

// --- URL/Token 处理 ---
const formatUrl = (url: string) => {
  if (!url) return '';
  return url.startsWith('http') ? url : '/dev-api' + url;
};

const uploadHeaders = computed(() => {
  let token = (localStorage.getItem("accessToken") || "").replace(/^"|"$/g, '').trim();
  return { "Authorization": "Bearer " + token, "accessToken": token };
});

// --- 表单数据 ---
const productForm = ref({
  productName: '',
  price: 0,
  stock: 1,
  cover: '',
  description: '',
  sellerId: 1,
  status: 1
});

// --- 交互方法 ---
const openAddDialog = () => {
  productForm.value = { productName: '', price: 0, stock: 1, cover: '', description: '', sellerId: 1, status: 1 };
  dialogVisible.value = true;
};

const handleGoodsClick = (item: any) => {
  currentProductId.value = item.id.toString();
  detailVisible.value = true;
};

const beforeUpload = (file: any) => {
  const isJPGorPNG = file.type === 'image/jpeg' || file.type === 'image/png';
  if (!isJPGorPNG) ElMessage.error('只能上传 JPG/PNG 格式图片!');
  return isJPGorPNG;
};

const handleUploadSuccess = (response: any) => {
  if (response.code === 200) {
    productForm.value.cover = response.data;
    ElMessage.success('图片上传成功！');
  } else {
    ElMessage.error(response.message || '图片上传失败');
  }
};

const submitProduct = async () => {
  if (!productForm.value.productName || !productForm.value.cover) {
    return ElMessage.warning('宝贝名称和图片都得有哦');
  }
  submitting.value = true;
  try {
    const res = await axios.post('/dev-api/web/product/add', productForm.value, {
      headers: uploadHeaders.value
    });
    if (res.data.code === 200) {
      ElMessage.success('发布成功！');
      dialogVisible.value = false;
      fetchGoodsList();
    }
  } catch (error) {
    ElMessage.error('发布失败');
  } finally {
    submitting.value = false;
  }
};

const fetchGoodsList = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/dev-api/web/product/list', {
      headers: uploadHeaders.value
    });
    if (res.data.code === 200) {
      goodsList.value = res.data.data;
    }
  } catch (error) {
    ElMessage.error('加载列表失败');
  } finally {
    loading.value = false;
  }
};

onMounted(fetchGoodsList);
</script>

<style scoped>
.shop-container { padding: 80px 24px 24px; max-width: 1200px; margin: 0 auto; background-color: #fff; min-height: 100vh; }

/* ✅ 头部布局：左右分布 */
.shop-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.header-actions { display: flex; align-items: center; gap: 12px; }
.search-bar { width: 240px; }

/* 以下是你原本的卡片样式，我一个字都没动 */
.shop-title { font-size: 24px; font-weight: bold; margin: 0; }
.shop-subtitle { font-size: 14px; color: #666; margin: 4px 0 0; }
.avatar-uploader { border: 1px dashed #d9d9d9; border-radius: 8px; cursor: pointer; position: relative; overflow: hidden; width: 120px; height: 120px; display: flex; justify-content: center; align-items: center; }
.avatar-uploader:hover { border-color: #ff2442; }
.uploader-icon { font-size: 28px; color: #8c939d; }
.preview-img { width: 100%; height: 100%; object-fit: cover; }
.goods-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 16px; }
.goods-card { background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.08); cursor: pointer; transition: transform 0.2s; }
.goods-card:hover { transform: translateY(-4px); box-shadow: 0 4px 16px rgba(0,0,0,0.12); }
.goods-image { position: relative; padding-top: 100%; overflow: hidden; }
.goods-image img { position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; }
.stock-mask { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; color: #fff; font-size: 14px; }
.goods-info { padding: 12px; }
.goods-title { font-size: 14px; font-weight: 500; margin: 0 0 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.goods-footer { display: flex; justify-content: space-between; align-items: center; }
.price-value { color: #ff2442; font-size: 18px; font-weight: bold; }
.stock-count { font-size: 12px; color: #999; }
</style>