export interface CommentItem {
  id: number
  productId: number
  userId: number
  username: string
  userAvatar: string
  orderItemId: number
  content: string
  star: number
  appendContent?: string
  appendAt?: string
  replyContent?: string
  replyAt?: string
  merchantId?: number
  createdAt: string
}

export interface AddCommentRequest {
  productId: number
  orderItemId: number
  content: string
  star: number
}

export interface AppendCommentRequest {
  commentId: number
  content: string
}

export interface ReplyCommentRequest {
  parentId: number
  content: string
}