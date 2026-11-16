<template>
  <div :key="vueRest"
       class="addBrand-container">
    <div :key="restKey"
         class="container">
      <el-form ref="ruleForm"
               :model="ruleForm"
               :rules="rules"
               :inline="true"
               label-width="180px"
               class="demo-ruleForm">
        <div>
          <el-form-item label="服务项目名称:"
                        prop="name">
            <el-input v-model="ruleForm.name"
                      placeholder="请填写服务项目名称"
                      maxlength="20" />
          </el-form-item>
          <el-form-item label="服务项目分类:"
                        prop="categoryId">
            <el-select v-model="ruleForm.categoryId"
                       placeholder="请选择服务项目分类">
              <el-option v-for="(item, index) in categoryOptions"
                         :key="index"
                         :label="item.name"
                         :value="item.id" />
            </el-select>
          </el-form-item>
        </div>
        <div>
          <el-form-item label="服务项目价格:"
                        prop="price">
            <el-input v-model="ruleForm.price"
                      placeholder="请设置服务项目价格" />
          </el-form-item>
        </div>
        <div>
          <el-form-item label="服务项目图片:"
                        prop="image">
            <image-upload :prop-image-url="imageUrl"
                          @imageChange="imageChange">
              图片大小不超过2M<br>仅能上传 PNG JPEG JPG类型图片<br>建议上传200*200或300*300尺寸的图片
            </image-upload>
          </el-form-item>
        </div>
        <div class="address">
          <el-form-item label="服务项目描述:"
                        prop="region">
            <el-input v-model="ruleForm.description"
                      type="textarea"
                      :rows="3"
                      maxlength="200"
                      placeholder="服务项目描述，最长200字" />
          </el-form-item>
        </div>
        <div class="subBox address">
          <el-button @click="() => $router.back()">
            取消
          </el-button>
          <el-button type="primary"
                     :class="{ continue: actionType === 'add' }"
                     @click="submitForm('ruleForm')">
            保存
          </el-button>
          <el-button v-if="actionType == 'add'"
                     type="primary"
                     @click="submitForm('ruleForm', 'goAnd')">
            保存并继续添加
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import ImageUpload from '@/components/ImgUpload/index.vue'
import {
  queryServiceItemById,
  addServiceItem,
  editServiceItem,
  getCategoryList
} from '@/api/serviceItem'

@Component({
  name: 'addServiceItem',
  components: {
    ImageUpload
  }
})
export default class extends Vue {
  private restKey: number = 0
  private imageUrl: string = ''
  private actionType: string = ''
  private categoryOptions: any[] = []
  private vueRest = '1'
  private ruleForm = {
    name: '',
    id: '',
    price: '',
    image: '',
    description: '',
    status: true,
    categoryId: ''
  }

  get rules() {
    return {
      name: [
        {
          required: true,
          validator: (rule: any, value: string, callback: Function) => {
            if (!value) {
              callback(new Error('请输入服务项目名称'))
            } else {
              const reg = /^([A-Za-z0-9\u4e00-\u9fa5]){2,20}$/
              if (!reg.test(value)) {
                callback(new Error('服务项目名称输入不符，请输入2-20个字符'))
              } else {
                callback()
              }
            }
          },
          trigger: 'blur'
        }
      ],
      categoryId: [
        { required: true, message: '请选择服务项目分类', trigger: 'change' }
      ],
      image: {
        required: true,
        message: '服务项目图片不能为空'
      },
      price: [
        {
          required: true,
          validator: (rules: any, value: string, callback: Function) => {
            const reg = /^([1-9]\d{0,5}|0)(\.\d{1,2})?$/
            if (!reg.test(value) || Number(value) <= 0) {
              callback(
                new Error(
                  '服务项目价格格式有误，请输入大于零且最多保留两位小数的金额'
                )
              )
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ]
    }
  }

  created() {
    this.getCategoryOptions()
    this.actionType = this.$route.query.id ? 'edit' : 'add'
    if (this.$route.query.id) {
      this.init()
    }
  }

  private async init() {
    queryServiceItemById(this.$route.query.id).then(res => {
      if (res && res.data && res.data.code === 1) {
        this.ruleForm = { ...res.data.data }
        this.ruleForm.price = String(res.data.data.price)
        this.ruleForm.status = res.data.data.status == '1'
        this.imageUrl = res.data.data.image
      } else {
        this.$message.error(res.data.msg)
      }
    })
  }

  // 获取服务项目分类
  private getCategoryOptions() {
    getCategoryList({ type: 1 }).then(res => {
      if (res.data.code === 1) {
        this.categoryOptions = res && res.data && res.data.data
      } else {
        this.$message.error(res.data.msg)
      }
    })
  }

  private submitForm(formName: any, st: any) {
    ;(this.$refs[formName] as any).validate((valid: any) => {
      if (valid) {
        if (!this.ruleForm.image) return this.$message.error('服务项目图片不能为空')
        let params: any = { ...this.ruleForm }
        params.status =
          this.actionType === 'add' ? 0 : this.ruleForm.status ? 1 : 0
        if (this.actionType == 'add') {
          delete params.id
          addServiceItem(params)
            .then(res => {
              if (res.data.code === 1) {
                this.$message.success('服务项目添加成功！')
                if (!st) {
                  this.$router.push({ path: '/service-item' })
                } else {
                  this.imageUrl = ''
                  this.ruleForm = {
                    name: '',
                    id: '',
                    price: '',
                    image: '',
                    description: '',
                    status: true,
                    categoryId: ''
                  }
                  this.restKey++
                }
              } else {
                this.$message.error(res.data.desc || res.data.msg)
              }
            })
            .catch(err => {
              this.$message.error('请求出错了：' + err.message)
            })
        } else {
          delete params.createTime
          delete params.updateTime
          editServiceItem(params)
            .then(res => {
              if (res && res.data && res.data.code === 1) {
                this.$router.push({ path: '/service-item' })
                this.$message.success('服务项目修改成功！')
              } else {
                this.$message.error(res.data.desc || res.data.msg)
              }
            })
            .catch(err => {
              this.$message.error('请求出错了：' + err.message)
            })
        }
      } else {
        return false
      }
    })
  }

  imageChange(value: any) {
    this.ruleForm.image = value
  }
}
</script>
<style lang="scss" scoped>
.addBrand-container {
  .el-form--inline .el-form-item__content {
    width: 293px;
  }

  .el-input {
    width: 350px;
  }

  .address {
    .el-form-item__content {
      width: 777px !important;
    }
  }
}
</style>
<style lang="scss" scoped>
.addBrand {
  &-container {
    margin: 30px;

    .container {
      position: relative;
      z-index: 1;
      background: #fff;
      padding: 30px;
      border-radius: 4px;
      min-height: 500px;

      .subBox {
        padding-top: 30px;
        text-align: center;
        border-top: solid 1px $gray-5;
      }
      .upload-item {
        .el-form-item__error {
          top: 90%;
        }
      }
    }
  }
}
</style>
