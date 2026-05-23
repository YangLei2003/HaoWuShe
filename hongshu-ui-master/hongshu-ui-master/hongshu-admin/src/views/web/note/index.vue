<template>
  <div class="app-container xhs-page">
    <div class="search-wrapper">
      <el-form
          :model="queryParams"
          ref="queryRef"
          :inline="true"
          v-show="showSearch"
          class="xhs-search-form"
      >
        <div class="search-inputs">
          <el-form-item label="笔记标题" prop="title">
            <el-input
                v-model="queryParams.title"
                placeholder="搜索标题"
                clearable
                class="xhs-input"
                @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="作者" prop="uid">
            <el-input
                v-model="queryParams.author"
                placeholder="搜作者"
                clearable
                class="xhs-input-small"
                @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="笔记类型" prop="noteType">
            <el-select
                v-model="queryParams.noteType"
                placeholder="类型"
                @change="handleQuery"
                clearable
                class="xhs-select"
            >
              <el-option
                  v-for="dict in note_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="auditStatus">
            <el-select
                v-model="queryParams.auditStatus"
                placeholder="状态"
                @change="handleQuery"
                clearable
                class="xhs-select"
            >
              <el-option
                  v-for="dict in audit_status"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="创建时间" class="xhs-date-picker">
            <el-date-picker
                v-model="dateRange"
                value-format="YYYY-MM-DD"
                type="daterange"
                range-separator="-"
                start-placeholder="开始"
                end-placeholder="结束"
            />
          </el-form-item>
        </div>

        <div class="search-actions">
          <el-button type="primary" icon="Search" @click="handleQuery" class="btn-xhs-red">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery" class="btn-xhs-gray">重置</el-button>
          <el-divider direction="vertical" />
          <el-button
              type="warning"
              plain
              icon="Download"
              @click="handleExport"
              v-hasPermi="['web:note:export']"
              class="btn-xhs-plain"
          >导出</el-button>
          <el-button type="danger" icon="refresh" @click="refreshNote" class="btn-xhs-danger">ES重置</el-button>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" class="xhs-toolbar" />
        </div>
      </el-form>
    </div>

    <div class="table-wrapper">
      <el-table
          v-loading="loading"
          :data="noteList"
          @selection-change="handleSelectionChange"
          class="xhs-modern-table"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="ID" align="center" prop="id" width="70" />

        <el-table-column label="笔记封面" align="center" width="110">
          <template #default="scope">
            <div class="cover-container">
              <el-image
                  class="xhs-cover"
                  :src="scope.row.noteCover"
                  fit="cover"
                  @mouseover="showPreview(scope.row.noteCover, $event)"
                  @mouseleave="hidePreview"
              />
            </div>
          </template>
        </el-table-column>

        <el-table-column label="笔记详情" width="200"> <template #default="scope">
          <div class="note-detail-compact">
            <div class="note-title-text">{{ scope.row.title }}</div>
            <div class="note-meta-inline">
              <el-tag size="small" class="type-tag-mini">{{ categoryFormatter(scope.row) }}</el-tag>
              <span class="author-name-mini">{{ scope.row.author }}</span>
            </div>
          </div>
        </template>
        </el-table-column>

        <el-table-column label="内容摘要" prop="content" width="180">
          <template #default="scope">
            <el-tooltip :content="scope.row.content" placement="top" :disabled="scope.row.content.length <= 15">
              <span class="content-preview">{{ scope.row.content }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="置顶" align="center" width="80">
          <template #default="scope">
            <dict-tag :options="pinned" :value="scope.row.pinned" />
          </template>
        </el-table-column>

        <el-table-column label="状态" align="center" width="90">
          <template #default="scope">
            <dict-tag :options="audit_status" :value="scope.row.auditStatus" />
          </template>
        </el-table-column>

        <el-table-column label="数据统计" align="center" width="220">
          <template #default="scope">
            <div class="stats-group">
              <span><el-icon><View /></el-icon> {{ scope.row.viewCount }}</span>
              <span><el-icon><Star /></el-icon> {{ scope.row.likeCount }}</span>
              <span><el-icon><CollectionTag /></el-icon> {{ scope.row.collectionCount }}</span>
              <span><el-icon><ChatDotRound /></el-icon> {{ scope.row.commentCount }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="更新时间" align="center" width="160">
          <template #default="scope">
            <span class="time-text">{{ parseTime(scope.row.updateTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" fixed="right" width="140">
          <template #default="scope">
            <div class="op-btns">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">详情</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">移除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <pagination
          v-show="total > 0"
          :total="total"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
          class="xhs-pagination"
      />
    </div>

    <div v-if="previewVisible" class="image-preview" :style="previewStyle">
      <img :src="previewSrc" alt="Preview" />
    </div>

    <el-dialog :title="title" v-model="open" width="600px" append-to-body class="xhs-dialog">
      <el-form ref="noteRef" :model="form" :rules="rules" label-width="90px" class="xhs-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类" prop="cpid">
              <el-tree-select
                  v-model="form.cpid"
                  :data="navbarOptions"
                  :props="{ value: 'id', label: 'title', children: 'children' }"
                  value-key="id"
                  placeholder="选择分类"
                  check-strictly
                  class="full-width"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="笔记标题" prop="title">
              <el-input v-model="form.title" placeholder="输入标题" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="笔记封面" prop="noteCover">
          <el-upload
              class="image-uploader xhs-uploader"
              list-type="picture-card"
              :show-file-list="false"
              :auto-upload="false"
              :on-change="handleChange"
          >
            <img v-if="form.noteCover" :src="form.noteCover" class="image" />
            <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="笔记类型" prop="noteType">
          <el-radio-group v-model="form.noteType" class="xhs-radio-group">
            <el-radio v-for="dict in note_type" :key="dict.value" :label="dict.value" border>{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否置顶" prop="pinned">
              <el-radio-group v-model="form.pinned">
                <el-radio v-for="dict in pinned" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="审核状态" prop="auditStatus">
              <el-radio-group v-model="form.auditStatus">
                <el-radio v-for="dict in audit_status" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel" class="btn-round">取消</el-button>
          <el-button type="primary" @click="submitForm" class="btn-round btn-xhs-red">保存修改</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Note">
/* JS代码完全保持一致，未做任何逻辑删减 */
import { listNote, addNote, delNote, getNote, updateNote, refreshNoteDate } from "@/api/web/note";
import { listNavbar } from "@/api/web/navbar";
import { ElMessageBox, ElMessage } from "element-plus";

const { proxy } = getCurrentInstance();
const { audit_status, note_type, pinned } = proxy.useDict("audit_status", "note_type", "pinned");

const route = useRoute();
const blogSort = ref(route.query.blogSort);
const noteList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);
const previewVisible = ref(false);
const previewSrc = ref("");
const previewStyle = reactive({ top: "0px", left: "0px" });
const navbarOptions = ref([]);
const categoriesMap = ref({});

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    title: undefined,
    noteType: undefined,
    auditStatus: undefined,
    pid: "",
    author: undefined,
  },
  rules: {
    title: [{ required: true, message: "笔记标题不能为空", trigger: "blur" }],
    noteType: [{ required: true, message: "笔记类型不能为空", trigger: "blur" }],
  },
});

const { queryParams, form, rules } = toRefs(data);

function showPreview(src, event) {
  previewSrc.value = src;
  previewVisible.value = true;
  previewStyle.top = event.clientY + 10 + "px";
  previewStyle.left = event.clientX + 10 + "px";
}
function hidePreview() { previewVisible.value = false; previewSrc.value = ""; }
function refreshNote() {
  ElMessageBox.confirm("是否确认重置ES数据？", "提示", { type: "warning" }).then(() => {
    refreshNoteDate().then(() => { ElMessage.success("重置成功"); getList(); });
  }).catch(() => {});
}
function getList() {
  loading.value = true;
  listNote(proxy.addDateRange(queryParams.value, dateRange.value)).then((response) => {
    noteList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}
function getTreeSelect() {
  navbarOptions.value = [];
  listNavbar().then((response) => {
    const navbar = { id: 0, title: "主类目", children: [], disabled: true };
    navbar.children = proxy.handleTree(response.data, "id", "pid");
    navbarOptions.value.push(navbar);
    createCategoriesMap(response.data);
  });
}
function createCategoriesMap(data) {
  categoriesMap.value = {};
  data.forEach((item) => { categoriesMap.value[item.id] = item.title; });
}
function categoryFormatter(row) { return categoriesMap.value[row.cpid] || "默认分类"; }
function cancel() { open.value = false; reset(); }
function reset() {
  form.value = { title: undefined, type: undefined, status: "0" };
  proxy.resetForm("noteRef");
}
function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { dateRange.value = []; proxy.resetForm("queryRef"); handleQuery(); }
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}
function handleAdd() { reset(); getTreeSelect(); open.value = true; title.value = "添加笔记"; }
function handleUpdate(row) {
  reset(); getTreeSelect();
  const noteId = row.id || ids.value;
  getNote(noteId).then((response) => {
    form.value = response.data;
    open.value = true;
    title.value = "笔记详情";
  });
}
function handleChange(file) {
  const reader = new FileReader();
  reader.readAsDataURL(file.raw);
  reader.onload = (e) => {
    form.value.noteCover = e.target.result;
    form.value.file = file.raw;
  };
}
function submitForm() {
  proxy.$refs["noteRef"].validate((valid) => {
    if (valid) {
      let params = new FormData();
      params.append("file", form.value.file);
      params.append("note", JSON.stringify(form.value));
      if (form.value.id != undefined) {
        updateNote(params).then(() => { proxy.$modal.msgSuccess("修改成功,请等待管理员后台审核~"); open.value = false; getList(); });
      } else {
        addNote(params).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); });
      }
    }
  });
}
function handleDelete(row) {
  const noteIds = row.id || ids.value;
  proxy.$modal.confirm('确定移除笔记编号为"' + noteIds + '"的内容吗？').then(() => {
    return delNote(noteIds);
  }).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {});
}
function handleExport() {
  proxy.download("system/post/export", { ...queryParams.value }, `note_${new Date().getTime()}.xlsx`);
}

