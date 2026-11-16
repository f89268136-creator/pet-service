<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar">
        <label>动态内容：</label>
        <el-input
          v-model="query.keyword"
          placeholder="请输入关键词"
          style="width: 220px"
          clearable
          @keyup.enter.native="search"
        />
        <label style="margin-left: 12px">状态：</label>
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="展示中" :value="1" />
          <el-option label="已隐藏" :value="0" />
        </el-select>
        <el-button type="primary" style="margin-left: 12px" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>

      <el-table v-if="tableData.length" :data="tableData" stripe class="tableBox">
        <el-table-column label="图片" width="100">
          <template slot-scope="scope">
            <el-image
              v-if="scope.row.images && scope.row.images.length"
              style="width: 70px; height: 46px; border-radius: 4px"
              :src="scope.row.images[0]"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="动态内容" min-width="220" show-overflow-tooltip />
        <el-table-column label="发布人 / 宠物" width="160">
          <template slot-scope="scope">
            {{ scope.row.userName || '-' }}<span v-if="scope.row.petName"> / {{ scope.row.petName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="storeName" label="关联门店" width="140" />
        <el-table-column label="互动" width="110">
          <template slot-scope="scope">赞 {{ scope.row.likeCount }} / 评 {{ scope.row.commentCount }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="150" />
        <el-table-column label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '展示中' : '已隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="toggleStatus(scope.row)">
              {{ scope.row.status === 1 ? '隐藏' : '恢复' }}
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
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import Empty from '@/components/Empty/index.vue'
import { getCommunityPage, updateCommunityStatus } from '@/api/community'

@Component({
  name: 'Community',
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

  created() {
    this.init()
  }

  private async init() {
    const params: any = { page: this.page, pageSize: this.pageSize }
    if (this.query.keyword) params.keyword = this.query.keyword
    if (this.query.status !== null) params.status = this.query.status
    await getCommunityPage(params)
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

  private toggleStatus(row: any) {
    const status = row.status === 1 ? 0 : 1
    updateCommunityStatus(row.id, status).then((res: any) => {
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