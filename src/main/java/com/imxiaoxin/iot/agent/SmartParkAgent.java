package com.imxiaoxin.iot.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

/**
 * @author imxiaoxin
 *
 */
@AiService(
    wiringMode = AiServiceWiringMode.EXPLICIT,
    chatModel = "qwenChatModel",
//    chatModel = "ollamaChatModel",
    tools = {"expEnvTools"}
)
public interface SmartParkAgent {
  @SystemMessage(fromResource = "prompts/exp_env_prompt.txt")
  String chat(@UserMessage String message);
}
