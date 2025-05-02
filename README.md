# How to complete this project

### Stage 1 Write and Compile a Simple Java Demo
1.  Print line `Hello from Docker!` in Main.java
```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, Docker!");
    }
}
```
From the root directory of the project:
2. Create `build` directory
```bash
mkdir -p build
```
3. Build the project
```bash
javac -d build src/main/java/project/Main.java
```
### Stage 2 Create a Manifest File
1. Create MANIFEST.MF file in the `resources` directory with content inside:
```text
Manifest-Version: 1.0
Main-Class: project.Main
```
2. Copy MANIFEST.MF file to the `build` directory
```bash
cp src/main/resources/META-INF/MANIFEST.MF build/MANIFEST.MF
```
### Stage 3 Build an Executable JAR File
1. Navigate to the `build` directory
```bash
cd build
```
2. Create an executable JAR file with the name `app.jar`
```bash
jar cmf MANIFEST.MF app.jar project/Main.class
```
3. Test the JAR file, it should print `Hello from Docker!`
```bash
java -jar app.jar
```
### Stage 4 Build a Minimal Docker Image
1. Create a Dockerfile in the root directory with the following content:
```dockerfile
FROM eclipse-temurin:21-jre-alpine
COPY build/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```
2. Build the Docker image with the name `java-docker-demo`
```bash
docker build -t java-docker-demo .
```
### Stage 5 Run Docker Container and Interact
1. Run the Docker container
```bash
docker run java-docker-demo
```
2. Check the expected output of the container
```text
Hello from Docker!
```
