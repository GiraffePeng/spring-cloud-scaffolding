package com.peng.payservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装 生成JSAPI调用微信支付所属参数的 构造DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WxPayParamDTO {
	
	/**
	 * 必传 商品描述  商品简单描述   商家名称-销售商品类目  例如：腾讯-游戏	 线上电商，商家名称必须为实际销售商品的商家
	 */
	private String body;
	
	/**
	 * 非必传    附加数据，在查询API和支付通知中原样返回，可作为自定义参数使用。 
	 */
	private String attach;
	
	/**
	 * 必传  微信支付分配的公众账号ID（企业号corpid即为此appId）
	 */
	private String appid;
	
	/**
	 * 必传  微信支付分配的商户号
	 */
	private String mchId;
	
	/**
	 * 必传 key设置路径：微信商户平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置
	 */
	private String appKey;
	
	/**
	 * 必传 后端回调地址
	 */
	private String notifyUrl;
	
	/**
	 * 必传 交易类型	 JSAPI -JSAPI支付    NATIVE -Native支付  APP -APP支付
	 */
	private String tradeType;
	
	/**
	 * 必传  商户订单号	 商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|* 且在同一个商户号下唯一。
	 */
	private String outTradeNo;
	
	/**
	 * 必传   订单金额  以分为单位 
	 */
	private int totalFee;
	
	/**
	 * 必传 终端IP	 支持IPV4和IPV6两种格式的IP地址。用户的客户端IP
	 */
	private String spbillCreateIp;
	
	/**
	 * 必传 用户的openid
	 */
	private String openid;
	
	/**
	 * 非必传 上传此参数no_credit--可限制用户不能使用信用卡支付
	 */
	private String limitPay;
}
