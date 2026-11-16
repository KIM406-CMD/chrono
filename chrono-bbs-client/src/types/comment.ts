export interface CommentItem {
  id: number
  productId: number
  /** 商品名称，商家评论管理列表展示用 */
  productName?: string
  userId: number
  username: string
  nickname?: string
  avatar: string
  orderItemId: number
  content: string
  images?: string[]
  star: number
  createdAt: string
  // 追评（仅一级评论可能有）
  append?: CommentItem | null
  // 商家回复列表
  replies?: CommentItem[]
}

export interface AddCommentRequest {
  productId: number
  orderItemId: number
  content: string
  star: number
  images?: string[]
}

export interface AppendCommentRequest {
  commentId: number
  content: string
}

export interface ReplyCommentRequest {
  parentId: number
  content: string
}