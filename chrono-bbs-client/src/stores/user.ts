import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { UserInfo } from '@/types/user'
import { Role } from '@/types/common'
import { getToken, removeToken, setToken as persistToken } from '@/utils/token'

const USER_INFO_KEY = 'user_info'

const loadUserInfo = (): UserInfo | null => {
  try {
    const raw = localStorage.getItem(USER_INFO_KEY)
    return raw ? (JSON.parse(raw) as UserInfo) : null
  } catch {
    return null
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const userInfo = ref<UserInfo | null>(loadUserInfo())

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => userInfo.value?.role)
  const roleName = computed(() => {
    switch (role.value) {
      case Role.USER:
        return 'USER'
      case Role.MERCHANT:
        return 'MERCHANT'
      case Role.ADMIN:
        return 'ADMIN'
      default:
        return ''
    }
  })

  function setToken(t: string) {
    token.value = t
    persistToken(t)
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    removeToken()
    localStorage.removeItem(USER_INFO_KEY)
  }

  return { token, userInfo, isLoggedIn, role, roleName, setToken, setUserInfo, logout }
})