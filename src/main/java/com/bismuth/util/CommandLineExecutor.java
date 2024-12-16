package com.bismuth.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandLineExecutor {
	private static final Logger logger = LogManager.getLogger(CommandLineExecutor.class);

	public static String execute(String cmd) {
		Process process = null;
		StringBuffer successOutput = new StringBuffer();
		StringBuffer errorOutput = new StringBuffer();
		BufferedReader successBufferReader = null;
		BufferedReader errorBufferReader = null;
		String msg = null;
		List<String> cmdList = new ArrayList<String>();
		String rtnValue = "";
		Runtime runtime = Runtime.getRuntime();
		
		if(System.getProperty("os.name").indexOf("Windows") > -1) {
			cmdList.add("cmd");
			cmdList.add("/c");
		} else {
			cmdList.add("/bin/sh");
			cmdList.add("-c");
		}

		logger.info("CommandLineExecutor cmd : " + cmd);
		
		cmdList.add(cmd);
		
		String[] array = (String[]) cmdList.toArray(new String[cmdList.size()]);

		try {
			process = runtime.exec(array);
			successBufferReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "EUC-KR"));

			while((msg = successBufferReader.readLine()) != null) {
				successOutput.append(msg + System.getProperty("line.separator"));
			}

			errorBufferReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "EUC-KR"));

			while((msg = errorBufferReader.readLine()) != null) {
				errorOutput.append(msg + System.getProperty("line.separator"));
			}

			process.waitFor();
			
			if(process.exitValue() == 0) {
				logger.info("성공");
				logger.info(successOutput.toString());
				
				rtnValue = successOutput.toString();
			} else {
				logger.info("비정상 종료");
				logger.info(errorOutput.toString());
				
				rtnValue = errorOutput.toString();
			}

			if(!errorOutput.toString().isEmpty()) {
				logger.info("오류");
				logger.info(errorOutput.toString());
				
				rtnValue = errorOutput.toString();
			}
		} catch(IOException e) {
			e.printStackTrace();
		} catch(InterruptedException e1) {
			e1.printStackTrace();
		} finally {
			try {
				process.destroy();
				
				if(successBufferReader != null) {
					successBufferReader.close();
				}

				if(errorBufferReader != null) {
					errorBufferReader.close();
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		return rtnValue;
	}

	public static String transferExecute(String cmd, Map<String, String> serverNames) {
		Process process = null;
		StringBuffer successOutput = new StringBuffer();
		StringBuffer errorOutput = new StringBuffer();
		BufferedReader successBufferReader = null;
		BufferedReader errorBufferReader = null;
		String msg = null;
		List<String> cmdList = new ArrayList<String>();
		String rtnValue = "";
		Runtime runtime = Runtime.getRuntime();
		
		if(System.getProperty("os.name").indexOf("Windows") > -1) {
			cmdList.add("cmd");
			cmdList.add("/c");
		} else {
			cmdList.add("/bin/sh");
			cmdList.add("-c");
		}

		logger.info("CommandLineExecutor cmd : " + cmd);
		
		cmdList.add(cmd);
		
		String[] array = (String[]) cmdList.toArray(new String[cmdList.size()]);

		try {
			process = runtime.exec(array);

			for(successBufferReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "EUC-KR")); (msg = successBufferReader.readLine()) != null; successOutput.append(msg + System.getProperty("line.separator"))) {
				if(!serverNames.isEmpty() && msg.contains("ex=")) {
					int cnt = msg.indexOf("ex=");
					String tab = msg.substring(0, cnt);
					String value = msg.substring(cnt + 4, msg.length() - 2);
					String exName = (String) serverNames.get(value.trim());
					
					if(exName != null && !"".equals(exName)) {
						msg = msg.replace(msg, tab + "ex=" + exName + ";");
					}
				}
			}

			errorBufferReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "EUC-KR"));

			while((msg = errorBufferReader.readLine()) != null) {
				errorOutput.append(msg + System.getProperty("line.separator"));
			}

			process.waitFor();
			
			if(process.exitValue() == 0) {
				logger.info("성공");
				logger.info(successOutput.toString());
				
				rtnValue = successOutput.toString();
			} else {
				logger.info("비정상 종료");
				logger.info(errorOutput.toString());
				
				rtnValue = errorOutput.toString();
			}

			if(!errorOutput.toString().isEmpty()) {
				logger.info("오류");
				logger.info(errorOutput.toString());
				
				rtnValue = errorOutput.toString();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} finally {
			try {
				process.destroy();
				
				if(successBufferReader != null) {
					successBufferReader.close();
				}

				if(errorBufferReader != null) {
					errorBufferReader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return rtnValue;
	}

	public static Map<String, Object> executeYes(String cmd) {
		Process process = null;
		Map<String, Object> output = new HashMap<String, Object>();
		List<String> cmdList = new ArrayList<String>();
		
		if(System.getProperty("os.name").indexOf("Windows") > -1) {
			cmdList.add("cmd");
			cmdList.add("/c");
		} else {
			cmdList.add("/bin/sh");
			cmdList.add("-c");
		}

		logger.debug("executeYes cmd : " + cmd);
		
		cmdList.add(cmd);
		
		String[] array = (String[]) cmdList.toArray(new String[cmdList.size()]);

		try {
			ProcessBuilder pb = new ProcessBuilder(new String[0]);
			
			pb.command(array);
			process = pb.start();
			
			OutputStream stdOutput = process.getOutputStream();
			InputStream inputStream = process.getInputStream();
			InputStream errorStream = process.getErrorStream();
			ThreadedStreamHandler inputStreamHandler = new ThreadedStreamHandler(inputStream, stdOutput, "y");
			ThreadedStreamHandler errorStreamHandler = new ThreadedStreamHandler(errorStream);
			
			inputStreamHandler.start();
			errorStreamHandler.start();
			
			int exitValue = process.waitFor();
			
			inputStreamHandler.interrupt();
			errorStreamHandler.interrupt();
			inputStreamHandler.join();
			errorStreamHandler.join();
			
			if(exitValue == 0) {
				output.put("result", "success");
				output.put("resultValue", inputStreamHandler.getOutputBuffer());
				
				logger.info("성공");
				logger.debug(inputStreamHandler.getOutputBuffer().toString());
			} else {
				output.put("result", "fail");
				output.put("resultValue", errorStreamHandler.getOutputBuffer());
				
				logger.info("에러");
				logger.info(errorStreamHandler.getOutputBuffer().toString());
			}
		} catch(IOException e) {
			e.printStackTrace();
		} catch(InterruptedException e1) {
			e1.printStackTrace();
		} finally {
			process.destroy();
		}

		return output;
	}

	public static Map<String, Object> executeUnixJob(String cmd) {
		Process process = null;
		Map<String, Object> output = new HashMap<String, Object>();
		StringBuffer successOutput = new StringBuffer();
		StringBuffer errorOutput = new StringBuffer();
		BufferedReader successBufferReader = null;
		BufferedReader errorBufferReader = null;
		String msg = null;
		List<String> cmdList = new ArrayList<String>();
		Runtime runtime = Runtime.getRuntime();
		
		if(System.getProperty("os.name").indexOf("Windows") > -1) {
			cmdList.add("cmd");
			cmdList.add("/c");
		} else {
			cmdList.add("/bin/sh");
			cmdList.add("-c");
		}

		logger.debug("CommandLineExecutor cmd : " + cmd);
		
		cmdList.add(cmd);
		
		String[] array = (String[]) cmdList.toArray(new String[cmdList.size()]);

		try {
			process = runtime.exec(array);
			successBufferReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "EUC-KR"));

			while((msg = successBufferReader.readLine()) != null) {
				successOutput.append(msg + System.getProperty("line.separator"));
			}

			errorBufferReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "EUC-KR"));

			while((msg = errorBufferReader.readLine()) != null) {
				errorOutput.append(msg + System.getProperty("line.separator"));
			}

			process.waitFor();
			
			if(process.exitValue() == 0) {
				output.put("result", "success");
				output.put("resultValue", successOutput);
				
				logger.info("성공");
				logger.debug(successOutput.toString());
			} else {
				output.put("result", "fail");
				output.put("resultValue", errorOutput);
				
				logger.info("에러");
				logger.info(errorOutput.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} finally {
			try {
				process.destroy();
				
				if(successBufferReader != null) {
					successBufferReader.close();
				}

				if(errorBufferReader != null) {
					errorBufferReader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return output;
	}
}