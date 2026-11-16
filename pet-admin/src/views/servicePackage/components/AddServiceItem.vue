<template>
  <div class="addItem">
    <div class="leftCont">
      <div v-show="searchKey.trim() == ''"
           class="tabBut">
        <span v-for="(item, index) in categories"
              :key="index"
              :class="{ act: index == keyInd }"
              @click="checkTypeHandle(index, item.id)">{{ item.name }}</span>
      </div>
      <div class="tabList">
        <div class="table"
             :class="{ borderNone: !itemList.length }">
          <div v-if="itemList.length == 0"
               style="padding-left: 10px">
            <Empty />
          </div>
          <el-checkbox-group v-if="itemList.length > 0"
                             v-model="checkedList"
                             @change="checkedListHandle">
            <div v-for="(item, index) in itemList"
                 :key="item.name + item.id"
                 class="items">
              <el-checkbox :key="index"
                           :label="item.name">
                <div class="item">
                  <span style="flex: 3; text-align: left">{{ item.name }}</span>
                  <span>{{ item.status == 0 ? '已下架' : '已上架' }}</span>
                  <span>{{ (Number(item.price) ).toFixed(2)*100/100 }}</span>
                </div>
              </el-checkbox>
            </div>
          </el-checkbox-group>
        </div>
      </div>
    </div>
    <div class="ritCont">
      <div class="tit">
        已选服务项目({{ checkedListAll.length }})
      </div>
      <div class="items">
        <div v-for="(item, ind) in checkedListAll"
             :key="ind"
             class="item">
          <span>{{ item.name }}</span>
          <span class="price">￥ {{ (Number(item.price) ).toFixed(2)*100/100 }} </span>
          <span class="del"
                @click="delCheck(item.name)">
            <img src="./../../../assets/icons/btn_clean@2x.png"
                 alt="">
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue, Watch } from 'vue-property-decorator'
import { getCategoryList, queryServiceItemList } from '@/api/serviceItem'
import Empty from '@/components/Empty/index.vue'

@Component({
  name: 'AddServiceItem',
  components: {
    Empty
  }
})
export default class extends Vue {
  @Prop({ default: '' }) private searchKey!: string
  @Prop({ default: [] }) private checkList!: any[]
  private categories: [] = []
  private itemList: [] = []
  private allItems: any[] = []
  private checkListCache: any[] = []
  private keyInd = 0
  public checkedList: any[] = []
  private checkedListAll: any[] = []
  private ids: any = new Set()

  created() {
    this.init()
  }

  @Watch('searchKey')
  private searchKeyChange(value: any) {
    if (value.trim()) {
      this.searchItems(this.searchKey)
    }
  }

  public init() {
    this.getCategories()
    this.checkedList = this.checkList.map((it: any) => it.name)
    this.checkedListAll = this.checkList.reverse()
  }

  // 获取服务项目分类
  public getCategories() {
    getCategoryList({ type: 1 }).then(res => {
      if (res && res.data && res.data.code === 1) {
        this.categories = res.data.data
        this.getItems(res.data.data[0].id)
      } else {
        this.$message.error(res.data.msg)
      }
    })
  }

  // 通过分类ID获取服务项目列表
  private getItems(id: number) {
    queryServiceItemList({ categoryId: id }).then(res => {
      if (res && res.data && res.data.code === 1) {
        if (res.data.data.length == 0) {
          this.itemList = []
          return
        }
        let newArr = res.data.data
        newArr.forEach((n: any) => {
          n.dishId = n.id
          n.copies = 1
        })
        this.itemList = newArr
        if (!this.ids.has(id)) {
          this.allItems = [...this.allItems, ...newArr]
        }
        this.ids.add(id)
      } else {
        this.$message.error(res.data.msg)
      }
    })
  }

  // 关键词搜索服务项目
  private searchItems(name: any) {
    queryServiceItemList({ name }).then(res => {
      if (res && res.data && res.data.code === 1) {
        let newArr = res.data.data
        newArr.forEach((n: any) => {
          n.dishId = n.id
        })
        this.itemList = newArr
      } else {
        this.$message.error(res.data.msg)
      }
    })
  }

  // 点击分类
  private checkTypeHandle(ind: number, id: any) {
    this.keyInd = ind
    this.getItems(id)
  }

