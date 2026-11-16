/**
 * 将富文本描述转换为纯文本，用于编辑表单回填。
 *
 * 保存到后端的商品描述可能来自演示数据或富文本编辑器（含 &lt;p&gt; 等标签），
 * 在 textarea 中直接展示会看到 HTML 源码。这里保留换行并去掉标签，
 * 避免卖家误改标签，同时让详情页按纯文本正常展示。
 */
const BLOCK_TAGS = new Set([
  'p', 'div', 'br', 'li', 'tr', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'blockquote', 'pre',
])

export const stripHtml = (html?: string | null): string => {
  if (!html) return ''
  if (typeof DOMParser === 'undefined') {
    return html.replace(/<[^>]*>/g, '').trim()
  }
  const doc = new DOMParser().parseFromString(html, 'text/html')
  for (const el of Array.from(doc.body.querySelectorAll('*'))) {
    if (BLOCK_TAGS.has(el.tagName.toLowerCase())) {
      el.append('\n')
    }
  }
  return (doc.body.textContent || '')
    .replace(/\r\n/g, '\n')
    .replace(/[ \t]+\n/g, '\n')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}
