package com.peng.payservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装 调用微信订单查询接口 构造DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WxQueryParamDTO {
	
	/**
	 * 必传  微信支付分配的公众账号ID（企业号corpid即为此appId）
	 */
	private String appid;
	
	/**
	 * 必传  微信支付分配的商户号
	 */
	private String mchId;
	
	/**
	 * 必传  支付所需要的 appkey值，可在商户平台设置
	 */
	private String apiKey;
	
	/**
	 * 必传  商户订单号	 商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|* 且在同一个商户号下唯一。
	 */
	private String outTradeNo;
	
}
