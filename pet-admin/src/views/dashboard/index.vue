<template>
  <div class="dashboard-container home">
    <!-- 营业数据 -->
    <Overview :overviewData="overviewData" />
    <!-- end -->
    <!-- 预约概览 -->
    <Orderview :orderviewData="orderviewData" />
    <!-- end -->
    <div class="homeMain">
      <!-- 服务项目总览 -->
      <ServiceItemStatistics :serviceItemData="serviceItemData" />
      <!-- end -->
      <!-- 服务套餐总览 -->
      <ServicePackageStatistics :servicePackageData="servicePackageData" />
      <!-- end -->
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import {
  getBusinessData,
  getAppointmentOverview,
  getServiceItemOverview,
  getServicePackageOverview,
} from '@/api/index'
// 组件
// 营业数据
import Overview from './components/overview.vue'
// 预约概览
import Orderview from './components/orderview.vue'
// 服务项目总览
import ServiceItemStatistics from './components/serviceItemStatistics.vue'
// 服务套餐总览
import ServicePackageStatistics from './components/servicePackageStatistics.vue'
@Component({
  name: 'Dashboard',
  components: {
    Overview,
    Orderview,
    ServiceItemStatistics,
    ServicePackageStatistics,
  },
})
export default class extends Vue {
  private overviewData = {}
  private orderviewData = {} as any
  private serviceItemData = {} as any
  private servicePackageData = {}
  created() {
    this.init()
  }
  init() {
    this.$nextTick(() => {
      this.getBusinessData()
      this.getAppointmentStatisticsData()
      this.getServiceItemStatisticsData()
      this.getServicePackageStatisticsData()
    })
  }
  // 获取营业数据
  async getBusinessData() {
    const data = await getBusinessData()
    this.overviewData = data.data.data
  }
  // 获取预约概览
  async getAppointmentStatisticsData() {
    const data = await getAppointmentOverview()
    this.orderviewData = data.data.data
  }
  // 获取服务项目总览数据
  async getServiceItemStatisticsData() {
    const data = await getServiceItemOverview()
    this.serviceItemData = data.data.data
  }
  // 获取服务套餐总览数据
  async getServicePackageStatisticsData() {
    const data = await getServicePackageOverview()
    this.servicePackageData = data.data.data
  }
}
</script>

<style lang="scss">
</style>
