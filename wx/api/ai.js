/**
 * AI 找物助手 API — SSE 流式对话（两步式图片上传）
 *
 * 图片上传 + 流式对话分两步:
 *  1. wx.uploadFile 上传图片到 /ai/upload, 拿回可访问的 url
 *  2. wx.request 走 /ai/chat, 把 url 放在 body 里, enableChunked 收 SSE
 *
 * 这样避开 wx.uploadFile 的 enableChunked 在不同基础库上不生效的坑
 *
 * 事件协议（每帧 data:{json}）：
 *  - { type: 'delta', content }
 *  - { type: 'done', items, action }
 *  - { type: 'error', msg }
 */
const { getToken } = require('../utils/auth')

function decodeUtf8(bytes) {
  let end = bytes.length
  for (let i = bytes.length - 1; i >= 0 && i >= bytes.length - 3; i--) {
    const b = bytes[i]
    if (b >= 0xc0) {
      const need = b >= 0xf0 ? 4 : (b >= 0xe0 ? 3 : 2)
      if (i + need > bytes.length) end = i
      break
    }
    if (b < 0x80) break
  }
  const complete = bytes.subarray(0, end)
  const rest = bytes.subarray(end)
  let percent = ''
  for (let i = 0; i < complete.length; i++) {
    percent += '%' + complete[i].toString(16).padStart(2, '0')
  }
  let text = ''
  try {
    text = decodeURIComponent(percent)
  } catch (e) {}
  return { text, rest }
}

function buildHeader(extra) {
  const token = getToken()
  const header = Object.assign({}, extra)
  if (token) header['Authorization'] = 'Bearer ' + token
  return header
}

function createSseHandler({ onDelta, onDone, onError }) {
  let byteRest = new Uint8Array(0)
  let textBuffer = ''
  let finished = false

  function handleChunk(data) {
    if (finished) return
    const chunk = new Uint8Array(data)
    const merged = new Uint8Array(byteRest.length + chunk.length)
    merged.set(byteRest)
    merged.set(chunk, byteRest.length)
    const { text, rest } = decodeUtf8(merged)
    byteRest = rest
    textBuffer += text

    const frames = textBuffer.split('\n\n')
    textBuffer = frames.pop()

    frames.forEach((frame) => {
      frame.split('\n').forEach((line) => {
        if (!line.startsWith('data:')) return
        let event = null
        try {
          event = JSON.parse(line.slice(5))
        } catch (e) {
          return
        }
        if (!event || finished) return
        if (event.type === 'delta') {
          onDelta && onDelta(event.content || '')
        } else if (event.type === 'done') {
          finished = true
          onDone && onDone({ items: event.items || [], action: event.action || null })
        } else if (event.type === 'error') {
          finished = true
          onError && onError(event.msg || 'AI 服务暂时不可用，请稍后再试')
        }
      })
    })
  }

  return {
    handleChunk,
    isFinished: () => finished,
    setFinished: (v) => { finished = v }
  }
}

/**
 * 纯文字流式对话
 */
function aiChatStream({ question, history, onDelta, onDone, onError }) {
  return requestChat({ question, history, imageUrl: '', onDelta, onDone, onError })
}

/**
 * 上传图片, 拿到可访问 URL
 */
function uploadImage(imagePath) {
  const app = getApp()
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: app.globalData.baseUrl + '/ai/upload',
      filePath: imagePath,
      name: 'image',
      header: buildHeader({}),
      timeout: 60000,
      success(res) {
        if (res.statusCode !== 200) {
          reject(new Error('图片上传失败: HTTP ' + res.statusCode))
          return
        }
        let url = ''
        try {
          url = JSON.parse(res.data).url || ''
        } catch (e) {}
        if (!url) {
          reject(new Error('图片上传失败: 返回数据异常'))
          return
        }
        resolve(url)
      },
      fail(err) {
        reject(new Error((err && err.errMsg) || '图片上传失败'))
      }
    })
  })
}

/**
 * 带图片的流式对话: 先上传图片, 再走 /ai/chat
 * @returns {object} 带 abort() 的对象
 */
function aiChatStreamWithImage({ question, imagePath, history, onDelta, onDone, onError }) {
  let aborted = false
  let chatTask = null

  uploadImage(imagePath)
    .then((imageUrl) => {
      if (aborted) return
      chatTask = requestChat({
        question, history, imageUrl, onDelta, onDone, onError
      })
    })
    .catch((err) => {
      if (aborted) return
      onError && onError(err.message || '图片上传失败，请重试')
    })

  return {
    abort() {
      aborted = true
      if (chatTask) chatTask.abort()
    }
  }
}

/**
 * 真正发起 SSE 对话(共用)
 */
function requestChat({ question, history, imageUrl, onDelta, onDone, onError }) {
  const app = getApp()
  const sse = createSseHandler({ onDelta, onDone, onError })

  const requestTask = wx.request({
    url: app.globalData.baseUrl + '/ai/chat',
    method: 'POST',
    header: buildHeader({ 'content-type': 'application/json' }),
    data: { question, history, imageUrl: imageUrl || '' },
    enableChunked: true,
    timeout: 60000,
    success() {
      if (!sse.isFinished()) {
        sse.setFinished(true)
        onError && onError('AI 服务暂时不可用，请稍后再试')
      }
    },
    fail() {
      if (!sse.isFinished()) {
        sse.setFinished(true)
        onError && onError('网络开小差了，请稍后再试')
      }
    }
  })

  requestTask.onChunkReceived((res) => {
    sse.handleChunk(res.data)
  })

  return requestTask
}

module.exports = {
  aiChatStream,
  aiChatStreamWithImage,
  uploadImage
}