<template>
  <div class="app-container">
    <el-form
        :model="queryParams"
        ref="queryRef"
        :inline="true"
        v-show="showSearch"
        class="search-form"
    >
      <el-form-item label="导航栏名称" prop="title">
        <el-input
            v-model="queryParams.title"
            placeholder="请输入导航栏名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
            v-model="queryParams.status"
            placeholder="导航栏状态"
            @change="handleQuery"
            clearable
            style="width: 200px"
        >
          <el-option
              v-for="dict in sys_normal_disable"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">
          搜索
        </el-button>
        <el-button icon="Refresh" @click="resetQuery"> 重置 </el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 toolbar">
      <el-col :span="1.5">
        <el-button
            type="primary"
            icon="Plus"
            @click="handleAdd"
            v-hasPermi="['web:category:add']"
            class="add-btn"
        >
          添加导航栏
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Sort" @click="toggleExpandAll" class="expand-btn">
          展开/折叠
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList">
      </right-toolbar>
    </el-row>

    <el-table
        v-if="refreshTable"
        v-loading="loading"
        :data="navbarList"
        row-key="id"
        :default-expand-all="isExpandAll"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        class="beauty-table"
        :header-cell-style="{ background: '#fafafa', color: '#333', fontWeight: 500 }"
    >
      <el-table-column
          prop="id"
          label="序号"
          :show-overflow-tooltip="true"
          width="150"
          align="center"
      >
      </el-table-column>
      <el-table-column
          prop="title"
          label="导航栏名称"
          :show-overflow-tooltip="true"
          width="150"
          align="center"
      >
      </el-table-column>

      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="60" align="center">
      </el-table-column>
      <el-table-column
          label="创建时间"
          align="center"
          width="180"
          prop="createTime"
      >
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
          label="更新时间"
          align="center"
          width="180"
          prop="updateTime"
      >
        <template #default="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
          label="操作"
          align="center"
          fixed="right"
          min-width="150"
          class-name="action-column"
      >
        <template #default="scope">
          <el-tooltip content="编辑" placement="top">
            <el-button
                type="primary"
                icon="Edit"
                size="small"
                @click="handleUpdate(scope.row)"
                v-hasPermi="['web:category:edit']"
                class="action-btn edit-btn"
            />
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button
                type="danger"
                icon="Delete"
                size="small"
                @click="handleDelete(scope.row)"
                v-hasPermi="['web:category:remove']"
                class="action-btn delete-btn"
            />
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加或修改导航栏对话框 -->
    <el-dialog :title="title" v-model="open" width="680px" append-to-body class="beauty-dialog">
      <el-form ref="navbarRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="13">
            <el-form-item label="上级分类" prop="pid">
              <el-tree-select
                  v-model="form.pid"
                  :data="navbarOptions"
                  :props="{
                  value: 'id',
                  label: 'title',
                  children: 'children',
                }"
                  value-key="id"
                  placeholder="选择上级分类"
                  check-strictly
                  class="beauty-select"
              />
            </el-form-item>
          </el-col>
          <el-col :span="13">
            <el-form-item label="导航栏名称" prop="title">
              <el-input v-model="form.title" placeholder="请输入导航栏名称" class="beauty-input" />
            </el-form-item>
          </el-col>
          <el-col :span="13">
            <el-form-item label="封面图" prop="normalCover">
              <el-upload
                  class="image-uploader"
                  list-type="picture-card"
                  :show-file-list="false"
                  :auto-upload="false"
                  :before-upload="beforeUpload"
                  :on-change="handleChange"
              >
                <img
                    v-if="form.normalCover"
                    :src="form.normalCover"
                    class="image"
                />
                <i v-else class="el-icon-plus image-uploader-icon">
                  <el-icon>
                    <Plus />
                  </el-icon>
                </i>
              </el-upload>
            </el-form-item>
          </el-col>
          <el-col :span="13">
            <el-form-item label="显示排序" prop="sort">
              <el-input-number v-model="form.sort" :min="0" class="beauty-number" />
            </el-form-item>
          </el-col>
          <el-col :span="13">
            <el-form-item>
              <template #label>
                <span> 导航栏状态 </span>
              </template>
              <el-radio-group v-model="form.status">
                <el-radio
                    v-for="dict in sys_normal_disable"
                    :key="dict.value"
                    :label="dict.value"
                >
                  {{ dict.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm" class="confirm-btn">确 定</el-button>
          <el-button @click="cancel" class="cancel-btn">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Navbar">
import {
  addNavbar,
  delNavbar,
  getNavbar,
  listNavbar,
  updateNavbar,
} from "@/api/web/navbar";

const { proxy } = getCurrentInstance();
const { sys_normal_disable } = proxy.useDict(
    "sys_show_hide",
    "sys_normal_disable"
);

const navbarList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const title = ref("");
const navbarOptions = ref([]);
const isExpandAll = ref(false);
const refreshTable = ref(true);

const data = reactive({
  form: {
    normalCover: "",
    file: null,
  },
  queryParams: {
    title: undefined,
    visible: undefined,
  },
  rules: {
    pid: [{ required: true, message: "请选择分类", trigger: "blur" }],
    title: [{ required: true, message: "导航栏名称不能为空", trigger: "blur" }],
    sort: [{ required: true, message: "导航栏顺序不能为空", trigger: "blur" }],
  },
});

const { queryParams, form, rules } = toRefs(data);

/** 查询导航栏列表 */
function getList() {
  loading.value = true;
  listNavbar(queryParams.value).then((response) => {
    navbarList.value = proxy.handleTree(response.data, "id", "pid");
    loading.value = false;
  });
}
/** 查询导航栏下拉树结构 */
function getTreeSelect() {
  navbarOptions.value = [];
  listNavbar().then((response) => {
    const navbar = { id: 0, title: "主类目", children: [] };
    navbar.children = proxy.handleTree(response.data, "id", "pid");
    navbarOptions.value.push(navbar);
  });
}
/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}
/** 表单重置 */
function reset() {
  form.value = {
    id: undefined,
    pid: 0,
    title: undefined,
    sort: undefined,
    isFrame: "1",
    visible: "0",
    status: "0",
  };
  proxy.resetForm("navbarRef");
}
/** 搜索按钮操作 */
function handleQuery() {
  getList();
}
/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}
/** 新增按钮操作 */
function handleAdd(row) {
  reset();
  getTreeSelect();
  if (row != null && row.id) {
    form.value.pid = row.id;
  } else {
    form.value.pid = 0;
  }
  open.value = true;
  title.value = "添加导航栏";
}
/** 展开/折叠操作 */
function toggleExpandAll() {
  refreshTable.value = false;
  isExpandAll.value = !isExpandAll.value;
  nextTick(() => {
    refreshTable.value = true;
  });
}
/** 修改按钮操作 */
async function handleUpdate(row) {
  reset();
  await getTreeSelect();
  getNavbar(row.id).then((response) => {
    form.value = response.data;
    form.value.pid = 0;
    open.value = true;
    title.value = "修改导航栏";
  });
}
function beforeUpload(file) {
  return false;
}
function handleChange(file) {
  const reader = new FileReader();
  reader.readAsDataURL(file.raw);
  reader.onload = (e) => {
    form.value.normalCover = e.target.result; // 设置预览图URL
    form.value.file = file.raw; // 存储选择的文件
  };
}
/** 提交按钮 */
function submitForm() {
  proxy.$refs["navbarRef"].validate((valid) => {
    if (valid) {
      let params = new FormData();
      params.append("file", form.value.file);
      params.append("category", JSON.stringify(form.value));
      if (form.value.id != undefined) {
        updateNavbar(params).then((response) => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addNavbar(params).then((response) => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}
/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal
      .confirm('是否确认删除名称为"' + row.title + '"的数据项?')
      .then(function () {
        return delNavbar(row.id);
      })
      .then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
      })
      .catch(() => {});
}

getList();
</script>

<style scoped>
/* 整体容器美化 */
.app-container {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 12px;
  min-height: 100%;
}

/* 搜索表单美化 */
.search-form {
  background: #ffffff;
  padding: 20px 20px 0 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.search-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.search-form :deep(.el-form-item__label) {
  color: #555;
  font-weight: 500;
}

/* 工具栏美化 */
.toolbar {
  background: #ffffff;
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 20px !important;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

/* 按钮样式 */
.add-btn {
  background: linear-gradient(135deg, #ff2442 0%, #ff4a63 100%);
  border: none;
  box-shadow: 0 2px 6px rgba(255, 36, 66, 0.2);
  transition: all 0.3s ease;
}

.add-btn:hover {
  background: linear-gradient(135deg, #ff4a63 0%, #ff5a73 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 36, 66, 0.3);
}

.expand-btn {
  border-color: #ffd4dc;
  color: #ff2442;
  transition: all 0.3s ease;
}

.expand-btn:hover {
  background-color: #fff5f6;
  border-color: #ff2442;
  color: #ff2442;
}

/* 表格美化 */
.beauty-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  background: #ffffff;
}

.beauty-table :deep(.el-table__header-wrapper) {
  background: #fafafa;
}

.beauty-table :deep(.el-table__row) {
  transition: all 0.2s ease;
}

.beauty-table :deep(.el-table__row:hover) {
  background-color: #fff5f6 !important;
}

.beauty-table :deep(.el-table__row:hover td) {
  background-color: #fff5f6 !important;
}

/* 操作按钮美化 */
.action-btn {
  margin: 0 4px;
  transition: all 0.2s ease;
}

.edit-btn {
  background-color: #fff0f2;
  border-color: #ffd4dc;
  color: #ff2442;
}

.edit-btn:hover {
  background-color: #ff2442;
  border-color: #ff2442;
  color: #ffffff;
  transform: scale(1.05);
}

.delete-btn {
  background-color: #fff0f2;
  border-color: #ffd4dc;
  color: #ff657a;
}

.delete-btn:hover {
  background-color: #ff657a;
  border-color: #ff657a;
  color: #ffffff;
  transform: scale(1.05);
}

/* 对话框美化 */
.beauty-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.12);
}

.beauty-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #fff5f6 0%, #ffffff 100%);
  padding: 20px 24px;
  border-bottom: 2px solid #ff2442;
}

.beauty-dialog :deep(.el-dialog__title) {
  color: #ff2442;
  font-weight: 600;
  font-size: 18px;
}

.beauty-dialog :deep(.el-dialog__headerbtn) {
  top: 20px;
  right: 20px;
}

.beauty-dialog :deep(.el-dialog__body) {
  padding: 24px;
  background: #ffffff;
}

.beauty-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

/* 表单元素美化 */
.beauty-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.2s ease;
  box-shadow: 0 0 0 1px #e0e0e0 inset;
}

.beauty-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #ff2442 inset;
}

.beauty-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(255, 36, 66, 0.2), 0 0 0 1px #ff2442 inset;
}

