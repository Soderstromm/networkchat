import chat.client.Client;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    @Test
    void logShouldWriteToFile(@TempDir Path tempDir) throws IOException {

        Path logPath = tempDir.resolve("test.log");
        Client client = new Client();
        client.setLogFilePath(logPath.toString());
        String testMessage = "Test message";


        client.log(testMessage); // Если log private, используйте reflection


        List<String> lines = Files.readAllLines(logPath);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains(testMessage));
    }

    @Test
    void logShouldAppendMessages(@TempDir Path tempDir) throws IOException {

        Path logPath = tempDir.resolve("test.log");
        Client client = new Client();
        client.setLogFilePath(logPath.toString());

        client.log("First");
        client.log("Second");

        List<String> lines = Files.readAllLines(logPath);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("First"));
        assertTrue(lines.get(1).contains("Second"));
    }
}
