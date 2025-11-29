Page({
  data: { url: '' },
  onLoad(options) {
    // expect query param url encoded
    const raw = options && options.url ? decodeURIComponent(options.url) : ''
    this.setData({ url: raw })
  }
})

