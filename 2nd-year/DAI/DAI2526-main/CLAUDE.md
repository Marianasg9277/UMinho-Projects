# CLAUDE.md — Contexto do Projeto DAI2526 (TUB)

> Ficheiro de memória para futuras sessões Claude Code.
> Não contém código funcional — apenas contexto arquitetural.

---

## 1. Estrutura Geral do Projeto

```
DAI2526/
├── backend/          ← Spring Boot REST API (porta 8080)
├── frontend/         ← HTML/CSS/JS puro (sem framework)
├── src/              ← app.js mínimo na raiz (legacy/sync)
├── docs/             ← Documentação
├── scripts/          ← Scripts utilitários
├── .github/          ← GitHub Actions / CI-CD
├── pom.xml           ← Maven raiz
└── sync_frontend.ps1 ← Sincroniza frontend para static/
```

**Package base do backend:** `com.example.loginapi`  
**Classe principal:** `LoginapiApplication.java`  
**Projeto:** TUB — Transportes Urbanos de Braga

---

## 2. Tecnologias

### Backend
- **Java 17** + **Spring Boot 4.0.3**
- **Spring Data JPA** + **Hibernate** (DDL mode: `update`)
- **PostgreSQL** (Azure) via HikariCP (max 10 conn)
- **Spring Security** — sessão HTTP, BCrypt (cost 10)
- **Lombok** — geração de código
- **ZXing 3.5.3** — geração de QR codes
- **OpenPDF 1.3.43** — geração de faturas PDF
- **Thymeleaf** — templates de email
- **SpringDoc OpenAPI 2.8.6** — Swagger UI
- **Flyway** — migrações SQL (parcial)

