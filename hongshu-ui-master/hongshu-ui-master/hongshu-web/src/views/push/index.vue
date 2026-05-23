<template>
  <div class="container" id="container">
    <div v-if="isLogin" class="push-container">
      <div class="header"><span class="header-icon"></span><span class="header-title">发布图文</span></div>
      <div class="img-list">
        <el-upload
            v-model:file-list="fileList"
            action="http://localhost:88/api/util/oss/saveBatch/0"
            list-type="picture-card"
            multiple
            :limit="9"
            :headers="uploadHeader"
            :auto-upload="false"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
        <el-dialog v-model="dialogVisible">
          <img w-full :src="dialogImageUrl" alt="Preview Image" />
        </el-dialog>
      </div>
      <el-divider style="margin: 0.75rem; width: 96%" />
      <div class="push-content">
        <el-input v-model="note.title" maxlength="20" show-word-limit type="text" placeholder="请输入标题" class="input-title" />
        <el-input v-model="note.content" maxlength="250" show-word-limit :autosize="{ minRows: 4, maxRows: 5 }" type="textarea" placeholder="填写更全面的描述信息，让更多的人看到你吧❤️" />
        <div class="tag-list">
          <el-tag v-for="tag in dynamicTags" :key="tag" closable @close="handleClose(tag)" class="tag-item" type="danger">{{ tag }}</el-tag>
          <el-input v-if="inputVisible" ref="InputRef" v-model="inputValue" style="width: 3.125rem" size="small" @keyup.enter="handleInputConfirm" @blur="handleInputBlur" />
        </div>
      </div>
      <el-divider style="margin: 0.75rem; width: 96%" />
      <div class="categorys" style="display: flex; gap: 12px; align-items: center;">
        <el-cascader
            ref="CascaderRef"
            v-model="categoryList"
            :options="options"
            @change="handleChange"
            :props="props"
            placeholder="请选择分类"
            style="flex: 1;"
        />
        <el-select v-model="note.productId" placeholder="关联我的橱窗商品(可选)" clearable style="flex: 1;">
          <el-option v-for="item in myProducts" :key="item.id" :label="item.productName" :value="item.id">
            <span style="float: left">{{ item.productName }}</span>
            <span style="float: right; color: #ff2442; font-size: 13px; font-weight: bold;">￥{{ item.price }}</span>
          </el-option>
        </el-select>
      </div>
      <div class="submit">
        <el-button type="danger" loading :disabled="true" v-if="pushLoading">发布</el-button>
        <button class="publishBtn" @click="pubslish()" v-else><span class="btn-content">发布</span></button>
      </div>
    </div>
    <div v-else><el-empty description="用户未登录" /></div>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch, nextTick, onMounted } from "vue";
import { Plus } from "@element-plus/icons-vue";
import { useRoute } from "vue-router";
import type { UploadUserFile, CascaderProps, ElInput } from "element-plus";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/store/userStore";
import { getCategoryTreeData } from "@/api/category";
import { saveNoteByDTO, getNoteById, updateNoteByDTO } from "@/api/note";
import { getTagByKeyword } from "@/api/tag";
import { getFileFromUrl } from "@/utils/util";
import axios from "axios";

const props: CascaderProps = { label: "title", value: "id", checkStrictly: true };
const CascaderRef = ref<any>(null);
const userStore = useUserStore();
const route = useRoute();
const fileList = ref<UploadUserFile[]>([]);
const dialogImageUrl = ref("");
const dialogVisible = ref(false);

// 修复 headers 类型报错：显式指定类型为 Record<string, string>
const uploadHeader = ref<Record<string, string>>({
  accessToken: userStore.getToken() || "",
});

const categoryList = ref<Array<any>>([]);
const options = ref([]);
const note = ref<any>({});
const showTagState = ref(false);
const selectTagList = ref<Array<any>>([]);
const currentPage = ref(1);
const pageSize = 10;
const tagTotal = ref(0);
const pushLoading = ref(false);
const isLogin = ref(false);
const inputValue = ref("");
const dynamicTags = ref<Array<string>>([]);
const inputVisible = ref(false);
const InputRef = ref<InstanceType<typeof ElInput>>();
const hotTags = ref<Array<string>>([]);
const myProducts = ref<Array<any>>([]);

const handleClose = (tag: string) => { dynamicTags.value.splice(dynamicTags.value.indexOf(tag), 1); };
const handleInputBlur = () => { inputVisible.value = false; };
const showInput = () => {
  inputVisible.value = true;
  nextTick(() => { InputRef.value!.input!.focus(); addTag(); });
};
const handleInputConfirm = () => {
  if (inputValue.value) dynamicTags.value.push(inputValue.value);
  inputVisible.value = false;
  inputValue.value = "";
  showTagState.value = false;
};

watch(() => inputValue.value, () => { addTag(); });

onMounted(() => {
  if (!isLogin.value) return;
  document.getElementById("container")?.addEventListener("click", (e) => {
    const target = e.target as HTMLElement;
    const tagContainer = document.getElementById("tagContainer");
    if (tagContainer && !tagContainer.contains(target)) showTagState.value = false;
  });
});

