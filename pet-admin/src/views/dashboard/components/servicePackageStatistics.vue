<template>
  <div class="container">
    <h2 class="homeTitle">
      服务套餐总览<span><router-link to="/service-package">服务套餐管理</router-link></span>
    </h2>
    <div class="orderviewBox">
      <ul>
        <li>
          <span class="status"><i class="iconfont icon-open"></i>已上架</span>
          <span class="num">{{ servicePackageData.sold }}</span>
        </li>
        <li>
          <span class="status"><i class="iconfont icon-stop"></i>已下架</span>
          <span class="num">{{ servicePackageData.discontinued }}</span>
        </li>
        <li class="add">
          <router-link to="/service-package/add">
            <i></i>
            <p>新增服务套餐</p>
          </router-link>
        </li>
      </ul>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator'
@Component({
  name: 'ServicePackageStatistics',
})
export default class extends Vue {
  @Prop() private servicePackageData!: any
}
</script>
