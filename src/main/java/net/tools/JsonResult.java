package net.tools;

public class JsonResult {

	private String code;// 结果编码
	private String message;// 结果描述
	private Object data;

	public JsonResult() {
		this.setCode(Constants.ResultCode.SUCCESS);
		this.setMessage(Constants.ResultCode.SUCCESS.msg());

	}

	public JsonResult(Constants.ResultCode code) {
		this.setCode(code);
		this.setMessage(code.msg());
	}

	public JsonResult(Constants.ResultCode code, String message) {
		this.setCode(code);
		this.setMessage(message);
	}

	public JsonResult(Constants.ResultCode code, String message, Object data) {
		this.setCode(code);
		this.setMessage(message);
		this.setData(data);
	}

	public String getCode() {
		return code;
	}

	public void setCode(Constants.ResultCode code) {
		this.code = code.val();
		this.message = code.msg();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "JsonResult [code=" + code + ", message=" + message + ", data=" + data + "]";
	}

}