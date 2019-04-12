package net.tools;

public class Constants {
	/***
	 * ɾ��״̬
	 */
	public static enum DeleteStatus {
		NORMAL("0", "NORMAL", "����"), DELETE("1", "DELETE", "ɾ��");
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
		/** �ɹ� */
		SUCCESS("200", "�ɹ�"), NULL_DATA("205", "������"),
		/** û�е�¼ */
		NOT_LOGIN("400", "û�е�¼"),

		/** �����쳣 */
		EXCEPTION("401", "�����쳣"),

		/** ϵͳ���� */
		SYS_ERROR("402", "ϵͳ����"),

		/** �������� */
		PARAMS_ERROR("403", "�������� "),

		/** ��֧�ֻ��Ѿ����� */
		NOT_SUPPORTED("410", "��֧�ֻ��Ѿ�����"),

		/** AuthCode���� */
		INVALID_AUTHCODE("444", "��Ч��AuthCode"),

		/** ̫Ƶ���ĵ��� */
		TOO_FREQUENT("445", "̫Ƶ���ĵ���"),

		/** δ֪�Ĵ��� */
		UNKNOWN_ERROR("499", "δ֪����");

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
