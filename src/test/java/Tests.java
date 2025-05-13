import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Tests {

//    STAGE 1
    @Test
    public void shouldPromptForUserNameAndGreetAndDockerGreeting() {
        String simulatedInput = "John\n";
        InputStream oldIn  = System.in;
        PrintStream oldOut = System.out;
        ByteArrayInputStream  testIn  = new ByteArrayInputStream(simulatedInput.getBytes());
        ByteArrayOutputStream testOut = new ByteArrayOutputStream();

        try {
            System.setIn(testIn);
            System.setOut(new PrintStream(testOut));

            Main.main(new String[0]);

        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }

        String output = testOut.toString();

        assertTrue("Your program should ask for the user's name with the message 'Enter your name:'",
                output.toLowerCase().contains("enter your name"));
        assertTrue("Your program should output 'Hello, John!' after the user name.",
                output.contains("Hello, John!"));
        assertTrue("Your program should output 'Greetings from Docker'!",
                output.contains("Greetings from Docker"));

    }

//    STAGE 2
    @Test
    public void buildDirectoryShouldExist() {
        File buildDir = new File("build_dir");
        String cwd = System.getProperty("user.dir");
        assertTrue("The 'build' directory was not found. Make sure you're in the correct directory (" + cwd + ").",
                buildDir.exists() && buildDir.isDirectory());
    }

    @Test
    public void mainClassShouldExistInBuildDirectory() {
        File mainClass = new File("build_dir/Main.class");
        assertTrue("The 'Main.class' file was not found in 'build_dir'. Ensure you compiled your Java file correctly.",
                mainClass.exists());
    }

    @Test
    public void checkManifestFileInBuild() {
        File manifestFileInBuild = new File("build_dir/MANIFEST.MF");
        assertTrue("The 'MANIFEST.MF' file was not found in the 'build_dir' directory.",
                manifestFileInBuild.exists());
    }

    @Test
    public void checkManifestContent() {
        File manifestFile = new File("build_dir/MANIFEST.MF");

        String content = "";
        try {
            content = Files.readString(manifestFile.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            fail("Error reading 'MANIFEST.MF': " + e.getMessage());
        }

        assertTrue("The 'MANIFEST.MF' file does not specify 'Manifest-Version: 1.0'.",
                content.contains("Manifest-Version: 1.0"));
        assertTrue("The 'MANIFEST.MF' file does not specify 'Main-Class: Main'.",
                content.contains("Main-Class: Main"));
    }

    //    STAGE 3

    /**
     * Checks files: Main.class, MANIFEST.MF, app.jar
     */
    @Test
    public void checkFilesInBuild() {
        File mainClassFile = new File("build_dir", "Main.class");
        if (!mainClassFile.exists()) {
            fail("The 'Main.class' file was not found in the 'build_dir' directory. Ensure you compiled your Java file correctly.");
        }

        File manifestFileInBuild = new File("build_dir/MANIFEST.MF");
        if (!manifestFileInBuild.exists()) {
            fail("The 'MANIFEST.MF' file was not found in the 'build' directory.");
        }

        File jarFile = new File("build_dir/app.jar");
        if (!jarFile.exists()) {
            fail("The 'app.jar' file was not found in the 'build' directory.");
        }
    }

    /**
     * Checks if the 'app.jar' file exists and contains the required entries: Main.class, META-INF/, META-INF/MANIFEST.MF
     */
    @Test
    public void checkJarContent() {
        File jarFile = new File("build_dir/app.jar");
        if (!jarFile.exists()) {
            fail("The 'app.jar' file was not found in the 'build_dir' directory.");
        }

        Set<String> expectedEntries = Set.of("Main.class", "META-INF/", "META-INF/MANIFEST.MF");

        Set<String> actualEntries = new HashSet<>();
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                actualEntries.add(entries.nextElement().getName());
            }
        } catch (IOException e) {
            fail("An error occurred while reading 'app.jar': " + e.getMessage());
        }

        final String HINT = "Your build_dir should contain only Main.class and MANIFEST.MF before you create jar file.";
        if (!actualEntries.containsAll(expectedEntries)) {
            fail(
                    "The 'app.jar' does not contain required entries. Expected entries: " + expectedEntries +
                            ", but found: " + actualEntries + ". " + HINT
            );
        }

        if (actualEntries.size() != expectedEntries.size()) {
            fail(
                    "The 'app.jar' contains unexpected additional entries. Expected exactly: " +
                            expectedEntries + ", but found: " + actualEntries + ". " + HINT
            );
        }
    }

    /**
     * Checks if the 'app.jar' file can be executed and produces the expected output.
     */
    @Test
    public void checkJarExecution() {
        File jarFile = new File("build_dir/app.jar");
        if (!jarFile.exists()) {
            fail("The 'app.jar' file was not found in the 'build_dir' directory.");
        }
        try {
            Process process = new ProcessBuilder("java", "-jar", jarFile.getAbsolutePath())
                    .redirectErrorStream(true)
                    .start();

            // Add input to the process
            process.getOutputStream().write("John\n".getBytes());
            process.getOutputStream().flush();
            process.getOutputStream().close();
            // Read the output
            StringBuilder output = new StringBuilder();
            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Check the output
            String outputString = output.toString();
            if (!outputString.contains("Hello, John!") || !outputString.contains("Greetings from Docker")) {
                fail("The 'app.jar' file did not produce the expected output. " +
                        "Output: " + outputString + ". Expected: 'Hello, John!' and 'Greetings from Docker' if the input was 'John'.");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                fail("The 'app.jar' file could not be executed. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            fail("An error occurred while executing 'app.jar': " + e.getMessage());
        }
    }

    // STAGE 4

    /**
     * Checks if the Dockerfile exists and contains the necessary instructions.
     */
    @Test
    public void checkDockerFileContent() {
        File dockerfile = new File("Dockerfile");
        if (!dockerfile.exists()) {
            fail("The 'Dockerfile' was not found in the project directory.");
        }

        String content = "";
        try {
            content = Files.readString(dockerfile.toPath());
        } catch (IOException e) {
            fail("Error reading 'Dockerfile': " + e.getMessage());
        }

        if (!content.contains("FROM") || !content.contains("COPY") || !content.contains("ENTRYPOINT")) {
            fail("The 'Dockerfile' does not contain the necessary instructions: FROM, COPY, and ENTRYPOINT.");
        }
    }

    /**
     * Checks if the Docker is running
     */
    @Test
    public void checkDockerInstallation() {
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "-v");
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            BufferedReader out = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String line = out.readLine();
            if (line == null || !line.contains("version")) {
                fail("Docker is not installed. Please install Docker and run it.");
            }
        } catch (Exception e) {
            fail("Docker is not running. Please start Docker and try again.");
        }
    }

    /**
     * Checks if the Docker image named 'java-in-docker' exists.
     */
    @Test
    public void checkDockerImage() {

        try {
            // List images filtered by our repo name
            ProcessBuilder pb = new ProcessBuilder(
                    "docker", "images",
                    "--filter", "reference=java-in-docker",
                    "--format", "{{.Repository}}"
            );
            pb.redirectErrorStream(true);
            Process proc = pb.start();

            BufferedReader out = new BufferedReader(
                    new InputStreamReader(proc.getInputStream())
            );
            String line = out.readLine();
            int exit = proc.waitFor();

            if (exit != 0) {
                fail("Could not list Docker images. Make sure Docker is running.");
            }

            if (line == null || !line.equals("java-in-docker")) {
                fail("Docker image named 'java-in-docker' not found. " +
                        "Build your image with param `-t java-in-docker`"
                );
            }

        } catch (Exception e) {
            fail("Error while checking Docker images: " + e.getMessage());
        }
    }

    // STAGE 5

    /**
     * Checks if Docker has a running container with the name "java-in-docker".
     */
    @Test
    public void checkRunContainer() {
        // Check if Docker is installed and running
        try {
            ProcessBuilder builder = new ProcessBuilder("docker", "-v");
            builder.redirectErrorStream(true);
            Process process = builder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                fail("Docker is not installed. Please install Docker and run it.");
            }
        } catch (IOException | InterruptedException e) {
            fail("An error occurred while checking Docker: " + e.getMessage());
        }

        // Check if the container is running
        try {
            ProcessBuilder builder = new ProcessBuilder("docker", "ps", "-a");
            builder.redirectErrorStream(true);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean containerFound = false;
            while ((line = reader.readLine()) != null) {
                if (line.contains("java-in-docker-container")) {
                    containerFound = true;
                    break;
                }
            }
            if (!containerFound) {
                fail("Docker container with the name 'java-in-docker-container' from image is not running. Please run the Docker container.");
            }
        } catch (IOException e) {
            fail("Docker is not running. Please start Docker and try again.");
        }
    }

    /**
     * Check if the container is run correctly from the image 'java-in-docker'.
     */
    @Test
    public void checkAppFromContainer() {
        try {
            ProcessBuilder builder = new ProcessBuilder("docker", "run", "--rm", "-i", "java-in-docker");
            builder.redirectErrorStream(true);
            Process process = builder.start();

            // Write input in a separate thread to avoid potential deadlocks
            Thread inputThread = new Thread(() -> {
                try {
                    process.getOutputStream().write("John\n".getBytes());
                    process.getOutputStream().flush();
                    process.getOutputStream().close();
                } catch (IOException e) {
                    fail("Error writing to Docker process: " + e.getMessage());
                }
            });
            inputThread.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                fail("Docker wasn't complete properly. Docker process exited with code " + exitCode);
            }

            String outputStr = output.toString();
            if (!outputStr.contains("Hello, John!") || !outputStr.contains("Greetings from Docker!")) {
                fail("The application did not output the expected message: 'Hello, John! Greeting from Docker!' if input was 'John'. " +
                        "The output was:\n" + outputStr);
            }

        } catch (Exception e) {
            fail("An error occurred while running the Docker container: " + e.getMessage());
        }
    }
}