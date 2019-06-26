package com.peng.payservice.server.util.wechat;

import java.util.Random;

public class TextMessage {
	
	/**
	 * 获取随机数
	 * @param length
	 * @return
	 */
	public static String generateRandomCode(int length){
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
        	builder.append(random.nextInt(10));
        }
        return builder.toString();
    }
	
}
