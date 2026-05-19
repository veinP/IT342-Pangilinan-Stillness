package edu.cit.pangilinan.stillness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = {"edu.cit.pangilinan.stillness"})
@EnableAsync
public class StillnessApplication {

	public static void main(String[] args) {
		loadDotEnv();
		SpringApplication.run(StillnessApplication.class, args);
	}

	private static void loadDotEnv() {
		java.io.File dir = new java.io.File(".").getAbsoluteFile();
		java.io.File envFile = null;
		for (int i = 0; i < 5; i++) {
			java.io.File possible = new java.io.File(dir, ".env");
			if (possible.exists() && possible.isFile()) {
				envFile = possible;
				break;
			}
			dir = dir.getParentFile();
			if (dir == null) {
				break;
			}
		}

		if (envFile != null) {
			System.out.println("[StillnessApplication] Found .env file at: " + envFile.getAbsolutePath());
			try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(envFile))) {
				String line;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#")) {
						continue;
					}
					int eqIdx = line.indexOf('=');
					if (eqIdx > 0) {
						String key = line.substring(0, eqIdx).trim();
						String val = line.substring(eqIdx + 1).trim();
						// Only set if not already present in environment or system properties to allow overrides
						if (System.getenv(key) == null && System.getProperty(key) == null) {
							System.setProperty(key, val);
						}
					}
				}
			} catch (Exception e) {
				System.err.println("[StillnessApplication] Failed to load .env file: " + e.getMessage());
			}
		} else {
			System.out.println("[StillnessApplication] No .env file found in search path.");
		}
	}

}

