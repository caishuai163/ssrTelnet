package net.tools;

import java.io.IOException;
import java.net.*;

public class PTUtil {
	/***
	 * ping操作
	 * 
	 * @param hostname
	 * @param timeout
	 *            in milliseconds
	 * @return
	 */
	public static JsonResult pingResult(String hostname, Integer timeout) {
		JsonResult jsonResult = new JsonResult();
		try {
			InetAddress address = InetAddress.getByName(hostname);
			boolean flag = address.isReachable(timeout);
			if (flag) {
				jsonResult.setMessage("ping结果:the address is reachable.");
			} else {
				jsonResult.setCode(Constants.ResultCode.EXCEPTION);
				jsonResult.setMessage("ping结果:the address is unreachable.");
			}
		} catch (UnknownHostException e) {
			jsonResult.setCode(Constants.ResultCode.EXCEPTION);
			jsonResult.setMessage("ping结果:UnknownHostException:" + e.getMessage());
		} catch (IOException e) {
			jsonResult.setCode(Constants.ResultCode.EXCEPTION);
			jsonResult.setMessage("ping结果:IOException:" + e.getMessage());
		}
		return jsonResult;
	}

	/***
	 * telnet 操作
	 * 
	 * @param hostname
	 * @param timeout
	 *            in milliseconds
	 * @return
	 */
	public static JsonResult telnetResult(String hostname, Integer port, Integer timeout) {
		JsonResult jsonResult = new JsonResult();
		try {
			Socket server = new Socket();
			InetSocketAddress address = new InetSocketAddress(hostname, port);
			server.connect(address, timeout);
			server.close();
			jsonResult.setMessage("telnet结果:success!");
		} catch (UnknownHostException e) {
			jsonResult.setCode(Constants.ResultCode.EXCEPTION);
			jsonResult.setMessage("telnet结果:UnknownHostException:" + e.getMessage());
		} catch (IOException e) {
			jsonResult.setCode(Constants.ResultCode.EXCEPTION);
			jsonResult.setMessage("telnet结果:IOException:" + e.getMessage());
		}
		return jsonResult;
	}
}