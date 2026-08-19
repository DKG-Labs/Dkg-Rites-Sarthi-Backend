package com.sarthi;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Context load test disabled during CI/CD builds without live test database")
@SpringBootTest
class SarthiBackendApplicationTests {

    @Test
    void contextLoads() {
    }
}
