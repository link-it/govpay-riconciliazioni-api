package it.govpay.riconciliazioni;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;

class GovPayRiconciliazioniApplicationTest {

    @Test
    void configureRegistersApplicationSource() {
        GovPayRiconciliazioniApplication application = new GovPayRiconciliazioniApplication();
        SpringApplicationBuilder builder = new SpringApplicationBuilder();

        SpringApplicationBuilder configured = application.configure(builder);

        assertSame(builder, configured);
    }

    @Test
    void jsonCustomizerIsNotNull() {
        GovPayRiconciliazioniApplication application = new GovPayRiconciliazioniApplication();
        application.timeZone = "Europe/Rome";

        JsonMapperBuilderCustomizer customizer = application.jsonCustomizer();

        assertNotNull(customizer);
    }
}
