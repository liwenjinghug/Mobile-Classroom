// ruoyi-ui/src/utils/forumImage.js
/**
 * 获取图片完整URL
 */
export function getImageUrl(imagePath) {
  if (!imagePath) return ''

  // 如果是完整URL，直接返回
  if (imagePath.startsWith('http')) {
    return imagePath
  }

  // 处理本地图片路径
  if (imagePath.startsWith('/') || imagePath.startsWith('./')) {
    return imagePath
  }

  // 其他情况认为是相对路径
  return `/src/assets/xxsq_images/${imagePath}`
}

/**
 * 图片错误处理
 */
export function handleImageError(event) {
  const img = event.target
  img.src = '/src/assets/xxsq_images/default_image.png'
  img.onerror = null
}

/**
 * 处理网络图片URL
 */
export function handleNetworkImage(url) {
  return new Promise((resolve, reject) => {
    try {
      new URL(url)
      resolve(url)
    } catch (_) {
      reject(new Error('请输入有效的图片URL'))
    }
  })
}
