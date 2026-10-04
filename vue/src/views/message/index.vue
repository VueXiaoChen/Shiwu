<template>
  <div class="app-container">
    <div class="flex rounded-xl border border-gray-200/80 overflow-hidden bg-white" style="height: calc(100vh - 152px); min-height: 520px">

      <!-- 左侧: 会话列表 -->
      <aside class="w-[300px] flex-shrink-0 flex flex-col border-r border-gray-200/80 bg-white">
        <!-- 标题 + 搜索 -->
        <div class="px-4 pt-4 pb-3 flex-shrink-0">
          <div class="flex items-center justify-between mb-3">
            <h3 class="text-[15px] font-semibold text-gray-800">会话记录</h3>
            <span class="text-xs text-gray-400">{{ convTotal }} 个会话</span>
          </div>
          <div class="relative">
            <svg class="w-4 h-4 text-gray-400 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            <input v-model="keyword" placeholder="搜索用户 / 物品 / 留言" class="w-full h-9 pl-10 pr-9 rounded-full border-0 bg-gray-100 text-[13px] text-gray-700 placeholder-gray-400 outline-none transition-all focus:bg-white focus:ring-2 focus:ring-[var(--color-primary)]/30" @keyup.enter="handleQuery"/>
            <button v-if="keyword" @click="keyword = ''; handleQuery()" class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-300 hover:text-gray-500 transition-colors">
              <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>
        </div>

        <!-- 会话卡片 -->
        <div class="flex-1 overflow-y-auto px-2 pb-2 conv-scroll relative">
          <div v-if="convLoading" class="absolute inset-0 bg-white/70 z-10 flex items-center justify-center">
            <svg class="w-6 h-6 animate-spin text-[var(--color-primary)]" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" stroke-dasharray="32" stroke-linecap="round" class="opacity-25"/><path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" stroke-width="3" stroke-linecap="round"/></svg>
          </div>

          <div v-for="row in convList" :key="convKey(row)" @click="selectConv(row)"
               class="px-3 py-3 mb-1 rounded-xl cursor-pointer transition-colors duration-150"
               :class="isActive(row) ? 'bg-[var(--color-primary-bg)]' : 'hover:bg-gray-50'">
            <div class="flex items-center gap-3">
              <!-- 双头像横向叠放 -->
              <div class="flex -space-x-3 flex-shrink-0">
                <img v-if="row.fromUserAvatar" :src="toFullUrl(row.fromUserAvatar)" class="w-9 h-9 rounded-full object-cover ring-2 ring-white" alt=""/>
                <span v-else class="w-9 h-9 rounded-full bg-[var(--color-primary)]/85 text-white text-xs font-medium flex items-center justify-center ring-2 ring-white">{{ (row.fromUserName || '?').charAt(0) }}</span>
                <img v-if="row.toUserAvatar" :src="toFullUrl(row.toUserAvatar)" class="w-9 h-9 rounded-full object-cover ring-2 ring-white" alt=""/>
                <span v-else class="w-9 h-9 rounded-full bg-orange-400/90 text-white text-xs font-medium flex items-center justify-center ring-2 ring-white">{{ (row.toUserName || '?').charAt(0) }}</span>
              </div>

              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between gap-2">
                  <p class="truncate text-[13px] font-medium text-gray-800">{{ row.fromUserName }}<span class="text-gray-400 font-normal mx-1">与</span>{{ row.toUserName }}</p>
                  <span class="text-[11px] text-gray-400 flex-shrink-0">{{ fmtListTime(row.createTime) }}</span>
                </div>
                <div class="flex items-center justify-between gap-2 mt-0.5">
                  <p class="truncate text-xs text-gray-500 leading-5">{{ row.content }}</p>
                </div>
              </div>
            </div>
            <!-- 物品标签 -->
            <div class="mt-2 pl-[48px] flex items-center gap-1.5">
              <span class="inline-flex items-center gap-1 max-w-full px-2 py-[3px] rounded-md text-[11px] leading-4 truncate"
                    :class="isActive(row) ? 'bg-white/80 text-[var(--color-primary)]' : 'bg-gray-100 text-gray-500'">
                <svg class="w-3 h-3 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
                <span class="truncate">{{ row.itemTitle || '物品已删除' }}</span>
              </span>
              <span class="text-[11px] text-gray-400 flex-shrink-0">{{ row.messageCount }}条</span>
            </div>
          </div>

          <div v-if="!convLoading && convList.length === 0" class="flex flex-col items-center justify-center py-20 text-gray-400">
            <svg class="w-10 h-10 mb-3 text-gray-200" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            <p class="text-[13px]">暂无会话</p>
          </div>
        </div>

        <!-- 底部分页 -->
        <div v-if="totalPages > 1" class="h-10 px-4 flex items-center justify-between border-t border-gray-100 text-xs text-gray-400 flex-shrink-0">
          <span>第 {{ convQuery.pageNum }} / {{ totalPages }} 页</span>
          <div class="flex items-center gap-1">
            <button :disabled="convQuery.pageNum <= 1" @click="changePage(-1)" class="w-6 h-6 rounded-md flex items-center justify-center transition-colors" :class="convQuery.pageNum <= 1 ? 'text-gray-300 cursor-not-allowed' : 'text-gray-500 hover:bg-gray-100'">
              <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="15 18 9 12 15 6"/></svg>
            </button>
            <button :disabled="convQuery.pageNum >= totalPages" @click="changePage(1)" class="w-6 h-6 rounded-md flex items-center justify-center transition-colors" :class="convQuery.pageNum >= totalPages ? 'text-gray-300 cursor-not-allowed' : 'text-gray-500 hover:bg-gray-100'">
              <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
            </button>
          </div>
        </div>
      </aside>

      <!-- 右侧: 对话记录 -->
      <section class="flex-1 min-w-0 flex flex-col">
        <template v-if="current">
          <!-- 会话头部 -->
          <header class="px-6 py-3.5 flex items-center gap-3.5 border-b border-gray-200/80 bg-white flex-shrink-0">
            <div class="flex -space-x-3 flex-shrink-0">
              <img v-if="current.userAAvatar" :src="toFullUrl(current.userAAvatar)" class="w-10 h-10 rounded-full object-cover ring-2 ring-white" alt=""/>
              <span v-else class="w-10 h-10 rounded-full bg-[var(--color-primary)]/85 text-white text-sm font-medium flex items-center justify-center ring-2 ring-white">{{ (current.userAName || '?').charAt(0) }}</span>
              <img v-if="current.userBAvatar" :src="toFullUrl(current.userBAvatar)" class="w-10 h-10 rounded-full object-cover ring-2 ring-white" alt=""/>
              <span v-else class="w-10 h-10 rounded-full bg-orange-400/90 text-white text-sm font-medium flex items-center justify-center ring-2 ring-white">{{ (current.userBName || '?').charAt(0) }}</span>
            </div>
            <div class="min-w-0">
              <p class="text-[15px] font-semibold text-gray-800 truncate">{{ current.userAName }}<span class="text-gray-400 font-normal mx-1.5">与</span>{{ current.userBName }}</p>
              <p class="text-xs text-gray-400 truncate mt-0.5">围绕「{{ current.itemTitle || '已删除物品' }}」的留言往来</p>
            </div>

            <div class="ml-auto flex items-center gap-2 flex-shrink-0">
              <span class="px-2.5 py-1 rounded-full text-xs bg-gray-100 text-gray-500">共 {{ detailList.length }} 条</span>
              <button @click="handleDeleteConversation" title="删除整段对话" class="h-8 px-3 rounded-lg text-xs font-medium text-gray-500 hover:text-red-500 hover:bg-red-50 transition-colors flex items-center gap-1.5">
                <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>删除对话
              </button>
            </div>
          </header>

          <!-- 消息气泡区 -->
          <div ref="chatBodyRef" class="flex-1 overflow-y-auto px-6 py-5 bg-[#f6f8fb] conv-scroll relative">
            <div v-if="detailLoading" class="absolute inset-0 bg-white/60 z-10 flex items-center justify-center">
              <svg class="w-6 h-6 animate-spin text-[var(--color-primary)]" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" stroke-dasharray="32" stroke-linecap="round" class="opacity-25"/><path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" stroke-width="3" stroke-linecap="round"/></svg>
            </div>

            <template v-for="(msg, index) in detailList" :key="msg.messageId">
              <!-- 日期分隔线 -->
              <div v-if="showDateDivider(index)" class="flex items-center gap-3 my-5">
                <span class="flex-1 h-px bg-gray-200/80"></span>
                <span class="text-[11px] text-gray-400">{{ fmtDate(msg.createTime) }}</span>
                <span class="flex-1 h-px bg-gray-200/80"></span>
              </div>

              <!-- 单条气泡: 甲在左, 乙在右 -->
              <div class="group flex items-start gap-3 mb-5 msg-in" :class="{ 'flex-row-reverse': !isLeft(msg) }">
                <img v-if="msg.fromUserAvatar" :src="toFullUrl(msg.fromUserAvatar)" class="w-9 h-9 rounded-full object-cover flex-shrink-0" alt=""/>
                <span v-else class="w-9 h-9 rounded-full text-sm font-medium text-white flex items-center justify-center flex-shrink-0" :class="isLeft(msg) ? 'bg-[var(--color-primary)]/85' : 'bg-orange-400/90'">{{ (msg.fromUserName || '?').charAt(0) }}</span>

                <div class="max-w-[58%] flex flex-col" :class="isLeft(msg) ? 'items-start' : 'items-end'">
                  <p class="text-xs text-gray-400 mb-1.5 px-1">{{ msg.fromUserName }}</p>
                  <div class="flex items-center gap-2" :class="{ 'flex-row-reverse': !isLeft(msg) }">
                    <div class="px-4 py-2.5 text-sm leading-6 whitespace-pre-wrap break-all"
                         :class="isLeft(msg)
                           ? 'bg-white text-gray-700 rounded-2xl rounded-tl-md shadow-[0_1px_2px_rgba(15,40,80,0.06)]'
                           : 'bg-[var(--color-primary)] text-white rounded-2xl rounded-tr-md shadow-[0_1px_3px_rgba(64,158,255,0.3)]'">{{ msg.content }}</div>
                    <!-- 悬停显示删除 -->
                    <button @click="handleDeleteMessage(msg)" title="删除这条留言" class="opacity-0 group-hover:opacity-100 transition-opacity w-7 h-7 rounded-full flex items-center justify-center text-gray-400 hover:text-red-500 hover:bg-red-50 flex-shrink-0">
                      <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                    </button>
                  </div>
                  <p class="text-[11px] text-gray-400 mt-1.5 px-1">
                    {{ fmtTime(msg.createTime) }}
                    <span v-if="msg.isRead === 1" class="ml-1">已读</span>
                    <span v-else class="ml-1 text-orange-400">未读</span>
                  </p>
                </div>
              </div>
            </template>

            <div v-if="!detailLoading && detailList.length === 0" class="h-full flex flex-col items-center justify-center text-gray-400">
              <p class="text-[13px]">该会话暂无留言</p>
            </div>
          </div>
        </template>

        <!-- 未选中会话的空状态 -->
        <div v-else class="flex-1 flex flex-col items-center justify-center bg-[#f6f8fb]">
          <div class="w-[72px] h-[72px] rounded-2xl bg-white shadow-[0_2px_8px_rgba(15,40,80,0.06)] flex items-center justify-center mb-5">
            <svg class="w-8 h-8 text-[var(--color-primary)]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/><line x1="8" y1="9" x2="16" y2="9"/><line x1="8" y1="13" x2="13" y2="13"/></svg>
          </div>
          <p class="text-sm font-medium text-gray-600">选择左侧会话查看对话记录</p>
          <p class="text-xs text-gray-400 mt-2">每个会话对应一件物品下两位用户之间的留言往来</p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import {computed, nextTick, onMounted, ref} from "vue";
