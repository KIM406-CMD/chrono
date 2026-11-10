import { MockMethod } from 'vite-plugin-mock'

const avatarSamples = [
  'https://api.dicebear.com/7.x/avataaars/svg?seed=chrono1',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=chrono2',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=chrono3',
  'https://api.dicebear.com/7.x/notionists/svg?seed=chrono4',
  'https://api.dicebear.com/7.x/thumbs/svg?seed=chrono5',
]

const mockMethods: MockMethod[] = [
  {
    url: '/api/upload/image',
    method: 'post',
    timeout: 400,
    response: () => {
      const url = avatarSamples[Math.floor(Math.random() * avatarSamples.length)]
      return {
        code: 200,
        message: '上传成功',
        data: { url },
        timestamp: Date.now(),
      }
    },
  },
]
export default mockMethods