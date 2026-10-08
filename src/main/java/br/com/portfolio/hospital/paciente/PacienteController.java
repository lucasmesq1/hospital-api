package br.com.portfolio.hospital.paciente;

import br.com.portfolio.hospital.shared.PaginaResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Pacientes não têm DELETE de propósito: em prontuário hospitalar os registros
 * são mantidos por obrigação legal.
 */
@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> criar(@Valid @RequestBody PacienteRequest request) {
        PacienteResponse criado = service.criar(request);
        return ResponseEntity.created(URI.create("/pacientes/" + criado.id())).body(criado);
    }

    @GetMapping("/{id}")
    public PacienteResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping
    public PaginaResponse<PacienteResponse> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        int tamanhoSeguro = Math.min(Math.max(tamanho, 1), 100); // evita pedir milhões de linhas
        return service.listar(nome, PageRequest.of(Math.max(pagina, 0), tamanhoSeguro, Sort.by("nome")));
    }

    @PutMapping("/{id}")
    public PacienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody PacienteRequest request) {
        return service.atualizar(id, request);
    }
}
