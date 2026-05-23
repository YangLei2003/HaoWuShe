<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="商品名称" prop="productName">
        <el-input
            v-model="queryParams.productName"
            placeholder="请输入商品名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="id" width="80" />

      <el-table-column label="商品主图" align="center" prop="cover" width="100">
        <template #default="scope">
          <el-image
              style="width: 60px; height: 60px; border-radius: 5px"
              :src="scope.row.cover"
              :preview-src-list="[scope.row.cover]"
              preview-teleported
              fit="cover"
          >
            <template #error>
              <div class="image-slot"><el-icon><Picture /></el-icon></div>
            </template>
          </el-image>
        </template>
      </el-table-column>

      <el-table-column label="商品名称" align="center" prop="productName" :show-overflow-tooltip="true" />

      <el-table-column label="价格" align="center" prop="price">
        <template #default="scope">
          <span style="color: #f56c6c; font-weight: bold">￥{{ scope.row.price ? parseFloat(scope.row.price).toFixed(2) : '0.00' }}</span>
        </template>
      </el-table-column>

      <el-table-column label="库存" align="center" prop="stock" />

      <el-table-column label="描述" align="center" prop="description">
        <template #default="scope">
          <el-tooltip :content="scope.row.description || '暂无描述'" placement="top" :disabled="!(scope.row.description && scope.row.description.length > 10)">
            <span>{{ scope.row.description ? (scope.row.description.length > 10 ? scope.row.description.substring(0, 10) + '...' : scope.row.description) : '-' }}</span>
          </el-tooltip>
        </template>
      </el-table-column>

      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>

      <el-table-column label="发布时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ scope.row.createTime }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
    />

    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="productRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :precision="2" :step="1" :min="0" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Product">
// 🚩 注意：这里需要根据你真实的 api 文件路径修改
import { listProduct, getProduct, delProduct, addProduct, updateProduct, changeProductStatus } from "@/api/web/product";

const { proxy } = getCurrentInstance();

const productList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    productName: undefined,
    status: undefined
  },
  rules: {
    productName: [{ required: true, message: "商品名称不能为空", trigger: "blur" }],
    price: [{ required: true, message: "价格不能为空", trigger: "blur" }],
    stock: [{ required: true, message: "库存不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询列表 */
/** 查询列表 */
function getList() {
  loading.value = true;
  console.log("正在请求数据，参数为：", queryParams.value); // 打印参数
  listProduct(queryParams.value).then(response => {
    console.log("接口返回结果：", response); // <--- 关键：看看 response 到底是什么
    productList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  }).catch(error => {
    console.error("请求出错了：", error);
    loading.value = false;
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
    id: null,
    productName: null,
    price: 0,
    stock: 0,
    cover: null,
    description: null,
    status: 1
  };
  proxy.resetForm("productRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加商品";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const id = row.id || ids.value;
  getProduct(id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改商品";
  });
}

/** 状态修改 */
function handleStatusChange(row) {
  let text = row.status === 1 ? "上架" : "下架";
  proxy.$modal.confirm('确认要"' + text + '""' + row.productName + '"吗？').then(function() {
    return changeProductStatus(row.id, row.status);
  }).then(() => {
    proxy.$modal.msgSuccess(text + "成功");
  }).catch(function() {
    row.status = row.status === 1 ? 0 : 1;
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["productRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateProduct(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addProduct(form.value).then(response => {
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
  const productIds = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除商品编号为"' + productIds + '"的数据项？').then(function() {
    return delProduct(productIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

onMounted(() => {
  getList();
});
</script>