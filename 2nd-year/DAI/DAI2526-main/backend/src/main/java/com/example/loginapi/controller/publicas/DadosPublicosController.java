package com.example.loginapi.controller.publicas;

import com.example.loginapi.dto.PrecoPassePublicoResponse;
import com.example.loginapi.model.infraestrutura.Coroa;
import com.example.loginapi.model.infraestrutura.RegraPreco;
import com.example.loginapi.model.titulos.TipoPasse;
import com.example.loginapi.repository.infraestrutura.CoroaRepository;
import com.example.loginapi.repository.infraestrutura.RegraPrecoRepository;
import com.example.loginapi.repository.titulos.TipoPasseRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.loginapi.model.titulos.Passe;


@RestController
@RequestMapping("/api")
@Tag(name = "Dados Públicos – Passes", description = "Informação pública sobre tipos de passe e coroas")
public class DadosPublicosController {

    private static final Map<String, String> ESTATUTO_LABELS = Map.of(
            "SEM_ESTATUTO", "Sem Estatuto",
            "ESTUDANTE",    "Estudante",
            "RESIDENTE",    "Residente",
            "SENIOR",       "Sénior",
            "MILITAR",      "Militar",
            "CRIANCA",      "Criança",
            "INCAPACITADO", "Incapacitado"
    );

    @Autowired private TipoPasseRepository tipoPasseRepo;
    @Autowired private CoroaRepository coroaRepo;
    @Autowired private RegraPrecoRepository regraPrecoRepo;

    @GetMapping("/tipos-passe")
    @Operation(summary = "Listar tipos de passe ativos")
    public List<TipoPasse> listarTiposPasse() {
        return tipoPasseRepo.findByAtivoTrue();
    }

    @GetMapping("/coroas")
    @Operation(summary = "Listar coroas/zonas ativas")
    public List<Coroa> listarCoroas() {
        return coroaRepo.findByAtivoTrue();
    }

    @GetMapping("/publico/precos-passes")
    @Operation(summary = "Preços de passes vigentes — uso público (preçário)")
    public List<PrecoPassePublicoResponse> precosPassesPublico() {
        List<RegraPreco> regras = regraPrecoRepo.findTodasVigentes(LocalDate.now());
        return regras.stream()
                .map(r -> {
                    String estatuto = r.getTipoEstatuto().name();
                    String label    = ESTATUTO_LABELS.getOrDefault(estatuto, estatuto);
                    String tipoPasse = r.getTipoPasse().getNome();
                    String coroa    = r.getCoroa().getNome();
                    return new PrecoPassePublicoResponse(
                            estatuto, label, tipoPasse, coroa,
                            r.getPreco(),
                            r.getTipoPasse().getDuracaoDias()
                    );
                })
                .collect(Collectors.toList());
    }
}
