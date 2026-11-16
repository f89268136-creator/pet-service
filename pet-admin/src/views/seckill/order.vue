<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar">
        <label>订单号 / 活动：</label>
        <el-input
          v-model="query.keyword"
          placeholder="请输入订单号或活动名称"
          style="width: 220px"
          clearable
          @keyup.enter.native="search"
        />
        <label style="margin-left: 12px">状态：</label>
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button type="primary" style="margin-left: 12px" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
        <el-button type="success" style="float: right" @click="verifyVisible = true">核销秒杀单</el-button>
      </div>

      <el-table v-if="tableData.length" :data="tableData" stripe class="tableBox">
        <el-table-column prop="orderNo" label="订单号" width="210" />
        <el-table-column prop="activityName" label="活动" min-width="140" />
        <el-table-column prop="setmealName" label="服务套餐" min-width="130" />
        <el-table-column prop="amount" label="金额" width="90" />
        <el-table-column prop="verifyCode" label="核销码" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="150" />
        <el-table-column label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="statusType(scope.row.status)" size="small">{{ scope.row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.status === 1"
              type="text"
              size="small"
              @click="openVerify(scope.row)"
            >核销</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      <Empty v-else :is-search="false" />

      <el-pagination
        v-if="counts > 0"
        class="pageList"
        :page-sizes="[10, 20, 30, 40]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="counts"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />

      <el-dialog title="核销秒杀订单" :visible.sync="verifyVisible" width="380px">
        <el-input v-model="verifyCode" placeholder="请输入 6 位核销码" maxlength="6" />
        <span slot="footer">
          <el-button @click="verifyVisible = false">取消</el-button>
          <el-button type="primary" @click="submitVerify">确认核销</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import Empty from '@/components/Empty/index.vue'
import { getSeckillOrderPage, verifySeckillOrder } from '@/api/seckill'

@Component({
  name: 'SeckillOrder',
  components: { Empty }
})
export default class extends Vue {
  private query = {
    keyword: '',
    status: null as number | null
  }
  private tableData: any[] = []
  private counts = 0
  private page = 1
  private pageSize = 10
  private verifyVisible = false
  private verifyCode = ''
  private statusOptions = [
    { label: '待支付', value: 0 },
    { label: '待使用', value: 1 },
    { label: '已使用', value: 2 },
    { label: '已取消', value: 3 }
  ]

  created() {
    this.init()
  }

  private async init() {
    const params: any = { page: this.page, pageSize: this.pageSize }
    if (this.query.keyword) params.keyword = this.query.keyword
    if (this.query.status !== null) params.status = this.query.status
    await getSeckillOrderPage(params)
      .then((res: any) => {
        if (String(res.data.code) === '1') {
          this.tableData = res.data.data.records || []
          this.counts = Number(res.data.data.total)
        } else {
          this.$message.error(res.data.msg || '查询失败')
        }
      })
      .catch((err: any) => this.$message.error('请求出错：' + err.message))
  }

  private search() {
    this.page = 1
    this.init()
  }

  private reset() {
    this.query = { keyword: '', status: null }
    this.search()
  }

  private handleSizeChange(size: number) {
    this.pageSize = size
    this.page = 1
    this.init()
  }

  private handleCurrentChange(current: number) {
    this.page = current
    this.init()
  }

  private statusType(status: number) {
    const map: any = { 0: 'warning', 1: '', 2: 'success', 3: 'info' }
    return map[status] || 'info'
  }

  private openVerify(row: any) {
    this.verifyCode = row.verifyCode || ''
    this.verifyVisible = true
  }

  private submitVerify() {
    if (!this.verifyCode) {
      this.$message.error('请输入核销码')
      return
    }
    verifySeckillOrder(this.verifyCode).then((res: any) => {
      if (String(res.data.code) === '1') {
        this.$message.success('核销成功')
        this.verifyVisible = false
        this.init()
      } else {
        this.$message.error(res.data.msg || '核销失败')
      }
    })
  }
}
</script>