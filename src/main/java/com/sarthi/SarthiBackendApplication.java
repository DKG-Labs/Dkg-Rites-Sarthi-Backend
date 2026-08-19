package com.sarthi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableCaching
public class SarthiBackendApplication {

	public static void main(String[] args) {
		loadDotEnv();
		SpringApplication.run(SarthiBackendApplication.class, args);
	}

	private static void loadDotEnv() {
		File envFile = new File(".env");
		if (envFile.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
				String line;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
						int idx = line.indexOf('=');
						String key = line.substring(0, idx).trim();
						String value = line.substring(idx + 1).trim();
						if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
							if (value.length() >= 2) {
								value = value.substring(1, value.length() - 1);
							}
						}
						System.setProperty(key, value);
						if (key.toLowerCase().contains("url") || key.toLowerCase().contains("username")) {
							System.out.println("[Sarthi-Env] " + key + " = " + value);
						}
					}
				}
			} catch (Exception e) {
				System.err.println("[Sarthi-Env] Error reading .env: " + e.getMessage());
			}
		} else {
			System.out.println("[Sarthi-Env] .env file not found at " + envFile.getAbsolutePath());
		}
	}

}
