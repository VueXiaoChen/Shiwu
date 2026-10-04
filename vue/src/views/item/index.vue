<template>
  <div class="app-container">
    <!-- 顶部查询 -->
    <div class="flex flex-wrap items-end gap-3 mb-4">
      <div class="flex items-center gap-2">
        <label class="text-sm text-gray-600 w-[70px]">物品名称</label>
        <input v-model="query.title" placeholder="请输入物品名称" class="h-9 px-3 rounded-lg border border-gray-300 text-sm outline-none focus:border-[var(--color-primary)] focus:ring-2 focus:ring-[var(--color-primary)]/20 w-52" @keyup.enter="handleQuery"/>
      </div>
      <div class="flex items-center gap-2">
        <label class="text-sm text-gray-600">信息类型</label>
        <select v-model="query.type" class="h-9 px-3 rounded-lg border border-gray-300 text-sm outline-none bg-white focus:border-[var(--color-primary)] focus:ring-2 focus:ring-[var(--color-primary)]/20 w-36">
          <option value="">全部</option>
          <option value="lost">寻物启事</option>
          <option value="found">失物招领</option>
        </select>
      </div>
      <div class="flex items-center gap-2">
        <label class="text-sm text-gray-600">物品分类</label>
        <select v-model="query.categoryId" class="h-9 px-3 rounded-lg border border-gray-300 text-sm outline-none bg-white focus:border-[var(--color-primary)] focus:ring-2 focus:ring-[var(--color-primary)]/20 w-36">
          <option value="">全部</option>
          <option v-for="c in categoryList" :key="c.categoryId" :value="c.categoryId">{{ c.categoryName }}</option>
        </select>
      </div>
      <div class="flex items-center gap-2">
        <label class="text-sm text-gray-600">状态</label>
        <select v-model="query.status" class="h-9 px-3 rounded-lg border border-gray-300 text-sm outline-none bg-white focus:border-[var(--color-primary)] focus:ring-2 focus:ring-[var(--color-primary)]/20 w-32">
          <option value="">全部</option>
          <option value="open">进行中</option>
          <option value="done">已完成</option>
        </select>
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
            <th class="w-[45px] px-3 py-3 text-center"><input type="checkbox" :checked="isAllSelected" @change="toggleSelectAll" class="w-4 h-4 rounded accent-[var(--color-primary)]"/></th>
            <th class="w-[60px] px-3 py-3 text-center font-medium text-gray-600">序号</th>
            <th class="w-[130px] px-3 py-3 text-center font-medium text-gray-600">图片</th>
            <th class="px-3 py-3 text-center font-medium text-gray-600">物品名称</th>
            <th class="w-[90px] px-3 py-3 text-center font-medium text-gray-600">类型</th>
            <th class="w-[100px] px-3 py-3 text-center font-medium text-gray-600">分类</th>
            <th class="w-[150px] px-3 py-3 text-center font-medium text-gray-600">地点</th>
            <th class="w-[90px] px-3 py-3 text-center font-medium text-gray-600">发布人</th>
            <th class="w-[80px] px-3 py-3 text-center font-medium text-gray-600">状态</th>
            <th class="w-[170px] px-3 py-3 text-center font-medium text-gray-600">发布时间</th>
            <th class="w-[130px] px-3 py-3 text-center font-medium text-gray-600">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, index) in itemList" :key="row.itemId" class="border-t border-gray-100 hover:bg-gray-50/50 transition-colors" :class="{ 'bg-[var(--color-primary-bg)]/30': selectedIds.has(row.itemId) }">
            <td class="px-3 py-3 text-center"><input type="checkbox" :checked="selectedIds.has(row.itemId)" @change="toggleRow(row.itemId)" class="w-4 h-4 rounded accent-[var(--color-primary)]"/></td>
            <td class="px-3 py-3 text-center text-gray-600">{{ (query.pageNum - 1) * query.pageSize + index + 1 }}</td>
            <td class="px-3 py-3">
              <div class="flex justify-center gap-1">
                <ImagePreview v-for="(img, i) in splitImages(row.images)" :key="i" :src="img" width="36px" height="36px"/>
                <span v-if="!row.images" class="text-gray-300">-</span>
              </div>
            </td>
            <td class="px-3 py-3 text-center"><p class="truncate cursor-pointer text-[var(--color-primary)] hover:text-[var(--color-primary-dark)] transition-colors" :title="row.title" @click="handleView(row)">{{ row.title }}</p></td>
            <td class="px-3 py-3 text-center">
              <span class="inline-block px-2 py-0.5 rounded text-xs font-medium" :class="row.type === 'lost' ? 'bg-orange-50 text-orange-600' : 'bg-green-50 text-green-600'">{{ row.type === 'lost' ? '寻物启事' : '失物招领' }}</span>
            </td>
            <td class="px-3 py-3 text-center text-gray-600">{{ row.categoryName }}</td>
            <td class="px-3 py-3 text-center text-gray-600"><p class="truncate" :title="row.location">{{ row.location }}</p></td>
            <td class="px-3 py-3 text-center text-gray-600"><p class="truncate" :title="row.userName">{{ row.userName }}</p></td>
            <td class="px-3 py-3 text-center">
              <span class="inline-block px-2 py-0.5 rounded text-xs font-medium" :class="row.status === 'open' ? 'bg-[var(--color-primary-bg)] text-[var(--color-primary)]' : 'bg-gray-100 text-gray-500'">{{ row.status === 'open' ? '进行中' : '已完成' }}</span>
            </td>
            <td class="px-3 py-3 text-center text-gray-500">{{ row.createTime }}</td>
            <td class="px-3 py-3 text-center">
              <button @click="handleUpdate(row)" class="text-[var(--color-primary)] hover:text-[var(--color-primary-dark)] text-sm mr-3 font-medium">修改</button>
              <button @click="handleDelete(row)" class="text-red-500 hover:text-red-600 text-sm font-medium">删除</button>
            </td>
          </tr>
          <tr v-if="itemList.length === 0"><td colspan="11" class="px-3 py-12 text-center text-gray-400">暂无数据</td></tr>
        </tbody>
      </table>
    </div>

    <pagination :total="total" v-model:page="query.pageNum" v-model:limit="query.pageSize" @pagination="getList"/>

    <!-- 修改对话框 -->
    <Modal title="修改物品信息" v-model="open" width="720px">
      <form @submit.prevent="submitForm" class="space-y-4">
        <div class="flex items-start gap-3">
          <label class="w-20 pt-2 text-sm text-gray-600 text-right flex-shrink-0">物品名称</label>
          <div class="flex-1">
            <input v-model="form.title" placeholder="请输入物品名称" class="w-full h-9 px-3 rounded-lg border border-gray-300 text-sm outline-none focus:border-[var(--color-primary)] focus:ring-2 focus:ring-[var(--color-primary)]/20"/>
          </div>
        </div>
        <div class="flex items-start gap-3">
          <label class="w-20 pt-2 text-sm text-gray-600 text-right flex-shrink-0">信息类型</label>
          <div class="flex-1 flex items-center gap-4 pt-1.5">
            <label class="flex items-center gap-1.5 text-sm text-gray-700 cursor-pointer">
              <input type="radio" value="lost" v-model="form.type" class="w-4 h-4 accent-[var(--color-primary)]"/>寻物启事
            </label>
            <label class="flex items-center gap-1.5 text-sm text-gray-700 cursor-pointer">
              <input type="radio" value="found" v-model="form.type" class="w-4 h-4 accent-[var(--color-primary)]"/>失物招领
            </label>
          </div>
        </div>
        <div class="flex items-start gap-3">
          <label class="w-20 pt-2 text-sm text-gray-600 text-right flex-shrink-0">物品分类</label>
          <div class="flex-1">
            <select v-model="form.categoryId" class="w-full h-9 px-3 rounded-lg border border-gray-300 text-sm outline-none bg-white focus:border-[var(--color-primary)] focus:ring-2 focus:ring-[var(--color-primary)]/20">
              <option :value="null" disabled>请选择物品分类</option>
              <option v-for="c in categoryList" :key="c.categoryId" :value="c.categoryId">{{ c.categoryName }}</option>
            </select>
          </div>
        </div>
        <div class="flex items-start gap-3">
          <label class="w-20 pt-2 text-sm text-gray-600 text-right flex-shrink-0">地点</label>
          <div class="flex-1">
            <input v-model="form.location" placeholder="请输入丢失/拾取地点" class="w-full h-9 px-3 rounded-lg border border-gray-300 text-sm outline-none focus:border-[var(--color-primary)] focus:ring-2 focus:ring-[var(--color-primary)]/20"/>
          </div>
        </div>
        <div class="flex items-start gap-3">
          <label class="w-20 pt-2 text-sm text-gray-600 text-right flex-shrink-0">发生日期</label>
          <div class="flex-1">
            <input type="date" v-model="form.happenTime" class="w-full h-9 px-3 rounded-lg border border-gray-300 text-sm outline-none bg-white focus:border-[var(--color-primary)] focus:ring-2 focus:ring-[var(--color-primary)]/20"/>
          </div>
        </div>
        <div class="flex items-start gap-3">
          <label class="w-20 pt-2 text-sm text-gray-600 text-right flex-shrink-0">联系方式</label>
          <div class="flex-1">
            <input v-model="form.contact" placeholder="请输入联系方式" class="w-full h-9 px-3 rounded-lg border border-gray-300 text-sm outline-none focus:border-[var(--color-primary)] focus:ring-2 focus:ring-[var(--color-primary)]/20"/>
          </div>
        </div>
        <div class="flex items-start gap-3">
          <label class="w-20 pt-2 text-sm text-gray-600 text-right flex-shrink-0">状态</label>
          <div class="flex-1 flex items-center gap-4 pt-1.5">
            <label class="flex items-center gap-1.5 text-sm text-gray-700 cursor-pointer">
              <input type="radio" value="open" v-model="form.status" class="w-4 h-4 accent-[var(--color-primary)]"/>进行中
            </label>
            <label class="flex items-center gap-1.5 text-sm text-gray-700 cursor-pointer">
              <input type="radio" value="done" v-model="form.status" class="w-4 h-4 accent-[var(--color-primary)]"/>已完成
            </label>
          </div>
        </div>
        <!-- 急寻和悬赏是寻物启事才有的东西 -->
        <div v-if="form.type === 'lost'" class="flex items-start gap-3">
          <label class="w-20 pt-2 text-sm text-gray-600 text-right flex-shrink-0">急寻悬赏</label>
          <div class="flex-1 flex items-center gap-4">
            <label class="flex items-center gap-1.5 text-sm text-gray-700 cursor-pointer pt-1.5">
              <input type="checkbox" :checked="form.urgent === 1" @change="form.urgent = $event.target.checked ? 1 : 0" class="w-4 h-4 rounded accent-[var(--color-primary)]"/>急寻
            </label>
            <input v-model="form.reward" placeholder="悬赏说明(选填, 如: 50元)" class="flex-1 h-9 px-3 rounded-lg border border-gray-300 text-sm outline-none focus:border-[var(--color-primary)] focus:ring-2 focus:ring-[var(--color-primary)]/20"/>
          </div>
        </div>
        <div class="flex items-start gap-3">
          <label class="w-20 pt-2 text-sm text-gray-600 text-right flex-shrink-0">详细描述</label>
          <div class="flex-1">
            <textarea v-model="form.description" rows="4" placeholder="请输入详细描述" class="w-full px-3 py-2 rounded-lg border border-gray-300 text-sm outline-none resize-none focus:border-[var(--color-primary)] focus:ring-2 focus:ring-[var(--color-primary)]/20"></textarea>
          </div>
        </div>
        <div class="flex items-start gap-3">
          <label class="w-20 pt-2 text-sm text-gray-600 text-right flex-shrink-0">物品图片</label>
          <div class="flex-1">
            <ImageUpload v-model="form.images" :limit="3"/>
          </div>
        </div>
      </form>
      <template #footer>
        <button @click="submitForm" class="h-9 px-5 rounded-lg text-sm font-medium text-white bg-[var(--color-primary)] hover:bg-[var(--color-primary-dark)] transition-colors">保存</button>
        <button @click="open = false" class="h-9 px-5 rounded-lg text-sm font-medium text-gray-600 border border-gray-300 bg-white hover:bg-gray-50 transition-colors">取消</button>
      </template>
    </Modal>

    <!-- 查看对话框 -->
    <Modal title="物品详情" v-model="viewOpen" width="720px">
      <div class="space-y-5">
        <!-- 顶部大图: 有图就当主视觉铺开, 标签浮在图片左上角 -->
        <div v-if="viewImages.length > 0">
          <div class="relative rounded-xl overflow-hidden bg-gray-100">
            <ImagePreview :src="mainSrc" width="100%" height="240px"/>
            <div class="absolute top-3 left-3 flex gap-1.5 pointer-events-none">
              <span class="inline-block px-2.5 py-1 rounded-full text-xs font-medium bg-white/95 shadow-sm" :class="viewItem.type === 'lost' ? 'text-orange-600' : 'text-green-600'">{{ viewItem.type === 'lost' ? '寻物启事' : '失物招领' }}</span>
              <span v-if="viewItem.urgent === 1" class="inline-block px-2.5 py-1 rounded-full text-xs font-medium bg-white/95 shadow-sm text-red-500">急寻</span>
              <span class="inline-block px-2.5 py-1 rounded-full text-xs font-medium bg-white/95 shadow-sm" :class="viewItem.status === 'open' ? 'text-[var(--color-primary)]' : 'text-gray-500'">{{ viewItem.status === 'open' ? '进行中' : '已完成' }}</span>
            </div>
          </div>
          <!-- 多图时的缩略图条: 点谁大图就换谁 -->
          <div v-if="viewImages.length > 1" class="flex gap-2 mt-3">
            <img v-for="(img, i) in viewImages" :key="i" :src="toFullUrl(img)" @click="mainIdx = i" alt=""
              class="w-16 h-16 rounded-lg object-cover cursor-pointer border-2 transition-all"
              :class="i === mainIdx ? 'border-[var(--color-primary)]' : 'border-transparent opacity-60 hover:opacity-100'"/>
          </div>
        </div>
        <!-- 没图时标签单独一行 -->
        <div v-else class="flex gap-1.5">
          <span class="inline-block px-2.5 py-1 rounded-full text-xs font-medium" :class="viewItem.type === 'lost' ? 'bg-orange-50 text-orange-600' : 'bg-green-50 text-green-600'">{{ viewItem.type === 'lost' ? '寻物启事' : '失物招领' }}</span>
          <span v-if="viewItem.urgent === 1" class="inline-block px-2.5 py-1 rounded-full text-xs font-medium bg-red-50 text-red-500">急寻</span>
          <span class="inline-block px-2.5 py-1 rounded-full text-xs font-medium" :class="viewItem.status === 'open' ? 'bg-[var(--color-primary-bg)] text-[var(--color-primary)]' : 'bg-gray-100 text-gray-500'">{{ viewItem.status === 'open' ? '进行中' : '已完成' }}</span>
        </div>

        <!-- 标题 + 发布信息 -->
        <div>
          <h3 class="text-xl font-bold text-gray-800 leading-snug">{{ viewItem.title }}</h3>
          <div class="flex flex-wrap items-center gap-x-2 gap-y-1 mt-2 text-xs text-gray-400">
            <span class="inline-flex items-center gap-1">
              <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              {{ viewItem.userName }}
            </span>
            <span class="text-gray-300">·</span>
            <span>发布于 {{ viewItem.createTime }}</span>
            <span class="text-gray-300">·</span>
            <span>浏览 {{ viewItem.views }} 次</span>
          </div>
        </div>

        <!-- 基础信息卡片: 2x2 网格, 每格一个小图标 + 标签 + 内容 -->
        <div class="grid grid-cols-2 gap-3">
          <div class="flex items-center gap-3 p-3.5 rounded-xl bg-gray-50">
            <div class="w-9 h-9 flex-shrink-0 flex items-center justify-center rounded-lg bg-white text-[var(--color-primary)] shadow-sm">
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
            </div>
            <div class="min-w-0">
              <p class="text-xs text-gray-400">物品分类</p>
              <p class="text-sm text-gray-700 font-medium truncate">{{ viewItem.categoryName }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3 p-3.5 rounded-xl bg-gray-50">
            <div class="w-9 h-9 flex-shrink-0 flex items-center justify-center rounded-lg bg-white text-[var(--color-primary)] shadow-sm">
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            </div>
            <div class="min-w-0">
              <p class="text-xs text-gray-400">发生日期</p>
              <p class="text-sm text-gray-700 font-medium truncate">{{ viewItem.happenTime || '-' }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3 p-3.5 rounded-xl bg-gray-50">
            <div class="w-9 h-9 flex-shrink-0 flex items-center justify-center rounded-lg bg-white text-[var(--color-primary)] shadow-sm">
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
            </div>
            <div class="min-w-0">
              <p class="text-xs text-gray-400">相关地点</p>
              <p class="text-sm text-gray-700 font-medium truncate" :title="viewItem.location">{{ viewItem.location }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3 p-3.5 rounded-xl bg-gray-50">
            <div class="w-9 h-9 flex-shrink-0 flex items-center justify-center rounded-lg bg-white text-[var(--color-primary)] shadow-sm">
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
            </div>
            <div class="min-w-0">
              <p class="text-xs text-gray-400">联系方式</p>
              <p class="text-sm text-gray-700 font-medium truncate" :title="viewItem.contact">{{ viewItem.contact }}</p>
            </div>
          </div>
        </div>

        <!-- 悬赏: 寻物启事填了悬赏才出现 -->
        <div v-if="viewItem.type === 'lost' && viewItem.reward" class="flex items-center gap-3 p-3.5 rounded-xl bg-amber-50 border border-amber-100">
          <div class="w-9 h-9 flex-shrink-0 flex items-center justify-center rounded-lg bg-white text-amber-500 shadow-sm">
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 15a7 7 0 1 0 0-14 7 7 0 0 0 0 14z"/><path d="M8.21 13.89L7 23l5-3 5 3-1.21-9.12"/></svg>
          </div>
          <div class="min-w-0">
            <p class="text-xs text-amber-500">悬赏答谢</p>
            <p class="text-sm text-amber-700 font-medium truncate">{{ viewItem.reward }}</p>
          </div>
        </div>

        <!-- 详细描述 -->
        <div>
          <p class="text-xs text-gray-400 mb-1.5">详细描述</p>
          <div class="p-4 rounded-xl bg-gray-50 text-sm text-gray-700 leading-7 whitespace-pre-wrap">{{ viewItem.description }}</div>
        </div>
      </div>
      <template #footer>
        <button @click="viewOpen = false" class="h-9 px-6 rounded-lg text-sm font-medium text-white bg-[var(--color-primary)] hover:bg-[var(--color-primary-dark)] transition-colors">关 闭</button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import {onMounted, ref, reactive, computed} from "vue";
import {deleteItemByItemIds, selectItemByItemId, selectItemList, updateItem} from "@/api/item/item.js";
import {selectCategoryAll} from "@/api/content/category.js";
import Pagination from "@/components/Pagination/index.vue";
import {ElMessage, ElMessageBox} from "@/utils/toast.js";

//修改表单
const open = ref(false)
const emptyForm = { itemId: null, type: 'lost', title: '', description: '', categoryId: null, images: '', location: '', happenTime: '', contact: '', urgent: 0, reward: '', status: 'open' }
const form = reactive({ ...emptyForm })

const handleUpdate = (row) => {
  Object.assign(form, emptyForm)
  const itemId = row ? row.itemId : ids.value[0]
  if (!itemId) return
  selectItemByItemId(itemId).then(res => {
    Object.assign(form, res.data)
    open.value = true
  })
}

//查看物品详情
const viewOpen = ref(false)
const viewItem = ref({})
//当前大图显示第几张
const mainIdx = ref(0)
//详情里的图片: 逗号分隔的字符串拆成数组, 一张一张展示
const viewImages = computed(() => splitImages(viewItem.value.images))
//把选中的图排到最前面传给ImagePreview, 这样大图显示它, 点开全屏预览时其他图也都在
const mainSrc = computed(() => {
  const list = viewImages.value
  if (list.length === 0) return ''
  return [list[mainIdx.value], ...list.filter((_, i) => i !== mainIdx.value)].join(',')
})

//图片字符串拆成数组(空值直接给空数组)
const splitImages = (s) => (s || '').split(',').filter(x => x)

//相对路径的图片补上接口前缀, http开头的直接用
const baseUrl = import.meta.env.VITE_APP_BASE_API
const toFullUrl = (url) => url.startsWith('http') ? url : baseUrl + url

const handleView = (row) => {
  //列表数据就是联表查出来的完整数据, 直接拿来展示
  viewItem.value = row
  //每次打开都从第一张图看起
  mainIdx.value = 0
  viewOpen.value = true
}

const handleDelete = (row) => {
  const itemIds = row ? [row.itemId] : ids.value
  if (!itemIds || itemIds.length === 0) return
  ElMessageBox.confirm('是否确认删除该物品信息?', '系统提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    deleteItemByItemIds(itemIds.join(',')).then(() => {
      ElMessage.success('删除成功')
      getList()
    })
  }).catch(() => {})
}

