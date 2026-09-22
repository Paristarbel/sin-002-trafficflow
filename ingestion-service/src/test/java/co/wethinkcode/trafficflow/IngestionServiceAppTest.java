package co.wethinkcode.trafficflow;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class IngestionServiceAppTest {

    @Test
    public void shouldConvertPlaceholderToNull() {
        assertNull(IngestionServiceApp.cleanValue("n/a"));
    }

    @Test
    public void cleanValue_returnsNullForPlaceholder() {
        assertNull(IngestionServiceApp.cleanValue("tbd"));
    }

    @Test
    public void shouldKeepValidValue() {
        assertEquals("downtown", IngestionServiceApp.cleanValue("downtown"));
    }
}