### Frontend
- **HTML5 + CSS3 + JavaScript ES6+ (Vanilla)**
- Sem framework (sem React, Vue, Angular)
- Google Fonts (Inter)
- Design: glass-morphism, cores TUB (#00a2e8, #007bb5)

---

## 3. Arquitetura Backend

### Localização dos ficheiros
```
backend/src/main/java/com/example/loginapi/
├── controller/     ← 22 controllers
├── service/        ← 21 services
├── repository/     ← 28 repositories (Spring Data JPA)
├── model/          ← entidades JPA + enums
├── dto/            ← request/response DTOs
└── config/         ← configuração (Security, OpenAPI, Web, DataSeeder)
```

### Controllers existentes (22)
| Controller | Prefixo URL |
|---|---|
| `AuthController` | `/api/auth` |
| `ClienteController` | `/api/cliente` |
| `ContaController` | `/api/conta` |
| `BilheteController` | `/api/bilhete` |
| `PasseController` | `/api/passe` |
| `PagamentoController` | `/api/pagamento` |
| `CartaoPagamentoController` | `/api/cartao` |
| `FaturaController` | `/api/fatura` |
| `NotificacaoController` | `/api/notificacao` |
| `FiscalizacaoController` | `/api/fiscalizacao` |
| `ValidacaoController` | `/api/validacao` |
| `EstatutoController` | `/api/estatuto` |
| `TransporteController` | `/api/transporte` |
| `MapaController` | `/api/mapa` |
| `DadosPublicosController` | `/api/publico` |
| `AdminController` | `/api/admin` |
| `AdminAutocarroController` | `/api/admin/autocarro` |
| `AdminEstatutoController` | `/api/admin/estatuto` |
| `AdminPricingController` | `/api/admin/pricing` |
| `AutocarroEstadoAdminController` | `/api/admin/autocarro/estado` |
| `SimulacaoAutocarroController` | `/api/simulacao` |
| `ExportController` | `/api/export` |

### Services existentes (21)
`AuthService`, `ClienteService`, `ContaService`, `PasseService`, `CompraPasseService`, `PagamentoService`, `CartaoPagamentoService`, `FaturaService`, `NotificacaoService`, `EstatutoService`, `PedidoEstatutoService`, `AuditLogService`, `EmailService`, `DocumentoService`, `QrCodeService`, `RotaLinhaService`, `GtfsImportService`, `PricingService`, `SaldoCompraService`, `SimulacaoAutocarroService`, `CustomUserDetailsService`

### Repositories existentes (28)
`UtilizadorRepository`, `ClienteRepository`, `ContaRepository`, `AutocarroRepository`, `AutocarroEstadoRepository`, `AutocarroUltimaPosicaoRepository`, `LinhaRepository`, `ParagemRepository`, `LinhaParagemRepository`, `PasseRepository`, `TipoPasseRepository`, `TipoBilheteRepository`, `TransacaoRepository`, `PagamentoRepository`, `CartaoPagamentoRepository`, `MovimentoContaRepository`, `NotificacaoRepository`, `AuditLogRepository`, `HistoricoValidacaoRepository`, `AvisoRepository`, `PasseQrTokenRepository`, `HorarioRepository`, `PedidoEstatutoRepository`, `RegraPrecoRepository`, `RotaLinhaPontoRepository`, `CoroaRepository`, `DocumentoEstatutoRepository`, `EstatutoUtilizadorRepository`

### Configuração de Segurança
- Ficheiro: `SecurityConfig.java`
- Endpoints públicos: `/api/auth/**`, `/api/publico/**`, Swagger
- Autenticação por sessão HTTP (não JWT)
- Roles: `ADMIN`, `CLIENTE`, `MOTORISTA`, `FISCALIZADOR`
- CSRF desativado para API
- CORS configurado via `application.properties`

### Base de Dados
- **PostgreSQL** (Azure, configurado via env vars)
- Variáveis: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- Scripts SQL: `schema.sql`, `data.sql`, `V1__criar_historico_validacoes.sql`
- DDL: `spring.jpa.hibernate.ddl-auto=update`

---

## 4. Entidades Relevantes

### `Linha.java`
Linha de transporte. Tem lista de `LinhaParagem`.

### `Paragem.java`
Paragem de autocarro. Campos esperados: `id`, `nome`, `latitude`, `longitude`, possivelmente `ativo`.

### `LinhaParagem.java`
Associação many-to-many entre `Linha` e `Paragem`. Pode ter campos de ordem/sequência.

### `Horario.java`
Horário de uma linha. Campos esperados: `id`, `linha`, `partida`, `chegada`, `diaSemana` ou similar.

### `Autocarro.java`
Autocarro/veículo. Tem estado (`AutocarroEstado`) e última posição (`AutocarroUltimaPosicao`).

### `RotaLinhaPonto.java`
Pontos geográficos que formam o percurso visual de uma linha no mapa.

### `Utilizador.java`
Utilizador do sistema. Enum `Role`: `ADMIN`, `CLIENTE`, `MOTORISTA`, `FISCALIZADOR`.

### Enums existentes
`EstadoComercialPasse`, `EstadoOperacionalPasse`, `EstadoPagamento`, `EstadoEstatutoUtilizador`, `EstadoPedidoEstatuto`, `TipoEstatuto`, `TipoMovimentoConta`

---

## 5. Funcionalidades Já Implementadas

| Módulo | Status |
|---|---|
| Autenticação (login/register) | Implementado |
| Perfil de cliente | Implementado |
| Bilhetes (compra, histórico) | Implementado |
| Passes (compra, renovação, QR) | Implementado |
| Carteira / Saldo | Implementado |
| Pagamentos + cartões | Implementado |
| QR code (geração + validação) | Implementado |
| Fiscalização | Implementado |
| Estatutos (estudante, sénior, etc.) | Implementado |
| Notificações | Implementado |
| Faturas (PDF) | Implementado |
| Backoffice admin | Implementado |
| Mapa / posição autocarros | Implementado |
| Gestão de autocarros (admin) | Implementado |
| Simulação de autocarros | Implementado |
| Export de dados | Implementado |
| GTFS import | Implementado |

---

## 6. Objetivo desta Fase

Implementar casos de uso simples e seguros de **gestão administrativa de paragens e horários**.

**Não tocar** (sem pedido explícito): pagamentos, passes, bilhetes, carteira, QR, autenticação.

---

## 7. Casos de Uso Prioritários

| UC | Descrição |
|---|---|
| UC4.5.1 | Criar Paragem |
| UC4.5.2 | Consultar Paragem |
| UC4.5.3 | Atualizar Paragem |
| UC4.5.4 | Eliminar Paragem (seguro) |
| UC4.6.1 | Criar Horário |
| UC4.6.2 | Consultar Horário |
| UC4.6.3 | Atualizar Horário |
| UC4.6.4 | Eliminar Horário |
| UC4.4.5 | Desassociar Paragem de Linha |

---

## 8. Decisões Técnicas

### Novos endpoints
- Prefixo: `/api/admin/paragens`, `/api/admin/horarios`
- Role obrigatória: `ADMIN`

### Estilo a seguir
- Padrão dos controllers existentes (anotações Spring, ResponseEntity, etc.)
- DTOs separados para request e response
- Service intermédio entre controller e repository
- Validações com `@Valid` / `@NotBlank` / `@NotNull`

### Reutilização
- Usar `ParagemRepository`, `HorarioRepository`, `LinhaParagemRepository` já existentes
- Criar controller/service/DTO apenas quando não existir equivalente

### Eliminar Paragem (UC4.5.4)
- Se `Paragem` tiver campo `ativo`: fazer eliminação lógica (`ativo = false`)
- Se não tiver: impedir eliminação quando existirem `LinhaParagem` associadas, retornar erro claro

### Desassociar Paragem de Linha (UC4.4.5)
- Remover apenas o registo `LinhaParagem` correspondente
- Não eliminar nem `Linha` nem `Paragem`

### Horários (UC4.6.x)
- CRUD simples sobre a entidade `Horario`
- Associar ao `id` da `Linha` correspondente

### O que NÃO fazer nesta fase
- KML / trajetos geográficos complexos
- Eliminação física de linhas
- Modificações em passes, bilhetes, pagamentos, QR, autenticação

---

## 9. Regras de Trabalho

1. **Antes de implementar**: listar exatamente os ficheiros a criar e a alterar — aguardar confirmação.
2. **Depois de implementar**: indicar como testar (curl / Swagger / frontend).
3. **Não inventar nova arquitetura**: seguir o padrão já existente.
4. **Não criar entidades desnecessárias**: verificar sempre se já existe.
5. **Não mexer em módulos complexos** sem pedido explícito do utilizador.
6. **Alterações mínimas e cirúrgicas**: não refatorar código existente aproveitando a oportunidade.

---

## 10. Frontend — Páginas Existentes (27)

`index.html`, `register.html`, `perfil.html`, `perfil_motorista.html`, `carteira.html`, `comprar_bilhete.html`, `comprar_passe.html`, `carregar_saldo.html`, `faturas.html`, `pagamentos.html`, `metodos_pagamento.html`, `notificacoes.html`, `avisos.html`, `historico_viagens.html`, `ver_linhas.html`, `mapa_autocarros.html`, `pedir_estatuto.html`, `ver_precario.html`, `verificar_qr.html`, `fiscalizacao.html`, `backoffice.html` + variantes adicionais.

**Ficheiros JS principais:** `app.js`, `i18n.js` (traduções PT), `session-guard.js`

---

## 11. Variáveis de Ambiente

| Variável | Descrição | Default |
|---|---|---|
| `DB_URL` | URL PostgreSQL | — |
| `DB_USERNAME` | Utilizador DB | — |
| `DB_PASSWORD` | Password DB | — |
| `SERVER_PORT` | Porta do servidor | 8080 |
| `CORS_ALLOWED_ORIGINS` | Origens CORS | localhost |
| `EMAIL_ENABLED` | Ativar email real | false |
| `PAGAMENTO_FALHA_SIMULADA` | Simular falhas de pagamento | false |
| `PASSE_AVISO_EXPIRACAO_DIAS` | Aviso expiração passe | 7 |
| `SIMULATOR_API_KEY` | Chave API simulação autocarro | — |

---

## 13. Implementações Realizadas Nesta Fase

> Estado: **compilado e validado** — não commitado.  
> Todos os endpoints ficam em `/api/admin/**`, protegidos automaticamente pela regra `hasRole("ADMIN")` já existente no `SecurityConfig`.

---

### UC4.5.x — Gestão Administrativa de Paragens

**Casos de uso implementados:**

| UC | Descrição | Estado |
|---|---|---|
| UC4.5.1 | Criar Paragem | Implementado |
| UC4.5.2 | Consultar Paragem | Implementado |
| UC4.5.3 | Atualizar Paragem | Implementado |
| UC4.5.4 | Eliminar Paragem | Implementado |

**Endpoints:**

| Método | URL | Notas |
|---|---|---|
| `POST` | `/api/admin/paragens` | 201 criado; 409 gtfsStopId duplicado; 400 inválido |
| `GET` | `/api/admin/paragens` | Lista todas; `?ativo=true/false` filtra |
| `GET` | `/api/admin/paragens/{id}` | 404 se não existe |
| `PUT` | `/api/admin/paragens/{id}` | Não altera `ativo`; 404/400/409 |
| `DELETE` | `/api/admin/paragens/{id}` | **Eliminação lógica**: `ativo = false` |

**Nota sobre DELETE:** a paragem **não é apagada fisicamente**. O campo `ativo` é colocado a `false`. A paragem continua na BD e visível na listagem admin (`?ativo=false`). Remoção idempotente — se já inativa, devolve sucesso na mesma.

**Ficheiros criados:**
- `dto/ParagemRequest.java`
- `dto/ParagemResponse.java`
- `service/ParagemService.java`
- `controller/AdminParagemController.java`

**Ficheiros alterados:**
- `repository/ParagemRepository.java` — adicionados `findAllByAtivo(Boolean)` e `existsByGtfsStopIdAndIdNot(String, Long)`

---

### UC4.6.x — Gestão Administrativa de Horários

**Casos de uso implementados:**

| UC | Descrição | Estado |
|---|---|---|
| UC4.6.1 | Criar Horário | Implementado |
| UC4.6.2 | Consultar Horário | Implementado |
| UC4.6.3 | Atualizar Horário | Implementado |
| UC4.6.4 | Eliminar Horário | Implementado |

**Endpoints:**

| Método | URL | Notas |
|---|---|---|
| `POST` | `/api/admin/horarios` | 201 criado; 404 linha não existe; 400 inválido |
| `GET` | `/api/admin/horarios` | Lista todos ordenados por linha→minutos; `?linhaId=` filtra |
| `GET` | `/api/admin/horarios/{id}` | 404 se não existe |
| `PUT` | `/api/admin/horarios/{id}` | 404 horário/linha não existem; 400 inválido |
| `DELETE` | `/api/admin/horarios/{id}` | **Eliminação física** — sem dependências críticas |

**Nota sobre `Horario.paragem`:** é uma `String` com o nome textual da paragem (ex: `"Largo do Prado"`), **não** uma FK para a entidade `Paragem`. Esta é a estrutura existente da entidade — foi mantida sem alterações.

**Nota sobre DELETE:** eliminação física direta (`deleteById`). `Horario` não é referenciado por passes, bilhetes, pagamentos, QR ou autenticação.

**Ficheiros criados:**
- `dto/HorarioRequest.java`
- `dto/HorarioResponse.java`
- `service/HorarioService.java`
- `controller/AdminHorarioController.java`

**Ficheiros alterados:**
- `repository/HorarioRepository.java` — adicionados `findAllOrderedByLinhaAndMinutos()` e `findAllByLinhaIdOrdered(Long)` via JPQL explícito

---

### UC4.4.5 — Desassociar Paragem de Linha

**Caso de uso implementado:**

| UC | Descrição | Estado |
|---|---|---|
| UC4.4.5 | Desassociar Paragem de Linha | Implementado |

**Endpoint:**

| Método | URL | Notas |
|---|---|---|
| `DELETE` | `/api/admin/linhas/{linhaId}/paragens/{paragemId}` | Remove associação; 404 linha/paragem/associação não existe |

**Notas importantes:**
- Remove apenas os registos `LinhaParagem` — **não apaga `Linha` nem `Paragem`**
- Remove **todos os sentidos** existentes para o par (linhaId, paragemId): IDA, VOLTA e sentido null
- **Recalcula `Linha.numParagens`** após remoção: conta IDA → se vazio conta VOLTA → se vazio conta total restante (retrocompatibilidade com sentido null)
- Operação dentro de `@Transactional` no service
- `registosRemovidos` na resposta indica quantos registos `LinhaParagem` foram eliminados (1 se só havia um sentido, 2 se havia IDA+VOLTA)
- Sem filtro `?sentido=` nesta fase — evolução futura possível sem breaking change

**Ficheiros criados:**
- `service/LinhaParagemService.java`
- `controller/AdminLinhaController.java`

**Ficheiros alterados:**
- `repository/LinhaParagemRepository.java` — adicionado `deleteByLinhaIdAndParagemId(Long, Long)` via `@Modifying` JPQL

---

### Estado de Validação

| Verificação | Resultado |
|---|---|
| `mvn compile` | **Passou sem erros** |
| Testes executados | **42** |
| Falhas | **0** |
| Erros | **1** (pré-existente) |
| Commit realizado | **Não** |

**Erro pré-existente (não relacionado):**  
`SaldoCompraServiceTest.passeComSaldoSuficiente_debitaESimula` — NPE porque `compraPasseService` não está injetado no teste (`@InjectMocks`/`@Mock` incompleto). Presente antes desta fase, no módulo de compra de passe com saldo. Nenhum dos ficheiros criados/alterados tem relação com este teste.

---

## 14. Deploy

- **Backend:** Azure App Service (JAR)
- **CI/CD:** GitHub Actions (`.github/workflows/`)
- **Base de dados:** Azure PostgreSQL
- `sync_frontend.ps1` — copia frontend para `backend/src/main/resources/static/`