import {deleteMessageByMessageIds, selectConversationDetail, selectConversationList} from "@/api/message/message.js";
import {ElMessage, ElMessageBox} from "@/utils/toast.js";

//相对路径的头像补上接口前缀, http开头的直接用
const baseUrl = import.meta.env.VITE_APP_BASE_API
const toFullUrl = (url) => url.startsWith('http') ? url : baseUrl + url

// ==================== 左侧会话列表 ====================
const keyword = ref('')
const convQuery = ref({pageNum: 1, pageSize: 20})
const convList = ref([])
const convTotal = ref(0)
const convLoading = ref(false)

const totalPages = computed(() => Math.max(1, Math.ceil(convTotal.value / convQuery.value.pageSize)))

//会话的唯一标识: 物品 + 归一化后的用户对(谁发谁收算同一个会话)
const convKey = (row) => {
  const [a, b] = [row.fromUserId, row.toUserId].sort((x, y) => x - y)
  return `${row.itemId}-${a}-${b}`
}
const isActive = (row) => current.value && convKey(row) === current.value.key

const getConvList = () => {
  convLoading.value = true
  const params = {...convQuery.value}
  if (keyword.value.trim()) params.keyword = keyword.value.trim()
  selectConversationList(params).then(res => {
    convList.value = res.rows
    convTotal.value = res.total
  }).finally(() => { convLoading.value = false })
}

