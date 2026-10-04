<template>
<div class="dash-root relative min-h-[calc(100vh-112px)] rounded-xl overflow-hidden p-5 select-none"
     :class="{ 'dash-fullscreen': isFullscreen }">
  <!-- 顶部标题栏 -->
  <div class="dash-fade flex items-end justify-between mb-5" style="--d: 0ms">
    <div class="flex items-center gap-3">
      <!-- 装饰竖条 -->
      <div class="w-1.5 h-9 rounded-full bg-gradient-to-b from-cyan-400 to-blue-500 shadow-[0_0_12px_rgba(34,211,238,0.7)]"></div>
      <div>
        <h1 class="text-xl font-bold tracking-widest text-white">校园失物招领 · 数据中心</h1>
        <p class="text-[11px] text-cyan-300/60 tracking-[0.3em] mt-0.5">LOST &amp; FOUND DATA CENTER</p>
      </div>
    </div>

    <div class="flex items-center gap-5">
      <!-- 全屏切换 -->
      <button @click="toggleFullscreen" :title="isFullscreen ? '退出全屏' : '进入全屏'"
              class="flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg border border-cyan-400/25 bg-cyan-400/5 hover:bg-cyan-400/10 hover:border-cyan-400/40 transition-colors">
        <svg v-if="!isFullscreen" class="w-4 h-4 text-cyan-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M8 3H5a2 2 0 0 0-2 2v3"/><path d="M21 8V5a2 2 0 0 0-2-2h-3"/><path d="M3 16v3a2 2 0 0 0 2 2h3"/><path d="M16 21h3a2 2 0 0 0 2-2v-3"/>
        </svg>
        <svg v-else class="w-4 h-4 text-cyan-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M8 3v3a2 2 0 0 1-2 2H3"/><path d="M21 8h-3a2 2 0 0 1-2-2V3"/><path d="M3 16h3a2 2 0 0 1 2 2v3"/><path d="M16 21v-3a2 2 0 0 1 2-2h3"/>
        </svg>
        <span class="text-xs text-slate-300">{{ isFullscreen ? '退出全屏' : '全屏' }}</span>
      </button>

      <!-- 今日新增 -->
      <div class="flex items-center gap-2 px-3.5 py-1.5 rounded-lg border border-cyan-400/25 bg-cyan-400/5">
        <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
        <span class="text-xs text-slate-300">今日新增</span>
        <span class="font-mono text-lg font-bold text-emerald-300 num-glow-green">+{{ stats.todayNewCount }}</span>
      </div>
      <!-- 实时时钟 -->
      <div class="text-right">
        <div class="font-mono text-lg font-bold text-cyan-300 num-glow tracking-wider">{{ clock.time }}</div>
        <div class="text-[11px] text-slate-400 tracking-wider">{{ clock.date }}</div>
      </div>
    </div>
  </div>

  <!-- KPI 指标卡 -->
  <div class="grid grid-cols-3 xl:grid-cols-6 gap-4 mb-4">
    <div v-for="(kpi, i) in kpiList" :key="kpi.key"
         class="dash-fade panel px-4 py-3.5 flex items-center gap-3.5 group"
         :style="{ '--d': (80 + i * 70) + 'ms' }">
      <!-- 图标 -->
      <div class="w-11 h-11 rounded-lg flex items-center justify-center border transition-transform duration-200 group-hover:scale-110"
           :class="kpi.iconBox">
        <svg class="w-5.5 h-5.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
             stroke-linecap="round" stroke-linejoin="round" :class="kpi.iconColor" v-html="kpi.icon"></svg>
      </div>
      <div class="min-w-0">
        <div class="font-mono text-[22px] leading-7 font-bold text-white num-glow">{{ kpi.display }}</div>
        <div class="text-xs text-slate-400 truncate">{{ kpi.label }}</div>
      </div>
    </div>
  </div>

  <!-- 中部图表区: 趋势 + 分类分布 + 状态占比 -->
  <div class="grid grid-cols-12 gap-4 mb-4" :class="{ 'flex-1 min-h-0': isFullscreen }">
    <div class="dash-fade panel col-span-12 xl:col-span-6 p-4" style="--d: 520ms">
      <div class="panel-title">近7天发布趋势</div>
      <div ref="trendRef" class="h-[280px]" :class="{ 'dash-fill': isFullscreen }"></div>
    </div>
    <div class="dash-fade panel col-span-6 xl:col-span-3 p-4" style="--d: 590ms">
      <div class="panel-title">分类分布</div>
      <div ref="categoryRef" class="h-[280px]" :class="{ 'dash-fill': isFullscreen }"></div>
    </div>
    <div class="dash-fade panel col-span-6 xl:col-span-3 p-4" style="--d: 660ms">
      <div class="panel-title">类型与状态占比</div>
      <div ref="statusRef" class="h-[280px]" :class="{ 'dash-fill': isFullscreen }"></div>
    </div>
  </div>

  <!-- 底部区: 浏览TOP5 + 最新发布 -->
  <div class="grid grid-cols-12 gap-4">
    <div class="dash-fade panel col-span-12 xl:col-span-5 p-4" style="--d: 730ms">
      <div class="panel-title">浏览量 TOP5</div>
      <div ref="topRef" class="h-[250px]" :class="{ 'dash-fill': isFullscreen }"></div>
    </div>
    <div class="dash-fade panel col-span-12 xl:col-span-7 p-4" style="--d: 800ms">
      <div class="panel-title">最新发布动态</div>

      <!-- 表头 -->
      <div class="flex items-center px-3 py-2 text-[11px] text-cyan-300/50 tracking-wider border-b border-cyan-400/10">
        <span class="w-16">类型</span>
        <span class="flex-1">物品名称</span>
        <span class="w-40">地点</span>
        <span class="w-32 text-right">发布时间</span>
      </div>

      <!-- 滚动列表 -->
      <div class="h-[212px] overflow-hidden">
        <div class="scroll-list" :class="{ scrolling: latestItems.length > 5 }">
          <div v-for="(row, i) in scrollRows" :key="i"
               class="flex items-center px-3 h-[42px] text-[13px] border-b border-white/5 hover:bg-cyan-400/5 transition-colors">
            <span class="w-16">
              <span class="inline-block px-2 py-0.5 rounded text-[11px] border"
                    :class="row.type === 'lost'
                      ? 'text-amber-300 border-amber-400/40 bg-amber-400/10'
                      : 'text-emerald-300 border-emerald-400/40 bg-emerald-400/10'">
                {{ row.type === 'lost' ? '寻物' : '招领' }}
              </span>
            </span>
            <span class="flex-1 text-slate-200 truncate pr-3">{{ row.title }}</span>
            <span class="w-40 text-slate-400 truncate pr-3">{{ row.location }}</span>
            <span class="w-32 text-right font-mono text-cyan-300/80">{{ formatTime(row.createTime) }}</span>
          </div>
          <div v-if="!latestItems.length" class="flex items-center justify-center h-[100px] text-slate-500 text-sm">
            暂无发布数据
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStats } from '@/api/dashboard/dashboard.js'

