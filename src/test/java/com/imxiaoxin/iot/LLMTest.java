package com.imxiaoxin.iot;


import com.imxiaoxin.iot.agent.SmartParkAgent;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LLMTest {

  /**
   * gpt-4o-mini语言模型接入测试
   */
  @Test
  public void testGPTDemo() {
    //初始化模型
    OpenAiChatModel model = OpenAiChatModel.builder()
        //LangChain4j提供的代理服务器，该代理服务器会将演示密钥替换成真实密钥， 再将请求转发给OpenAI API
        .baseUrl("http://langchain4j.dev/demo/openai/v1")
        .apiKey("demo") //设置模型apiKey
        .modelName("gpt-4o-mini") //设置模型名称
        .build();

    //向模型提问
    String answer = model.chat("你好");
    //输出结果
    System.out.println(answer);
  }

  @Autowired
  private OllamaChatModel ollamaChatModel;

  @Test
  public void testOllamaModel(){
    String ret = ollamaChatModel.chat("你现在是一个智慧园区的安全专家。园区当前发生环境告警：类型编码为 二氧化碳浓度，" +
        "告警级别为 1（0:预警 1:危险 2:危急），传感器上报的异常详情为：【当前二氧化碳浓度过高】。" +
        "请用简短专业的语言（50字以内），分析可能的原因，并给出安保人员的第一步处置建议。");
    System.out.println(ret);
  }

  @Autowired
  private QwenChatModel qwenChatModel;

  @Test
  public void testQwenModel(){
    String ret = qwenChatModel.chat("你现在是一个智慧园区的安全专家。园区当前发生环境告警：类型编码为 二氧化碳浓度，" +
        "告警级别为 1（0:预警 1:危险 2:危急），传感器上报的异常详情为：【当前二氧化碳浓度过高】。" +
        "请用简短专业的语言（50字以内），分析可能的原因，并给出安保人员的第一步处置建议。");
    System.out.println(ret);
  }

  @Autowired
  private SmartParkAgent smartParkAgent;

  @Test
  public void testSmartParkAgent(){
    String ret = smartParkAgent.chat("当前停车场发生环境告警：告警类型【二氧化碳浓度】，告警级别【预警】，传感器上报的异常原因：【【告警级别: 预警】当前CO2 过高,阈值1000ppm ,当前1194ppm 】。请立刻给出处置建议。");
    System.out.println(ret);
  }

}