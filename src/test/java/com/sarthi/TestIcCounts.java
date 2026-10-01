package com.sarthi;

import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestIcCounts implements CommandLineRunner {

    @Autowired
    private SleeperWorkflowRepository sleeperWorkflowRepository;

    @Override
    public void run(String... args) throws Exception {
        try {
            Long count = sleeperWorkflowRepository.countSleeperIcIssuedFiltered(null, null, null, null);
            System.out.println("IC COUNT SUCCESS: " + count);
        } catch (Exception e) {
            System.out.println("IC COUNT ERROR:");
            e.printStackTrace();
        }
    }
}