const handleQuery = () => { convQuery.value.pageNum = 1; getConvList() }
const changePage = (step) => { convQuery.value.pageNum += step; getConvList() }

// ==================== 右侧对话记录 ====================
const current = ref(null)
const detailList = ref([])
const detailLoading = ref(false)
const chatBodyRef = ref(null)

//点开会话: 把双方信息固定下来(甲=最新留言的发送方, 气泡永远甲左乙右)
const selectConv = (row) => {
  current.value = {
    key: convKey(row),
    itemId: row.itemId, itemTitle: row.itemTitle,
    userAId: row.fromUserId, userAName: row.fromUserName, userAAvatar: row.fromUserAvatar,
    userBId: row.toUserId, userBName: row.toUserName, userBAvatar: row.toUserAvatar
  }
  getDetail()
}

const isLeft = (msg) => msg.fromUserId === current.value.userAId

const getDetail = () => {
  detailLoading.value = true
  selectConversationDetail({
    itemId: current.value.itemId,
    userAId: current.value.userAId,
    userBId: current.value.userBId
  }).then(res => {
    detailList.value = res.data || []
    //新留言在底部, 打开直接滚到底
    nextTick(() => {
      if (chatBodyRef.value) chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
    })
  }).finally(() => { detailLoading.value = false })
}

