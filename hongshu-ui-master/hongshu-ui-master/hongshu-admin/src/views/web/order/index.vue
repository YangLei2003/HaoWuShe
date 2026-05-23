<template>
  <div class="app-container">
    <el-table v-loading="loading" :data="orderList">
      <el-table-column label="订单编号" prop="orderNo" />
      <el-table-column label="商品ID" prop="productId" />
      <el-table-column label="买家ID" prop="buyerId" />
      <el-table-column label="卖家ID" prop="sellerId" />
      <el-table-column label="金额" prop="amount" />
      <el-table-column label="状态">
        <template #default="scope">
          <el-tag v-if="scope.row.status == 0" type="warning">待付款</el-tag>
          <el-tag v-else-if="scope.row.status == 1" type="success">已付款</el-tag>
          <el-tag v-else-if="scope.row.status == 2" type="primary">已完成</el-tag>
          <el-tag v-else type="danger">已取消</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付时间" prop="payTime" />
      <el-table-column label="创建时间" prop="createTime" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 修改订单对话框 -->
    <el-dialog title="修改订单" v-model="dialogVisible" width="30%">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="订单编号">
          <el-input v-model="formData.orderNo"  />
        </el-form-item>
        <el-form-item label="商品ID">
          <el-input v-model="formData.productId"  />
        </el-form-item>
        <el-form-item label="买家ID">
          <el-input v-model="formData.buyerId"  />
        </el-form-item>
        <el-form-item label="卖家ID">
          <el-input v-model="formData.sellerId"  />
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="formData.amount" :precision="2" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status" placeholder="请选择状态">
            <el-option label="待付款" :value="0" />
            <el-option label="已付款" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付时间">
          <el-date-picker
              v-model="formData.payTime"
              type="datetime"
              placeholder="选择支付时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DDTHH:mm:ss.SSSZ" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUpdate">确认修改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { listOrder, delOrder, updateOrder } from "@/api/web/order";
import { ElMessage } from 'element-plus'

const orderList = ref([]);
const loading = ref(false);
const total = ref(0);
const dialogVisible = ref(false);
const queryParams = ref({
  pageNum: 1,
  pageSize: 10
});

// 表单数据
const formData = ref({
  id: null,
  orderNo: '',
  productId: null,
  buyerId: null,
  sellerId: null,
  amount: 0,
  status: 0,
  payTime: ''
});

function getList() {
  loading.value = true;
  listOrder(queryParams.value).then(res => {
    orderList.value = res.rows;
    total.value = res.total;
    loading.value = false;
  });
}

function handleDelete(row) {
  delOrder(row.id).then(() => {
    ElMessage.success('删除成功');
    getList();
  });
}

// 打开修改对话框
function handleUpdate(row) {
  // 复制要修改的数据到表单
  formData.value = {
    id: row.id,
    orderNo: row.orderNo,
    productId: row.productId,
    buyerId: row.buyerId,
    sellerId: row.sellerId,
    amount: row.amount,
    status: row.status,
    payTime: row.payTime || ''
  };
  dialogVisible.value = true;
}

// 提交修改
function submitUpdate() {
  updateOrder(formData.value).then(() => {
    ElMessage.success('修改成功');
    dialogVisible.value = false;
    getList();
  }).catch(() => {
    ElMessage.error('修改失败');
  });
}

getList();
</script>