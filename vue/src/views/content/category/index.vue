<template>
  <div class="app-container">
    <!-- 顶部查询 -->
    <div class="flex flex-wrap items-end gap-3 mb-4">
      <div class="flex items-center gap-2">
        <label class="text-sm text-gray-600 w-[70px]">分类名称</label>
        <input v-model="query.categoryName" placeholder="请输入分类名称" class="h-9 px-3 rounded-lg border border-gray-300 text-sm outline-none focus:border-[var(--color-primary)] focus:ring-2 focus:ring-[var(--color-primary)]/20 w-52" @keyup.enter="handleQuery"/>
      </div>
      <div class="flex gap-2">
        <button @click="handleQuery" :disabled="loading" class="h-9 px-4 rounded-lg text-sm font-medium text-white transition-colors flex items-center gap-1" :class="loading ? 'bg-[var(--color-primary)]/50 cursor-not-allowed' : 'bg-[var(--color-primary)] hover:bg-[var(--color-primary-dark)]'">
          <svg v-if="!loading" class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <svg v-else class="w-3.5 h-3.5 animate-spin" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" stroke-dasharray="32" stroke-linecap="round" class="opacity-25"/><path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" stroke-width="3" stroke-linecap="round"/></svg>
          {{ loading ? '查询中...' : '搜索' }}
        </button>
        <button @click="resetQuery" :disabled="loading" class="h-9 px-4 rounded-lg text-sm font-medium text-gray-600 border border-gray-300 bg-white hover:bg-gray-50 transition-colors flex items-center gap-1" :class="{ 'opacity-50 cursor-not-allowed': loading }">
          <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>重置
        </button>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="flex gap-2 mb-2">
      <button @click="handleInsert" class="h-9 px-4 rounded-lg text-sm font-medium text-[var(--color-primary)] border border-[var(--color-primary)] bg-white hover:bg-[var(--color-primary-bg)] transition-colors flex items-center gap-1">
        <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>新增
      </button>
      <button :disabled="single" @click="handleUpdate()" class="h-9 px-4 rounded-lg text-sm font-medium transition-colors flex items-center gap-1" :class="single ? 'text-gray-400 border border-gray-200 bg-gray-50 cursor-not-allowed' : 'text-green-600 border border-green-500 bg-white hover:bg-green-50'">
        <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>修改
      </button>
      <button :disabled="multiple" @click="handleDelete()" class="h-9 px-4 rounded-lg text-sm font-medium transition-colors flex items-center gap-1" :class="multiple ? 'text-gray-400 border border-gray-200 bg-gray-50 cursor-not-allowed' : 'text-red-500 border border-red-400 bg-white hover:bg-red-50'">
        <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>删除
      </button>
    </div>

    <!-- 数据表格 -->
    <div class="overflow-x-auto border border-gray-200 rounded-lg relative">
      <div v-if="loading" class="absolute inset-0 bg-white/60 z-10 flex items-center justify-center rounded-lg">
        <div class="flex flex-col items-center gap-2">
          <svg class="w-8 h-8 animate-spin text-[var(--color-primary)]" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" stroke-dasharray="32" stroke-linecap="round" class="opacity-25"/><path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" stroke-width="3" stroke-linecap="round"/></svg>
          <span class="text-sm text-gray-500">加载中...</span>
        </div>
      </div>
      <table class="w-full text-sm table-fixed">
        <thead class="bg-gray-50">
          <tr>
            <th class="w-[55px] px-3 py-3 text-center"><input type="checkbox" :checked="isAllSelected" @change="toggleSelectAll" class="w-4 h-4 rounded accent-[var(--color-primary)]"/></th>
            <th class="w-[100px] px-3 py-3 text-center font-medium text-gray-600">序号</th>
            <th class="px-3 py-3 text-center font-medium text-gray-600">分类名称</th>
            <th class="w-[100px] px-3 py-3 text-center font-medium text-gray-600">分类图标</th>
            <th class="w-[120px] px-3 py-3 text-center font-medium text-gray-600">显示排序</th>
            <th class="w-[180px] px-3 py-3 text-center font-medium text-gray-600">创建时间</th>
            <th class="w-[200px] px-3 py-3 text-center font-medium text-gray-600">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, index) in categoryList" :key="row.categoryId" class="border-t border-gray-100 hover:bg-gray-50/50 transition-colors" :class="{ 'bg-[var(--color-primary-bg)]/30': selectedIds.has(row.categoryId) }">
            <td class="px-3 py-3 text-center"><input type="checkbox" :checked="selectedIds.has(row.categoryId)" @change="toggleRow(row.categoryId)" class="w-4 h-4 rounded accent-[var(--color-primary)]"/></td>
            <td class="px-3 py-3 text-center text-gray-600">{{ (query.pageNum - 1) * query.pageSize + index + 1 }}</td>
            <td class="px-3 py-3 text-center"><p class="truncate text-gray-700" :title="row.categoryName">{{ row.categoryName }}</p></td>
            <td class="px-3 py-3 text-center">
              <div class="flex justify-center">
                <ImagePreview v-if="row.icon" :src="row.icon" width="40px" height="40px"/>
                <span v-else class="text-gray-300">-</span>
              </div>
            </td>
            <td class="px-3 py-3 text-center text-gray-600">{{ row.categorySort }}</td>
            <td class="px-3 py-3 text-center text-gray-500">{{ row.createTime }}</td>
            <td class="px-3 py-3 text-center">
              <button @click="handleUpdate(row)" class="text-[var(--color-primary)] hover:text-[var(--color-primary-dark)] text-sm mr-3 font-medium">修改</button>
              <button @click="handleDelete(row)" class="text-red-500 hover:text-red-600 text-sm font-medium">删除</button>
            </td>
          </tr>
          <tr v-if="categoryList.length === 0"><td colspan="7" class="px-3 py-12 text-center text-gray-400">暂无数据</td></tr>
        </tbody>
      </table>
    </div>

    <pagination :total="total" v-model:page="query.pageNum" v-model:limit="query.pageSize" @pagination="getList"/>

    <!-- 新增/修改对话框 -->
    <Modal :title="title" v-model="open" width="520px">
      <form @submit.prevent="submitForm" class="space-y-4">
        <div class="flex items-start gap-3">
          <label class="w-20 pt-2 text-sm text-gray-600 text-right flex-shrink-0">分类名称</label>
          <div class="flex-1">
            <input v-model="form.categoryName" placeholder="请输入分类名称" maxlength="20" class="w-full h-9 px-3 rounded-lg border border-gray-300 text-sm outline-none focus:border-[var(--color-primary)] focus:ring-2 focus:ring-[var(--color-primary)]/20"/>
          </div>
        </div>
        <div class="flex items-start gap-3">
          <label class="w-20 pt-2 text-sm text-gray-600 text-right flex-shrink-0">分类图标</label>
          <div class="flex-1">
            <ImageUpload v-model="form.icon" :limit="1"/>
          </div>
        </div>
        <div class="flex items-start gap-3">
          <label class="w-20 pt-2 text-sm text-gray-600 text-right flex-shrink-0">显示排序</label>
          <div class="flex-1">
            <input v-model.number="form.categorySort" type="number" min="0" placeholder="请输入显示排序" class="w-full h-9 px-3 rounded-lg border border-gray-300 text-sm outline-none focus:border-[var(--color-primary)] focus:ring-2 focus:ring-[var(--color-primary)]/20"/>
            <p class="mt-1 text-xs text-gray-400">数字越小排序越靠前</p>
          </div>
        </div>
      </form>
      <template #footer>
        <button @click="submitForm" class="h-9 px-5 rounded-lg text-sm font-medium text-white bg-[var(--color-primary)] hover:bg-[var(--color-primary-dark)] transition-colors">保存</button>
        <button @click="open = false" class="h-9 px-5 rounded-lg text-sm font-medium text-gray-600 border border-gray-300 bg-white hover:bg-gray-50 transition-colors">取消</button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import {onMounted, ref, reactive, computed} from "vue";