  // 添加服务项目
  private checkedListHandle(value: [string]) {
    // 倒序展示 - 数据处理前反正 为正序
    this.checkedListAll.reverse()
    // value 是一个只包含服务项目名的数组 需要从 itemList 中筛选出对应的详情
    const list = this.allItems.filter((item: any) => {
      let data
      value.forEach((it: any) => {
        if (item.name == it) {
          data = item
        }
      })
      return data
    })
    // 编辑的时候需要与已有项目合并后去重
    const itemListCat = [...this.checkedListAll, ...list]
    let arrData: any[] = []
    this.checkedListAll = itemListCat.filter((item: any) => {
      let allArrDate
      if (arrData.length == 0) {
        arrData.push(item.name)
        allArrDate = item
      } else {
        const st = arrData.some(it => item.name == it)
        if (!st) {
          arrData.push(item.name)
          allArrDate = item
        }
      }
      return allArrDate
    })
    // 如果是移除项目 走这里
    if (value.length < arrData.length) {
      this.checkedListAll = this.checkedListAll.filter((item: any) => {
        if (value.some(it => it == item.name)) {
          return item
        }
      })
    }
    this.$emit('checkList', this.checkedListAll)
    // 数据处理完反转为倒序
    this.checkedListAll.reverse()
  }

  open() {
    this.checkListCache = JSON.parse(JSON.stringify(this.checkList))
  }

  close() {
    this.checkList = this.checkListCache
  }

  // 删除
  private delCheck(name: any) {
    const index = this.checkedList.findIndex(it => it === name)
    const indexAll = this.checkedListAll.findIndex(
      (it: any) => it.name === name
    )

    this.checkedList.splice(index, 1)
    this.checkedListAll.splice(indexAll, 1)
    this.$emit('checkList', this.checkedListAll)
  }
}
</script>
<style lang="scss">
.addItem {
  .el-checkbox__label {
    width: 100%;
  }
  .empty-box {
    margin-top: 50px;
    margin-bottom: 0px;
  }
}
</style>
<style lang="scss" scoped>
.addItem {
  padding: 0 20px;
  display: flex;
  line-height: 40px;
  .empty-box {
    img {
      width: 190px;
      height: 147px;
    }
  }

  .borderNone {
    border: none !important;
  }
  span,
  .tit {
    color: #333;
  }
  .leftCont {
    display: flex;
    border-right: solid 1px #efefef;
    width: 60%;
    padding: 15px;
    .tabBut {
      width: 110px;
      font-weight: bold;
      border-right: solid 2px #f4f4f4;
      span {
        display: block;
        text-align: center;
        cursor: pointer;
        position: relative;
      }
    }
    .act {
      border-color: $mine !important;
      color: $mine !important;
    }
    .act::after {
      content: ' ';
      display: inline-block;
      background-color: $mine;
      width: 2px;
      height: 40px;
      position: absolute;
      right: -2px;
    }
    .tabList {
      flex: 1;
      padding: 15px;
      height: 400px;
      overflow-y: scroll;
      .table {
        border: solid 1px #f4f4f4;
        border-bottom: solid 1px #f4f4f4;
        .items {
          border-bottom: solid 1px #f4f4f4;
          padding: 0 10px;
          display: flex;
          .el-checkbox,
          .el-checkbox__label {
            width: 100%;
          }
          .item {
            display: flex;
            padding-right: 20px;
            span {
              display: inline-block;
              text-align: center;
              flex: 1;
              font-weight: normal;
            }
          }
        }
      }
    }
  }
  .ritCont {
    width: 40%;
    .tit {
      margin: 0 15px;
      font-weight: bold;
    }
    .items {
      height: 338px;
      padding: 4px 15px;
      overflow: scroll;
    }
    .item {
      box-shadow: 0px 1px 4px 3px rgba(0, 0, 0, 0.03);
      display: flex;
      text-align: center;
      padding: 0 10px;
      margin-bottom: 20px;
      border-radius: 6px;
      color: #818693;
      span:first-child {
        text-align: left;
        color: #20232a;
        flex: 70%;
      }
      .price {
        display: inline-block;
        flex: 70%;
        text-align: left;
      }
      .del {
        cursor: pointer;
        img {
          position: relative;
          top: 5px;
          width: 20px;
        }
      }
    }
  }
}
</style>
