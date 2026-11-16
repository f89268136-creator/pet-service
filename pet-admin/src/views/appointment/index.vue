<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar">
        <label>预约单号：</label>
        <el-input
          v-model="query.number"
          placeholder="请输入预约单号"
          style="width: 180px"
          clearable
          @keyup.enter.native="search"
        />
        <label style="margin-left: 12px">手机号：</label>
        <el-input
          v-model="query.phone"
          placeholder="请输入手机号"
          style="width: 150px"
          clearable
          @keyup.enter.native="search"
        />
        <label style="margin-left: 12px">服务日期：</label>
        <el-date-picker
          v-model="query.serviceDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="选择日期"
          style="width: 150px"
        />
        <label style="margin-left: 12px">状态：</label>
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button type="primary" style="margin-left: 12px" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
        <el-button type="success" style="float: right" @click="openVerify()">核销到店</el-button>
      </div>

      <el-table v-if="tableData.length" :data="tableData" stripe class="tableBox">
        <el-table-column prop="number" label="预约单号" width="190" />
        <el-table-column label="宠物" width="150">
          <template slot-scope="scope">{{ scope.row.petName }}（{{ scope.row.petSpecies }}）</template>
        </el-table-column>
        <el-table-column prop="serviceName" label="服务项目" min-width="120" />
        <el-table-column prop="storeName" label="门店" min-width="150" />
        <el-table-column label="预约时间" width="190">
          <template slot-scope="scope">{{ scope.row.serviceDate }} {{ scope.row.serviceTime }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="90" />
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="statusType(scope.row.status)" size="small">{{ scope.row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="270" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="showDetail(scope.row)">详情</el-button>
            <el-button v-if="scope.row.status === 2" type="text" size="small" @click="confirmHandle(scope.row)">确认</el-button>
            <el-button v-if="scope.row.status === 3" type="text" size="small" @click="openVerify(scope.row)">核销</el-button>
            <el-button v-if="scope.row.status === 4" type="text" size="small" @click="completeHandle(scope.row)">完成</el-button>
            <el-button v-if="scope.row.status <= 3" type="text" size="small" class="delBut" @click="openCancel(scope.row)">取消</el-button>
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

      <el-dialog title="预约详情" :visible.sync="detailVisible" width="620px">
        <div v-if="detail" class="detail-box">
          <p><span>预约单号：</span>{{ detail.number }}</p>
          <p><span>状态：</span>{{ detail.statusText }}</p>
          <p><span>宠物：</span>{{ detail.petName }} / {{ detail.petBreed }}</p>
          <p><span>主人：</span>{{ detail.userName || '-' }} {{ detail.phone || '' }}</p>
          <p><span>服务项目：</span>{{ detail.serviceName }}</p>
          <p><span>金额：</span>{{ detail.amount }}</p>
          <p><span>门店：</span>{{ detail.storeName }}</p>
          <p><span>地址：</span>{{ detail.storeAddress }}</p>
          <p><span>服务时间：</span>{{ detail.serviceDate }} {{ detail.serviceTime }}</p>
          <p><span>核销码：</span>{{ detail.verifyCode || '-' }}</p>
          <p><span>下单时间：</span>{{ detail.orderTime }}</p>
          <p><span>核销时间：</span>{{ detail.verifyTime || '-' }}</p>
          <p><span>备注：</span>{{ detail.remark || '-' }}</p>
        </div>
      </el-dialog>

      <el-dialog title="核销到店" :visible.sync="verifyVisible" width="380px">
        <el-input v-model="verifyCode" placeholder="请输入 6 位核销码" maxlength="6" />
        <span slot="footer">
          <el-button @click="verifyVisible = false">取消</el-button>
          <el-button type="primary" @click="submitVerify">确认核销</el-button>
        </span>
      </el-dialog>

      <el-dialog title="取消预约" :visible.sync="cancelVisible" width="420px">
        <el-input v-model="cancelReason" type="textarea" :rows="3" placeholder="请输入取消原因" />
        <span slot="footer">
          <el-button @click="cancelVisible = false">返回</el-button>
          <el-button type="danger" @click="submitCancel">确认取消</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import Empty from '@/components/Empty/index.vue'
import {
  cancelAppointment,
  completeAppointment,
  confirmAppointment,
  getAppointmentDetail,
  getAppointmentPage,
  verifyAppointment
} from '@/api/appointment'

@Component({
  name: 'Appointment',
  components: { Empty }
})
export default class extends Vue {
  private query = {
    number: '',
    phone: '',
    serviceDate: '',
    status: null as number | null
  }
  private tableData: any[] = []
  private counts = 0
  private page = 1
  private pageSize = 10
  private detailVisible = false
  private detail: any = null
  private verifyVisible = false
  private verifyCode = ''
  private cancelVisible = false
  private cancelId = 0
  private cancelReason = ''
  private statusOptions = [
    { label: '待支付', value: 1 },
    { label: '待确认', value: 2 },
    { label: '待服务', value: 3 },
    { label: '服务中', value: 4 },
    { label: '已完成', value: 5 },
    { label: '已取消', value: 6 }
  ]

  created() {
    this.init()
  }

  private async init() {
    const params: any = { page: this.page, pageSize: this.pageSize }
    if (this.query.number) params.number = this.query.number
    if (this.query.phone) params.phone = this.query.phone
    if (this.query.serviceDate) params.serviceDate = this.query.serviceDate
    if (this.query.status !== null) params.status = this.query.status
    await getAppointmentPage(params)
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
    this.query = { number: '', phone: '', serviceDate: '', status: null }
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
    const map: any = { 1: 'warning', 2: 'info', 3: '', 4: '', 5: 'success', 6: 'danger' }
    return map[status] || 'info'
  }

  private showDetail(row: any) {
    getAppointmentDetail(row.id).then((res: any) => {
      if (String(res.data.code) === '1') {
        this.detail = res.data.data
        this.detailVisible = true
      } else {
        this.$message.error(res.data.msg || '查询失败')
      }
    })
  }

  private confirmHandle(row: any) {
    this.$confirm(`确认预约单 ${row.number}？`, '提示', { type: 'warning' })
      .then(() => this.operate(confirmAppointment(row.id), '已确认预约'))
      .catch(() => {})
  }

  private completeHandle(row: any) {
    this.$confirm(`确认服务已完成？`, '提示', { type: 'warning' })
      .then(() => this.operate(completeAppointment(row.id), '服务已完成'))
      .catch(() => {})
  }

  private openVerify(row?: any) {
    this.verifyCode = row && row.verifyCode ? row.verifyCode : ''
    this.verifyVisible = true
  }

  private submitVerify() {
    if (!this.verifyCode) {
      this.$message.error('请输入核销码')
      return
    }
    this.operate(verifyAppointment(this.verifyCode), '核销成功').then(() => {
      this.verifyVisible = false
    })
  }

  private openCancel(row: any) {
    this.cancelId = row.id
    this.cancelReason = ''
    this.cancelVisible = true
  }

  private submitCancel() {
    if (!this.cancelReason.trim()) {
      this.$message.error('请输入取消原因')
      return
    }
    this.operate(cancelAppointment(this.cancelId, this.cancelReason), '预约已取消').then(() => {
      this.cancelVisible = false
    })
  }

  private async operate(promise: Promise<any>, successText: string) {
    const res: any = await promise
    if (String(res.data.code) === '1') {
      this.$message.success(successText)
      this.init()
    } else {
      this.$message.error(res.data.msg || '操作失败')
    }
  }
}
</script>

<style lang="scss" scoped>
.tableBar {
  margin-bottom: 20px;
  white-space: nowrap;
}
.detail-box p {
  margin: 8px 0;
  line-height: 22px;
}
.detail-box span {
  display: inline-block;
  width: 90px;
  color: #666;
  text-align: right;
}
</style>
