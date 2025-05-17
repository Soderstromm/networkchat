import chat.server.Server;
import org.junit.jupiter.api.Test;


public class ServerTest {
    @Test
    void testSettingsLoading() {
        Server server = new Server();
    }

    @Test
    void testLogging() {
        Server server = new Server();
        server.log("Test message");
    }
}
