package com.peng.payservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装查询订单返回信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WxQueryResultDTO {
	
	/**交易状态	 
	 * SUCCESS—支付成功
	 * REFUND—转入退款
	 * NOTPAY—未支付
	 * CLOSED—已关闭
	 * REVOKED—已撤销（付款码支付）
	 * USERPAYING--用户支付中（付款码支付）
	 * PAYERROR--支付失败(其他原因，如银行返回失败)
	 * */
	private String tradeState;
	
	/**
	 * 是否关注公众账号	 用户是否关注公众账号，Y-关注，N-未关注
	 */
	private String isSubscribe;
	
	/**
	 * 用户id
	 */
	private String openId;
	
	/**
	 * 付款金额 订单总金额，单位为分
	 */
	private Integer totalFee;
	
	/**
	 * 商户订单号	
	 */
	private String outTradeNo;
	
	/**
	 * 支付完成时间		
	 */
	private String timeEnd;
	
	/**
	 *微信支付订单号		
	 */
	private String transactionId;
	
}
