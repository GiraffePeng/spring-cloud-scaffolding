package com.peng.payservice.properties;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

/**
 * 以下参数请参考 https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_2
 * 
 */
public class WeixinOrderQueryProperty {

	private String appid;
	
	private String mchId;
	
	private String outTradeNo;
	
	private String nonceStr;
	
	private String sign;
	
	/**
	 * 公众账号ID
	 * @return
	 */
	public String getAppid() {
		return appid;
	}

	/**
	 * 公众账号ID:微信支付分配的公众账号ID（企业号corpid即为此appId）
	 * @param appid
	 */
	public void setAppid(String appid) {
		this.appid = appid;
	}

	/**
	 * 商户ID
	 * @return
	 */
	public String getMchId() {
		return mchId;
	}

	/**
	 * 商户ID:微信支付分配的商户号
	 * @param mchId
	 */
	public void setMchId(String mchId) {
		this.mchId = mchId;
	}

	/**
	 * 随机字符串
	 * @return
	 */
	public String getNonceStr() {
		return nonceStr;
	}

	/**
	 * 随机字符串:随机字符串，长度要求在32位以内。推荐随机数生成算法
	 * @param nonceStr
	 */
	public void setNonceStr(String nonceStr) {
		this.nonceStr = nonceStr;
	}

	/**
	 * 签名
	 * @return
	 */
	public String getSign() {
		return sign;
	}

	/**
	 * 签名:通过签名算法计算得出的签名值，详见签名生成算法,参考https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=4_3
	 * @param sign
	 */
	public void setSign(String sign) {
		this.sign = sign;
	}


	/**
	 * 商户订单号
	 * @return
	 */
	public String getOutTradeNo() {
		return outTradeNo;
	}

	/**
	 * 商户订单号:商户系统内部订单号，要求32个字符内、且在同一个商户号下唯一。 详见商户订单号
	 * @param outTradeNo
	 */
	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	
	/**
	 * 以下字段必须按照字母顺序排列，如果必填字段为空，会抛出异常
	 * @return
	 * @throws Exception 
	 */
	public String assemblePrepayParamsForSign(String apiKey) throws Exception{
		StringBuilder builder = new StringBuilder();
		if(StringUtils.isEmpty(this.appid)){
			throw new Exception("appid不能为空");
		}
		builder.append("appid=" + this.appid);

		if(StringUtils.isEmpty(this.mchId)){
			throw new Exception("mch_id不能为空");
		}
		builder.append("&mch_id=" + this.mchId);
		
		if(StringUtils.isEmpty(this.nonceStr)){
			throw new Exception("nonceStr不能为空");
		}
		builder.append("&nonce_str=" + this.nonceStr);
		
		if(StringUtils.isEmpty(this.outTradeNo)){
			throw new Exception("out_trade_no不能为空");
		}
		builder.append("&out_trade_no=" + this.outTradeNo);
		builder.append("&key=" + apiKey);
		return builder.toString();
	}
	
	/**
	 * 由于assemblePrepayParamsForSign是前一步骤，因此此处就不做数据检查了
	 * @return
	 */
	public Map<String, String> assembleXmlMapForPrepayId(){
		Map<String,String> map = new HashMap<String, String>();
		map.put("appid", this.appid);
		map.put("mch_id", this.mchId);
		map.put("nonce_str", this.nonceStr);
		map.put("out_trade_no", this.outTradeNo);
		map.put("sign", this.sign);
		return map;
	}
	
}
