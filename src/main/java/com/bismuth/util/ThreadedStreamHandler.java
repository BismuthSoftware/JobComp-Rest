package com.bismuth.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

class ThreadedStreamHandler extends Thread {
	InputStream inputStream;
	
	String adminPassword;
	
	OutputStream outputStream;
	
	PrintWriter printWriter;
	
	StringBuilder outputBuffer = new StringBuilder();
	
	private boolean sudoIsRequested = false;

	ThreadedStreamHandler(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	ThreadedStreamHandler(InputStream inputStream, OutputStream outputStream, String adminPassword) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.printWriter = new PrintWriter(outputStream);
		this.adminPassword = adminPassword;
		this.sudoIsRequested = true;
	}

	public void run() {
		if(this.sudoIsRequested) {
			this.printWriter.println(this.adminPassword);
			this.printWriter.flush();
		}

		BufferedReader bufferedReader = null;

		try {
			bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));
			String line = null;

			while((line = bufferedReader.readLine()) != null) {
				this.outputBuffer.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Throwable e1) {
			e1.printStackTrace();
		} finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {
			}
		}
	}

	@SuppressWarnings("unused")
	private void doSleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}

	}

	public StringBuilder getOutputBuffer() {
		return this.outputBuffer;
	}
}