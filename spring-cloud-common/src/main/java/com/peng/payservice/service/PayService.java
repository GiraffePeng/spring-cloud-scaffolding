package com.peng.payservice.service;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.peng.common.Result;
import com.peng.payservice.dto.WxPayParamDTO;
import com.peng.payservice.dto.WxPayResultDTO;
import com.peng.payservice.dto.WxQueryParamDTO;
import com.peng.payservice.dto.WxQueryResultDTO;


public interface PayService {
	/**
	 * 微信支付 (jsAPI方式) 构建DTO 传入 即可获得jsapi所需参数
	 * @param wxPayParamDTO
	 * @return
	 */
	@PostMapping("weChatPay")
    Result<WxPayResultDTO> weChatPay(@RequestBody WxPayParamDTO wxPayParamDTO);
	
	/**
	 * 微信支付 查询支付后的订单信息
	 * @param wxPayParamDTO
	 * @return
	 */
	@PostMapping("weChatQuery")
	Result<WxQueryResultDTO> weChatQuery(@RequestBody WxQueryParamDTO wxQueryParamDTO);
	
}
