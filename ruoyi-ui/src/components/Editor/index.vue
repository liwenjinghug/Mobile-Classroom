<template>
  <div>
    <el-upload
      :action="uploadUrl"
      :before-upload="handleBeforeUpload"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      name="file"
      :show-file-list="false"
      :headers="headers"
      style="display: none"
      ref="upload"
      v-if="this.type == 'url'"
    >
    </el-upload>

    <el-upload
      class="upload-file-insert"
      :action="uploadCommonUrl"
      :before-upload="beforeFileUpload"
      :on-success="handleFileSuccess"
      :on-error="handleUploadError"
      :headers="headers"
      :show-file-list="false"
      style="display: none"
    >
      <button ref="fileInsertButton"></button>
    </el-upload>

    <div class="editor" ref="editor" :style="styles"></div>
  </div>
</template>

<script>
import axios from "axios"
import Quill from "quill"
import "quill/dist/quill.core.css"
import "quill/dist/quill.snow.css"
import "quill/dist/quill.bubble.css"
import { getToken } from "@/utils/auth"

export default {
  name: "Editor",
  props: {
    /* 编辑器的内容 */
    value: {
      type: String,
      default: "",
    },
    /* 高度 */
    height: {
      type: Number,
      default: null,
    },
    /* 最小高度 */
    minHeight: {
      type: Number,
      default: null,
    },
    /* 只读 */
    readOnly: {
      type: Boolean,
      default: false,
    },
    /* 上传文件大小限制(MB) */
    fileSize: {
      type: Number,
      default: 5,
    },
    /* 类型（base64格式、url格式） */
    type: {
      type: String,
      default: "url",
    }
  },
  data() {
    return {
      uploadUrl: process.env.VUE_APP_BASE_API + "/common/upload", // 图片上传地址
      uploadCommonUrl: process.env.VUE_APP_BASE_API + "/common/upload", // (新增) 通用文件上传地址
      headers: {
        Authorization: "Bearer " + getToken()
      },
      Quill: null,
      currentValue: "",
      options: {
        theme: "snow",
        bounds: document.body,
        debug: "warn",
        modules: {
          // 工具栏配置
          toolbar: {
            container: [
              ["bold", "italic", "underline", "strike"],       // 加粗 斜体 下划线 删除线
              ["blockquote", "code-block"],                    // 引用  代码块
              [{ list: "ordered" }, { list: "bullet" }],       // 有序、无序列表
              [{ indent: "-1" }, { indent: "+1" }],            // 缩进
              [{ size: ["small", false, "large", "huge"] }],   // 字体大小
              [{ header: [1, 2, 3, 4, 5, 6, false] }],         // 标题
              [{ color: [] }, { background: [] }],             // 字体颜色、字体背景颜色
              [{ align: [] }],                                 // 对齐方式
              ["clean"],                                       // 清除文本格式
              ["link", "image", "video"]                       // 链接、图片、视频
            ],
            // (新增) 在这里定义按钮的处理逻辑
            handlers: {
              // 劫持 'link' 按钮 -> 触发文件上传
              link: (value) => {
                if (value) {
                  // 触发上面新增的隐藏上传按钮
                  this.$refs.fileInsertButton.click();
                } else {
                  this.Quill.format("link", false);
                }
              },
              // 这里的 image 处理逻辑也可以移到这里，或者保持你在 init() 里的写法
              // 为了不破坏你原有的逻辑，image 我暂时不动，只加 link
            }
          }
        },
        placeholder: "请输入内容",
        readOnly: this.readOnly,
      },
    }
  },
  computed: {
    styles() {
      let style = {}
      if (this.minHeight) {
        style.minHeight = `${this.minHeight}px`
      }
      if (this.height) {
        style.height = `${this.height}px`
      }
      return style
    }
  },
  watch: {
    value: {
      handler(val) {
        if (val !== this.currentValue) {
          this.currentValue = val === null ? "" : val
          if (this.Quill) {
            this.Quill.clipboard.dangerouslyPasteHTML(this.currentValue)
          }
        }
      },
      immediate: true,
    },
  },
  mounted() {
    this.init()
  },
  beforeDestroy() {
    this.Quill = null
  },
  methods: {
    init() {
      const editor = this.$refs.editor
      this.Quill = new Quill(editor, this.options)

      // 原有的图片上传逻辑 (保持不变)
      if (this.type == 'url') {
        let toolbar = this.Quill.getModule("toolbar")
        toolbar.addHandler("image", (value) => {
          if (value) {
            this.$refs.upload.$children[0].$refs.input.click()
          } else {
            this.quill.format("image", false)
          }
        })
        this.Quill.root.addEventListener('paste', this.handlePasteCapture, true)
      }

      this.Quill.clipboard.dangerouslyPasteHTML(this.currentValue)

      this.Quill.on("text-change", (delta, oldDelta, source) => {
        const html = this.$refs.editor.children[0].innerHTML
        const text = this.Quill.getText()
        const quill = this.Quill
        this.currentValue = html
        this.$emit("input", html)
        this.$emit("on-change", { html, text, quill })
      })
      this.Quill.on("text-change", (delta, oldDelta, source) => {
        this.$emit("on-text-change", delta, oldDelta, source)
      })
      this.Quill.on("selection-change", (range, oldRange, source) => {
        this.$emit("on-selection-change", range, oldRange, source)
      })
      this.Quill.on("editor-change", (eventName, ...args) => {
        this.$emit("on-editor-change", eventName, ...args)
      })
    },

    // ================= (新增) 附件上传相关方法 =================
    // 1. 上传前校验
    beforeFileUpload(file) {
      // 可以在这里限制文件类型，例如不让传 exe 等
      return true;
    },

    // 2. 附件上传成功 -> 插入链接到光标处
    handleFileSuccess(res, file) {
      // res 是后端返回的数据，根据若依标准，通常包含 url 和 fileName
      if (res.code === 200) {
        // 获取当前光标位置
        let range = this.Quill.getSelection(true);
        let length = range ? range.index : this.Quill.getLength();

        // 获取文件名和下载链接
        let fileName = res.originalFilename || file.name;
        // 拼接完整的下载地址
        let fileUrl = process.env.VUE_APP_BASE_API + res.fileName;

        // 插入文本
        this.Quill.insertText(length, fileName, "link", fileUrl);

        // 移动光标到插入文本之后，避免后续输入也被加上链接
        this.Quill.setSelection(length + fileName.length);

        this.$message.success("附件插入成功");
      } else {
        this.$message.error(res.msg);
      }
    },
    // =========================================================

    // ... (原有的图片上传相关方法，保持不变)
    handleBeforeUpload(file) {
      const type = ["image/jpeg", "image/jpg", "image/png", "image/svg"]
      const isJPG = type.includes(file.type)
      if (!isJPG) {
        this.$message.error(`图片格式错误!`)
        return false
      }
      if (this.fileSize) {
        const isLt = file.size / 1024 / 1024 < this.fileSize
        if (!isLt) {
          this.$message.error(`上传文件大小不能超过 ${this.fileSize} MB!`)
          return false
        }
      }
      return true
    },
    handleUploadSuccess(res, file) {
      if (res.code == 200) {
        let quill = this.Quill
        let length = quill.getSelection().index
        quill.insertEmbed(length, "image", process.env.VUE_APP_BASE_API + res.fileName)
        quill.setSelection(length + 1)
      } else {
        this.$message.error("图片插入失败")
      }
    },
    handleUploadError() {
      this.$message.error("插入失败")
    },
    handlePasteCapture(e) {
      const clipboard = e.clipboardData || window.clipboardData
      if (clipboard && clipboard.items) {
        for (let i = 0; i < clipboard.items.length; i++) {
          const item = clipboard.items[i]
          if (item.type.indexOf('image') !== -1) {
            e.preventDefault()
            const file = item.getAsFile()
            this.insertImage(file)
          }
        }
      }
    },
    insertImage(file) {
      const formData = new FormData()
      formData.append("file", file)
      axios.post(this.uploadUrl, formData, { headers: { "Content-Type": "multipart/form-data", Authorization: this.headers.Authorization } }).then(res => {
        this.handleUploadSuccess(res.data)
      })
    }
  }
}
</script>

