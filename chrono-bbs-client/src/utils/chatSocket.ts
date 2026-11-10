/**
 * 聊天 WebSocket 客户端。
 *
 * <p>连接后端原生 WebSocket 端点 {@code /chat/{userId}?token=<jwt>}，
 * 负责发送文本消息、接收实时消息与离线补推消息，并在连接意外断开时自动重连。
 * 聊天页与顶部导航共享同一连接，避免重复建立会话。</p>
 */

/** 后端 WebSocket 下行消息帧。 */
export interface ChatFrame {
  /** 帧类型：TEXT / IMAGE，错误帧为 ERROR。 */
  type: string
  /** 会话 ID。 */
  conversationId?: number
  /** 消息 ID。 */
  messageId?: number
  /** 发送方用户 ID。 */
  senderId?: number
  /** 发送方角色：0-用户，1-商家。 */
  senderRole?: number
  /** 消息内容。 */
  content?: string
  /** 关联商品 ID。 */
  productId?: number | null
  /** 是否已读：0-未读，1-已读。 */
  isRead?: number
  /** 服务端下发时间戳（毫秒）。 */
  timestamp?: number
  /** 错误帧中的提示信息。 */
  message?: string
}

/** 需要客户端回填的时间戳/消息 ID。 */
export interface ChatOutgoingMessage {
  conversationId: number
  content: string
  type?: 'TEXT' | 'IMAGE'
  productId?: number
}

export type ChatConnectionStatus = 'idle' | 'connecting' | 'open' | 'closed'

type FrameListener = (frame: ChatFrame) => void
type StatusListener = (status: ChatConnectionStatus) => void

const MAX_RECONNECT_DELAY = 30_000
const POLICY_VIOLATION = 1008

class ChatSocketClient {
  private socket: WebSocket | null = null
  private userId = 0
  private token = ''
  private manualClose = true
  private reconnectAttempts = 0
  private reconnectTimer: number | null = null
  private status: ChatConnectionStatus = 'idle'
  private frameListeners = new Set<FrameListener>()
  private statusListeners = new Set<StatusListener>()

  /** 当前连接状态。 */
  get currentStatus(): ChatConnectionStatus {
    return this.status
  }

  /**
   * 建立或复用连接。
   *
   * @param userId 当前登录用户 ID
   * @param token  JWT，后端同时校验路径用户与 Redis 中的登录态
   */
  connect(userId: number, token: string): void {
    if (!userId || !token) return
    const reusable = this.socket
      && (this.socket.readyState === WebSocket.OPEN || this.socket.readyState === WebSocket.CONNECTING)
      && this.userId === userId
      && this.token === token
    if (reusable) return

    this.disconnect()
    this.userId = userId
    this.token = token
    this.manualClose = false
    this.reconnectAttempts = 0
    this.open()
  }

  /** 主动关闭连接，停止自动重连（登出或离开商城布局时调用）。 */
  disconnect(): void {
    this.manualClose = true
    if (this.reconnectTimer !== null) {
      window.clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    const socket = this.socket
    this.socket = null
    if (socket && socket.readyState <= WebSocket.OPEN) {
      socket.close()
    }
    this.userId = 0
    this.token = ''
    this.setStatus('idle')
  }

  /**
   * 发送一条聊天消息。
   *
   * @param message 消息内容与会话 ID
   * @return {@code true} 表示已写入连接；连接未就绪时返回 {@code false}
   */
  send(message: ChatOutgoingMessage): boolean {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) return false
    this.socket.send(JSON.stringify({
      type: message.type ?? 'TEXT',
      conversationId: message.conversationId,
      content: message.content,
      productId: message.productId,
      timestamp: Date.now(),
    }))
    return true
  }

  /**
   * 订阅实时消息帧。
   *
   * @param listener 帧监听器
   * @return 取消订阅函数
   */
  onFrame(listener: FrameListener): () => void {
    this.frameListeners.add(listener)
    return () => this.frameListeners.delete(listener)
  }

  /**
   * 订阅连接状态变化，订阅时立即回调当前状态。
   *
   * @param listener 状态监听器
   * @return 取消订阅函数
   */
  onStatus(listener: StatusListener): () => void {
    this.statusListeners.add(listener)
    listener(this.status)
    return () => this.statusListeners.delete(listener)
  }

  private open(): void {
    if (this.manualClose) return
    this.setStatus('connecting')

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const url = `${protocol}//${window.location.host}/chat/${this.userId}?token=${encodeURIComponent(this.token)}`
    const socket = new WebSocket(url)
    this.socket = socket

    socket.onopen = () => {
      if (this.socket !== socket) return
      this.reconnectAttempts = 0
      this.setStatus('open')
    }

    socket.onmessage = (event) => {
      let frame: ChatFrame
      try {
        frame = JSON.parse(event.data) as ChatFrame
      } catch {
        return
      }
      this.frameListeners.forEach((listener) => listener(frame))
    }

    socket.onclose = (event) => {
      if (this.socket !== socket) return
      this.socket = null
      this.setStatus('closed')
      // 鉴权失败（1008）时重连也无法恢复，等待用户重新登录
      if (event.code === POLICY_VIOLATION) {
        this.manualClose = true
        return
      }
      this.scheduleReconnect()
    }

    socket.onerror = () => {
      // 浏览器随后会触发 onclose，由 onclose 统一处理重连
    }
  }

  private scheduleReconnect(): void {
    if (this.manualClose || this.reconnectTimer !== null) return
    const delay = Math.min(MAX_RECONNECT_DELAY, 1_000 * 2 ** this.reconnectAttempts)
    this.reconnectAttempts += 1
    this.reconnectTimer = window.setTimeout(() => {
      this.reconnectTimer = null
      this.open()
    }, delay)
  }

  private setStatus(status: ChatConnectionStatus): void {
    if (this.status === status) return
    this.status = status
    this.statusListeners.forEach((listener) => listener(status))
  }
}

/** 全局聊天连接单例。 */
export const chatSocket = new ChatSocketClient()
