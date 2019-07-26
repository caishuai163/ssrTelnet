package net.tools;

public class Constants {
	/***
	 * 删除状态
	 */
	public static enum DeleteStatus {
		NORMAL("0", "NORMAL", "正常"), DELETE("1", "DELETE", "删除");
		private DeleteStatus(String value, String name, String desc) {
			this.value = value;
			this.name = name;
			this.desc = desc;
		}

		private final String value;
		private final String name;
		private final String desc;

		public String getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public String getDesc() {
			return desc;
		}
	}

	/***
	 * Result code
	 */
	public static enum ResultCode {
		/** 成功 */
		SUCCESS("200", "成功"), NULL_DATA("205", "无数据"),
		/** 没有登录 */
		NOT_LOGIN("400", "没有登录"),

		/** 发生异常 */
		EXCEPTION("401", "发生异常"),

		/** 系统错误 */
		SYS_ERROR("402", "系统错误"),

		/** 参数错误 */
		PARAMS_ERROR("403", "参数错误 "),

		/** 不支持或已经废弃 */
		NOT_SUPPORTED("410", "不支持或已经废弃"),

		/** AuthCode错误 */
		INVALID_AUTHCODE("444", "无效的AuthCode"),

		/** 太频繁的调用 */
		TOO_FREQUENT("445", "太频繁的调用"),

		/** 未知的错误 */
		UNKNOWN_ERROR("499", "未知错误");

		private ResultCode(String val, String msg) {
			this.val = val;
			this.msg = msg;
		}

		private String val;
		private String msg;

		public String val() {
			return val;
		}

		public String msg() {
			return msg;
		}

	}
}
