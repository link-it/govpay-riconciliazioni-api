package it.govpay.riconciliazioni;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifica che il contesto applicativo parta con la configurazione di test (H2 in memoria).
 */
@SpringBootTest
@ActiveProfiles("test")
class ContextLoadsTest {

    @Test
    void contextLoads() {
        // Il test fallisce se il contesto non si avvia
    }
}
