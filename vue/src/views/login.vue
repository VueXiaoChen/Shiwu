<template>
  <div class="login-page" :style="{ backgroundImage: `url(${backgroundImg})` }">

    <!-- 卡片 -->
    <div class="login-card">
      <div class="login-header">
        <h3 class="login-title">欢迎回来</h3>
        <p class="login-subtitle">请输入您的登录信息</p>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <!-- 用户名 -->
        <div class="field">
          <label class="field-label">用户名</label>
          <div class="field-wrap" :class="{ 'field-error': errors.userName }">
            <svg class="field-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            <input
              v-model="loginForm.userName"
              type="text"
              placeholder="请输入用户名"
              class="field-input"
            />
          </div>
          <p v-if="errors.userName" class="field-msg">{{ errors.userName }}</p>
        </div>

        <!-- 密码 -->
        <div class="field">
          <label class="field-label">密码</label>
          <div class="field-wrap" :class="{ 'field-error': errors.password }">
            <svg class="field-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></svg>
            <input
              v-model="loginForm.password"
              :type="showPwd ? 'text' : 'password'"
              placeholder="请输入密码"
              class="field-input"
            />
            <button type="button" class="field-toggle" @click="showPwd = !showPwd">
              <svg v-if="!showPwd" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            </button>
          </div>
          <p v-if="errors.password" class="field-msg">{{ errors.password }}</p>
        </div>

        <!-- 登录按钮 -->
        <button
          type="submit"
          :disabled="loading"
          class="submit-btn"
          :class="{ 'submit-loading': loading }"
        >
          <svg v-if="loading" class="spinner" viewBox="0 0 24 24" fill="none">
            <circle class="spinner-track" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
            <path class="spinner-head" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
          </svg>
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import useUserStore from "@/stores/modules/userStore.js";
import { useRouter } from "vue-router";
import backgroundImg from "@/assets/images/background.jpg";

const loading = ref(false)
const showPwd = ref(false)

const loginForm = reactive({
  userName: '',
  password: ''
})

const errors = reactive({
  userName: '',
  password: ''
})

const validateForm = () => {
  let valid = true
  errors.userName = ''
  errors.password = ''

  if (!loginForm.userName) {
    errors.userName = '请输入用户名'
    valid = false
  }
  if (!loginForm.password) {
    errors.password = '请输入密码'
    valid = false
  }
  return valid
}

const userStore = useUserStore()
const router = useRouter()

const handleLogin = () => {
  if (!validateForm()) return

  loading.value = true
  userStore.login({ ...loginForm }).then(() => {
    const redirectPath = '/'
    router.push(redirectPath)
  }).catch(() => {
    loading.value = false
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh; display: flex; flex-direction: column; align-items: center;
  justify-content: center; padding: 24px;
  background-size: cover; background-position: center; background-repeat: no-repeat;
  background-attachment: fixed;
}

/* Brand */
.login-brand {
  display: flex; align-items: center; gap: 10px; margin-bottom: 32px;
}
.brand-icon {
  width: 40px; height: 40px; border-radius: 12px;
  background: linear-gradient(135deg, #38BDF8, #0EA5E9);
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4px 16px rgba(14,165,233,0.3);
}
.brand-icon svg { width: 20px; height: 20px; color: #fff; }
.brand-name { font-size: 20px; font-weight: 800; color: #0F172A; letter-spacing: -0.02em; }

/* Card */
.login-card {
  width: 100%; max-width: 420px; background: #fff; border-radius: 20px;
  padding: 40px 36px; box-shadow: 0 16px 60px rgba(15, 23, 42, 0.18), 0 2px 8px rgba(15, 23, 42, 0.08);
  border: 1px solid rgba(255,255,255,0.6);
}

.login-header { text-align: center; margin-bottom: 36px; }
.login-title { font-size: 26px; font-weight: 800; color: #0F172A; letter-spacing: -0.02em; margin-bottom: 6px; }
.login-subtitle { font-size: 14px; color: #94A3B8; }

.login-form { display: flex; flex-direction: column; gap: 22px; }

/* Field */
.field { display: flex; flex-direction: column; gap: 6px; }
.field-label { font-size: 13px; font-weight: 600; color: #475569; }
.field-wrap {
  display: flex; align-items: center; gap: 10px; padding: 0 14px;
  height: 46px; background: #F8FAFC; border-radius: 12px;
  border: 1.5px solid #E2E8F0; transition: all 0.2s ease;
}
.field-wrap:focus-within {
  background: #fff; border-color: #0EA5E9;
  box-shadow: 0 0 0 3px rgba(14,165,233,0.1);
}
.field-error {
  border-color: #FCA5A5 !important;
  box-shadow: 0 0 0 3px rgba(239,68,68,0.08) !important;
}
.field-icon { width: 18px; height: 18px; color: #94A3B8; flex-shrink: 0; }
.field-wrap:focus-within .field-icon { color: #0EA5E9; }
.field-error .field-icon { color: #FCA5A5; }
.field-input {
  flex: 1; border: none; background: transparent; outline: none;
  font-size: 14px; color: #0F172A; min-width: 0;
}
.field-input::placeholder { color: #94A3B8; }
.field-toggle {
  background: none; border: none; cursor: pointer; padding: 4px;
  color: #94A3B8; transition: color 0.2s; display: flex;
}
.field-toggle:hover { color: #475569; }
.field-toggle svg { width: 17px; height: 17px; }
.field-msg { font-size: 12px; color: #EF4444; padding-left: 4px; }

/* Submit */
.submit-btn {
  margin-top: 6px; height: 48px; border: none; border-radius: 12px;
  font-size: 15px; font-weight: 700; color: #fff; cursor: pointer;
  background: linear-gradient(135deg, #0EA5E9, #06B6D4);
  box-shadow: 0 4px 20px rgba(14,165,233,0.3);
  display: flex; align-items: center; justify-content: center; gap: 8px;
  transition: all 0.2s ease;
}
.submit-btn:hover { transform: translateY(-1px); box-shadow: 0 6px 28px rgba(14,165,233,0.4); }
.submit-btn:active { transform: scale(0.97); }
.submit-loading { opacity: 0.65; cursor: not-allowed; pointer-events: none; }

/* Spinner */
.spinner { width: 18px; height: 18px; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.spinner-track { opacity: 0.2; }
.spinner-head { opacity: 0.8; }

@media (max-width: 480px) {
  .login-card { padding: 28px 20px; }
}
</style>
