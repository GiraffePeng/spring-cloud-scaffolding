package com.peng.payservice.properties;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

/**
 * 以下参数请参考 https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_1
 * 
 */
public class WeixinOrderApiProperty {

	private String appid;
	
	private String mchId;
	
	private String deviceInfo = "WEB";
	
	private String nonceStr;
	
	private String sign;
	
	private String body;
	
	private String detail;
	
	private String attach;
	
	private String outTradeNo;
	
	private String feeType = "CNY";
	
	private int totalFee;
	
	private String spbillCreateIp;
	
	/**
	 * yyyyMMddHHmmss
	 */
	private String timeStart;
	
	/**
	 * yyyyMMddHHmmss
	 */
	private String timeExpire;
	
	private String goodsTag;
	
	private String notifyUrl;
	
	private String tradeType;
	
	private String productId;
	
	private String limitPay;
	
	private String openid;

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
	 * 设备号
	 * @return
	 */
	public String getDeviceInfo() {
		return deviceInfo;
	}

	/**
	 * 设备号:自定义参数，可以为终端设备号(门店号或收银设备ID)，PC网页或公众号内支付可以传"WEB"
	 * @param deviceInfo
	 */
	public void setDeviceInfo(String deviceInfo) {
		this.deviceInfo = deviceInfo;
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
	 * 商品描述
	 * @return
	 */
	public String getBody() {
		return body;
	}

	/**
	 * 商品描述:商品简单描述，该字段请按照规范传递，具体请见参数规定,参考https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=4_2
	 * @param body
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * 商品详情
	 * @return
	 */
	public String getDetail() {
		return detail;
	}

	/**
	 * 商品详情:
	 * 商品详细列表，使用Json格式，传输签名前请务必使用CDATA标签将JSON文本串保护起来。
	 * cost_price Int 可选 32 订单原价，商户侧一张小票订单可能被分多次支付，订单原价用于记录整张小票的支付金额。当订单原价与支付金额不相等则被判定为拆单，无法享受优惠。
	 * receipt_id String 可选 32 商家小票ID
	 * goods_detail 服务商必填 []：
	 *  └ goods_id String 必填 32 商品的编号
	 *  └ wxpay_goods_id String 可选 32 微信支付定义的统一商品编号
	 *  └ goods_name String 可选 256 商品名称 
	 *  └ quantity Int 必填  32 商品数量
	 *  └ price Int 必填 32 商品单价，如果商户有优惠，需传输商户优惠后的单价 
	 * 注意：单品总金额应<=订单总金额total_fee，否则会无法享受优惠。
	 * @param detail
	 */
	public void setDetail(String detail) {
		this.detail = detail;
	}

	/**
	 * 附加数据
	 * @return
	 */
	public String getAttach() {
		return attach;
	}

	/**
	 * 附加数据
	 * @param attach
	 */
	public void setAttach(String attach) {
		this.attach = attach;
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
	 * 标价币种
	 * @return
	 */
	public String getFeeType() {
		return feeType;
	}

	/**
	 * 标价币种:符合ISO 4217标准的三位字母代码，默认人民币：CNY，详细列表请参见货币类型
	 * @param feeType
	 */
	public void setFeeType(String feeType) {
		this.feeType = feeType;
	}

	/**
	 * 标价金额
	 * @return
	 */
	public int getTotalFee() {
		return totalFee;
	}

	/**
	 * 标价金额:订单总金额，单位为分，详见支付金额
	 * @param totalFee
	 */
	public void setTotalFee(int totalFee) {
		this.totalFee = totalFee;
	}

	/**
	 * 终端IP
	 * @return
	 */
	public String getSpbillCreateIp() {
		return spbillCreateIp;
	}

	/**
	 * 终端IP:APP和网页支付提交用户端ip，Native支付填调用微信支付API的机器IP。
	 * @param spbillCreateIp
	 */
	public void setSpbillCreateIp(String spbillCreateIp) {
		this.spbillCreateIp = spbillCreateIp;
	}

	/**
	 * 交易起始时间
	 * @return
	 */
	public String getTimeStart() {
		return timeStart;
	}

	/**
	 * 交易起始时间:订单生成时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010。其他详见时间规则
	 * @param timeStart
	 */
	public void setTimeStart(String timeStart) {
		this.timeStart = timeStart;
	}

	/**
	 * 交易结束时间
	 * @return
	 */
	public String getTimeExpire() {
		return timeExpire;
	}

	/**
	 * 交易结束时间
	 * @param timeExpire
	 */
	public void setTimeExpire(String timeExpire) {
		this.timeExpire = timeExpire;
	}

	/**
	 * 商品标记
	 * @return
	 */
	public String getGoodsTag() {
		return goodsTag;
	}

	/**
	 * 商品标记，使用代金券或立减优惠功能时需要的参数，说明详见代金券或立减优惠
	 * @param goodsTag
	 */
	public void setGoodsTag(String goodsTag) {
		this.goodsTag = goodsTag;
	}

	/**
	 * 通知地址
	 * @return
	 */
	public String getNotifyUrl() {
		return notifyUrl;
	}

	/**
	 * 通知地址:异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
	 * @param notifyUrl
	 */
	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	/**
	 * 交易类型
	 * @return
	 */
	public String getTradeType() {
		return tradeType;
	}

	/**
	 * 交易类型:取值如下：JSAPI，NATIVE，APP等，微信内用JS-SDK交易用JSAPI。说明详见参数规定
	 * @param tradeType
	 */
	public void setTradeType(String tradeType) {
		this.tradeType = tradeType;
	}

	/**
	 * 商品ID
	 * @return
	 */
	public String getProductId() {
		return productId;
	}

	/**
	 * 商品ID:trade_type=NATIVE时（即扫码支付），此参数必传。此参数为二维码中包含的商品ID，商户自行定义。
	 * @param productId
	 */
	public void setProductId(String productId) {
		this.productId = productId;
	}

	/**
	 * 指定支付方式
	 * @return
	 */
	public String getLimitPay() {
		return limitPay;
	}

	/**
	 * 指定支付方式:上传此参数no_credit--可限制用户不能使用信用卡支付
	 * @param limitPay
	 */
	public void setLimitPay(String limitPay) {
		this.limitPay = limitPay;
	}

	/**
	 * 用户标识
	 * @return
	 */
	public String getOpenid() {
		return openid;
	}

	/**
	 * 用户标识:trade_type=JSAPI时（即公众号支付），此参数必传，此参数为微信用户在商户对应appid下的唯一标识。
	 * openid如何获取，可参考【获取openid】。
	 * 企业号请使用【企业号OAuth2.0接口】获取企业号内成员userid，再调用【企业号userid转openid接口】进行转换
	 * @param openid
	 */
	public void setOpenid(String openid) {
		this.openid = openid;
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
		
		if(!StringUtils.isEmpty(this.attach)){
			builder.append("&attach=" + this.attach);
		}
		if(StringUtils.isEmpty(this.body)){
			throw new Exception("body不能为空");
		}
		builder.append("&body=" + this.body);
		
		if(!StringUtils.isEmpty(this.detail)){
			builder.append("&detail=" + this.detail);
		}
		if(!StringUtils.isEmpty(this.deviceInfo)){
			builder.append("&device_info=" + this.deviceInfo);
		}
		if(!StringUtils.isEmpty(this.feeType)){
			builder.append("&fee_type=" + this.feeType);
		}
		if(!StringUtils.isEmpty(this.goodsTag)){
			builder.append("&goods_tag=" + this.goodsTag);
		}
		if(!StringUtils.isEmpty(this.limitPay)){
			builder.append("&limit_pay=" + this.limitPay);
		}

		if(StringUtils.isEmpty(this.mchId)){
			throw new Exception("mch_id不能为空");
		}
		builder.append("&mch_id=" + this.mchId);
		
		if(StringUtils.isEmpty(this.nonceStr)){
			throw new Exception("nonceStr不能为空");
		}
		builder.append("&nonce_str=" + this.nonceStr);
		
		if(StringUtils.isEmpty(this.notifyUrl)){
			throw new Exception("notify_url不能为空");
		}
		builder.append("&notify_url=" + this.notifyUrl);
		
		if(StringUtils.isEmpty(this.openid) && "JSAPI".equals(this.tradeType)){
			throw new Exception("trade_type=JSAPI时,openid不能为空");
		}
		builder.append("&openid=" + this.openid);
		
		if(StringUtils.isEmpty(this.outTradeNo)){
			throw new Exception("out_trade_no不能为空");
		}
		builder.append("&out_trade_no=" + this.outTradeNo);
		
		if(!StringUtils.isEmpty(this.productId)){
			builder.append("&product_id=" + this.productId);
		}
		
		if(StringUtils.isEmpty(this.spbillCreateIp)){
			throw new Exception("spbill_create_ip不能为空");
		}
		builder.append("&spbill_create_ip=" + this.spbillCreateIp);
		
		if(!StringUtils.isEmpty(this.timeExpire)){
			builder.append("&time_expire=" + this.timeExpire);
		}
		
		if(!StringUtils.isEmpty(this.timeStart)){
			builder.append("&time_start=" + this.timeStart);
		}
		
		if(this.totalFee > 0){
			builder.append("&total_fee=" + this.totalFee);
		}else{
			throw new Exception("total_fee必须大于0");
		}
		
		if(StringUtils.isEmpty(this.tradeType)){
			throw new Exception("trade_type不能为空");
		}
		builder.append("&trade_type=" + this.tradeType);
		
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
		
		if(!StringUtils.isEmpty(this.attach)){
			map.put("attach", this.attach);
		}
		map.put("body", this.body);
		
		if(!StringUtils.isEmpty(this.detail)){
			map.put("detail", this.detail);
		}
		if(!StringUtils.isEmpty(this.deviceInfo)){
			map.put("device_info", this.deviceInfo);
		}
		if(!StringUtils.isEmpty(this.feeType)){
			map.put("fee_type", this.feeType);
		}
		if(!StringUtils.isEmpty(this.goodsTag)){
			map.put("goods_tag", this.goodsTag);
		}
		if(!StringUtils.isEmpty(this.limitPay)){
			map.put("limit_pay", this.limitPay);
		}
		map.put("mch_id", this.mchId);
		map.put("nonce_str", this.nonceStr);
		map.put("notify_url", this.notifyUrl);
		
		if(!StringUtils.isEmpty(this.openid) && "JSAPI".equals(this.tradeType)){
			map.put("openid", this.openid);
		}
		map.put("out_trade_no", this.outTradeNo);
		
		if(!StringUtils.isEmpty(this.productId)){
			map.put("product_id", this.productId);
		}
		
		map.put("spbill_create_ip", this.spbillCreateIp);
		
		if(!StringUtils.isEmpty(this.timeExpire)){
			map.put("time_expire", this.timeExpire);
		}
		
		if(!StringUtils.isEmpty(this.timeStart)){
			map.put("time_start", this.timeStart);
		}
		map.put("total_fee", String.valueOf(this.totalFee));
		map.put("trade_type", this.tradeType);
		map.put("sign", this.sign);
		return map;
	}
	
}
