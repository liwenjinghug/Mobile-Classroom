import request from '@/utils/request'

// Open Trivia Database API - 免费公开题库
const TRIVIA_API_BASE = 'https://opentdb.com/api.php'

// 题目类型映射：新编码 1=判断 2=选择 3=简答
export const QUESTION_TYPE_MAP = {
  'multiple': 2, // 选择题
  'boolean': 1   // 判断题
}

// 难度映射
export const DIFFICULTY_MAP = {
  'easy': 1,
  'medium': 2,
  'hard': 3
}

// 获取题库分类
export function getTriviaCategories() {
  return new Promise((resolve, reject) => {
    fetch('https://opentdb.com/api_category.php')
      .then(response => response.json())
      .then(data => {
        if (data.trivia_categories) {
          resolve({
            code: 200,
            data: data.trivia_categories.map(cat => ({
              id: cat.id,
              name: cat.name
            }))
          })
        } else {
          reject(new Error('获取分类失败'))
        }
      })
      .catch(error => {
        console.error('获取题库分类失败:', error)
        reject(error)
      })
  })
}

// 获取题目
export function getTriviaQuestions(params = {}) {
  const {
    amount = 10,
    category = null,
    difficulty = null,
    type = null
  } = params

  let url = `${TRIVIA_API_BASE}?amount=${amount}`
  if (category) url += `&category=${category}`
  if (difficulty) url += `&difficulty=${difficulty}`
  if (type) url += `&type=${type}`

  return new Promise((resolve, reject) => {
    fetch(url)
      .then(response => response.json())
      .then(data => {
        if (data.response_code === 0 && data.results) {
          const questions = data.results.map((item, index) => ({
            id: null, // 新题目ID为空
            examId: null,
            questionContent: decodeHtml(item.question),
            questionType: QUESTION_TYPE_MAP[item.type] || 2,
            difficulty: DIFFICULTY_MAP[item.difficulty] || 1,
            score: getDefaultScore(QUESTION_TYPE_MAP[item.type] || 2),
            sortOrder: index + 1,
            questionOptions: item.type === 'boolean' ?
              JSON.stringify(['正确', '错误']) :
              JSON.stringify(shuffleArray([item.correct_answer, ...item.incorrect_answers]).map(decodeHtml)),
            correctAnswer: item.type === 'boolean' ?
              (item.correct_answer === 'True' ? '正确' : '错误') :
              decodeHtml(item.correct_answer),
            analysis: '', // 题目解析为空，可后续添加
            category: item.category,
            selected: false, // 是否被选中
            tempId: 'temp_' + Date.now() + '_' + index // 临时ID用于前端标识
          }))
          resolve({
            code: 200,
            data: questions
          })
        } else {
          reject(new Error(`API错误: ${getErrorMessage(data.response_code)}`))
        }
      })
      .catch(error => {
        console.error('获取题目失败:', error)
        reject(error)
      })
  })
}

// HTML解码
function decodeHtml(html) {
  const txt = document.createElement('textarea')
  txt.innerHTML = html
  return txt.value
}

// 数组打乱
function shuffleArray(array) {
  const newArray = [...array]
  for (let i = newArray.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[newArray[i], newArray[j]] = [newArray[j], newArray[i]]
  }
  return newArray
}

// 获取默认分数：新编码 1=判断 2=选择 3=简答
function getDefaultScore(questionType) {
  switch (questionType) {
    case 1: // 判断
      return 1
    case 2: // 选择
      return 2
    case 3: // 简答
      return 5
    default:
      return 2
  }
}

// 错误信息映射
function getErrorMessage(code) {
  const messages = {
    1: '没有足够的题目',
    2: '参数无效',
    3: 'Token未找到',
    4: 'Token为空'
  }
  return messages[code] || '未知错误'
}

// 获取题目数量信息
export function getTriviaCount(category = null) {
  let url = 'https://opentdb.com/api_count.php'
  if (category) url += `?category=${category}`

  return new Promise((resolve, reject) => {
    fetch(url)
      .then(response => response.json())
      .then(data => {
        resolve({
          code: 200,
          data: data.category_question_count || data
        })
      })
      .catch(error => {
        console.error('获取题目数量失败:', error)
        reject(error)
      })
  })
}

// 检查题目是否重复（基于题目内容）
export function checkQuestionDuplicate(existingQuestions, newQuestion) {
  const content1 = (newQuestion.questionContent || '').trim().toLowerCase()
  return existingQuestions.some(q => {
    const content2 = (q.questionContent || '').trim().toLowerCase()
    return content1 === content2
  })
}
