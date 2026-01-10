plugins {
    java
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.cedarmeadowmeats.order-workflow.send-email-confirmation"
version = "0.0.2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2023.0.3"
extra["awsSdkBom"] = "2.31.27"
extra["awsLambdaJavaCore"] = "1.2.3"
extra["awsLambdaJavaEvents"] = "3.11.4"
extra["awsLambdaJavaSerialization"] = "1.1.5"
extra["awsLambdaJavaTests"] = "1.1.1"

dependencies {
    implementation(platform("software.amazon.awssdk:bom:${property("awsSdkBom")}"))
    implementation("software.amazon.awssdk:sesv2")
    developmentOnly("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.cloud:spring-cloud-starter-function-web")
    implementation("org.springframework.cloud:spring-cloud-function-adapter-aws")
    implementation("com.amazonaws:aws-lambda-java-core:${property("awsLambdaJavaCore")}")
    implementation("com.amazonaws:aws-lambda-java-events:${property("awsLambdaJavaEvents")}")
    implementation("com.amazonaws:aws-lambda-java-serialization:${property("awsLambdaJavaSerialization")}")
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
}
