plugins {
	java
	id("org.springframework.boot") version "3.5.5"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.diffplug.spotless") version "6.25.0"
}

group = "com.confido"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

springBoot {
    mainClass.set("com.confido.api.ApiApplication")
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

spotless {
    java {
        // Use Google Java Style
        googleJavaFormat()

        // 🚀 Automatically remove unused imports
        removeUnusedImports()

        // Optional: apply custom import ordering
        importOrder("java", "javax", "org", "com")
    }
}