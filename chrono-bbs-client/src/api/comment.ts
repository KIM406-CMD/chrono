import service from '@/api'
import type { PageResult } from '@/types/common'
import type { CommentItem, AddCommentRequest, AppendCommentRequest, ReplyCommentRequest } from '@/types/comment'

// 9.1 商品评论列表（公开）
export const getComments = (productId: number, params?: { page?: number; size?: number; star?: number }) => {
  return service.get<any, { code: number; message: string; data: PageResult<CommentItem>; timestamp: number }>(`/comment/list/${productId}`, { params })
}

// 9.2 发表评论
export const addComment = (data: AddCommentRequest) => {
  return service.post('/comment', data)
}

// 9.3 追加评论
export const appendComment = (data: AppendCommentRequest) => {
  return service.post('/comment/append', data)
}

// 9.4 商家回复
export const replyComment = (data: ReplyCommentRequest) => {
  return service.post('/comment/reply', data)
}