/* ==================== 统计数据 ==================== */
const stats = reactive({
  itemTotal: 0, lostCount: 0, foundCount: 0, openCount: 0, doneCount: 0,
  userCount: 0, messageCount: 0, todayNewCount: 0
})
const latestItems = ref([])

// 滚动列表: 条数多的时候复制一份实现无缝滚动
const scrollRows = computed(() =>
  latestItems.value.length > 5 ? [...latestItems.value, ...latestItems.value] : latestItems.value
)

/* ==================== KPI 卡片 ==================== */
// 每张卡的数字滚动动画值放在 display 里
const kpiList = reactive([
  { key: 'itemTotal', label: '物品总数', display: 0,
    iconBox: 'border-cyan-400/30 bg-cyan-400/10', iconColor: 'text-cyan-300',
    icon: '<path d="M21 8l-9-5-9 5 9 5 9-5z"/><path d="M3 8v8l9 5 9-5V8"/><path d="M12 13v8"/>' },
  { key: 'lostCount', label: '寻物启事', display: 0,
    iconBox: 'border-amber-400/30 bg-amber-400/10', iconColor: 'text-amber-300',
    icon: '<circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/>' },
  { key: 'foundCount', label: '失物招领', display: 0,
    iconBox: 'border-emerald-400/30 bg-emerald-400/10', iconColor: 'text-emerald-300',
    icon: '<path d="M20 12v7a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1v-7"/><path d="M2 7h20v5H2z"/><path d="M12 22V7"/><path d="M12 7c1.5 0 3-1.5 3-3a2 2 0 0 0-3 3z"/><path d="M12 7c-1.5 0-3-1.5-3-3a2 2 0 0 1 3 3z"/>' },
  { key: 'doneCount', label: '已完成', display: 0,
    iconBox: 'border-sky-400/30 bg-sky-400/10', iconColor: 'text-sky-300',
    icon: '<path d="M22 11.1V12a10 10 0 1 1-5.9-9.1"/><path d="M22 4L12 14l-3-3"/>' },
  { key: 'userCount', label: '注册用户', display: 0,
    iconBox: 'border-blue-400/30 bg-blue-400/10', iconColor: 'text-blue-300',
    icon: '<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.9"/><path d="M16 3.1a4 4 0 0 1 0 7.8"/>' },
  { key: 'messageCount', label: '用户消息', display: 0,
    iconBox: 'border-teal-400/30 bg-teal-400/10', iconColor: 'text-teal-300',
    icon: '<path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>' }
])

