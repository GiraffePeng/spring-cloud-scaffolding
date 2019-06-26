package com.peng.payservice.server.util.wechat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import com.peng.common.Result;
import com.peng.payservice.dto.WxPayParamDTO;
import com.peng.payservice.dto.WxPayResultDTO;
import com.peng.payservice.dto.WxQueryParamDTO;
import com.peng.payservice.dto.WxQueryResultDTO;
import com.peng.payservice.properties.WeixinOrderApiProperty;
import com.peng.payservice.properties.WeixinOrderQueryProperty;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WxPayUtil {

	// 微信统一下单接口
	public final static String UNION_PAY = "https://api.mch.weixin.qq.com/pay/unifiedorder";

	// 微信查询订单信息接口
	public final static String UNION_QUERY = "https://api.mch.weixin.qq.com/pay/orderquery";

	// 后端异步回调成功返回消息
	public final static String SUCCESS_XML = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";

	// 后端回调失败返回消息
	public final static String FAIL_XML = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";

	private final static String CHARSET = "UTF-8";

	// 调用微信统一下单接口 获取 PrepayId
	public static String getPrepayId(WeixinOrderApiProperty property, String appKey) throws Exception {
		String url = property.assemblePrepayParamsForSign(appKey);

		property.setSign(MD5.MD5Encode(url).toUpperCase());
		log.info("微信支付参与签名的sign url{}", url);
		Map<String, String> map = property.assembleXmlMapForPrepayId();
		String xmlPost = getXmlString(map);
		log.info("调用统一下单接口 入参xml post:{}", xmlPost);
		String xmlResult = XmlHttpsRequest(UNION_PAY, xmlPost);
		String prepayId = "";
		log.info("调用统一下单接口 出参为:{}", xmlResult);
		if (xmlResult != null) {
			Map<String, String> resultMap;
			resultMap = XMLUtil.doXMLParse(xmlResult);
			String returnCode = resultMap.get("return_code");
			String resultCode = resultMap.get("result_code");
			if (returnCode.equals("SUCCESS") && resultCode.equals("SUCCESS")) {
				prepayId = resultMap.get("prepay_id");
			}
		}
		return prepayId;
	}

	// 封装微信支付 jsapi方式所需要的参数，然后以Result方式返回
	public static Result<WxPayResultDTO> wxPay(WxPayParamDTO paramDTO) {
		WxPayResultDTO wxPayResultDTO = null;
		try {
			WeixinOrderApiProperty property = new WeixinOrderApiProperty();
			BeanUtils.copyProperties(property, paramDTO);
			String prepayId = getPrepayId(property,paramDTO.getAppKey());
			String timestamp = String.valueOf(System.currentTimeMillis());
			String nonceStr = TextMessage.generateRandomCode(6);
			String packageStr = "prepay_id=" + prepayId;
			String signType = "MD5";

			String url = "appId=APPID&nonceStr=NONCESTR&package=PACKAGE&signType=SIGNTYPE&timeStamp=TIMESTAMP&key="
					+ paramDTO.getAppKey();
			url = url.replace("APPID", paramDTO.getAppid());
			url = url.replace("NONCESTR", nonceStr);
			url = url.replace("PACKAGE", packageStr);
			url = url.replace("TIMESTAMP", timestamp);
			url = url.replace("SIGNTYPE", signType);
			log.info("微信支付jsapi生成的签名url为:{}", url);
			String sign = MD5.MD5Encode(url).toUpperCase();

			log.info("微信支付jsapi生成的签名为{} ", sign);

			wxPayResultDTO = WxPayResultDTO.builder().appId(paramDTO.getAppid()).timeStamp(timestamp).nonceStr(nonceStr)
					.signType(signType).packages(packageStr).paySign(sign).ordCode(paramDTO.getOutTradeNo()).build();
		} catch (Exception e) {
			log.error("调用微信统一下单接口 拼装数据发生错误,错误信息:{},错误:{}", e.getMessage(), e);
			return new Result<WxPayResultDTO>(500, "调用微信支付失败");
		}
		return new Result<WxPayResultDTO>(wxPayResultDTO);
	}

	/**
	 * 该接口提供所有微信支付订单的查询，商户可以通过查询订单接口主动查询订单状态，完成下一步的业务逻辑。 需要调用查询接口的情况：
	 *  ◆ 当商户后台、网络、服务器等出现异常，商户系统最终未接收到支付通知；
     *  ◆ 调用支付接口后，返回系统错误或未知交易状态情况； 
     *  ◆调用付款码支付API，返回USERPAYING的状态； 
     *  ◆ 调用关单或撤销接口API之前，需确认支付状态；
	 */
	public static Result<WxQueryResultDTO> orderQuery(WxQueryParamDTO paramDTO) {
		WxQueryResultDTO wxQueryResultDTO = null;
		try {
			WeixinOrderQueryProperty property = new WeixinOrderQueryProperty();
			BeanUtils.copyProperties(property, paramDTO);
			String url = property.assemblePrepayParamsForSign(paramDTO.getApiKey());
			property.setSign(MD5.MD5Encode(url).toUpperCase());
			log.info("微信查询订单 参与签名的sign url{}", url);
			Map<String, String> map = property.assembleXmlMapForPrepayId();
			String xmlPost = getXmlString(map);
			log.info("微信查询订单接口 入参xml post:{}", xmlPost);
			String xmlResult = XmlHttpsRequest(UNION_QUERY, xmlPost);
			log.info("微信查询订单接口 出参为:{}", xmlResult);
			if (xmlResult != null) {
				Map<String, String> resultMap;
				resultMap = XMLUtil.doXMLParse(xmlResult);
				String returnCode = resultMap.get("return_code");
				String resultCode = resultMap.get("result_code");
				if (returnCode.equals("SUCCESS") && resultCode.equals("SUCCESS")) {
					wxQueryResultDTO = WxQueryResultDTO.builder().tradeState(resultMap.get("trade_state"))
							.isSubscribe(resultMap.get("is_subscribe")).openId(resultMap.get("openid"))
							.totalFee(Integer.parseInt(resultMap.get("total_fee")))
							.outTradeNo(resultMap.get("out_trade_no")).timeEnd(resultMap.get("time_end"))
							.transactionId(resultMap.get("transaction_id")).build();
				}
			}
		} catch (Exception e) {
			log.error("调用微信查询订单接口发生错误,错误信息:{},错误:{}", e.getMessage(), e);
			return new Result<WxQueryResultDTO>(500, "调用微信查询失败");
		}
		return new Result<WxQueryResultDTO>(wxQueryResultDTO);
	}

	public static String XmlHttpsRequest(String requestUrl, String postXmlString) {
		String xmlResult = null;
		StringBuffer buffer = new StringBuffer();
		try {
			// 创建SSLcontext管理器对像，使用我们指定的信任管理器初始化
			TrustManager[] tm = { new MyX509TrustManager() };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			SSLSocketFactory ssf = sslContext.getSocketFactory();

			URL url = new URL(requestUrl);
			HttpsURLConnection httpsUrlConn = (HttpsURLConnection) url.openConnection();
			httpsUrlConn.setSSLSocketFactory(ssf);
			httpsUrlConn.setDoInput(true);
			httpsUrlConn.setDoOutput(true);
			httpsUrlConn.setUseCaches(false);
			httpsUrlConn.setRequestMethod(RequestMethod.POST.name());
			// 设定传送的内容类型是可序列化的java对象(如果不设此项,在传送序列化对象时,当WEB服务默认的不是这种类型时可能抛java.io.EOFException)
			httpsUrlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			httpsUrlConn.setRequestProperty("Content-Length", postXmlString.length() + "");

			// 当有数据需要提交时
			if (postXmlString != null) {
				OutputStream outputStream = httpsUrlConn.getOutputStream();
				// 防止中文乱码
				outputStream.write(postXmlString.getBytes(CHARSET));
				outputStream.close();
				outputStream = null;
			}

			// 将返回的输入流转换成字符串
			InputStream inputStream = httpsUrlConn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, CHARSET);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}

			bufferedReader.close();
			inputStreamReader.close();

			inputStream.close();
			inputStream = null;

			httpsUrlConn.disconnect();
			xmlResult = buffer.toString();
		} catch (ConnectException ce) {
			log.error("Weixin server connection timed out.");
		} catch (Exception e) {
			log.error("https request error:{}", e);
		}
		return xmlResult;
	}

	/**
	 * @describe: 构造统一下单请求报文
	 */
	private static String getXmlString(Map<String, String> map) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<xml>");
		Iterator<Entry<String, String>> iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			String element = entry.getKey();
			buffer.append("<" + element + ">");
			buffer.append(entry.getValue());
			buffer.append("</" + element + ">");
		}

		buffer.append("</xml>");
		return buffer.toString();
	}

}
