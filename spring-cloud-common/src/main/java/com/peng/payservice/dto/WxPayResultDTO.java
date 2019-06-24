package com.peng.payservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装JSAPI调用微信支付需要的参数信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WxPayResultDTO {
	
	/*公众号id	 商户注册具有支付权限的公众号成功后即可获得*/
	private String appId;
	
	/*时间戳	 当前的时间  示例:1414561699	*/
	private String timeStamp;
	
	/*随机字符串	 随机字符串，不长于32位。推荐随机数生成算法 详见微信平台*/
	private String nonceStr;
	
	/*订单详情扩展字符串	 统一下单接口返回的prepay_id参数值，提交格式如：prepay_id=*** */
	private String packages;
	
	/*签名方式	 签名类型，默认为MD5，支持HMAC-SHA256和MD5。注意此处需与统一下单的签名类型一致*/
	private String signType;
	
	/*签名，详见签名生成算法*/
	private String paySign;
	
	/*保险订单号*/
	private String ordCode;
}
