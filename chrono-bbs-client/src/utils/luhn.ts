/**
 * 银行卡号工具：格式化、Luhn 校验与有效期/CVV 格式校验。
 * 前端只做预校验，最终仍以后端校验结果为准。
 */

/** 去掉非数字字符，并截断到 19 位（银行卡最长位数）。 */
export const normalizeCardNo = (value: string) => value.replace(/\D/g, '').slice(0, 19)

/** 每 4 位插入一个空格，便于阅读。 */
export const formatCardNo = (value: string) =>
  normalizeCardNo(value).replace(/(\d{4})(?=\d)/g, '$1 ')

/** Luhn 校验：卡号长度 16-19 位且通过模 10 校验。 */
export const isValidCardNo = (value: string) => {
  const digits = normalizeCardNo(value)
  if (digits.length < 16 || digits.length > 19) return false

  let sum = 0
  let double = false
  for (let i = digits.length - 1; i >= 0; i--) {
    let n = Number(digits[i])
    if (double) {
      n *= 2
      if (n > 9) n -= 9
    }
    sum += n
    double = !double
  }
  return sum % 10 === 0
}

/** 将输入格式化为 MM/YY。 */
export const formatExpiry = (value: string) => {
  const digits = value.replace(/\D/g, '').slice(0, 4)
  return digits.length > 2 ? `${digits.slice(0, 2)}/${digits.slice(2)}` : digits
}

/** 有效期格式 MM/YY，月份 01-12，且不早于当前月份。 */
export const isValidExpiry = (value: string) => {
  const match = /^(\d{2})\/(\d{2})$/.exec(value.trim())
  if (!match) return false
  const month = Number(match[1])
  const year = 2000 + Number(match[2])
  if (month < 1 || month > 12) return false

  const now = new Date()
  const currentYear = now.getFullYear()
  const currentMonth = now.getMonth() + 1
  return year > currentYear || (year === currentYear && month >= currentMonth)
}

/** CVV 为 3-4 位数字。 */
export const isValidCvv = (value: string) => /^\d{3,4}$/.test(value.trim())
