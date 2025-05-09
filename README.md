# How to complete this project

### Stage 1 Write a simple Java program
1. In the `main()` method ask user the name and output the line with greetings.
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.print("Enter your name: ");
        Scanner scanner = new Scanner(System.in);
        String name = scanner.nextLine();
        System.out.printf("Hello, %s! Greetings from Docker!", name);
    }
}
```
The output logs should be:
```text
Enter your name: 
John
Hello, John! Greetings from Docker!
```
### Stage 2 Compile and create a Manifest File
From the root directory of the project:
1. Create `build` directory
```bash
mkdir -p build
```
2. Compile the `Main.java` in the `build` directory
```bash
javac -d build src/main/java/project/Main.java
```
3. Create `resources` and `META-INF` directory in the `java` directory src/main
```bash
mkdir -p src/main/resources/META-INF
```
4. Create MANIFEST.MF file in the `META-INF` directory with content inside:
```text
Manifest-Version: 1.0
Main-Class: project.Main
```
5. Copy MANIFEST.MF file to the `build` directory
```bash
cp src/main/resources/META-INF/MANIFEST.MF build/MANIFEST.MF
```
### Stage 3 Build an Executable JAR File
1. Navigate to the `build` directory
2. Create an executable JAR file with the name `app.jar`
```bash
jar cmf MANIFEST.MF app.jar project/Main.class
```
3. Test the JAR file, it should request the name and output the line with greetings.
```bash
java -jar app.jar
```
### Stage 4 Build a Minimal Docker Image
From the root directory of the project:
1. Create a Dockerfile in the root directory with the following content:
```dockerfile
FROM eclipse-temurin:21-jre-alpine
COPY build/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```
2. Build the Docker image with the name `java-in-docker`
```bash
docker build -t java-in-docker .
```
### Stage 5 Run Docker Container and Interact
1. Run the Docker container with enabled interactive mode
```bash
docker run -i --name java-in-docker-container java-in-docker
```
2. Check the expected output of the container
```text
Enter your name: 
John
Hello, John! Greetings from Docker!
```