onMounted(() => {
  if (route.query.blogSort) {
    try {
      blogSort.value = JSON.parse(route.query.blogSort);
      queryParams.value.pid = blogSort.value.blogSortUid;
    } catch (e) { console.error(e); }
  }
  getList();
  getTreeSelect();
});
</script>

<style scoped lang="scss">
$xhs-red: #ff2442;
$xhs-bg: #f9f9f9;
$radius: 12px;

.xhs-page {
  background-color: $xhs-bg;
  padding: 20px;
}

/* 搜索卡片 */
.search-wrapper {
  background: #fff;
  padding: 20px 24px;
  border-radius: $radius;
  box-shadow: 0 2px 12px rgba(0,0,0,0.03);
  margin-bottom: 20px;

  .xhs-search-form {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: space-between;
  }

  .search-inputs {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    :deep(.el-form-item) {
      margin-bottom: 0;
      margin-right: 15px;
      .el-form-item__label { font-weight: 600; }
    }
  }

  .search-actions {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-top: 10px;
  }

  .xhs-input { width: 140px; }
  .xhs-input-small { width: 110px; }
  .xhs-select { width: 100px; }
}

/* 表格美化 */
.table-wrapper {
  background: #fff;
  padding: 20px;
  border-radius: $radius;
  box-shadow: 0 2px 12px rgba(0,0,0,0.03);
}

