package co.wethinkcode.trafficflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class IngestionServiceAppTest {

    @Test
    public void shouldConvertPlaceholderToNull() {

        assertNull(
                IngestionServiceApp.cleanValue("N/A")
        );
    }

    @Test
    public void shouldKeepValidValue() {

        assertEquals(
                "Downtown",
                IngestionServiceApp.cleanValue("Downtown")
        );
    }
}