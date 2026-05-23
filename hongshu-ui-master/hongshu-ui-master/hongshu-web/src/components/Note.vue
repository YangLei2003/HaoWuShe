<template>
  <div class="feeds-container" v-infinite-scroll="loadMoreData" :infinite-scroll-distance="50">
    <Waterfall
        :list="noteList"
        :width="options.width"
        :gutter="options.gutter"
        :hasAroundGutter="options.hasAroundGutter"
        :animation-effect="options.animationEffect"
        :animation-duration="options.animationDuration"
        :animation-delay="options.animationDelay"
        :breakpoints="options.breakpoints"
        style="min-width: 740px"
    >
      <template #item="{ item }">
        <el-skeleton style="width: 240px" :loading="!item.isLoading" animated>
          <template #template>
            <el-image
                :src="item.noteCover"
                :style="{
                width: '240px',
                maxHeight: '300px',
                height: item.noteCoverHeight + 'px',
                borderRadius: '8px',
              }"
                @load="handleLoad(item)"
            >
            </el-image>
            <div style="padding: 14px">
              <el-skeleton-item variant="h3" style="width: 100%" />
              <div style="display: flex; align-items: center; margin-top: 2px; height: 16px">
                <el-skeleton style="--el-skeleton-circle-size: 20px">
                  <template #template><el-skeleton-item variant="circle" /></template>
                </el-skeleton>
                <el-skeleton-item variant="text" style="margin-left: 10px" />
              </div>
            </div>
          </template>

          <template #default>
            <div class="card" style="max-width: 240px">
              <div class="image-container">
                <el-image
                    :src="item.noteCover"
                    :style="{
                    width: '240px',
                    maxHeight: '300px',
                    height: item.noteCoverHeight + 'px',
                    borderRadius: '8px',
                  }"
                    fit="cover"
                    @click="toDetail(item)"
                >
                </el-image>
                <div v-if="item.auditStatus === '0'" class="overlay">审核中</div>
                <div v-if="item.auditStatus === '2'" class="overlay not-passed">未通过⚠️</div>
              </div>
              <div class="footer">
                <a class="title"><span>{{ item.title }}</span></a>
                <div class="author-wrapper">
                  <a class="author">
                    <img class="author-avatar" :src="item.avatar" />
                    <span class="name">{{ item.username }}</span>
                  </a>
                  <span class="like-wrapper like-active">
                    <i class="iconfont icon-follow-fill" :style="{ width: '1em', height: '1em', color: item.isLike ? 'red' : 'black' }" v-if="item.isLike"></i>
                    <i class="iconfont icon-follow" style="width: 1em; height: 1em" v-else></i>
                    <span class="count">{{ item.likeCount }}</span>
                  </span>
                </div>
              </div>
              <div class="top-tag-area" v-show="type === 1 && item.pinned === '1'">
                <div class="top-wrapper">置顶</div>
              </div>
            </div>
          </template>
        </el-skeleton>
      </template>
    </Waterfall>
  </div>

  <Main
      v-show="mainShow"
      :nid="nid"
      :nowTime="new Date()"
      class="animate__animated animate__zoomIn animate__delay-0.5s"
      @click-main="close"
  ></Main>

  <ProductDetail
      v-model:visible="productVisible"
      :productId="currentProductId"
      @refresh="handleRefresh"
  />

</template>

<script lang="ts" setup>
import { Waterfall } from "vue-waterfall-plugin-next";
import "vue-waterfall-plugin-next/dist/style.css";
import { ref, onMounted, watch } from "vue";
import { getTrendByUser } from "@/api/user";
import Main from "@/views/main/main.vue";
import ProductDetail from "@/views/shop/ProductDetail.vue";
import { options } from "@/constant/constant";
import { useRoute } from "vue-router";
import { useUserStore } from "@/store/userStore";
import { ElMessage } from "element-plus";

const route = useRoute();
const props = defineProps({
  type: { type: Number, default: 1 },
});

// --- 状态变量 ---
const noteList = ref<Array<any>>([]);
const noteTotal = ref(0);
const uid = route.query.uid as string;
const currentPage = ref(1);
const pageSize = 10;
const nid = ref("");
const mainShow = ref(false);
const userStore = useUserStore();
const currentUid = userStore.getUserInfo().id;

// 🚩 控制高级商品组件的状态
const productVisible = ref(false);
const currentProductId = ref<string | number>("");

// --- 逻辑方法 ---
const handleLoad = (item: any) => { item.isLoading = true; };
const close = () => { mainShow.value = false; };

// 🚩 修正后的详情跳转逻辑
const toDetail = (item: any) => {
  if (props.type === 4) {
    // 自动兼容各种 ID 命名
    const targetId = item.id || item.productId || item.goodsId;
    if (!targetId) {
      ElMessage.error("商品ID缺失，无法打开详情");
      return;
    }
    currentProductId.value = targetId;
    productVisible.value = true;
  } else {
    nid.value = item.id;
    mainShow.value = true;
  }
};

// 🚩 刷新回调：当商品下架或修改后，刷新瀑布流
const handleRefresh = () => {
  currentPage.value = 1;
  noteList.value = [];
  getNoteList(props.type);
};

const setData = (res: any) => {
  const { records, total } = res.data;
  noteTotal.value = total;
  const filteredRecords = records.filter((item: any) => {
    return item.uid === currentUid || (item.auditStatus !== "0" && item.auditStatus !== "2");
  });
  noteList.value.push(...filteredRecords);
};

const getNoteList = (type: number) => {
  getTrendByUser(currentPage.value, pageSize, uid, type).then((res) => {
    setData(res);
  });
};

const loadMoreData = () => {
  currentPage.value += 1;
  getNoteList(props.type);
};

watch(() => props.type, (newType) => {
  currentPage.value = 1;
  noteList.value = [];
  getNoteList(newType);
});

onMounted(() => { getNoteList(1); });
</script>

<style lang="less" scoped>
/* 瀑布流基础样式 */
.image-container { position: relative; display: inline-block; cursor: pointer; }
.overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); color: white; display: flex; align-items: center; justify-content: center; border-radius: 8px; font-size: 20px; pointer-events: none; }
.overlay.not-passed { color: #ff2442; }

.feeds-container {
  position: relative;
  margin: 0 auto;
  .card { position: relative; background: #fff; border-radius: 8px; overflow: hidden; margin-bottom: 12px; }
  .top-tag-area { position: absolute; left: 12px; top: 12px; z-index: 4; .top-wrapper { background: #ff2442; border-radius: 999px; color: #fff; font-size: 12px; padding: 5px 8px; } }
  .footer {
    padding: 12px;
    .title { font-size: 14px; color: #333; margin-bottom: 8px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
    .author-wrapper {
      display: flex; align-items: center; justify-content: space-between; font-size: 12px; color: #666;
      .author { display: flex; align-items: center; .author-avatar { width: 20px; height: 20px; border-radius: 50%; margin-right: 6px; } }
      .like-wrapper { display: flex; align-items: center; .count { margin-left: 2px; } }
    }
  }
}
</style>