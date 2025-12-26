package org.whu.timeflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.whu.timeflow.common.Result;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@Tag(name = "AI 服务", description = "LLM 相关能力")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    // 💡 替换为你的 DeepSeek API Key
//    @Value("${ai.deepseek.api-key:sk-xxxx}")
    private final String apiKey = "sk-";

    private static final String DS_URL = "https://api.deepseek.com/chat/completions";

    // 1. 日记续写
    @Operation(summary = "日记续写/润色")
    @PostMapping("/diary/completion")
    public Result<String> diaryCompletion(@RequestBody Map<String, String> params) {
        String content = params.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error("内容不能为空");
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "deepseek-chat");
            body.put("messages", List.of(
                    Map.of("role", "system", "content", "你是一个温暖的日记助手。请根据用户的日记片段，进行续写或润色，风格要治愈、温暖，字数控制在100字以内。"),
                    Map.of("role", "user", "content", content)
            ));
            body.put("stream", false);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(DS_URL, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map resBody = response.getBody();
                List choices = (List) resBody.get("choices");
                Map firstChoice = (Map) choices.get(0);
                Map message = (Map) firstChoice.get("message");
                String aiText = (String) message.get("content");
                return Result.success(aiText);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("AI 服务暂时不可用");
        }
        return Result.error("请求失败");
    }

    // 2. 语音记账解析
    @Operation(summary = "智能记账解析")
    @PostMapping("/bill/parse")
    public Result<Map<String, Object>> billParse(@RequestBody Map<String, String> params) {
        String content = params.get("content");
        if (content == null || content.trim().isEmpty()) {
            log.info("账单识别 结果=失败 原因=识别内容为空");
            return Result.error("识别内容不能为空");
        }
        log.info("账单识别 开始 内容长度={}", content.length());

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // 🧠 Prompt Engineering: 强制要求返回 JSON 格式
            String prompt = "你是一个智能记账助手。请分析用户的输入，提取账单信息。\n" +
                    "用户输入: \"" + content + "\"\n\n" +
                    "请提取以下3个字段，并直接返回纯 JSON 格式（不要包含 markdown 代码块）：\n" +
                    "1. amount (数字/浮点数，保留两位小数)\n" +
                    "2. type (必须从以下列表中选择最匹配的一项: 餐饮, 交通, 购物, 娱乐, 医疗, 教育, 居家, 其他。默认为'其他')\n" +
                    "3. remark (提取具体的消费内容作为备注，去除金额和类型词)\n\n" +
                    "示例返回: {\"amount\": 35.5, \"type\": \"餐饮\", \"remark\": \"麦当劳\"}";

            Map<String, Object> body = new HashMap<>();
            body.put("model", "deepseek-chat");
            body.put("messages", List.of(
                    Map.of("role", "system", "content", "You are a helpful assistant that extracts billing info into JSON."),
                    Map.of("role", "user", "content", prompt)
            ));
            // 开启 JSON 模式 (DeepSeek V3 支持 response_format，为兼容性暂用 prompt 约束，效果通常足够)
            body.put("stream", false);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(DS_URL, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map resBody = response.getBody();
                List choices = (List) resBody.get("choices");
                Map firstChoice = (Map) choices.get(0);
                Map message = (Map) firstChoice.get("message");
                String aiJsonText = (String) message.get("content");

                // 🧹 清洗数据：防止 AI 返回 ```json ... ```
                aiJsonText = aiJsonText.replace("```json", "").replace("```", "").trim();

                // 解析 JSON 字符串为 Map
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> result = mapper.readValue(aiJsonText, Map.class);

                log.info("账单识别 结果=成功 金额={} 类型={} 备注={}",
                        result.get("amount"), result.get("type"), result.get("remark"));
                return Result.success(result);
            }
            log.warn("账单识别 结果=失败 原因=请求失败 状态码={}", response.getStatusCode());
        } catch (Exception e) {
            log.error("账单识别 结果=失败 原因=异常", e);
            return Result.error("AI 解析失败: " + e.getMessage());
        }
        return Result.error("请求失败");
    }
}
