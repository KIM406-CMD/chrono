import service from '@/api'

export interface UploadImageResponse {
  url: string
}

export const uploadImage = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return service.post<any, { code: number; message: string; data: UploadImageResponse; timestamp: number }>(
    '/upload/image',
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    },
  )
}