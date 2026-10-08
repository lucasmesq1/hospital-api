package br.com.portfolio.hospital.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * O relógio vira um "bean" para que os testes possam fixar a data/hora.
 * Em produção usamos UTC; o cliente converte para America/Recife se quiser.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
