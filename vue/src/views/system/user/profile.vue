<template>
  <div class="text-center my-5 mx-auto max-w-[500px]">
    <!-- 头像上传 -->
    <div class="mb-4">
      <div class="relative inline-block cursor-pointer group" @click="triggerUpload">
        <img :src="userStore.avatar" class="w-[120px] h-[120px] rounded-full object-cover border-4 border-white shadow-lg group-hover:opacity-80 transition-opacity" alt=""/>
        <div class="absolute inset-0 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity bg-black/30">
          <svg class="w-6 h-6 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg>
        </div>
      </div>
      <input ref="avatarInput" type="file" accept="image/jpeg,image/png" class="hidden" @change="handleAvatarChange"/>
    </div>

    <!-- 用户信息列表 -->
    <ul class="inline-block text-left w-full max-w-[300px] list-none p-0">
      <li class="py-2.5 border-b border-gray-100 text-sm">
        用户名称
        <span class="float-right text-gray-600">{{ userStore.name }}</span>
      </li>
      <li class="py-2.5 border-b border-gray-100 text-sm">
        性别
        <span class="float-right text-gray-600">{{ state.user.sex === 0 ? '男' : '女' }}</span>
      </li>
    </ul>

    <!-- 操作按钮 -->
    <div class="mt-5 flex gap-3 justify-center">
      <button @click="editUserInfo" class="h-9 px-5 rounded-lg text-sm font-medium text-white bg-[var(--color-primary)] hover:bg-[var(--color-primary-dark)] transition-colors">修改基本资料</button>
      <button @click="editPassword" class="h-9 px-5 rounded-lg text-sm font-medium text-white bg-[var(--color-primary)] hover:bg-[var(--color-primary-dark)] transition-colors">修改密码</button>
    </div>

    <!-- 修改基本资料对话框 -->
    <Modal title="修改基本资料" v-model="userInfoOpen" width="500px">
      <form @submit.prevent="submitUserInfo" class="space-y-4">
        <div class="flex items-start gap-3">
          <label class="w-20 pt-2 text-sm text-gray-600 text-right flex-shrink-0">用户名</label>
          <div class="flex-1">
            <input v-model="form.userName" class="w-full h-9 px-3 rounded-lg border border-gray-300 text-sm outline-none focus:border-[var(--color-primary)] focus:ring-2 focus:ring-[var(--color-primary)]/20"/>
          </div>
        </div>
        <div class="flex items-start gap-3">
          <label class="w-20 pt-2 text-sm text-gray-600 text-right flex-shrink-0">性别</label>
          <div class="flex gap-6 pt-2">
            <label class="flex items-center gap-1.5 text-sm cursor-pointer"><input type="radio" v-model="form.sex" :value="0" class="accent-[var(--color-primary)]"/>男</label>
            <label class="flex items-center gap-1.5 text-sm cursor-pointer"><input type="radio" v-model="form.sex" :value="1" class="accent-[var(--color-primary)]"/>女</label>
          </div>
        </div>
      </form>
      <template #footer>
        <button @click="submitUserInfo" class="h-9 px-5 rounded-lg text-sm font-medium text-white bg-[var(--color-primary)] hover:bg-[var(--color-primary-dark)] transition-colors">保存</button>
        <button @click="userInfoOpen = false" class="h-9 px-5 rounded-lg text-sm font-medium text-gray-600 border border-gray-300 bg-white hover:bg-gray-50 transition-colors">取消</button>
      </template>
    </Modal>

    <!-- 修改密码对话框 -->
    <Modal title="修改密码" v-model="pwdOpen" width="500px">
      <form @submit.prevent="submitPwd" class="space-y-4">
        <div class="flex items-start gap-3">
          <label class="w-20 pt-2 text-sm text-gray-600 text-right flex-shrink-0">旧密码</label>
          <div class="flex-1">
            <input v-model="pwdForm.oldPassword" type="password" placeholder="请输入旧密码" class="w-full h-9 px-3 rounded-lg border border-gray-300 text-sm outline-none focus:border-[var(--color-primary)] focus:ring-2 focus:ring-[var(--color-primary)]/20"/>
          </div>
        </div>
        <div class="flex items-start gap-3">
          <label class="w-20 pt-2 text-sm text-gray-600 text-right flex-shrink-0">新密码</label>
          <div class="flex-1">
            <input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" class="w-full h-9 px-3 rounded-lg border border-gray-300 text-sm outline-none focus:border-[var(--color-primary)] focus:ring-2 focus:ring-[var(--color-primary)]/20"/>
          </div>
        </div>
        <div class="flex items-start gap-3">
          <label class="w-20 pt-2 text-sm text-gray-600 text-right flex-shrink-0">确认密码</label>
          <div class="flex-1">
            <input v-model="pwdForm.confirmPassword" type="password" placeholder="请确认新密码" class="w-full h-9 px-3 rounded-lg border border-gray-300 text-sm outline-none focus:border-[var(--color-primary)] focus:ring-2 focus:ring-[var(--color-primary)]/20"/>
          </div>
        </div>
      </form>
      <template #footer>
        <button @click="submitPwd" class="h-9 px-5 rounded-lg text-sm font-medium text-white bg-[var(--color-primary)] hover:bg-[var(--color-primary-dark)] transition-colors">保存</button>
        <button @click="pwdOpen = false" class="h-9 px-5 rounded-lg text-sm font-medium text-gray-600 border border-gray-300 bg-white hover:bg-gray-50 transition-colors">取消</button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import useUserStore from "@/stores/modules/userStore.js";
import {onMounted, reactive, ref, watch} from "vue";
import {getInfo} from "@/api/login.js";
import {getToken} from "@/utils/auth.js";
import {ElMessage} from "@/utils/toast.js";
import {updateProfile, updatePwd} from "@/api/system/user.js";
import axios from "axios";

const pwdOpen = ref(false)
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

const editPassword = () => { pwdOpen.value = true }

const submitPwd = () => {
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  updatePwd({...pwdForm.value}).then(() => {
    ElMessage.success('修改成功')
    pwdOpen.value = false
  })
}

const userInfoOpen = ref(false)
const form = ref({})

const submitUserInfo = () => {
  updateProfile({...form.value}).then(() => {
    userInfoOpen.value = false
    ElMessage.success('修改成功')
    getUser()
    userStore.name = form.value.userName
  })
}

const userStore = useUserStore()
const state = reactive({ user: {} })
const avatarInput = ref(null)

const triggerUpload = () => { avatarInput.value?.click() }

const handleAvatarChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return

  const isJpg = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpg) {
    ElMessage.error('上传头像图片只能是jpg或者png格式!')
    return
  }

  const formData = new FormData()
  formData.append('file', file)
  const uploadUrl = import.meta.env.VITE_APP_BASE_API + "/system/user/profile/avatar"

  try {
    const res = await axios.post(uploadUrl, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': 'Bearer ' + getToken()
      }
    })
    if (res.data.code === 200) {
      userStore.avatar = import.meta.env.VITE_APP_BASE_API + res.data.imgUrl
    } else {
      ElMessage.error(res.data.msg || '修改失败')
    }
  } catch {
    ElMessage.error('上传失败')
  }
}

const editUserInfo = () => { userInfoOpen.value = true }

const getUser = () => {
  getInfo().then(res => {
    state.user = res.data
    form.value = { userName: state.user.userName, sex: state.user.sex }
  })
}

onMounted(() => { getUser() })

watch(() => state.user, user => {
  if (user) { form.value = { userName: user.userName, sex: user.sex } }
}, { immediate: true })
</script>
