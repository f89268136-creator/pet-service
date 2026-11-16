<template>
  <div class="container">
    <h2 class="homeTitle">
      服务项目总览<span><router-link to="/service-item">服务项目管理</router-link></span>
    </h2>
    <div class="orderviewBox">
      <ul>
        <li>
          <span class="status"><i class="iconfont icon-open"></i>已上架</span>
          <span class="num">{{ serviceItemData.sold }}</span>
        </li>
        <li>
          <span class="status"><i class="iconfont icon-stop"></i>已下架</span>
          <span class="num">{{ serviceItemData.discontinued }}</span>
        </li>
        <li class="add">
          <router-link to="/service-item/add">
            <i></i>
            <p>新增服务项目</p>
          </router-link>
        </li>
      </ul>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator'
@Component({
  name: 'ServiceItemStatistics',
})
export default class extends Vue {
  @Prop() private serviceItemData!: any
}
</script>
