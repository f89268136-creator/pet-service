<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar">
        <span>门店信息维护，坐标用于附近门店 GEO 检索。</span>
        <el-button type="primary" style="float: right" @click="syncGeo">重建 GEO 索引</el-button>
      </div>

      <el-table v-if="tableData.length" :data="tableData" stripe class="tableBox">
        <el-table-column prop="name" label="门店名称" min-width="160" />
        <el-table-column prop="address" label="地址" min-width="220" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column label="营业时间" width="140">
          <template slot-scope="scope">{{ scope.row.openTime }} - {{ scope.row.closeTime }}</template>
        </el-table-column>
        <el-table-column label="坐标" width="190">
          <template slot-scope="scope">{{ scope.row.longitude }}, {{ scope.row.latitude }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '营业中' : '停业' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="openEdit(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Empty v-else :is-search="false" />

      <el-dialog title="编辑门店" :visible.sync="editVisible" width="540px">
        <el-form label-width="100px">
          <el-form-item label="门店名称">
            <el-input v-model="form.name" maxlength="64" />
          </el-form-item>
          <el-form-item label="地址">
            <el-input v-model="form.address" maxlength="255" />
          </el-form-item>
          <el-form-item label="电话">
            <el-input v-model="form.phone" maxlength="20" />
          </el-form-item>
          <el-form-item label="经度">
            <el-input-number v-model="form.longitude" :precision="6" :step="0.001" :controls="false" style="width: 100%" />
          </el-form-item>
          <el-form-item label="纬度">
            <el-input-number v-model="form.latitude" :precision="6" :step="0.001" :controls="false" style="width: 100%" />
          </el-form-item>
          <el-form-item label="营业开始">
            <el-time-picker v-model="form.openTime" value-format="HH:mm:ss" style="width: 100%" />
          </el-form-item>
          <el-form-item label="营业结束">
            <el-time-picker v-model="form.closeTime" value-format="HH:mm:ss" style="width: 100%" />
          </el-form-item>
          <el-form-item label="简介">
            <el-input v-model="form.description" type="textarea" :rows="2" maxlength="255" />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :label="1">营业中</el-radio>
              <el-radio :label="0">停业</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <span slot="footer">
          <el-button @click="editVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit">保存</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import Empty from '@/components/Empty/index.vue'
import { getStoreList, syncStoreGeo, updateStore } from '@/api/store'

@Component({
  name: 'Store',
  components: { Empty }
})
export default class extends Vue {
  private tableData: any[] = []
  private editVisible = false
  private form: any = {}

  created() {
    this.init()
  }

  private async init() {
    await getStoreList()
      .then((res: any) => {
        if (String(res.data.code) === '1') {
          this.tableData = res.data.data || []
        } else {
          this.$message.error(res.data.msg || '查询失败')
        }
      })
      .catch((err: any) => this.$message.error('请求出错：' + err.message))
  }

  private openEdit(row: any) {
    this.form = { ...row }
    this.editVisible = true
  }

  private submitEdit() {
    if (!this.form.name || !this.form.address) {
      this.$message.error('门店名称和地址不能为空')
      return
    }
    updateStore(this.form).then((res: any) => {
      if (String(res.data.code) === '1') {
        this.$message.success('门店已更新')
        this.editVisible = false
        this.init()
      } else {
        this.$message.error(res.data.msg || '保存失败')
      }
    })
  }

  private syncGeo() {
    syncStoreGeo().then((res: any) => {
      if (String(res.data.code) === '1') {
        this.$message.success(`GEO 索引已重建，共 ${res.data.data} 家门店`)
      } else {
        this.$message.error(res.data.msg || '同步失败')
      }
    })
  }
}
</script>