import { describe, expect, it } from 'vitest'
import {
  formatCardNo,
  formatExpiry,
  isValidCardNo,
  isValidCvv,
  isValidExpiry,
} from './luhn'

describe('luhn', () => {
  it('识别合法与非法卡号', () => {
    expect(isValidCardNo('4242 4242 4242 4242')).toBe(true)
    expect(isValidCardNo('1234 5678 9012 3456')).toBe(false)
    expect(isValidCardNo('424242424242')).toBe(false)
  })

  it('按 4 位分组并去除非数字字符', () => {
    expect(formatCardNo('4242-4242-4242-4242')).toBe('4242 4242 4242 4242')
  })

  it('格式化并校验有效期', () => {
    expect(formatExpiry('1230')).toBe('12/30')
    expect(isValidExpiry('12/30')).toBe(true)
    expect(isValidExpiry('13/30')).toBe(false)
    expect(isValidExpiry('01/20')).toBe(false)
  })

  it('校验 CVV 为 3-4 位数字', () => {
    expect(isValidCvv('123')).toBe(true)
    expect(isValidCvv('1234')).toBe(true)
    expect(isValidCvv('12')).toBe(false)
    expect(isValidCvv('12a')).toBe(false)
  })
})
