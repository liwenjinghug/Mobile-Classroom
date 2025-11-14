import axios from 'axios'
import {Loading, Message, MessageBox} from 'element-ui'
import { saveAs } from 'file-saver'
import { getToken } from '@/utils/auth'
import errorCode from '@/utils/errorCode'
import { blobValidate } from "@/utils/ruoyi"

const baseURL = process.env.VUE_APP_BASE_API
let downloadLoadingInstance
// toggle this to true during debugging to show full server response in an alert
export const DEBUG_SHOW_FULL_ERROR = false

async function extractErrorMessageFromAxiosError(err) {
  try {
    if (!err) return '未知错误'
    if (!err.response) {
      return err.message || '网络错误或未收到响应'
    }
    const status = err.response.status
    const data = err.response.data
    let text = ''
    if (data) {
      if (data instanceof Blob && data.text) {
        try { text = await data.text() } catch (e) { text = '' }
      } else if (typeof data === 'string') {
        text = data
      } else if (data instanceof ArrayBuffer) {
        try { text = new TextDecoder().decode(new Uint8Array(data)) } catch (e) { text = '' }
      } else if (data && typeof data === 'object' && data.toString) {
        try { text = JSON.stringify(data) } catch (e) { text = String(data) }
      }
    }
    if (text) {
      try {
        const obj = JSON.parse(text)
        const msg = obj.msg || obj.message || obj.error || obj.errMsg || obj.code && (errorCode[obj.code] || obj.msg) || null
        if (msg) return `(${status}) ${msg}`
      } catch (e) {
        return `(${status}) ${text}`
      }
    }
    return `HTTP ${status} ${err.response.statusText || ''}`
  } catch (e) {
    console.error('extractErrorMessageFromAxiosError failed', e)
    return err && err.message ? err.message : '下载发生未知错误'
  }
}

export default {
  name(name, isDelete = true) {
    const requestUrl = "/common/download?fileName=" + encodeURIComponent(name) + "&delete=" + isDelete
    axios({
      method: 'get',
      url: requestUrl,
      responseType: 'blob',
      headers: { 'Authorization': 'Bearer ' + getToken() }
    }).then((res) => {
      const data = res.data
      const isBlob = blobValidate(data)
      if (isBlob) {
        const blob = (data instanceof Blob) ? data : new Blob([data])
        let filename = this.parseFilenameFromHeaders(res.headers) || name
        try { filename = filename ? decodeURIComponent(filename) : filename } catch (e) { }
        this.saveAs(blob, filename)
      } else {
        this.printErrMsg(data)
      }
    }).catch(async (err) => {
      console.error('download.name error', err)
      const msg = await extractErrorMessageFromAxiosError(err)
      if (DEBUG_SHOW_FULL_ERROR) {
        // show full server response in a modal for debugging
        let full = ''
        try {
          if (err && err.response && err.response.data) {
            const d = err.response.data
            if (d instanceof Blob && d.text) full = await d.text()
            else if (typeof d === 'string') full = d
            else full = JSON.stringify(d)
          } else {
            full = JSON.stringify(err)
          }
        } catch (e) { full = String(err) }
        MessageBox.alert(`<pre style="white-space:pre-wrap">${full}</pre>`, '下载失败（详细信息）', { dangerouslyUseHTMLString: true })
      }
      Message.error(msg || '下载文件出现错误，请联系管理员！')
    })
  },
  resource(resource) {
    const requestUrl = "/common/download/resource?resource=" + encodeURIComponent(resource)
    axios({
      method: 'get',
      url: requestUrl,
      responseType: 'blob',
      headers: { 'Authorization': 'Bearer ' + getToken() }
    }).then((res) => {
      const data = res.data
      const isBlob = blobValidate(data)
      if (isBlob) {
        const blob = (data instanceof Blob) ? data : new Blob([data])
        let filename = this.parseFilenameFromHeaders(res.headers) || resource.split('/').pop() || 'resource'
        try { filename = filename ? decodeURIComponent(filename) : filename } catch (e) { }
        this.saveAs(blob, filename)
      } else {
        this.printErrMsg(data)
      }
    }).catch(async (err) => {
      console.error('download.resource error', err)
      const msg = await extractErrorMessageFromAxiosError(err)
      if (DEBUG_SHOW_FULL_ERROR) {
        let full = ''
        try {
          const d = err.response && err.response.data
          if (d instanceof Blob && d.text) full = await d.text()
          else if (typeof d === 'string') full = d
          else full = JSON.stringify(d)
        } catch (e) { full = String(err) }
        MessageBox.alert(`<pre style="white-space:pre-wrap">${full}</pre>`, '下载失败（详细信息）', { dangerouslyUseHTMLString: true })
      }
      Message.error(msg || '下载文件出现错误，请联系管理员！')
    })
  },
  zip(url, name) {
    const requestUrl = (url && url.startsWith('/')) ? url : ('/' + (url || ''))
    downloadLoadingInstance = Loading.service({ text: "正在下载数据，请稍候", spinner: "el-icon-loading", background: "rgba(0, 0, 0, 0.7)", })
    axios({
      method: 'get',
      url: requestUrl,
      responseType: 'blob',
      headers: { 'Authorization': 'Bearer ' + getToken() }
    }).then((res) => {
      const data = res.data
      const isBlob = blobValidate(data)
      if (isBlob) {
        const blob = (data instanceof Blob) ? data : new Blob([data], { type: 'application/zip' })
        this.saveAs(blob, name)
      } else {
        this.printErrMsg(data)
      }
      downloadLoadingInstance.close()
    }).catch(async (r) => {
      console.error('download.zip error', r)
      const msg = await extractErrorMessageFromAxiosError(r)
      if (DEBUG_SHOW_FULL_ERROR) {
        let full = ''
        try {
          const d = r.response && r.response.data
          if (d instanceof Blob && d.text) full = await d.text()
          else if (typeof d === 'string') full = d
          else full = JSON.stringify(d)
        } catch (e) { full = String(r) }
        MessageBox.alert(`<pre style="white-space:pre-wrap">${full}</pre>`, '下载失败（详细信息）', { dangerouslyUseHTMLString: true })
      }
      Message.error(msg || '下载文件出现错误，请联系管理员！')
      downloadLoadingInstance.close()
    })
  },
  saveAs(text, name, opts) {
    saveAs(text, name, opts)
  },
  async printErrMsg(data) {
    try {
      const resText = await (data && data.text ? data.text() : Promise.resolve(''))
      console.error('printErrMsg response text:', resText)
      const rspObj = resText ? JSON.parse(resText) : {}
      const errMsg = errorCode[rspObj.code] || rspObj.msg || errorCode['default']
      Message.error(errMsg)
    } catch (e) {
      console.error('printErrMsg parse failed', e)
      Message.error('下载失败，且无法解析服务器返回信息')
    }
  },
  parseFilenameFromHeaders(headers) {
    const contentDisposition = (headers && (headers['content-disposition'] || headers['Content-Disposition'])) || ''
    const matches = contentDisposition.match(/filename\*?=(?:UTF-8''|("?))?([^;\n\r"]+)/i)
    if (matches && matches[2]) {
      try { return decodeURIComponent(matches[2].trim()) } catch (e) { return matches[2].trim() }
    }
    return null
  }
}