const addTag = () => {
  selectTagList.value = [];
  currentPage.value = 1;
  setData();
  showTagState.value = true;
};
const setData = () => {
  getTagByKeyword(currentPage.value, pageSize, inputValue.value).then((res) => {
    const { records, total } = res.data;
    selectTagList.value.push(...records);
    tagTotal.value = total;
  });
};

const getMyProducts = () => {
  // 使用你 F12 看到的真实路径：/dev-api/web/product/list
  // 这样会自动走你的代理配置，解决跨域和端口问题
  axios.get('/dev-api/web/product/list', {
    headers: uploadHeader.value
  }).then(res => {
    // 调试用：看看接口返回的具体数据
    console.log("商品列表返回：", res.data);

    // 状态码 200 表示请求成功
    if (res.data && res.data.code === 200) {
      // 获取当前用户 ID
      const currentUid = userStore.getUserInfo().id;

      // 核心过滤逻辑：使用你后端定义的 sellerId
      const allProducts = res.data.data || [];
      myProducts.value = allProducts.filter((item: any) =>
          String(item.sellerId) === String(currentUid)
      );
    } else {
      ElMessage.error(res.data.msg || "获取商品列表失败");
    }
  }).catch(err => {
    console.error("请求商品接口出错：", err);
    ElMessage.error("商品接口连接异常");
  });
};

const handleChange = (ids: Array<any>) => {
  categoryList.value = ids;
  CascaderRef.value.togglePopperVisible();
};
const getHotTag = () => {
  getTagByKeyword(1, pageSize, "").then((res) => {
    const { records } = res.data;
    records.forEach((item: any) => { hotTags.value.push(item.title); });
  });
};

const getNoteByIdMethod = (noteId: string) => {
  getNoteById(noteId).then((res) => {
    const { data } = res;
    note.value = data;
    const urls = JSON.parse(data.urls);
    urls.forEach((item: string) => {
      const fileName = item.substring(item.lastIndexOf("/") + 1);
      getFileFromUrl(item, fileName).then((r: any) => {
        fileList.value.push({ name: fileName, url: item, raw: r });
      });
    });
    categoryList.value = [data.cpid, data.cid];
    data.tagList.forEach((item: any) => { dynamicTags.value.push(item.title); });
  });
};

const pubslish = () => {
  if (fileList.value.length <= 0) { ElMessage.error("请选择图片"); return; }
  if (!note.value.title?.trim()) { ElMessage.error("请输入标题"); return; }
  if (!note.value.content?.trim()) { ElMessage.error("请输入内容"); return; }

  pushLoading.value = true;
  let params = new FormData();
  fileList.value.forEach((f: any) => { params.append("uploadFiles", f.raw); });

  note.value.count = fileList.value.length;
  note.value.type = 1;
  note.value.cpid = categoryList.value[0];
  note.value.cid = categoryList.value[1];
  note.value.tagList = dynamicTags.value;

  const coverImage = new Image();
  coverImage.src = fileList.value[0].url!;
  coverImage.onload = () => {
    const size = coverImage.width / coverImage.height;
    note.value.noteCoverHeight = size >= 1.3 ? 200 : 300;
    params.append("noteData", JSON.stringify(note.value));
    if (note.value.id) updateNote(params);
    else saveNote(params);
  };
};

const updateNote = (p: FormData) => {
  updateNoteByDTO(p).then(() => { resetData(); ElMessage.success("修改成功"); })
      .catch(() => ElMessage.error("修改失败")).finally(() => pushLoading.value = false);
};
const saveNote = (p: FormData) => {
  saveNoteByDTO(p).then(() => { resetData(); ElMessage.success("发布成功"); })
      .catch(() => ElMessage.error("发布失败")).finally(() => pushLoading.value = false);
};
const resetData = () => {
  note.value = {}; categoryList.value = []; fileList.value = [];
  dynamicTags.value = []; pushLoading.value = false;
};

const initData = () => {
  isLogin.value = userStore.isLogin();
  if (isLogin.value) {
    const noteId = route.query.noteId as string;
    if (noteId) getNoteByIdMethod(noteId);
    getCategoryTreeData().then((res) => { options.value = res.data; });
    getHotTag();
    getMyProducts();
  }
};
initData();
</script>

<style lang="less" scoped>
/* 样式部分保持不变 */
:deep(.el-upload-list--picture-card .el-upload-list__item) { width: 100px; height: 100px; }
:deep(.el-upload--picture-card) { width: 100px; height: 100px; }
.container {
  flex: 1; padding-top: 72px;
  .push-container {
    margin: 1vw auto 0 11vw; width: 720px; border-radius: 8px; box-shadow: var(--el-box-shadow-lighter);
    .header { padding: 15px 20px; .header-icon { display: inline-block; width: 6px; height: 16px; background: #3a64ff; border-radius: 3px; margin-right: 3px; } }
    .img-list { width: 650px; margin: auto; padding: 6px; }
    .push-content { padding: 6px 12px; .input-title { margin-bottom: 12px; } }
    .categorys { padding: 0 12px 10px 12px; }
    .submit { padding: 10px 12px; text-align: center;
      .publishBtn { width: 100px; height: 36px; background-color: #ff2442; color: #fff; border-radius: 24px; border: none; cursor: pointer; transition: 0.3s; }
      .publishBtn:hover { transform: scale(1.05); }
    }
  }
}
</style>