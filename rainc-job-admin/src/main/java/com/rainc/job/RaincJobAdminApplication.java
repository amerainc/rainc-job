package com.rainc.job;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StopWatch;

/**
 * @author rainc
 */
@SpringBootApplication
@Log4j2
public class RaincJobAdminApplication {

	public static void main(String[] args) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		SpringApplication.run(RaincJobAdminApplication.class, args);
		stopWatch.stop();
		log.info("启动成功耗时：{}毫秒",stopWatch.getLastTaskTimeMillis());
	}

}