// 数字滚动: 1秒内从0滚到目标值, ease-out越滚越慢
const rafIds = []
const countUp = (kpi, target) => {
  const duration = 1000
  const start = performance.now()
  const step = (now) => {
    const p = Math.min((now - start) / duration, 1)
    kpi.display = Math.round(target * (1 - Math.pow(1 - p, 3)))
    if (p < 1) rafIds.push(requestAnimationFrame(step))
  }
  rafIds.push(requestAnimationFrame(step))
}

/* ==================== 实时时钟 ==================== */
const clock = reactive({ time: '', date: '' })
let clockTimer = null
const tickClock = () => {
  const now = new Date()
  const pad = n => String(n).padStart(2, '0')
  clock.time = `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
  const weeks = ['日', '一', '二', '三', '四', '五', '六']
  clock.date = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} 星期${weeks[now.getDay()]}`
}

const formatTime = (t) => {
  if (!t) return ''
  // 后端返回 yyyy-MM-dd HH:mm:ss, 列表里只展示 MM-dd HH:mm
  return String(t).slice(5, 16)
}

/* ==================== ECharts 图表 ==================== */
const trendRef = ref(null)
const categoryRef = ref(null)
const statusRef = ref(null)
const topRef = ref(null)
const charts = []
let resizeObserver = null

// 深色系图表通用文字/线条颜色
const AXIS_TEXT = 'rgba(148,163,184,0.9)'
const SPLIT_LINE = 'rgba(148,163,184,0.12)'
const TOOLTIP = {
  backgroundColor: 'rgba(10,25,47,0.92)',
  borderColor: 'rgba(34,211,238,0.35)',
  textStyle: { color: '#e2e8f0', fontSize: 12 }
}

const initChart = (el, option) => {
  if (!el) return null
  const chart = echarts.init(el)
  chart.setOption(option)
  charts.push(chart)
  return chart
}

/* ==================== 常驻循环动画 ==================== */
// 所有循环动画的定时器统一收集, 卸载时一起清理
const loopTimers = []

// 轮播高亮: 每隔一段时间自动高亮下一个数据项并弹出提示框, 鼠标放上去时暂停
const startCarousel = (chart, dataLen, interval = 2400) => {
  if (!chart || !dataLen) return
  let idx = -1
  let paused = false
  chart.on('mouseover', () => {
    paused = true
    chart.dispatchAction({ type: 'downplay', seriesIndex: 0 })
  })
  chart.on('globalout', () => { paused = false })
  loopTimers.push(setInterval(() => {
    if (paused) return
    chart.dispatchAction({ type: 'downplay', seriesIndex: 0, dataIndex: idx })
    idx = (idx + 1) % dataLen
    chart.dispatchAction({ type: 'highlight', seriesIndex: 0, dataIndex: idx })
    chart.dispatchAction({ type: 'showTip', seriesIndex: 0, dataIndex: idx })
  }, interval))
}

// 环形缓慢旋转: 不断改双环的起始角度, 鼠标放上去时暂停
const startRotate = (chart) => {
  if (!chart) return
  let angle = 90
  let paused = false
  chart.on('mouseover', () => { paused = true })
  chart.on('globalout', () => { paused = false })
  loopTimers.push(setInterval(() => {
    if (paused) return
    angle = (angle - 0.6 + 360) % 360
    chart.setOption({ series: [{ startAngle: angle }, { startAngle: angle }] })
  }, 60))
}

