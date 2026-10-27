package com.assentinel.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:assentinel-test",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver"
})
class AsSentinelCoreApplicationTests {

	@Test
	void contextLoads() {
	}

}