// ==================== 删除 ====================
const handleDeleteMessage = (msg) => {
  ElMessageBox.confirm('是否确认删除这条留言?', '系统提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    deleteMessageByMessageIds(msg.messageId).then(() => {
      ElMessage.success('删除成功')
      //本地先剔掉, 再刷新左侧(最新一条/条数可能变了)
      detailList.value = detailList.value.filter(m => m.messageId !== msg.messageId)
      if (detailList.value.length === 0) current.value = null
      getConvList()
    })
  }).catch(() => {})
}

const handleDeleteConversation = () => {
  if (detailList.value.length === 0) return
  ElMessageBox.confirm(`是否确认删除该会话的全部 ${detailList.value.length} 条留言?`, '系统提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    const ids = detailList.value.map(m => m.messageId).join(',')
    deleteMessageByMessageIds(ids).then(() => {
      ElMessage.success('删除成功')
      current.value = null
      detailList.value = []
      getConvList()
    })
  }).catch(() => {})
}

// ==================== 时间格式化 ====================
//后端时间格式固定为 yyyy-MM-dd HH:mm:ss, 直接切字符串就行
const fmtDate = (t) => t ? t.slice(0, 10) : ''
const fmtTime = (t) => t ? t.slice(11, 16) : ''
//列表时间: 今天只显示时分, 今年显示月-日, 更早显示完整日期
const fmtListTime = (t) => {
  if (!t) return ''
  const now = new Date()
  const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  if (t.slice(0, 10) === today) return t.slice(11, 16)
  if (t.slice(0, 4) === String(now.getFullYear())) return t.slice(5, 10)
  return t.slice(0, 10)
}
//跨天的地方插一条日期分隔线
const showDateDivider = (index) => {
  if (index === 0) return true
  return fmtDate(detailList.value[index].createTime) !== fmtDate(detailList.value[index - 1].createTime)
}

onMounted(() => getConvList())
</script>

<style scoped>
/* 细滚动条, 默认藏起来, 滑过再显 */
.conv-scroll::-webkit-scrollbar {
  width: 6px;
}
.conv-scroll::-webkit-scrollbar-thumb {
  background: transparent;
  border-radius: 3px;
}
.conv-scroll:hover::-webkit-scrollbar-thumb {
  background: rgba(0, 23, 45, 0.12);
}

/* 消息气泡入场: 轻微上浮渐显 */
.msg-in {
  animation: msgIn 0.25s ease-out both;
}
@keyframes msgIn {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
