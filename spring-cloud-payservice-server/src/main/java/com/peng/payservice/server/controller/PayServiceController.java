package com.peng.payservice.server.controller;

import org.springframework.web.bind.annotation.RestController;

import com.peng.common.Result;
import com.peng.payservice.dto.WxPayParamDTO;
import com.peng.payservice.dto.WxPayResultDTO;
import com.peng.payservice.dto.WxQueryParamDTO;
import com.peng.payservice.dto.WxQueryResultDTO;
import com.peng.payservice.server.util.wechat.WxPayUtil;
import com.peng.payservice.service.PayService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class PayServiceController implements PayService{

	@Override
	public Result<WxPayResultDTO> weChatPay(WxPayParamDTO wxPayParamDTO) {
		return WxPayUtil.wxPay(wxPayParamDTO);
	}

	@Override
	public Result<WxQueryResultDTO> weChatQuery(WxQueryParamDTO wxQueryParamDTO) {
		return WxPayUtil.orderQuery(wxQueryParamDTO);
	}

}
