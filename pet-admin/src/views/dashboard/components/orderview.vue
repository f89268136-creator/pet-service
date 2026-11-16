<template>
  <div class="container">
    <h2 class="homeTitle">
      预约概览<i>{{ days[1] }}</i
      ><span><router-link to="/appointment">预约记录</router-link></span>
    </h2>
    <div class="orderviewBox">
      <ul>
        <li>
          <span class="status"
            ><i class="iconfont icon-waiting"></i>待确认</span
          >
          <span class="num tip"
            ><router-link to="/appointment?status=2">{{
              orderviewData.waitingConfirm
            }}</router-link></span
          >
        </li>
        <li>
          <span class="status"
            ><i class="iconfont icon-staySway"></i>待服务</span
          >
          <span class="num tip"
            ><router-link to="/appointment?status=3">{{
              orderviewData.waitingService
            }}</router-link></span
          >
        </li>
        <li>
          <span class="status"
            ><i class="iconfont icon-complete"></i>已完成</span
          >
          <span class="num"
            ><router-link to="/appointment?status=5">{{
              orderviewData.completed
            }}</router-link></span
          >
        </li>
        <li>
          <span class="status"><i class="iconfont icon-cancel"></i>已取消</span>
          <span class="num"
            ><router-link to="/appointment?status=6">{{
              orderviewData.cancelled
            }}</router-link></span
          >
        </li>
        <li>
          <span class="status"><i class="iconfont icon-all"></i>全部预约</span>
          <span class="num"
            ><router-link to="/appointment">{{
              orderviewData.total
            }}</router-link></span
          >
        </li>
      </ul>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator'
import { getday } from '@/utils/formValidate'
@Component({
  name: 'Orderview',
})
export default class extends Vue {
  @Prop() private orderviewData!: any
  get days() {
    return getday()
  }
}
</script>
