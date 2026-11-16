<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar">
        <label>活动名称：</label>
        <el-input
          v-model="query.keyword"
          placeholder="请输入活动名称"
          style="width: 180px"
          clearable
          @keyup.enter.native="search"
        />
        <label style="margin-left: 12px">状态：</label>
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
        <el-button type="primary" style="margin-left: 12px" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
        <el-button type="success" style="float: right" @click="openCreate">新增秒杀</el-button>
      </div>

      <el-table v-if="tableData.length" :data="tableData" stripe class="tableBox">
        <el-table-column prop="name" label="活动名称" min-width="150" />
        <el-table-column prop="setmealName" label="服务套餐" min-width="140" />
        <el-table-column label="原价 / 秒杀价" width="150">
          <template slot-scope="scope">
            <span style="text-decoration: line-through; color: #999">￥{{ scope.row.originalPrice }}</span>
            <span style="color: #f56c6c; margin-left: 6px">￥{{ scope.row.seckillPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存 / 剩余" width="110">
          <template slot-scope="scope">{{ scope.row.stock }} / {{ scope.row.remain }}</template>
        </el-table-column>
        <el-table-column prop="limitPerUser" label="限购" width="70" />
        <el-table-column label="活动时间" width="250">
          <template slot-scope="scope">
            <div>{{ scope.row.beginTime }}</div>
            <div>{{ scope.row.endTime }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="toggleStatus(scope.row)">
              {{ scope.row.status === 1 ? '停用' : '启用' }}
            </el-button>
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

      <el-dialog title="新增秒杀活动" :visible.sync="createVisible" width="520px">
        <el-form label-width="100px">
          <el-form-item label="活动名称">
            <el-input v-model="form.name" placeholder="请输入活动名称" maxlength="32" />
          </el-form-item>
          <el-form-item label="服务套餐">
            <el-select v-model="form.setmealId" placeholder="请选择服务套餐" style="width: 100%">
              <el-option v-for="item in servicePackages" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="秒杀价">
            <el-input-number v-model="form.seckillPrice" :min="0.01" :precision="2" :step="10" />
          </el-form-item>
          <el-form-item label="库存">
            <el-input-number v-model="form.stock" :min="1" :max="100000" />
          </el-form-item>
          <el-form-item label="每人限购">
            <el-input-number v-model="form.limitPerUser" :min="1" :max="10" />
          </el-form-item>
          <el-form-item label="开始时间">
            <el-date-picker
              v-model="form.beginTime"
              type="datetime"
              value-format="yyyy-MM-dd HH:mm"
              placeholder="开始时间"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="结束时间">
            <el-date-picker
              v-model="form.endTime"
              type="datetime"
              value-format="yyyy-MM-dd HH:mm"
              placeholder="结束时间"
              style="width: 100%"
            />
          </el-form-item>
        </el-form>
        <span slot="footer">
          <el-button @click="createVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCreate">保存</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import Empty from '@/components/Empty/index.vue'
import { createSeckill, getSeckillPage, updateSeckillStatus } from '@/api/seckill'
import { getServicePackagePage } from '@/api/servicePackage'

@Component({
  name: 'Seckill',
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
  private createVisible = false
  private servicePackages: any[] = []
  private form = this.emptyForm()

  created() {
    this.init()
    this.loadServicePackages()
  }

  private emptyForm() {
    return {
      name: '',
      setmealId: null as number | null,
      seckillPrice: 99,
      stock: 20,
      limitPerUser: 1,
      beginTime: '',
      endTime: ''
    }
  }

  private async init() {
    const params: any = { page: this.page, pageSize: this.pageSize }
    if (this.query.keyword) params.keyword = this.query.keyword
    if (this.query.status !== null) params.status = this.query.status
    await getSeckillPage(params)
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

  private loadServicePackages() {
    getServicePackagePage({ page: 1, pageSize: 100 }).then((res: any) => {
      if (String(res.data.code) === '1') {
        this.servicePackages = res.data.data.records || []
      }
    })
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

  private openCreate() {
    this.form = this.emptyForm()
    this.createVisible = true
  }

  private submitCreate() {
    if (!this.form.name || !this.form.setmealId || !this.form.beginTime || !this.form.endTime) {
      this.$message.error('请填写完整的活动信息')
      return
    }
    createSeckill(this.form).then((res: any) => {
      if (String(res.data.code) === '1') {
        this.$message.success('秒杀活动已创建')
        this.createVisible = false
        this.init()
      } else {
        this.$message.error(res.data.msg || '创建失败')
      }
    })
  }

  private toggleStatus(row: any) {
    const status = row.status === 1 ? 0 : 1
    updateSeckillStatus(row.id, status).then((res: any) => {
      if (String(res.data.code) === '1') {
        this.$message.success('状态已更新')
        this.init()
      } else {
        this.$message.error(res.data.msg || '操作失败')
      }
    })
  }
}
</script>