// 近7天发布趋势: 双系列渐变面积折线
const renderTrend = (trend7d) => {
  return initChart(trendRef.value, {
    animationDuration: 1600,
    animationEasing: 'cubicOut',
    tooltip: { trigger: 'axis', ...TOOLTIP },
    legend: {
      data: ['寻物启事', '失物招领'], top: 0, right: 0,
      textStyle: { color: AXIS_TEXT }, itemWidth: 14, itemHeight: 8
    },
    grid: { left: 8, right: 16, top: 36, bottom: 4, containLabel: true },
    xAxis: {
      type: 'category', boundaryGap: false,
      data: trend7d.map(d => d.day.slice(5)),
      axisLine: { lineStyle: { color: SPLIT_LINE } },
      axisLabel: { color: AXIS_TEXT }
    },
    yAxis: {
      type: 'value', minInterval: 1,
      splitLine: { lineStyle: { color: SPLIT_LINE } },
      axisLabel: { color: AXIS_TEXT }
    },
    series: [
      {
        name: '寻物启事', type: 'line', smooth: true, symbolSize: 6,
        data: trend7d.map(d => d.lost),
        lineStyle: { width: 2.5, color: '#fbbf24' },
        itemStyle: { color: '#fbbf24', borderColor: '#0a1628', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(251,191,36,0.35)' },
            { offset: 1, color: 'rgba(251,191,36,0.02)' }
          ])
        }
      },
      {
        name: '失物招领', type: 'line', smooth: true, symbolSize: 6,
        data: trend7d.map(d => d.found),
        lineStyle: { width: 2.5, color: '#34d399' },
        itemStyle: { color: '#34d399', borderColor: '#0a1628', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(52,211,153,0.35)' },
            { offset: 1, color: 'rgba(52,211,153,0.02)' }
          ])
        }
      }
    ]
  })
}

// 分类分布: 南丁格尔玫瑰图(只取有物品的分类, 最多8个)
const renderCategory = (categoryDist) => {
  const data = categoryDist.filter(d => Number(d.value) > 0).slice(0, 8)
  const chart = initChart(categoryRef.value, {
    tooltip: { trigger: 'item', ...TOOLTIP, formatter: '{b}: {c} 件 ({d}%)' },
    color: ['#22d3ee', '#3b82f6', '#34d399', '#fbbf24', '#38bdf8', '#2dd4bf', '#60a5fa', '#a3e635'],
    series: [{
      type: 'pie', roseType: 'radius',
      radius: ['18%', '72%'], center: ['50%', '52%'],
      // 展开式入场 + 每个扇区错开出现
      animationType: 'expansion',
      animationDuration: 1200,
      animationDelay: idx => idx * 90,
      // 高亮时扇区放大一圈, 配合轮播更有动感
      emphasis: {
        scale: true, scaleSize: 8,
        itemStyle: { shadowBlur: 16, shadowColor: 'rgba(34,211,238,0.5)' }
      },
      itemStyle: { borderRadius: 5, borderColor: 'rgba(10,22,40,0.9)', borderWidth: 2 },
      label: { color: AXIS_TEXT, fontSize: 11, formatter: '{b}\n{c}件' },
      labelLine: { lineStyle: { color: SPLIT_LINE } },
      data
    }]
  })
  return { chart, dataLen: data.length }
}

// 类型与状态占比: 内环类型(寻物/招领) + 外环状态(进行中/已完成), 中间放总数
const renderStatus = () => {
  return initChart(statusRef.value, {
    // 入场展开动画, 之后由定时器驱动双环缓慢旋转
    animationType: 'expansion',
    animationDuration: 1400,
    tooltip: { trigger: 'item', ...TOOLTIP, formatter: '{a}<br/>{b}: {c} 件 ({d}%)' },
    legend: { bottom: 0, textStyle: { color: AXIS_TEXT }, itemWidth: 14, itemHeight: 8 },
    title: {
      text: String(stats.itemTotal), subtext: '物品总数',
      left: 'center', top: '38%',
      textStyle: { color: '#22d3ee', fontSize: 26, fontWeight: 'bold', fontFamily: 'monospace' },
      subtextStyle: { color: AXIS_TEXT, fontSize: 11 }
    },
    series: [
      {
        name: '信息类型', type: 'pie', radius: ['30%', '45%'], center: ['50%', '46%'],
        label: { show: false },
        itemStyle: { borderColor: 'rgba(10,22,40,0.9)', borderWidth: 2 },
        data: [
          { name: '寻物启事', value: stats.lostCount, itemStyle: { color: '#fbbf24' } },
          { name: '失物招领', value: stats.foundCount, itemStyle: { color: '#34d399' } }
        ]
      },
      {
        name: '处理状态', type: 'pie', radius: ['54%', '70%'], center: ['50%', '46%'],
        label: { show: false },
        itemStyle: { borderRadius: 4, borderColor: 'rgba(10,22,40,0.9)', borderWidth: 2 },
        data: [
          { name: '进行中', value: stats.openCount, itemStyle: { color: '#22d3ee' } },
          { name: '已完成', value: stats.doneCount, itemStyle: { color: '#3b82f6' } }
        ]
      }
    ]
  })
}

