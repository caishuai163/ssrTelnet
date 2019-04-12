package net.tools;

import java.io.IOException;
import java.net.*;

public class PTUtil {
	/***
	 * ping����
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
				jsonResult.setMessage("ping���:the address is reachable.");
			} else {
				jsonResult.setCode(Constants.ResultCode.EXCEPTION);
				jsonResult.setMessage("ping���:the address is unreachable.");
			}
		} catch (UnknownHostException e) {
			jsonResult.setCode(Constants.ResultCode.EXCEPTION);
			jsonResult.setMessage("ping���:UnknownHostException:" + e.getMessage());
		} catch (IOException e) {
			jsonResult.setCode(Constants.ResultCode.EXCEPTION);
			jsonResult.setMessage("ping���:IOException:" + e.getMessage());
		}
		return jsonResult;
	}

	/***
	 * telnet ����
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
			jsonResult.setMessage("telnet���:success!");
		} catch (UnknownHostException e) {
			jsonResult.setCode(Constants.ResultCode.EXCEPTION);
			jsonResult.setMessage("telnet���:UnknownHostException:" + e.getMessage());
		} catch (IOException e) {
			jsonResult.setCode(Constants.ResultCode.EXCEPTION);
			jsonResult.setMessage("telnet���:IOException:" + e.getMessage());
		}
		return jsonResult;
	}
}