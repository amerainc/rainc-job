package com.rainc.job;

import com.rainc.job.core.util.IpUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

class RaincJobExecutorSamplesSpringbootApplicationTests {

	@Test
	void contextLoads() {
		String ip = IpUtil.getIp();
		System.out.println(ip);
	}

}