// 浏览量TOP5: 横向渐变条形图(第一名在最上面)
const renderTop = (viewsTop5) => {
  const rows = [...viewsTop5].reverse()
  const chart = initChart(topRef.value, {
    // 弹性生长入场, 每根条子错开
    animationDuration: 1300,
    animationEasing: 'elasticOut',
    animationDelay: idx => idx * 150,
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, ...TOOLTIP, formatter: '{b}: {c} 次浏览' },
    grid: { left: 8, right: 40, top: 8, bottom: 4, containLabel: true },
    xAxis: {
      type: 'value', minInterval: 1,
      splitLine: { lineStyle: { color: SPLIT_LINE } },
      axisLabel: { color: AXIS_TEXT }
    },
    yAxis: {
      type: 'category',
      data: rows.map(d => d.title),
      axisLine: { lineStyle: { color: SPLIT_LINE } },
      axisLabel: { color: AXIS_TEXT, width: 110, overflow: 'truncate' }
    },
    series: [{
      type: 'bar', barWidth: 14,
      data: rows.map(d => d.views),
      // 高亮时条子发光, 配合轮播扫过的效果
      emphasis: {
        itemStyle: { shadowBlur: 14, shadowColor: 'rgba(34,211,238,0.6)' }
      },
      itemStyle: {
        borderRadius: [0, 7, 7, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: 'rgba(59,130,246,0.9)' },
          { offset: 1, color: 'rgba(34,211,238,0.95)' }
        ])
      },
      label: { show: true, position: 'right', color: '#22d3ee', fontFamily: 'monospace' }
    }]
  })
  return { chart, dataLen: rows.length }
}

/* ==================== 全屏模式 ==================== */
const isFullscreen = ref(false)

// 进入/退出全屏: 大屏覆盖整个视口隐藏后台布局, 同时调用浏览器原生全屏
const toggleFullscreen = async () => {
  if (isFullscreen.value) {
    if (document.fullscreenElement) {
      await document.exitFullscreen().catch(() => {})
    }
    exitFullscreenUI()
  } else {
    enterFullscreenUI()
    try {
      await document.documentElement.requestFullscreen()
    } catch (e) {
      // 浏览器拒绝/不支持原生全屏时, 页面级全屏依然生效
    }
  }
}

const enterFullscreenUI = () => {
  isFullscreen.value = true
  // 锁定背景滚动, 避免全屏下滚动穿透
  document.body.style.overflow = 'hidden'
  // 容器尺寸变化后让所有图表重新适配
  nextTick(() => charts.forEach(c => c.resize()))
}

const exitFullscreenUI = () => {
  isFullscreen.value = false
  document.body.style.overflow = ''
  nextTick(() => charts.forEach(c => c.resize()))
}

// 用户按 Esc 退出浏览器全屏时, 同步恢复页面布局
const onFullscreenChange = () => {
  if (!document.fullscreenElement && isFullscreen.value) {
    exitFullscreenUI()
  }
}

/* ==================== 数据加载 ==================== */
const loadData = async () => {
  const res = await getDashboardStats()

  Object.keys(stats).forEach(key => { stats[key] = res[key] ?? 0 })
  latestItems.value = res.latestItems || []

  // KPI数字滚动
  kpiList.forEach(kpi => countUp(kpi, stats[kpi.key]))

  // 等DOM渲染完再初始化图表
  await nextTick()
  const trendChart = renderTrend(res.trend7d || [])
  const category = renderCategory(res.categoryDist || [])
  const statusChart = renderStatus()
  const top = renderTop(res.viewsTop5 || [])

  // 常驻动画: 趋势图沿7天轮播提示框, 玫瑰图/TOP5轮播高亮, 双环缓慢旋转
  startCarousel(trendChart, (res.trend7d || []).length, 3000)
  startCarousel(category.chart, category.dataLen, 2400)
  startCarousel(top.chart, top.dataLen, 2000)
  startRotate(statusChart)

  // 容器尺寸变化时(侧边栏收起/窗口拉伸)所有图表自适应
  resizeObserver = new ResizeObserver(() => charts.forEach(c => c.resize()))
  resizeObserver.observe(trendRef.value.closest('.dash-root'))
}