.beauty-select :deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.2s ease;
}

.beauty-select :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #ff2442 inset;
}

.beauty-number :deep(.el-input__wrapper) {
  border-radius: 8px;
}

/* 按钮美化 */
.confirm-btn {
  background: linear-gradient(135deg, #ff2442 0%, #ff4a63 100%);
  border: none;
  box-shadow: 0 2px 6px rgba(255, 36, 66, 0.2);
}

.confirm-btn:hover {
  background: linear-gradient(135deg, #ff4a63 0%, #ff5a73 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 36, 66, 0.3);
}

.cancel-btn {
  border-color: #e0e0e0;
  color: #666;
}

.cancel-btn:hover {
  border-color: #ff2442;
  color: #ff2442;
  background-color: #fff5f6;
}

/* 图片上传美化 */
.image-uploader .el-upload {
  border: 2px dashed #ffd4dc !important;
  border-radius: 12px !important;
  transition: all 0.3s ease;
}

.image-uploader .el-upload:hover {
  border-color: #ff2442 !important;
  background-color: #fff5f6;
}

.image-uploader-icon {
  font-size: 28px;
  color: #ffb3bd;
  width: 100%;
  height: 100%;
  line-height: 145px;
  margin-left: 1px;
  text-align: center;
}

.image {
  width: 100%;
  height: 100%;
  display: block;
  border-radius: 10px;
  object-fit: cover;
}

/* 树形选择器下拉框美化 */
:deep(.el-tree-select .el-tree-node__content:hover) {
  background-color: #fff5f6;
}

:deep(.el-tree-select .el-tree-node.is-current > .el-tree-node__content) {
  background-color: #fff0f2;
  color: #ff2442;
}

/* 单选框美化 */
:deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: #ff2442;
  border-color: #ff2442;
}

:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #ff2442;
}

:deep(.el-radio:hover .el-radio__inner) {
  border-color: #ff2442;
}

/* 标签tag美化 */
:deep(.el-tag) {
  border-radius: 12px;
  padding: 0 12px;
  height: 26px;
  line-height: 24px;
}

:deep(.el-tag--success) {
  background-color: #f0f9f0;
  border-color: #b8e0b8;
  color: #1fb416;
}

:deep(.el-tag--danger) {
  background-color: #fff5f6;
  border-color: #ffd4dc;
  color: #ff2442;
}
</style>