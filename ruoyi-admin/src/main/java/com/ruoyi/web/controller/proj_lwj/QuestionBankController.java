package com.ruoyi.web.controller.proj_lwj;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.*;

/**
 * 题库API控制器
 *
 * @author ruoyi
 * @date 2025-01-16
 */
@RestController
@RequestMapping("/proj_lwj/questionBank")
public class QuestionBankController extends BaseController
{
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 使用免费的Open Trivia Database API
    private static final String TRIVIA_API_URL = "https://opentdb.com/api.php";

    /**
     * 搜索题目
     */
    @GetMapping("/search")
    public AjaxResult searchQuestions(
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "difficulty", required = false) Integer difficulty,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit)
    {
        try {
            // 构建API请求URL
            StringBuilder urlBuilder = new StringBuilder(TRIVIA_API_URL);
            urlBuilder.append("?amount=").append(Math.min(limit, 50)); // 限制最多50题

            // 根据难度映射API参数
            if (difficulty != null) {
                String difficultyStr = mapDifficulty(difficulty);
                if (difficultyStr != null) {
                    urlBuilder.append("&difficulty=").append(difficultyStr);
                }
            }

            // 根据题型映射API参数
            if (type != null) {
                String typeStr = mapQuestionType(type);
                if (typeStr != null) {
                    urlBuilder.append("&type=").append(typeStr);
                }
            }

            String apiUrl = urlBuilder.toString();

            // 调用外部API
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode resultsNode = rootNode.get("results");

                List<Map<String, Object>> questions = new ArrayList<>();

                if (resultsNode != null && resultsNode.isArray()) {
                    for (JsonNode questionNode : resultsNode) {
                        Map<String, Object> question = parseQuestion(questionNode);

                        // 如果有关键词搜索，进行过滤
                        if (keyword != null && !keyword.trim().isEmpty()) {
                            String questionText = (String) question.get("question");
                            if (questionText == null ||
                                !questionText.toLowerCase().contains(keyword.toLowerCase())) {
                                continue;
                            }
                        }

                        questions.add(question);
                    }
                }

                return AjaxResult.success(questions);
            } else {
                return AjaxResult.error("外部API调用失败");
            }

        } catch (Exception e) {
            logger.error("搜索题目失败", e);
            return AjaxResult.error("搜索题目失败: " + e.getMessage());
        }
    }

    /**
     * 解析题目数据
     */
    private Map<String, Object> parseQuestion(JsonNode questionNode) {
        Map<String, Object> question = new HashMap<>();

        // 生成唯一ID
        question.put("id", UUID.randomUUID().toString());

        // 获取题目内容
        String questionText = questionNode.get("question").asText();
        question.put("question", decodeHtml(questionText));

        // 获取正确答案
        String correctAnswer = questionNode.get("correct_answer").asText();
        question.put("correctAnswer", decodeHtml(correctAnswer));

        // 获取题目类型
        String apiType = questionNode.get("type").asText();
        Integer mappedType = mapApiTypeToLocal(apiType);
        question.put("type", mappedType);

        // 获取难度
        String apiDifficulty = questionNode.get("difficulty").asText();
        Integer mappedDifficulty = mapApiDifficultyToLocal(apiDifficulty);
        question.put("difficulty", mappedDifficulty);

        // 处理选项
        List<String> options = new ArrayList<>();
        if ("multiple".equals(apiType)) {
            // 多选题：添加正确答案和错误选项
            options.add(decodeHtml(correctAnswer));
            JsonNode incorrectAnswers = questionNode.get("incorrect_answers");
            if (incorrectAnswers != null && incorrectAnswers.isArray()) {
                for (JsonNode incorrect : incorrectAnswers) {
                    options.add(decodeHtml(incorrect.asText()));
                }
            }
            // 随机打乱选项顺序
            Collections.shuffle(options);
            question.put("options", options);
        } else {
            // 判断题：True/False
            options.add("True");
            options.add("False");
            question.put("options", options);
        }

        return question;
    }

    /**
     * 映射本地难度到API难度
     */
    private String mapDifficulty(Integer difficulty) {
        switch (difficulty) {
            case 1: return "easy";
            case 2: return "medium";
            case 3: return "hard";
            default: return null;
        }
    }

    /**
     * 映射本地题型到API题型：新编码 1=判断 2=选择 3=简答
     */
    private String mapQuestionType(Integer type) {
        switch (type) {
            case 1: return "boolean";  // 判断题
            case 2: return "multiple"; // 选择题
            default: return null; // 其他题型API不支持
        }
    }

    /**
     * 映射API题型到本地题型：新编码 1=判断 2=选择 3=简答
     */
    private Integer mapApiTypeToLocal(String apiType) {
        switch (apiType) {
            case "multiple": return 2; // 选择题
            case "boolean": return 1;  // 判断题
            default: return 2; // 默认选择题
        }
    }

    /**
     * 映射API难度到本地难度
     */
    private Integer mapApiDifficultyToLocal(String apiDifficulty) {
        switch (apiDifficulty) {
            case "easy": return 1;
            case "medium": return 2;
            case "hard": return 3;
            default: return 2; // 默认一般
        }
    }

    /**
     * HTML解码
     */
    private String decodeHtml(String text) {
        if (text == null) return null;

        return text.replace("&quot;", "\"")
                  .replace("&#039;", "'")
                  .replace("&amp;", "&")
                  .replace("&lt;", "<")
                  .replace("&gt;", ">")
                  .replace("&nbsp;", " ");
    }
}