onMounted(() => {
  tickClock()
  clockTimer = setInterval(tickClock, 1000)
  document.addEventListener('fullscreenchange', onFullscreenChange)
  loadData()
})

onUnmounted(() => {
  document.removeEventListener('fullscreenchange', onFullscreenChange)
  document.body.style.overflow = ''
  if (document.fullscreenElement) document.exitFullscreen().catch(() => {})
  clearInterval(clockTimer)
  loopTimers.forEach(id => clearInterval(id))
  rafIds.forEach(id => cancelAnimationFrame(id))
  resizeObserver?.disconnect()
  charts.forEach(c => c.dispose())
})
</script>

<style scoped>
/* 大屏底色: 深海军蓝渐变 + 网格线纹理 */
.dash-root {
  background:
    linear-gradient(rgba(34, 211, 238, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(34, 211, 238, 0.035) 1px, transparent 1px),
    radial-gradient(ellipse at 20% -10%, #12325c 0%, transparent 55%),
    radial-gradient(ellipse at 90% 110%, #0d2a4a 0%, transparent 55%),
    linear-gradient(160deg, #0a1628 0%, #0d1f35 100%);
  background-size: 32px 32px, 32px 32px, auto, auto, auto;
}

/* 玻璃拟态面板 + 左上/右下装饰角标线 */
.panel {
  position: relative;
  background: rgba(15, 34, 58, 0.55);
  backdrop-filter: blur(6px);
  border: 1px solid rgba(34, 211, 238, 0.16);
  border-radius: 10px;
  transition: border-color 0.25s ease, box-shadow 0.25s ease;
}
.panel:hover {
  border-color: rgba(34, 211, 238, 0.4);
  box-shadow: 0 0 18px rgba(34, 211, 238, 0.08);
}
.panel::before,
.panel::after {
  content: '';
  position: absolute;
  width: 14px;
  height: 14px;
  pointer-events: none;
}
.panel::before {
  top: -1px;
  left: -1px;
  border-top: 2px solid rgba(34, 211, 238, 0.7);
  border-left: 2px solid rgba(34, 211, 238, 0.7);
  border-top-left-radius: 10px;
}
.panel::after {
  bottom: -1px;
  right: -1px;
  border-bottom: 2px solid rgba(34, 211, 238, 0.7);
  border-right: 2px solid rgba(34, 211, 238, 0.7);
  border-bottom-right-radius: 10px;
}

/* 面板标题: 左侧发光小竖条 */
.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.1em;
  color: rgba(226, 232, 240, 0.92);
  margin-bottom: 6px;
}
.panel-title::before {
  content: '';
  width: 3px;
  height: 13px;
  border-radius: 2px;
  background: linear-gradient(to bottom, #22d3ee, #3b82f6);
  box-shadow: 0 0 8px rgba(34, 211, 238, 0.8);
}

/* 数字发光 */
.num-glow {
  text-shadow: 0 0 12px rgba(34, 211, 238, 0.45);
}
.num-glow-green {
  text-shadow: 0 0 12px rgba(52, 211, 153, 0.5);
}

/* 全屏模式: 大屏覆盖整个视口, 隐藏后台布局只显示科技大屏 */
.dash-fullscreen {
  position: fixed;
  inset: 0;
  z-index: 9999;
  min-height: 100vh;
  border-radius: 0;
  display: flex;
  flex-direction: column;
}

/* 全屏时图表占满所在面板剩余高度 */
.dash-fullscreen .dash-fill {
  height: 100%;
}

/* 全屏时标题与 KPI 数字放大, 大屏感更强 */
.dash-fullscreen h1 {
  font-size: 28px;
}
.dash-fullscreen .num-glow {
  font-size: 30px;
  line-height: 2.1rem;
}

/* 入场动画: 各模块按 --d 延迟依次淡入上移 */
.dash-fade {
  opacity: 0;
  animation: dashFadeUp 0.4s ease-out forwards;
  animation-delay: var(--d, 0ms);
}
@keyframes dashFadeUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 最新发布无缝向上滚动(条数超过一屏才滚), hover暂停 */
.scroll-list.scrolling {
  animation: dashScrollUp 16s linear infinite;
}
.scroll-list.scrolling:hover {
  animation-play-state: paused;
}
@keyframes dashScrollUp {
  from {
    transform: translateY(0);
  }
  to {
    transform: translateY(-50%);
  }
}
</style>
