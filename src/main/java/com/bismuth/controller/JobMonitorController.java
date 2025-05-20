package com.bismuth.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.bismuth.util.CommandLineExecutor;
import com.bismuth.util.FileDownload;
import com.bismuth.util.PropertyUtil;
import com.bismuth.util.constant.ConstKey;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobMonitorController {
	@RequestMapping(value = {"/jobMonitor"}, method = {RequestMethod.GET})
	public List<String> jobTree() {
		List<String> commandList = new ArrayList<String>();
		Map<String, String> serviceList = PropertyUtil.getProperties("rootfolder");
		StringBuilder command = new StringBuilder();
		Iterator<Entry<String, String>> localIterator = serviceList.entrySet().iterator();

		if(localIterator.hasNext()) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) localIterator.next();
			String serviceName = (String) entry.getValue();

			command.setLength(0);
			command.append("ajsprint -F ").append(serviceName);
			command.append(" -f \"%jn^%cm\" -G ").append("\"/*\"");

			String groupList = CommandLineExecutor.execute(command.toString());

			commandList.add(groupList);
			command.setLength(0);
			command.append("ajsshow -F ").append(serviceName);
			command.append(" -f \"%j^%p^%C^%d^%s^%k^%e\" -E ").append("\"/*\"");

			String jobExeList = CommandLineExecutor.execute(command.toString());

			commandList.add(jobExeList);
			command.setLength(0);
			command.append("ajsprint -F ").append(serviceName);
			command.append(" -f \"%jn^%cm\" -N ").append("\"/*\"");

			String jobAllList = CommandLineExecutor.execute(command.toString());

			commandList.add(jobAllList);
		}

		return commandList;
	}

	@RequestMapping(value = {"/jobGroupNetInfo"}, method = {RequestMethod.POST})
	public List<String> remoteJobGroupNetInfo(String serviceName, String path) {
		List<String> commandList = new ArrayList<String>();
		StringBuilder command = new StringBuilder();

		command.append("ajsprint -F ").append(serviceName);
		command.append(" -f \"%jn^%cm\" -G ").append("\"" + path + "\"");

		String jobGroupList = CommandLineExecutor.execute(command.toString());

		commandList.add(jobGroupList);
		command.setLength(0);
		command.append("ajsshow -F ").append(serviceName);
		command.append(" -f \"%j^%p^%C^%d^%s^%k^%e\" -E ").append("\"" + path + "\"");

		String jobExeList = CommandLineExecutor.execute(command.toString());

		commandList.add(jobExeList);
		command.setLength(0);
		command.append("ajsprint -F ").append(serviceName);
		command.append(" -f \"%jn^%cm\" -N ").append("\"" + path + "\"");

		String jobAllList = CommandLineExecutor.execute(command.toString());

		commandList.add(jobAllList);

		return commandList;
	}

	@RequestMapping(value = {"/transferRootJobNet"}, method = {RequestMethod.POST})
	public List<String> remoteRootJobNetInfo(@RequestParam("serviceName") String serviceName, @RequestParam("path") String path, @RequestParam("sorceNames") List<String> sorceNames, @RequestParam("targetNames") List<String> targetNames) {
		List<String> commandList = new ArrayList<String>();
		StringBuilder command = new StringBuilder();
		
		System.out.println("PATH : " + path);
		
		Map<String, String> ServerNames = new HashMap<String, String>();

		String sorceName;
		
		for(int i = 0; i < sorceNames.size(); i++) {
			sorceName = (String) sorceNames.get(i);
			
			String targetName = (String) targetNames.get(i);
			
			if(!sorceName.contains("[") && !sorceName.contains("]")) {
				ServerNames.put(sorceName, targetName);
			} else {
				ServerNames.put(sorceName.replace("[", "").replace("]", ""), targetName.replace("[", "").replace("]", ""));
			}
		}

		command.append("ajsprint -F ").append(serviceName);
		command.append(" -N ").append("\"" + path + "\"");
		
		String jobRootJobNet = CommandLineExecutor.transferExecute(command.toString(), ServerNames);
		
		commandList.add(jobRootJobNet);
		sorceName = this.getJobResourceGroup(serviceName, path);
		commandList.add(sorceName);
		
		return commandList;
	}

	@RequestMapping(value = {"/transferRootJobNetDev"}, method = {RequestMethod.POST})
	public String transferRootJobNetDev(String unit, String serviceName, String path, StringBuffer rootJobNetInfo) throws IOException {
		System.out.println("serviceName : " + serviceName);
		System.out.println("PATH : " + path);
		System.out.println("unit : " + unit);
		System.out.println("rootJobNetInfo : " + rootJobNetInfo.toString());
		
		StringBuilder filePath = new StringBuilder();
		
		filePath.append((new File("")).getCanonicalPath()).append(PropertyUtil.getProperty("filepath"));
		
		StringBuilder fileName = new StringBuilder();
		
		fileName.append(unit).append("-");
		fileName.append(this.getCurrentDate("yyyyMMddHHmmss")).append(".dmp");
		
		FileDownload.download(filePath.toString(), fileName.toString(), rootJobNetInfo.toString());
		StringBuilder command = new StringBuilder();
		
		command.append("ajsdefine -F ").append(serviceName);
		command.append(" -S -p -i -d ").append(path).append(" ");
		command.append(filePath.toString()).append(File.separator).append(fileName);
		
		System.out.println("command : " + command.toString());
		
		Map<String, Object> output = CommandLineExecutor.executeYes(command.toString());
		String result = (String) output.get("result");
		System.out.println("result : " + result);
		
		command.setLength(0);
		command.append("ajschange -F ").append(serviceName);
		command.append(" -o jp1user -R ").append(path);
		command.append(unit);
		
		String resultValue = CommandLineExecutor.execute(command.toString());
		
		System.out.println("transferRootJobNetDev 소유자를 jp1user로 변경 : " + resultValue);
		
		command.setLength(0);
		command.append("ajschange -F ").append(serviceName);
		command.append(" -m 4444 -J -R ").append(path);
		command.append(unit);
		resultValue = CommandLineExecutor.execute(command.toString());
		
		System.out.println("transferRootJobNetDev 소유자 변경 : " + resultValue);
		
		return result;
	}

	@RequestMapping(value = {"/transferRootJobNetDevtoDev"}, method = {RequestMethod.POST})
	public String transferRootJobNetDevtoDev(String sourceServiceName, String targetServiceName, String unit, String path) throws IOException {
		String transferPath = "";

		if("".equals(path)) {
			path = "/";
			transferPath = path + unit;
		} else {
			transferPath = path + "/" + unit;
		}

		System.out.println("transferPath : " + transferPath);

		StringBuilder command = new StringBuilder();

		command.append("ajsprint -F ").append(sourceServiceName);
		command.append(" -N ").append("\"" + transferPath + "\"");

		System.out.println("transferRootJobNetDevtoDev command : " + command.toString());

		String jobNetList = CommandLineExecutor.execute(command.toString());
		StringBuilder filePath = new StringBuilder();

		filePath.append((new File("")).getCanonicalPath()).append(PropertyUtil.getProperty("filepath"));

		StringBuilder fileName = new StringBuilder();

		fileName.append(unit).append("-");
		fileName.append(this.getCurrentDate("yyyyMMddHHmmss")).append(".dmp");

		FileDownload.download(filePath.toString(), fileName.toString(), jobNetList);
		String result = this.transferCommand(targetServiceName, path, unit, filePath.toString(), fileName.toString());

		return result;
	}

	private String getJobResourceGroup(String serviceName, String path) {
		System.out.println("path : " + path);
		
		StringBuilder command = new StringBuilder();
		
		command.append("ajsprint -F ").append(serviceName);
		command.append(" -f \"%gr\" -N ").append("\"" + path + "\"");
		
		System.out.println("getJobResourceGroup command : " + command.toString());
		
		String resourceGroup = CommandLineExecutor.execute(command.toString());
		
		return resourceGroup;
	}

	private String transferCommand(String serviceName, String path, String unit, String filePath, String fileName) throws IOException {
		StringBuilder command = new StringBuilder();
		String unitPath = "";

		if("/".equals(path)) {
			unitPath = unit;
		} else {
			unitPath = "/" + unit;
		}
		
		command.setLength(0);
		command.append("ajsprint -F ").append(serviceName);
		command.append(" -f \"%JN\" ").append(path);
		
		String checkPath = CommandLineExecutor.execute(command.toString());
		
		if(checkPath.contains("KAVS0161-I") && !"/".equals(path)) {			
			this.checkJobGroup(serviceName, path);
		}
		
		boolean runFlag = true;

		while(runFlag) {
			runFlag = false;
			command.setLength(0);
			command.append("ajskill -F ").append(serviceName);
			command.append(" -T ").append(path);
			command.append(unitPath);

			System.out.println("transferCommand command : " + command.toString());

			String killOutput = CommandLineExecutor.execute(command.toString());

			System.out.println("killOutput : " + killOutput);

			command.setLength(0);
			command.append("ajsshow -F ").append(serviceName);
			command.append(" -f %C -RE ").append(path);
			command.append(unitPath);

			System.out.println("transferCommand command : " + command.toString());

			String resultValue = CommandLineExecutor.execute(command.toString());

			System.out.println("resultValue : " + resultValue);

			if(resultValue != null) {
				String[] killResultValues = resultValue.toString().split(ConstKey.FILE_NEWLINE);

				for(String value : killResultValues) {
					if(value.contains("running")) {
						runFlag = true;
						break;
					}
				}
			}
	    }

	    command.setLength(0);
	    command.append("ajsleave -F ").append(serviceName);
	    command.append(" -T ").append(path);
	    command.append(unitPath);

	    System.out.println("transferCommand command : " + command.toString());

	    String resultValue = CommandLineExecutor.execute(command.toString());

	    System.out.println("resultValue : " + resultValue);

	    command.setLength(0);
	    command.append("ajsdefine -F ").append(serviceName);
	    command.append(" -S -p -i -d ").append(path).append(" ");
	    command.append(filePath).append(File.separator).append(fileName);

	    System.out.println("transferCommand command : " + command.toString());

	    Map<String, Object> output = CommandLineExecutor.executeYes(command.toString());

	    String result = (String)output.get("result");

	    System.out.println("result : " + result);

	    return result;
	}

	private String getCurrentDate(String pattern) {
		String now = "";
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat(pattern);

		now = format.format(date);

		return now;
	}
	
	private void checkJobGroup(String serviceName, String path) throws IOException {
		StringBuilder command = new StringBuilder();
		String[] result1 = path.split("/");		
		String currentPath = "";
		String prePath = "";

		for(int i = 1; i < result1.length; i++) {
			prePath = i == 1 ? "/" : currentPath;
			currentPath = currentPath + "/";
			currentPath = currentPath + result1[i];

			command.setLength(0);
			command.append("ajsprint -F ").append(serviceName);
			command.append(" ");
			command.append(currentPath);
			String result = CommandLineExecutor.execute(command.toString());			
			
			if(result.contains("KAVS0161-I")) {					
				FileDownload.createJobGroupFile(serviceName, prePath, result1[i]);
			}
		}
	}
}