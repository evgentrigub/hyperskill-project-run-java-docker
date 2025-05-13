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
Right-click on the Main.java and select `Open in` ->`Terminal`. 
In the terminal:
1. Create `build_dir` directory
```bash 
mkdir ../../../build_dir
```
2. Create MANIFEST.MF file in the `META-INF` directory with content inside:
```text
Manifest-Version: 1.0
Main-Class: Main
```
Use `echo` command and destination path to create the MANIFEST.MF file:
```bash
echo "Manifest-Version: 1.0\nMain-Class: Main" > ../../../build_dir/MANIFEST.MF
```
3. Switch to the root directory, that contains the `src` and `build_dir` directory
```bash
cd ../../../
```
4.Compile the `Main.java` in the `build_dir` directory
```bash
javac -d build_dir src/main/java/Main.java
```
Now the `build_dir` directory should contain the compiled class file `Main.class` and the `MANIFEST.MF` file inside.

### Stage 3 Build an Executable JAR File
1. Navigate to the `build_dir` directory
2. Create an executable JAR file with the name `app.jar`
```bash
jar cmf MANIFEST.MF app.jar Main.class
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
COPY build_dir/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```
2. Build the Docker image with the name `java-in-docker`
```bash
docker build -t java-in-docker .
```
### Stage 5 Run Docker Container and Interact
1. Run the Docker container with the name `java-in-docker-container` and enabled interactive mode (i.e. `-i` flag)
```bash
docker run -i --name java-in-docker-container java-in-docker
```
2. Check the expected output of the container
```text
Enter your name: 
John
Hello, John! Greetings from Docker!
```
