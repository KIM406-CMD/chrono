import { computed, reactive } from 'vue'

export interface GeneralSettings {
  orderNotice: boolean
  promotionNotice: boolean
  chatNotice: boolean
  compactList: boolean
  showPriceDecimal: boolean
}

export const GENERAL_SETTINGS_KEY = 'general_settings'

export const defaultGeneralSettings: GeneralSettings = {
  orderNotice: true,
  promotionNotice: true,
  chatNotice: true,
  compactList: false,
  showPriceDecimal: true,
}

const loadGeneralSettings = (): GeneralSettings => {
  try {
    const raw = localStorage.getItem(GENERAL_SETTINGS_KEY)
    return raw
      ? { ...defaultGeneralSettings, ...(JSON.parse(raw) as Partial<GeneralSettings>) }
      : { ...defaultGeneralSettings }
  } catch {
    return { ...defaultGeneralSettings }
  }
}

export const generalSettings = reactive<GeneralSettings>(loadGeneralSettings())

export const saveGeneralSettings = () => {
  localStorage.setItem(GENERAL_SETTINGS_KEY, JSON.stringify(generalSettings))
}

export const resetGeneralSettings = () => {
  Object.assign(generalSettings, defaultGeneralSettings)
  saveGeneralSettings()
}

export const reloadGeneralSettings = () => {
  Object.assign(generalSettings, loadGeneralSettings())
}

export const formatPriceByPreference = (fen: number) => {
  const yuan = fen / 100
  return generalSettings.showPriceDecimal ? yuan.toFixed(2) : String(Math.floor(yuan))
}

export const productGridColumns = computed(() =>
  generalSettings.compactList
    ? 'repeat(6, 1fr)'
    : 'repeat(5, 1fr)',
)