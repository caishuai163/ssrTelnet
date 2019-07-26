package net.tools;

import java.io.IOException;
import java.net.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PTUtil {
	private static final Logger logger = LoggerFactory.getLogger(PTUtil.class);

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
			jsonResult.setMessage(
					"ping结果:UnknownHostException:" + e.getMessage());
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
	public static JsonResult telnetResult(String hostname, Integer port,
			Integer timeout) {
		JsonResult jsonResult = new JsonResult();
		try {
			Socket server = new Socket();
			InetSocketAddress address = new InetSocketAddress(hostname, port);
			server.connect(address, timeout);
			server.close();
			jsonResult.setMessage("telnet结果:success!");
		} catch (UnknownHostException e) {
			jsonResult.setCode(Constants.ResultCode.EXCEPTION);
			jsonResult.setMessage(
					"telnet结果:UnknownHostException:" + e.getMessage());
		} catch (IOException e) {
			jsonResult.setCode(Constants.ResultCode.EXCEPTION);
			jsonResult.setMessage("telnet结果:IOException:" + e.getMessage());
		}
		return jsonResult;
	}

	public static boolean isTelnetSuccess(String ip, int port) {
		logger.info("开始telnet {}:{}", ip, port);
		for (int i = 0; i < 10; i++) {
			JsonResult telnetResult = telnetResult(ip, port, 5000);
			logger.info("第{}次telnet {}:{} 结束。结果：{}", i + 1, ip, port,
					telnetResult);
			if (telnetResult.getCode()
					.equals(Constants.ResultCode.SUCCESS.val())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isPingSuccess(String ip) {
		logger.info("开始ping {}", ip);
		for (int i = 0; i < 10; i++) {
			JsonResult pingResult = PTUtil.pingResult(ip, 1000);
			logger.info("第{}次ping {} 结束，结果：{}", i + 1, ip, pingResult);
			if (pingResult.getCode()
					.equals(Constants.ResultCode.SUCCESS.val())) {
				return true;
			}
		}
		return false;
	}
}