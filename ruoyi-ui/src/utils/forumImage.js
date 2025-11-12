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

  // 处理后端返回的 /profile/xxx 格式
  if (imagePath.startsWith('/') && !imagePath.startsWith('/src')) {
    // 先尝试按文件名从前端静态资源加载（webpack打包时会把 assets/xxsq_images 中的文件打包），
    // 这样在开发/测试环境下其他人 pull 下来也能直接打开图片（不依赖后端运行时文件）。
    try {
      const fileName = imagePath.substring(imagePath.lastIndexOf('/') + 1)
      if (fileName) {
        return require(`@/assets/xxsq_images/${fileName}`)
      }
    } catch (e) {
      // 本地没有对应文件，回退到后端 URL
    }
    const prefix = (process && process.env && process.env.VUE_APP_BASE_API) ? process.env.VUE_APP_BASE_API : ''
    return prefix + imagePath
  }

  // 处理前端项目内别名 @/assets/xxsq_images/xxx 或 /src/assets/xxsq_images/xxx
  // 优先尝试使用 webpack 的 require 来解析静态资源，这样在 dev/build 下路径可靠
  try {
    if (imagePath.startsWith('@/assets/xxsq_images/') || imagePath.startsWith('/src/assets/xxsq_images/')) {
      // 取文件名部分
      const name = imagePath.replace(/^@?\/src?\/assets\/xxsq_images\//, '')
      return require(`@/assets/xxsq_images/${name}`)
    }

    // 如果是文件名形式（如 welcome.jpg 或 UUID.ext），尝试从本地 assets 目录加载，否则回退到后端 /profile
    if (!imagePath.includes('/')) {
      try {
        return require(`@/assets/xxsq_images/${imagePath}`)
      } catch (e) {
        // 如果本地没有该文件，则回退到后端访问路径
        const prefix = (process && process.env && process.env.VUE_APP_BASE_API) ? process.env.VUE_APP_BASE_API : ''
        return prefix + `/profile/${imagePath}`
      }
    }
  } catch (e) {
    // 如果 require 抛出异常，忽略并使用后续逻辑
  }

  // 其他情况认为是相对路径到 xxsq_images，尝试用 require
  try {
    const normalized = imagePath.replace(/^\.\//, '')
    return require(`@/assets/xxsq_images/${normalized}`)
  } catch (e) {
    // 如果都无法通过 require 解析，默认回退为原始路径（可能是 /src/... 或其他可访问路径）
    return imagePath
  }
}

/**
 * 图片错误处理
 */
export function handleImageError(event) {
  const img = event.target
  // 取消后续 onerror，防止循环
  img.onerror = null
  try {
    // 使用项目内存在的默认头像或占位图
    img.src = require('@/assets/xxsq_images/welcome.jpg')
  } catch (e) {
    // 如果 require 失败，使用一个简单的数据URL 1x1 PNG（总能加载）作为最后兜底
    img.src = 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw=='
  }
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