const submitForm = () => {
  if (!form.title || !form.title.trim()) {
    ElMessage.warning('请输入物品名称')
    return
  }
  if (!form.categoryId) {
    ElMessage.warning('请选择物品分类')
    return
  }
  if (!form.location || !form.location.trim()) {
    ElMessage.warning('请输入地点')
    return
  }
  if (!form.contact || !form.contact.trim()) {
    ElMessage.warning('请输入联系方式')
    return
  }
  if (!form.description || !form.description.trim()) {
    ElMessage.warning('请输入详细描述')
    return
  }
  //失物招领没有急寻和悬赏的说法, 提交前清掉
  if (form.type === 'found') {
    form.urgent = 0
    form.reward = ''
  }
  updateItem({...form}).then(() => {
    ElMessage.success('修改成功')
    open.value = false; getList()
  })
}

const query = ref({ pageNum: 1, pageSize: 10, title: '', type: '', categoryId: '', status: '' })
const itemList = ref([])
const total = ref(0)
const ids = ref([])
const selectedIds = reactive(new Set())
const loading = ref(false)

//分类下拉数据(查询区和修改表单共用)
const categoryList = ref([])

const single = computed(() => ids.value.length !== 1)
const multiple = computed(() => ids.value.length === 0)
const isAllSelected = computed(() => itemList.value.length > 0 && selectedIds.size === itemList.value.length)

const syncSelected = () => { ids.value = [...selectedIds] }
const toggleRow = (id) => {
  if (selectedIds.has(id)) selectedIds.delete(id)
  else selectedIds.add(id)
  syncSelected()
}
const toggleSelectAll = () => {
  if (isAllSelected.value) itemList.value.forEach(r => selectedIds.delete(r.itemId))
  else itemList.value.forEach(r => selectedIds.add(r.itemId))
  syncSelected()
}

const handleQuery = () => { query.value.pageNum = 1; getList() }
const resetQuery = () => {
  Object.assign(query.value, { title: '', type: '', categoryId: '', status: '' })
  handleQuery()
}

const getList = () => {
  loading.value = true
  selectItemList(query.value).then(res => {
    itemList.value = res.rows
    total.value = res.total
    //翻页后清掉勾选, 避免选中的行不在当前页造成误删
    selectedIds.clear()
    syncSelected()
  }).finally(() => { loading.value = false })
}

onMounted(() => {
  getList()
  selectCategoryAll().then(res => { categoryList.value = res.data || [] })
})
</script>