import {deleteCategoryByCategoryIds, insertCategory, selectCategoryByCategoryId, selectCategoryList, updateCategory} from "@/api/content/category.js";
import Pagination from "@/components/Pagination/index.vue";
import {ElMessage, ElMessageBox} from "@/utils/toast.js";

const title = ref('')
const open = ref(false)

const form = reactive({ categoryId: null, categoryName: '', icon: '', categorySort: 0 })

const handleInsert = () => {
  Object.assign(form, { categoryId: null, categoryName: '', icon: '', categorySort: 0 })
  open.value = true
  title.value = '新增分类'
}

const handleUpdate = (row) => {
  Object.assign(form, { categoryId: null, categoryName: '', icon: '', categorySort: 0 })
  const categoryId = row ? row.categoryId : ids.value[0]
  if (!categoryId) return
  selectCategoryByCategoryId(categoryId).then(res => {
    Object.assign(form, res.data)
    open.value = true
    title.value = '修改分类'
  })
}

const handleDelete = (row) => {
  const categoryIds = row ? [row.categoryId] : ids.value
  if (!categoryIds || categoryIds.length === 0) return
  ElMessageBox.confirm('是否确认删除物品分类?', '系统提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    deleteCategoryByCategoryIds(categoryIds.join(',')).then(() => {
      ElMessage.success('删除成功')
      getList()
    })
  }).catch(() => {})
}

const submitForm = () => {
  if (!form.categoryName || !form.categoryName.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  if (form.categorySort === '' || form.categorySort == null || form.categorySort < 0) {
    ElMessage.warning('请输入正确的显示排序')
    return
  }
  if (form.categoryId != null) {
    updateCategory({...form}).then(() => {
      ElMessage.success('修改成功')
      open.value = false; getList()
    })
  } else {
    insertCategory({...form}).then(() => {
      ElMessage.success('新增成功')
      open.value = false; getList()
    })
  }
}

const query = ref({ pageNum: 1, pageSize: 10, categoryName: '' })
const categoryList = ref([])
const total = ref(0)
const ids = ref([])
const selectedIds = reactive(new Set())
const loading = ref(false)

const single = computed(() => ids.value.length !== 1)
const multiple = computed(() => ids.value.length === 0)
const isAllSelected = computed(() => categoryList.value.length > 0 && selectedIds.size === categoryList.value.length)

const syncSelected = () => { ids.value = [...selectedIds] }
const toggleRow = (id) => {
  if (selectedIds.has(id)) selectedIds.delete(id)
  else selectedIds.add(id)
  syncSelected()
}
const toggleSelectAll = () => {
  if (isAllSelected.value) categoryList.value.forEach(r => selectedIds.delete(r.categoryId))
  else categoryList.value.forEach(r => selectedIds.add(r.categoryId))
  syncSelected()
}

const handleQuery = () => { query.value.pageNum = 1; getList() }
const resetQuery = () => { query.value.categoryName = ''; handleQuery() }

const getList = () => {
  loading.value = true
  selectCategoryList(query.value).then(res => {
    categoryList.value = res.rows
    total.value = res.total
  }).finally(() => { loading.value = false })
}

onMounted(() => getList())
</script>
