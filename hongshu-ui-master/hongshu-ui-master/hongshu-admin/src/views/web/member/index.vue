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
          <el-form-item label="好物人号" prop="hsId">
            <el-input
                v-model="queryParams.hsId"
                placeholder="搜索ID"
                clearable
                class="xhs-input"
                @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="会员名称" prop="username">
            <el-input
                v-model="queryParams.username"
                placeholder="搜索名称"
                clearable
                class="xhs-input"
                @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input
                v-model="queryParams.phone"
                placeholder="联系电话"
                clearable
                class="xhs-input-phone"
                @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select
                v-model="queryParams.status"
                placeholder="状态"
                @change="handleQuery"
                clearable
                class="xhs-select"
            >
              <el-option
                  v-for="dict in sys_normal_disable"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="注册时间" class="xhs-date-picker">
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
              type="primary"
              icon="Plus"
              @click="handleAdd"
              v-hasPermi="['web:member:add']"
              class="btn-xhs-add-solid"
          >新增会员</el-button>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" class="xhs-toolbar" />
        </div>
      </el-form>
    </div>

    <div class="table-wrapper">
      <el-table
          v-loading="loading"
          :data="memberList"
          @selection-change="handleSelectionChange"
          class="xhs-modern-table"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="编号" align="center" prop="id" width="80" />
        <el-table-column label="头像" align="center" width="100">
          <template #default="scope">
            <el-avatar
                :size="50"
                :src="scope.row.avatar"
                class="xhs-avatar-shadow"
            />
          </template>
        </el-table-column>
        <el-table-column label="好物信息" width="180"> <template #default="scope">
          <div class="member-info">
            <div class="member-name">{{ scope.row.username }}</div>
            <div class="member-id">ID: {{ scope.row.hsId }}</div>
          </div>
        </template>
        </el-table-column>
        <el-table-column label="性别" align="center" width="80">
          <template #default="scope">
            <dict-tag :options="sys_user_sex" :value="scope.row.gender" />
          </template>
        </el-table-column>
        <el-table-column label="联系电话" align="center" prop="phone" width="130" />
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope">
            <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="注册时间" align="center" prop="createTime" width="160" sortable="custom">
          <template #default="scope">
            <span class="time-text">{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" fixed="right" width="150">
          <template #default="scope">
            <div class="op-btns">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog :title="title" v-model="open" width="550px" append-to-body class="xhs-dialog">
      <el-form ref="memberRef" :model="form" :rules="rules" label-width="90px" class="xhs-form">
        <el-form-item label="会员名称" prop="username">
          <el-input v-model="form.username" placeholder="给好物人起个名" />
        </el-form-item>
        <el-form-item label="头像上传" prop="avatar">
          <el-upload
              class="image-uploader"
              list-type="picture-card"
              :show-file-list="false"
              :auto-upload="false"
              :before-upload="beforeUpload"
              :on-change="handleChange"
          >
            <img v-if="form.avatar" :src="form.avatar" class="image" />
            <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio v-for="dict in sys_user_sex" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="输入手机号" />
        </el-form-item>
        <el-form-item label="密码设置" prop="password">
          <el-input v-model="form.password" placeholder="建议包含字母数字" show-password />
        </el-form-item>
        <el-form-item label="当前状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="好物备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="写点什么..." rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel" class="btn-round">取 消</el-button>
          <el-button type="primary" @click="submitForm" class="btn-round btn-xhs-red">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Member">
/* JS逻辑完全保留，未做任何功能性删减 */
import { listMember, addMember, delMember, getMember, updateMember } from "@/api/web/member";

const { proxy } = getCurrentInstance();
const { sys_normal_disable, sys_user_sex } = proxy.useDict("sys_normal_disable", "sys_user_sex");
const memberList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    username: undefined,
    phone: undefined,
    status: undefined,
  },
  rules: {
    username: [{ required: true, message: "会员名称不能为空", trigger: "blur" }],
    phone: [
      { required: true, message: "手机号不能为空", trigger: "blur" },
      { pattern: /^1[3456789]\d{9}$/, message: "请输入正确手机号", trigger: "blur" },
    ],
    password: [{ required: true, message: "密码不能为空", trigger: "blur" }],
  },
});

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listMember(proxy.addDateRange(queryParams.value, dateRange.value)).then((response) => {
    memberList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}
function cancel() { open.value = false; reset(); }
function reset() {
  form.value = { id: undefined, username: undefined, phone: undefined, password: undefined, status: "0", remark: undefined };
  proxy.resetForm("memberRef");
}
function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { dateRange.value = []; proxy.resetForm("queryRef"); handleQuery(); }
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}
function handleAdd() { reset(); open.value = true; title.value = "添加会员"; }
function handleUpdate(row) {
  reset();
  const memberId = row.id || ids.value;
  getMember(memberId).then((response) => {
    form.value = response.data;
    open.value = true;
    title.value = "修改会员";
  });
}
function beforeUpload(file) { return false; }
function handleChange(file) {
  const reader = new FileReader();
  reader.readAsDataURL(file.raw);
  reader.onload = (e) => {
    form.value.avatar = e.target.result;
    form.value.file = file.raw;
  };
}
function submitForm() {
  proxy.$refs["memberRef"].validate((valid) => {
    if (valid) {
      let params = new FormData();
      params.append("file", form.value.file);
      params.append("user", JSON.stringify(form.value));
      if (form.value.id != undefined) {
        updateMember(params).then(() => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addMember(params).then(() => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}
function handleDelete(row) {
  const memberIds = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除会员编号为"' + memberIds + '"的数据项？').then(() => {
    return delMember(memberIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}
getList();
</script>

<style scoped lang="scss">
/* 小红书风格全局变量 */
$xhs-red: #ff2442;
$xhs-black: #333333;
$xhs-gray: #666666;
$xhs-bg: #f9f9f9;
$radius: 12px;

.xhs-page {
  background-color: $xhs-bg;
  min-height: calc(100vh - 84px);
  padding: 20px;
}

/* 搜索栏卡片 */
.search-wrapper {
  background: #fff;
  padding: 24px;
  border-radius: $radius;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
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
      display: flex;
      align-items: center;

      .el-form-item__label {
        font-weight: 600;
        color: $xhs-black;
      }
    }
  }

  .search-actions {
    display: flex;
    align-items: center;
    margin-top: 10px;
    gap: 10px;
  }

  /* 输入组件窄化，适配大屏一行流 */
  .xhs-input { width: 130px; }
  .xhs-input-phone { width: 150px; }
  .xhs-select { width: 110px; }
}

/* 按钮样式美化 */
.btn-xhs-red {
  background-color: $xhs-red !important;
  border-color: $xhs-red !important;
  border-radius: 20px !important;
  padding: 8px 18px;
}

.btn-xhs-gray {
  background-color: #f5f5f5 !important;
  border: none !important;
  color: #333 !important;
  border-radius: 20px !important;
  padding: 8px 18px;
  &:hover { background-color: #eee !important; }
}

.btn-xhs-add-solid {
  background-color: $xhs-red !important;
  border-color: $xhs-red !important;
  color: #ffffff !important; /* 强制白色文字 */
  border-radius: 20px !important;
  padding: 8px 18px;
  font-weight: 600;
  box-shadow: 0 4px 10px rgba(255, 36, 66, 0.2);

  &:hover {
    background-color: #e0203d !important;
    transform: translateY(-1px);
  }
}

/* 表格包装 */
.table-wrapper {
  background: #fff;
  padding: 20px;
  border-radius: $radius;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
}

.xhs-modern-table {
  --el-table-header-bg-color: #fcfcfc;
  --el-table-row-hover-bg-color: #fff0f2;

  :deep(th.el-table__cell) {
    color: #333;
    font-weight: 700;
  }

  .member-info {
    text-align: left;
    max-width: 140px; /* 限制文字容器最大宽度 */

    .member-name {
      font-weight: 600;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis; /* 超出显示省略号 */
      white-space: nowrap;      /* 不换行 */
    }

    .member-id {
      font-size: 12px;
      color: #999;
      margin-top: 2px;
      font-family: monospace; /* ID类用等宽字体更有质感 */
    }
  }

  .xhs-avatar-shadow {
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    border: 2px solid #fff;
  }
}

/* 图片上传美化 */
.image-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: $radius;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    width: 120px;
    height: 120px;
    transition: 0.3s;
    &:hover { border-color: $xhs-red; }
  }
}
.image-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  line-height: 120px;
}
.image { width: 120px; height: 120px; object-fit: cover; }

.btn-round { border-radius: 20px !important; }

/* 响应式调整 */
@media screen and (max-width: 1600px) {
  .search-inputs {
    flex: 1;
    .xhs-input, .xhs-select { width: 110px; }
  }
}
</style>