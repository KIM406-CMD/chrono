import { describe, expect, it } from 'vitest'
import { sanitizeHtml } from './sanitizeHtml'

describe('sanitizeHtml', () => {
  it('保留常用富文本标签', () => {
    const html = '<p>商品<strong>详情</strong></p><ul><li>规格</li></ul>'
    expect(sanitizeHtml(html)).toContain('<p>')
    expect(sanitizeHtml(html)).toContain('<strong>详情</strong>')
    expect(sanitizeHtml(html)).toContain('<li>规格</li>')
  })

  it('移除 script 与事件属性', () => {
    const html = '<p onclick="alert(1)">内容</p><script>alert(1)</script>'
    const result = sanitizeHtml(html)
    expect(result).not.toContain('<script')
    expect(result).not.toContain('onclick')
    expect(result).toContain('内容')
  })

  it('移除 javascript 链接', () => {
    const result = sanitizeHtml('<a href="javascript:alert(1)">链接</a>')
    expect(result).not.toContain('javascript:')
    expect(result).toContain('链接')
  })

  it('保留安全的图片地址', () => {
    const result = sanitizeHtml('<img src="https://example.com/a.jpg" alt="商品">')
    expect(result).toContain('src="https://example.com/a.jpg"')
    expect(result).toContain('alt="商品"')
  })
})
