<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar">
        <label>门店 ID：</label>
        <el-input-number v-model="query.storeId" :min="1" :controls="false" style="width: 90px" />
        <label style="margin-left: 12px">服务项目：</label>
        <el-select v-model="query.serviceId" placeholder="请选择" style="width: 200px">
          <el-option v-for="item in services" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <label style="margin-left: 12px">日期：</label>
        <el-date-picker
          v-model="query.date"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="选择日期"
          style="width: 150px"
        />
        <el-button type="primary" style="margin-left: 12px" @click="init">查询</el-button>
        <el-button style="float: right" @click="openCreate">新增时段</el-button>
        <el-button type="primary" style="float: right; margin-right: 10px" @click="openBatch">批量生成</el-button>
      </div>

      <el-table v-if="tableData.length" :data="tableData" stripe class="tableBox">
        <el-table-column prop="slotDate" label="日期" width="130" />
        <el-table-column label="时段" width="150">
          <template slot-scope="scope">{{ scope.row.startTime }} - {{ scope.row.endTime }}</template>
        </el-table-column>
        <el-table-column prop="capacity" label="容量" width="90" />
        <el-table-column prop="booked" label="已预约" width="90" />
        <el-table-column label="剩余" width="90">
          <template slot-scope="scope">{{ Math.max(scope.row.capacity - scope.row.booked, 0) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '开放' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="toggleStatus(scope.row)">
              {{ scope.row.status === 1 ? '关闭' : '开放' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <Empty v-else :is-search="false" />

      <el-dialog title="新增时段" :visible.sync="createVisible" width="460px">
        <el-form label-width="100px">
          <el-form-item label="门店 ID">
            <el-input-number v-model="createForm.storeId" :min="1" :controls="false" />
          </el-form-item>
          <el-form-item label="服务项目">
            <el-select v-model="createForm.serviceId" placeholder="请选择" style="width: 100%">
              <el-option v-for="item in services" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="日期">
            <el-date-picker v-model="createForm.slotDate" type="date" value-format="yyyy-MM-dd" style="width: 100%" />
          </el-form-item>
          <el-form-item label="开始时间">
            <el-time-picker v-model="createForm.startTime" value-format="HH:mm:ss" placeholder="开始时间" style="width: 100%" />
          </el-form-item>
          <el-form-item label="结束时间">
            <el-time-picker v-model="createForm.endTime" value-format="HH:mm:ss" placeholder="结束时间" style="width: 100%" />
          </el-form-item>
          <el-form-item label="容量">
            <el-input-number v-model="createForm.capacity" :min="1" :max="50" />
          </el-form-item>
        </el-form>
        <span slot="footer">
          <el-button @click="createVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCreate">保存</el-button>
        </span>
      </el-dialog>

      <el-dialog title="批量生成时段" :visible.sync="batchVisible" width="520px">
        <el-form label-width="110px">
          <el-form-item label="门店 ID">
            <el-input-number v-model="batchForm.storeId" :min="1" :controls="false" />
          </el-form-item>
          <el-form-item label="服务项目">
            <el-select v-model="batchForm.serviceIds" multiple placeholder="可多选" style="width: 100%">
              <el-option v-for="item in services" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="开始日期">
            <el-date-picker v-model="batchForm.startDate" type="date" value-format="yyyy-MM-dd" style="width: 100%" />
          </el-form-item>
          <el-form-item label="连续天数">
            <el-input-number v-model="batchForm.days" :min="1" :max="30" />
          </el-form-item>
          <el-form-item label="每天开始">
            <el-time-picker v-model="batchForm.startTime" value-format="HH:mm:ss" style="width: 100%" />
          </el-form-item>
          <el-form-item label="每天结束">
            <el-time-picker v-model="batchForm.endTime" value-format="HH:mm:ss" style="width: 100%" />
          </el-form-item>
          <el-form-item label="时段长度">
            <el-input-number v-model="batchForm.intervalMinutes" :min="15" :max="240" :step="15" />
            <span style="margin-left: 8px">分钟</span>
          </el-form-item>
          <el-form-item label="容量">
            <el-input-number v-model="batchForm.capacity" :min="1" :max="50" />
          </el-form-item>
        </el-form>
        <span slot="footer">
          <el-button @click="batchVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBatch">生成</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import Empty from '@/components/Empty/index.vue'
import { queryServiceItemList } from '@/api/serviceItem'
import { batchCreateSlots, createSlot, getSlotList, updateSlotStatus } from '@/api/appointment'

@Component({
  name: 'AppointmentSlot',
  components: { Empty }
})
export default class extends Vue {
  private services: any[] = []
  private tableData: any[] = []
  private query = {
    storeId: 1,
    serviceId: null as number | null,
    date: this.formatDate(new Date(Date.now() + 86400000))
  }
  private createVisible = false
  private createForm = this.emptyCreateForm()
  private batchVisible = false
  private batchForm = {
    storeId: 1,
    serviceIds: [] as number[],
    startDate: this.formatDate(new Date(Date.now() + 86400000)),
    days: 1,
    startTime: '10:00:00',
    endTime: '18:00:00',
    intervalMinutes: 60,
    capacity: 1
  }

  created() {
    this.loadServices()
  }

  private emptyCreateForm() {
    return {
      storeId: 1,
      serviceId: null as number | null,
      slotDate: this.formatDate(new Date(Date.now() + 86400000)),
      startTime: '10:00:00',
      endTime: '11:00:00',
      capacity: 1,
      status: 1
    }
  }

  private formatDate(date: Date) {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  private loadServices() {
    queryServiceItemList({})
      .then((res: any) => {
        if (String(res.data.code) === '1') {
          this.services = res.data.data || []
          if (this.services.length > 0) {
            this.query.serviceId = this.services[0].id
            this.createForm.serviceId = this.services[0].id
            this.init()
          }
        }
      })
      .catch((err: any) => this.$message.error('加载服务项目失败：' + err.message))
  }

  private init() {
    if (!this.query.serviceId || !this.query.date) {
      this.$message.warning('请选择服务项目和日期')
      return
    }
    getSlotList({
      storeId: this.query.storeId,
      serviceId: this.query.serviceId,
      date: this.query.date
    })
      .then((res: any) => {
        if (String(res.data.code) === '1') {
          this.tableData = res.data.data || []
        } else {
          this.$message.error(res.data.msg || '查询失败')
        }
      })
      .catch((err: any) => this.$message.error('请求出错：' + err.message))
  }

  private openCreate() {
    this.createForm = this.emptyCreateForm()
    this.createForm.storeId = this.query.storeId
    this.createForm.serviceId = this.query.serviceId
    this.createForm.slotDate = this.query.date
    this.createVisible = true
  }

  private submitCreate() {
    if (!this.createForm.serviceId || !this.createForm.slotDate || !this.createForm.startTime || !this.createForm.endTime) {
      this.$message.error('请完整填写时段信息')
      return
    }
    createSlot(this.createForm)
      .then((res: any) => {
        if (String(res.data.code) === '1') {
          this.$message.success('时段已创建')
          this.createVisible = false
          this.init()
        } else {
          this.$message.error(res.data.msg || '创建失败')
        }
      })
      .catch((err: any) => this.$message.error('请求出错：' + err.message))
  }

  private openBatch() {
    this.batchForm.storeId = this.query.storeId
    this.batchForm.serviceIds = this.query.serviceId ? [this.query.serviceId] : []
    this.batchForm.startDate = this.query.date
    this.batchVisible = true
  }

  private submitBatch() {
    if (!this.batchForm.serviceIds.length) {
      this.$message.error('请选择服务项目')
      return
    }
    batchCreateSlots(this.batchForm)
      .then((res: any) => {
        if (String(res.data.code) === '1') {
          this.$message.success(`已生成 ${res.data.data} 个时段`)
          this.batchVisible = false
          this.init()
        } else {
          this.$message.error(res.data.msg || '生成失败')
        }
      })
      .catch((err: any) => this.$message.error('请求出错：' + err.message))
  }

  private toggleStatus(row: any) {
    const nextStatus = row.status === 1 ? 0 : 1
    updateSlotStatus(row.id, nextStatus)
      .then((res: any) => {
        if (String(res.data.code) === '1') {
          this.$message.success(nextStatus === 1 ? '时段已开放' : '时段已关闭')
          this.init()
        } else {
          this.$message.error(res.data.msg || '操作失败')
        }
      })
      .catch((err: any) => this.$message.error('请求出错：' + err.message))
  }
}
</script>

<style lang="scss" scoped>
.tableBar {
  margin-bottom: 20px;
  white-space: nowrap;
}
</style>
