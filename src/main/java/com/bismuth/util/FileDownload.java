package com.bismuth.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileDownload {
	public static void download(String filePath, String fileName, String strUnitInfo) throws IOException {
		File dir = null;
		File newFile = null;
		StringBuilder sb = new StringBuilder();
		BufferedWriter bw = null;

		try {
			dir = new File(filePath);
			
			if(!dir.exists()) {
				dir.mkdirs();
			}

			newFile = new File(sb.append(filePath).append(File.separator).append(fileName).toString());
			bw = new BufferedWriter(new FileWriter(newFile));
			bw.write(strUnitInfo.toString());
		} catch(IOException e) {
			newFile.delete();
			
			if(dir.listFiles() != null && dir.listFiles().length == 0) {
				dir.delete();
			}

			throw e;
		} finally {
			if(bw != null) {
				bw.close();
			}
		}
	}

	public static void deleteFile(String filePath, String fileName) {
		File deleteFile = null;
		StringBuilder sb = new StringBuilder();

		try {
			deleteFile = new File(sb.append(filePath).append(File.separator).append(fileName).toString());
			
			if(deleteFile.exists()) {
				deleteFile.delete();
			}

		} catch (Exception e) {
			throw e;
		}
	}
	
	public static void createJobGroupFile(String serviceName, String prePath, String jobGroup) throws IOException {
		StringBuilder command = new StringBuilder();
		String migrationTemp = PropertyUtil.getProperty("migrationTemp");

		File dir = null;
		File newFile = null;
		StringBuilder sb = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		BufferedWriter bw = null;

		try {
			dir = new File(sb.append((new File("")).getCanonicalPath()).append(migrationTemp).append(prePath).toString());

			if(!dir.exists()) {
				dir.mkdirs();
			}

			newFile = new File(sb2.append((new File("")).getCanonicalPath()).append(migrationTemp).append(prePath).append(File.separator).append(jobGroup + ".dmp").toString());
			bw = new BufferedWriter(new FileWriter(newFile));
			bw.write("unit=" + jobGroup + ",,jp1user,;\n");
			bw.write("{\n");
			bw.write("        ty=g;        \n");
			bw.write("}\n");
			bw.close();

			command.append("ajsdefine -F ").append(serviceName);
			command.append(" -S -p -i -d ").append(prePath).append(" ");
			command.append((new File("")).getCanonicalPath()).append(migrationTemp).append(prePath).append(File.separator).append(jobGroup + ".dmp").toString();

			CommandLineExecutor.executeYes(command.toString());
		} catch (IOException e) {
			newFile.delete();

			if((dir.listFiles() != null) && (dir.listFiles().length == 0)) {
				dir.delete();
			}

			throw e;
		} finally {
			if(bw != null) {
				bw.close();
			}
		}
	}
}