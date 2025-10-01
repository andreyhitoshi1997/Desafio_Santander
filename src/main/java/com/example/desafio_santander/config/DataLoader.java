package com.example.desafio_santander.config;

import com.example.desafio_santander.model.Agencia;
import com.example.desafio_santander.repository.AgenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Profile("!test") // Não executa durante testes
@ConditionalOnProperty(name = "app.load-sample-data", havingValue = "true", matchIfMissing = true)
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    private AgenciaRepository agenciaRepository;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Only load data if database is empty
            if (agenciaRepository.count() == 0) {
                logger.info("Carregando dados de exemplo para as agências...");

                // Create sample agencies at different positions
                Agencia agencia1 = new Agencia();
                agencia1.setPosX(5);
                agencia1.setPosY(10);

                Agencia agencia2 = new Agencia();
                agencia2.setPosX(-3);
                agencia2.setPosY(7);

                Agencia agencia3 = new Agencia();
                agencia3.setPosX(15);
                agencia3.setPosY(-2);

                Agencia agencia4 = new Agencia();
                agencia4.setPosX(0);
                agencia4.setPosY(0);

                Agencia agencia5 = new Agencia();
                agencia5.setPosX(-8);
                agencia5.setPosY(-5);

                agenciaRepository.save(agencia1);
                agenciaRepository.save(agencia2);
                agenciaRepository.save(agencia3);
                agenciaRepository.save(agencia4);
                agenciaRepository.save(agencia5);

                logger.info("Carregadas {} agências de exemplo", agenciaRepository.count());
            } else {
                logger.info("Dados já existem no banco. Total de agências: {}", agenciaRepository.count());
            }
        } catch (Exception e) {
            logger.warn("Erro ao carregar dados de exemplo: {}", e.getMessage());
        }
    }
}
