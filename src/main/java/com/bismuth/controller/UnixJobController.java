package com.bismuth.controller;

import java.util.Map;
import com.bismuth.util.CommandLineExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UnixJobController {
	private static final Logger logger = LogManager.getLogger(UnixJobController.class);

	@RequestMapping(value = {"/unixJobExecution"}, method = {RequestMethod.POST})
	public synchronized String unixJobExecution(@RequestParam("serviceName") String serviceName, @RequestParam("path") String path, @RequestParam("prm") String prm) {
		StringBuilder command = new StringBuilder();
		String result = "";
		
		command.append("ajsprint -F ").append(serviceName);
		command.append(" -f %pm ");
		command.append(path);
		
		Map<String, Object> output = CommandLineExecutor.executeUnixJob(command.toString());
		
		result = (String) output.get("result");
		
		String pramValue = output.get("resultValue").toString();
		
		pramValue = pramValue.replace("$", "\\$").trim();
		
		logger.info("unixJobExecution prm : " + prm);
		logger.info("unixJobExecution pramValue : " + pramValue);
		
		if(!"success".equals(result)) {
			if(pramValue.contains("KAVS0161-I")) {
				result = "91";
			} else {
				result = "99";
			}

			return result;
		} else {
			command.setLength(0);
			command.append("ajschgjob -F ").append(serviceName);
			command.append(" -pm ").append("\"" + prm + "\"").append(" ");
			command.append(path);
			output = CommandLineExecutor.executeUnixJob(command.toString());
			result = (String) output.get("result");
			
			if("success".equals(result)) {
				String rootJobPath = path.substring(0, path.lastIndexOf("/"));
				
				command.setLength(0);
				command.append("ajsentry -F ").append(serviceName);
				command.append(" -n -T ").append(rootJobPath);
				output = CommandLineExecutor.executeUnixJob(command.toString());
				result = (String) output.get("result");
				
				String resultValue = output.get("resultValue").toString();
				
				if("success".equals(result)) {
					result = "00";
				} else if(resultValue.contains("KAVS0161-I")) {
					result = "91";
				} else if(resultValue.contains("KAVS0681-E")) {
					result = "92";
				} else {
					result = "99";
				}

				command.setLength(0);
				command.append("ajschgjob -F ").append(serviceName);
				command.append(" -pm ").append("\"" + pramValue + "\"").append(" ");
				command.append(path);
				output = CommandLineExecutor.executeUnixJob(command.toString());
				
				logger.info("unixJobExecution result pramValue : " + pramValue);
			} else {
				String resultValue = output.get("resultValue").toString();
				
				if(resultValue.contains("KAVS0161-I")) {
					result = "91";
				} else {
					result = "99";
				}
			}

			return result;
		}
	}
}