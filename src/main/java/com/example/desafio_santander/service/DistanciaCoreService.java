import com.example.desafio_santander.dto.agencia.AgenciaDTO;
import com.example.desafio_santander.dto.agencia.DistanciaResponseDTO;
import com.example.desafio_santander.exception.DistanciaException;
import com.example.desafio_santander.model.Agencia;
import com.example.desafio_santander.repository.AgenciaRepository;
import com.example.desafio_santander.service.DistanciaUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class DistanciaCoreService implements DistanciaUseCase {

    private static final Logger logger = LoggerFactory.getLogger(DistanciaCoreService.class);
    private final AgenciaRepository agenciaRepository;

    @Override
    public DistanciaResponseDTO calcularDistancias(int userPosX, int userPosY) {
        logger.info("Core: calculando distâncias X={}, Y={}", userPosX, userPosY);

        List<Agencia> agenciasList = agenciaRepository.findAll();
        if (agenciasList.isEmpty()) {
            throw new DistanciaException("Nenhuma agência encontrada para calcular distâncias");
        }

        // Mapeia entidades → DTOs (simples; pode usar MapStruct se quiser)
        List<AgenciaDTO> agencias = agenciasList.stream()
                .map(a -> new AgenciaDTO(a.getId(), a.getPosX(), a.getPosY()))
                .toList();

        Map<String, Double> ordenado = agencias.stream()
                .collect(Collectors.toMap(
                        a -> "AGENCIA_" + a.getId(),
                        a -> calcularDistancia(userPosX, userPosY, a.getPosX(), a.getPosY())
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a,b)->a,
                        LinkedHashMap::new
                ));

        // Formata “distância = X.XX” apenas aqui (apresentação)
        Map<String, String> resposta = ordenado.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.format("distância = %.2f", e.getValue()),
                        (a,b)->a,
                        LinkedHashMap::new
                ));

        return new DistanciaResponseDTO(resposta);
    }

    private double calcularDistancia(int x1, int y1, int x2, int y2) {
        long dx = (long) x2 - x1;
        long dy = (long) y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
}