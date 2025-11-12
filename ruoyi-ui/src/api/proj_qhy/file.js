import request from '@/utils/request'

/**
 * 上传图片到本地assets目录（调用后端接口）
 * @param {Object} data - { base64: 图片base64字符串, fileName: 文件名 }
 */
export function uploadLocalImage(data) {
  return request({
    url: '/proj_qhy/file/uploadLocal',
    method: 'post',
    data: data
  })
}
