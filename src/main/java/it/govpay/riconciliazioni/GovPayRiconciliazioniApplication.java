package it.govpay.riconciliazioni;

import java.util.TimeZone;

import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class })
public class GovPayRiconciliazioniApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(GovPayRiconciliazioniApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(GovPayRiconciliazioniApplication.class, args);
    }

    @Value("${riconciliazioni.time-zone:Europe/Rome}")
    String timeZone;

    /**
     * Impostazione del timezone nel mapper Jackson
     */
    @Bean
    public JsonMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.defaultTimeZone(TimeZone.getTimeZone(this.timeZone));
    }

    /**
     * Registra il modulo per serializzare {@code JsonNullable} (generato dagli
     * schemi OpenAPI con `nullable: true`).
     */
    @Bean
    public JsonNullableModule jsonNullableModule() {
        return new JsonNullableModule();
    }

}
