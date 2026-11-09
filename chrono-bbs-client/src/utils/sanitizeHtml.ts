/**
 * 轻量富文本清洗：仅保留商品详情常用的安全标签，
 * 移除脚本、事件属性与危险的 javascript: 链接，避免 v-html 引入 XSS。
 */

const ALLOWED_TAGS = new Set([
  'p', 'br', 'div', 'span', 'strong', 'b', 'em', 'i', 'u', 's',
  'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
  'ul', 'ol', 'li', 'blockquote', 'pre', 'code',
  'img', 'a', 'table', 'thead', 'tbody', 'tr', 'th', 'td',
])

const ALLOWED_ATTRS = new Set(['href', 'src', 'alt', 'title', 'width', 'height'])

const isSafeUrl = (value: string) => !/^\s*javascript:/i.test(value)

export const sanitizeHtml = (html: string): string => {
  if (!html) return ''
  if (typeof DOMParser === 'undefined') return ''

  const doc = new DOMParser().parseFromString(html, 'text/html')

  const walk = (parent: Element) => {
    for (const child of Array.from(parent.children)) {
      const tag = child.tagName.toLowerCase()
      if (!ALLOWED_TAGS.has(tag)) {
        child.replaceWith(...Array.from(child.childNodes))
        continue
      }

      for (const attr of Array.from(child.attributes)) {
        const name = attr.name.toLowerCase()
        if (!ALLOWED_ATTRS.has(name) || ((name === 'href' || name === 'src') && !isSafeUrl(attr.value))) {
          child.removeAttribute(attr.name)
        }
      }

      if (tag === 'a') {
        child.setAttribute('rel', 'noopener noreferrer')
        child.setAttribute('target', '_blank')
      }

      walk(child)
    }
  }

  walk(doc.body)
  return doc.body.innerHTML
}
