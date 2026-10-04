import { createApp } from 'vue'
import { createPinia } from 'pinia'
import store from './stores'

import '@/assets/styles/tailwind.css'

import App from './App.vue'
import router from './router'
//引入路由守卫
import './permission'

import SvgIcon from '@/components/SvgIcon'
import 'virtual:svg-icons-register'

//分页组件
import Pagination from '@/components/Pagination'

//图标选择组件
import IconSelect from '@/components/IconSelect'

// 图片预览组件
import ImagePreview from "@/components/ImagePreview"

// 图片上传组件
import ImageUpload from "@/components/ImageUpload"

// 文件上传组件
import FileUpload from "@/components/FileUpload"

// 富文本编辑器组件
import Editor from "@/components/Editor"

// 自定义 Modal 组件
import Modal from "@/components/Modal"

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(store)

app.component('Pagination', Pagination)

app.component('svg-icon', SvgIcon)

app.component('IconSelect', IconSelect)

app.component('ImagePreview', ImagePreview)

app.component('ImageUpload', ImageUpload)

app.component('FileUpload', FileUpload)

app.component('Editor', Editor)

app.component('Modal', Modal)

app.mount('#app')
