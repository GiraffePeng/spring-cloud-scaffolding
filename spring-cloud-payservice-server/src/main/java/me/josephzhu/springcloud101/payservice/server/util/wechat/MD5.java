package me.josephzhu.springcloud101.payservice.server.util.wechat;

import java.security.MessageDigest;

/**
 * 
 * @author		: lrf
 * @Date		: 2015年7月28日
 */
public class MD5 {
    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "a", "b", "c", "d", "e", "f"};

    /**
     * 转换字节数组为16进制字串
     * @param b 字节数组
     * @return 16进制字串
     */
    public static String byteArrayToHexString(byte[] b) {
        StringBuilder resultSb = new StringBuilder();
        for (byte aB : b) {
            resultSb.append(byteToHexString(aB));
        }
        return resultSb.toString();
    }

    /**
     * 转换byte到16进制
     * @param b 要转换的byte
     * @return 16进制格式
     */
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    /**
     * MD5编码
     * @param origin 原始字符串
     * @return 经过MD5加密之后的结果
     */
    public static String MD5Encode(String origin) {
        String resultString = null;
        try {
            resultString = origin;
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(resultString.getBytes("UTF-8"));
            resultString = byteArrayToHexString(md.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultString;
    }
    
    public static void main(String[] args){
    	String key = "appid=wx6e6e0ab1c1c83bf6&attach=1&body=订单金额：1元&device_info=WEB&fee_type=CNY&goods_tag=WXG&key=8a2bda134f012150014f0121eb1c0000&limit_pay=no_credit&mch_id=1283203801&nonce_str=638548&notify_url=http://www.XXX.com/weixin/notify&openid=oGEVfuCZJ35CmAhhMi20m64u8BTc&out_trade_no=123&product_id=0&spbill_create_ip=1.202.87.96&time_expire=20170111175705&time_start=20170111173705&total_fee=1&trade_type=JSAPI&key=8a2bda134f012150014f0121eb1c0000";
    	
    	String sign = "BD87B90525D6FDC6CC948AA604AD313E";
    	
    	String sig1 = MD5Encode(key).toUpperCase();
    	System.out.println("sign == sig1 ?" + sign.equals(sig1));
    }
    
}
