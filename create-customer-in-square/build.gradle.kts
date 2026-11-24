plugins {
    java
    id("org.springframework.boot") version "4.2.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.cedarmeadowmeats.order-workflow.create-customer-in-square"
version = "0.0.2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2026.0.0"
extra["awsSdkBom"] = "2.59.8"
extra["awsLambdaJavaCore"] = "1.4.1"
extra["awsLambdaJavaEvents"] = "3.16.1"
extra["awsLambdaJavaSerialization"] = "1.4.1"
extra["awsLambdaJavaTests"] = "1.1.2"

dependencies {
    developmentOnly("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.cloud:spring-cloud-starter-function-web")
    implementation("org.springframework.cloud:spring-cloud-function-adapter-aws")
    implementation("com.amazonaws:aws-lambda-java-core:${property("awsLambdaJavaCore")}")
    implementation("com.amazonaws:aws-lambda-java-events:${property("awsLambdaJavaEvents")}")
    implementation("com.amazonaws:aws-lambda-java-serialization:${property("awsLambdaJavaSerialization")}")
    implementation("com.squareup:square:48.1.0.20261118")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.amazonaws:aws-lambda-java-tests:${property("awsLambdaJavaTests")}")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    // HTML reports create long, bracketed file names that break `sam build` temp cleanup on Windows
    reports.html.required = false
}