.xhs-modern-table {
  --el-table-row-hover-bg-color: #fff0f2;

  .cover-container {
    width: 60px;
    height: 80px;
    margin: 0 auto;
    border-radius: 6px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  }

  .xhs-cover {
    width: 100%;
    height: 100%;
    transition: transform 0.3s;
    &:hover { transform: scale(1.1); }
  }

  .note-detail-compact {
    display: flex;
    flex-direction: column;
    gap: 4px; /* 缩小标题和标签的间距 */

    .note-title-text {
      font-weight: 600;
      font-size: 13px; /* 稍微缩小字号 */
      color: #333;
      /* 强制单行显示，超出省略号 */
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      width: 100%;
    }

    .note-meta-inline {
      display: flex;
      align-items: center;
      gap: 8px;

      .type-tag-mini {
        height: 20px;
        padding: 0 4px;
        font-size: 10px; /* 极简标签 */
        border-radius: 4px;
        background: #fff0f2;
        color: #ff2442;
        border: none;
      }

      .author-name-mini {
        font-size: 11px;
        color: #999;
        /* 限制作者名字长度，防止挤开 */
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        max-width: 80px;
      }
    }
  }

  .content-preview {
    color: #666;
    font-size: 13px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .stats-group {
    display: flex;
    justify-content: center;
    gap: 12px;
    font-size: 12px;
    color: #888;
    span { display: flex; align-items: center; gap: 3px; }
  }
}

/* 按钮及其他组件 */
.btn-xhs-red { background: $xhs-red !important; border: none !important; border-radius: 20px !important; }
.btn-xhs-gray { background: #f5f5f5 !important; border: none !important; border-radius: 20px !important; color: #333 !important; }
.btn-xhs-danger { border-radius: 20px !important; }
.btn-xhs-plain { border-radius: 20px !important; }
.btn-round { border-radius: 20px !important; }

/* 预览框 */
.image-preview {
  position: fixed;
  z-index: 2000;
  border: 1px solid #eee;
  background: #fff;
  padding: 4px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  img { max-width: 240px; border-radius: 4px; }
}

/* 表单美化 */
.xhs-radio-group {
  :deep(.el-radio.is-bordered) {
    border-radius: 8px;
    &.is-checked { border-color: $xhs-red; background: #fff0f2; }
  }
}

.xhs-uploader {
  :deep(.el-upload--picture-card) {
    width: 100px; height: 130px; line-height: 140px; border-radius: 8px;
  }
  .image { width: 100px; height: 130px; object-fit: cover; border-radius: 8px; }
}
</style>