import Vue from "vue";
import Router from "vue-router";
import Layout from "@/layout/index.vue";
import {
  getToken,
  setToken,
  removeToken,
  getStoreId,
  setStoreId,
  removeStoreId,
  setUserInfo,
  getUserInfo,
  removeUserInfo
} from "@/utils/cookies";
import store from "@/store";

Vue.use(Router);

const router = new Router({
  scrollBehavior: (to, from, savedPosition) => {
    if (savedPosition) {
      return savedPosition;
    }
    return { x: 0, y: 0 };
  },
  base: process.env.BASE_URL,
  routes: [
    {
      path: "/login",
      component: () =>
        import(/* webpackChunkName: "login" */ "@/views/login/index.vue"),
      meta: { title: "宠物服务", hidden: true, notNeedAuth: true }
    },
    {
      path: "/404",
      component: () => import(/* webpackChunkName: "404" */ "@/views/404.vue"),
      meta: { title: "宠物服务", hidden: true, notNeedAuth: true }
    },
    {
      path: "/",
      component: Layout,
      redirect: "/dashboard",
      children: [
        {
          path: "dashboard",
          component: () =>
            import(/* webpackChunkName: "dashboard" */ "@/views/dashboard/index.vue"),
          name: "Dashboard",
          meta: {
            title: "工作台",
            icon: "dashboard",
            affix: true
          }
        },
		{
          path: "/statistics",
          component: () =>
            import(/* webpackChunkName: "shopTable" */ "@/views/statistics/index.vue"),
          meta: {
            title: "数据统计",
            icon: "icon-statistics"
          }
        },
        {
          path: "appointment",
          component: () =>
            import(/* webpackChunkName: "shopTable" */ "@/views/appointment/index.vue"),
          meta: {
            title: "预约管理",
            icon: "icon-order"
          }
        },
        {
          path: "appointment/slot",
          component: () =>
            import(/* webpackChunkName: "shopTable" */ "@/views/appointment/slot.vue"),
          meta: {
            title: "时段管理",
            icon: "icon-statistics"
          }
        },
        {
          path: "seckill",
          component: () =>
            import(/* webpackChunkName: "shopTable" */ "@/views/seckill/index.vue"),
          meta: {
            title: "服务套餐秒杀",
            icon: "icon-combo"
          }
        },
        {
          path: "seckill/order",
          component: () =>
            import(/* webpackChunkName: "shopTable" */ "@/views/seckill/order.vue"),
          meta: {
            title: "秒杀订单",
            icon: "icon-order"
          }
        },
        {
          path: "community",
          component: () =>
            import(/* webpackChunkName: "shopTable" */ "@/views/community/index.vue"),
          meta: {
            title: "宠物社区",
            icon: "icon-user"
          }
        },
        {
          path: "store",
          component: () =>
            import(/* webpackChunkName: "shopTable" */ "@/views/store/index.vue"),
          meta: {
            title: "门店管理",
            icon: "icon-dish"
          }
        },
        {
          path: "service-package",
          component: () =>
            import(/* webpackChunkName: "shopTable" */ "@/views/servicePackage/index.vue"),
          meta: {
            title: "服务套餐",
            icon: "icon-combo"
          }
        },
        {
          path: "service-item",
          component: () =>
            import(/* webpackChunkName: "shopTable" */ "@/views/serviceItem/index.vue"),
          meta: {
            title: "服务项目",
            icon: "icon-dish"
          }
        },
        {
          path: "/service-item/add",
          component: () =>
            import(/* webpackChunkName: "shopTable" */ "@/views/serviceItem/addServiceItem.vue"),
          meta: {
            title: "新建服务项目",
            hidden: true
          }
        },
        
        {
          path: "category",
          component: () =>
            import(/* webpackChunkName: "shopTable" */ "@/views/category/index.vue"),
          meta: {
            title: "服务分类",
            icon: "icon-category"
          }
        },
        {
          path: "employee",
          component: () =>
            import(/* webpackChunkName: "shopTable" */ "@/views/employee/index.vue"),
          meta: {
            title: "员工管理",
            icon: "icon-employee"
          }
        },
        
        {
          path: "/employee/add",
          component: () =>
            import(/* webpackChunkName: "dashboard" */ "@/views/employee/addEmployee.vue"),
          meta: {
            title: "添加员工",
            hidden: true
          }
        },
        
        {
          path: "/service-package/add",
          component: () =>
            import(/* webpackChunkName: "shopTable" */ "@/views/servicePackage/addServicePackage.vue"),
          meta: {
            title: "新建服务套餐",
            hidden: true
          }
        }
      ]
    },
    {
      path: "*",
      redirect: "/404",
      meta: { hidden: true }
    }
  ]
});

export default router;
