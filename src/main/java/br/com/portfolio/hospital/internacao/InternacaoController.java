package br.com.portfolio.hospital.internacao;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InternacaoController {

    private final InternacaoService service;

    public InternacaoController(InternacaoService service) {
        this.service = service;
    }

    @PostMapping("/internacoes")
    public ResponseEntity<InternacaoResponse> internar(@Valid @RequestBody InternacaoRequest request) {
        InternacaoResponse criada = service.internar(request);
        return ResponseEntity.created(URI.create("/internacoes/" + criada.id())).body(criada);
    }

    @PatchMapping("/internacoes/{id}/alta")
    public InternacaoResponse darAlta(@PathVariable Long id) {
        return service.darAlta(id);
    }

    /** Pacientes atualmente internados (equivale ao "censo hospitalar"). */
    @GetMapping("/internacoes/em-aberto")
    public List<InternacaoResponse> emAberto() {
        return service.listarEmAberto();
    }

    @GetMapping("/pacientes/{pacienteId}/internacoes")
    public List<InternacaoResponse> historico(@PathVariable Long pacienteId) {
        return service.listarPorPaciente(pacienteId);
    }
}
