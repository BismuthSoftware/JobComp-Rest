package com.bismuth;

import com.bismuth.util.PropertyUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JCompRestApplication {
	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(new Class[]{ JCompRestApplication.class });		
		application.addListeners(new ApplicationListener[]{new ApplicationPidFileWriter("./bin/shutdown.pid")});
		application.run(args);
	}

	@Bean
	void init() throws Exception {
		PropertyUtil.loadDbConfig();
	}
}