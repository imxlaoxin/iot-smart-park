package com.imxiaoxin.iot.controller;


import com.imxiaoxin.iot.service.IBillingRuleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 停车场计费规则配置表 前端控制器
 * </p>
 *
 * @author imxiaoxin
 */
@Tag(name = "计费规则配置接口管理")
@Slf4j
@RestController
@RequestMapping("/iot/billing-rule")
public class BillingRuleController {

  @Autowired
  private IBillingRuleService iBillingRuleService;

}