<style>
/* ... (样式保持不变) */
.editor, .ql-toolbar {
  white-space: pre-wrap !important;
  line-height: normal !important;
}
.quill-img {
  display: none;
}
.ql-snow .ql-tooltip[data-mode="link"]::before {
  content: "请输入链接地址:";
}
.ql-snow .ql-tooltip.ql-editing a.ql-action::after {
  border-right: 0px;
  content: "保存";
  padding-right: 0px;
}
.ql-snow .ql-tooltip[data-mode="video"]::before {
  content: "请输入视频地址:";
}
.ql-snow .ql-picker.ql-size .ql-picker-label::before,
.ql-snow .ql-picker.ql-size .ql-picker-item::before {
  content: "14px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="small"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="small"]::before {
  content: "10px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="large"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="large"]::before {
  content: "18px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="huge"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="huge"]::before {
  content: "32px";
}
.ql-snow .ql-picker.ql-header .ql-picker-label::before,
.ql-snow .ql-picker.ql-header .ql-picker-item::before {
  content: "文本";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="1"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="1"]::before {
  content: "标题1";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="2"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="2"]::before {
  content: "标题2";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="3"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="3"]::before {
  content: "标题3";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="4"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="4"]::before {
  content: "标题4";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="5"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="5"]::before {
  content: "标题5";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="6"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="6"]::before {
  content: "标题6";
}
.ql-snow .ql-picker.ql-font .ql-picker-label::before,
.ql-snow .ql-picker.ql-font .ql-picker-item::before {
  content: "标准字体";
}
.ql-snow .ql-picker.ql-font .ql-picker-label[data-value="serif"]::before,
.ql-snow .ql-picker.ql-font .ql-picker-item[data-value="serif"]::before {
  content: "衬线字体";
}
.ql-snow .ql-picker.ql-font .ql-picker-label[data-value="monospace"]::before,
.ql-snow .ql-picker.ql-font .ql-picker-item[data-value="monospace"]::before {
  content: "等宽字体";
}
</style>
