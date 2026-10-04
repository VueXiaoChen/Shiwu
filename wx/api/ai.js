/**
 * AI 找物助手 API — SSE 流式对话
 *
 * 后端 /ai/chat 返回 text/event-stream，这里没法走 utils/api.js 的
 * Promise 封装（拿不到分块数据），单独用 wx.request 的
 * enableChunked + onChunkReceived 边收边回调
 *
 * 事件协议（每帧 data:{json}）：
 *  - { type: 'delta', content } 回复文本增量
 *  - { type: 'done', items, action } 结束，带匹配物品卡片和引导发布按钮
 *  - { type: 'error', msg } 后端降级提示
 */
const { getToken } = require('../utils/auth')

/**
 * 把字节数组解码成 UTF-8 字符串，尾部不完整的多字节字符先扣下
 * （中文3字节，可能正好被 chunk 从中间切断，硬解会出乱码）
 * @param {Uint8Array} bytes - 待解码字节
 * @returns {{text: string, rest: Uint8Array}} 解出的文本和留到下次的尾部字节
 */
function decodeUtf8(bytes) {
  let end = bytes.length
  // 从末尾往前找多字节字符的起始字节，看这个字符的字节数够不够
  for (let i = bytes.length - 1; i >= 0 && i >= bytes.length - 3; i--) {
    const b = bytes[i]
    if (b >= 0xc0) {
      // 起始字节，算出该字符应占几个字节
      const need = b >= 0xf0 ? 4 : (b >= 0xe0 ? 3 : 2)
      if (i + need > bytes.length) end = i
      break
    }
    if (b < 0x80) break // 普通 ASCII，说明尾部是完整的
  }
  const complete = bytes.subarray(0, end)
  const rest = bytes.subarray(end)
  // 字节转 %XX 形式再用 decodeURIComponent 解码成 UTF-8 文本
  let percent = ''
  for (let i = 0; i < complete.length; i++) {
    percent += '%' + complete[i].toString(16).padStart(2, '0')
  }
  let text = ''
  try {
    text = decodeURIComponent(percent)
  } catch (e) {
    // 极端情况解不出来就放弃这段，别让页面崩了
  }
  return { text, rest }
}

/**
 * 发起流式对话
 * @param {object} options
 * @param {string} options.question - 用户本轮的问题
 * @param {Array}  options.history - 对话历史 [{ role: 'user'|'assistant', content }]
 * @param {Function} options.onDelta - 收到一段回复文本时回调(content)
 * @param {Function} options.onDone - 回复结束时回调({ items, action })
 * @param {Function} options.onError - 出错时回调(msg)
 * @returns {object} requestTask，页面卸载时可 abort
 */
function aiChatStream({ question, history, onDelta, onDone, onError }) {
  const app = getApp()
  const token = getToken()

  const header = { 'content-type': 'application/json' }
  if (token) {
    header['Authorization'] = 'Bearer ' + token
  }

  let byteRest = new Uint8Array(0) // 上次没解完的尾部字节
  let textBuffer = ''              // 还没凑成完整 SSE 帧的文本
  let finished = false             // done/error 是否已经回调过

  const requestTask = wx.request({
    url: app.globalData.baseUrl + '/ai/chat',
    method: 'POST',
    header,
    data: { question, history },
    enableChunked: true,
    timeout: 60000,
    success(res) {
      // 流走完了还没收到 done/error，说明后端没按 SSE 返回（如被网关拦了）
      if (!finished) {
        finished = true
        onError && onError('AI 服务暂时不可用，请稍后再试')
      }
    },
    fail() {
      if (!finished) {
        finished = true
        onError && onError('网络开小差了，请稍后再试')
      }
    }
  })

  requestTask.onChunkReceived((res) => {
    if (finished) return
    // 拼上上次剩的字节一起解码
    const chunk = new Uint8Array(res.data)
    const merged = new Uint8Array(byteRest.length + chunk.length)
    merged.set(byteRest)
    merged.set(chunk, byteRest.length)
    const { text, rest } = decodeUtf8(merged)
    byteRest = rest
    textBuffer += text

    // SSE 帧之间用空行分隔，最后一段可能是半帧，留在缓冲区
    const frames = textBuffer.split('\n\n')
    textBuffer = frames.pop()

    frames.forEach((frame) => {
      // 一帧里可能有多行，只认 data: 开头的
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
  })

  return requestTask
}

module.exports = {
  aiChatStream
}
