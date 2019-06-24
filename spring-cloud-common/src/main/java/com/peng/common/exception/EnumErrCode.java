package com.peng.common.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @describe: 业务异常编码
 * @version 1.0
 */
public enum EnumErrCode {

	SYSMC_CODE_1001(1001,"测试异常demo");

	@Getter
	@Setter
	private int code;

	@Getter
	@Setter
	private String desc;

	EnumErrCode(int code, String desc){
		this.code = code;
		this.desc = desc;
	}
}
