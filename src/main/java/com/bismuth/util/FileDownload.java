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
}