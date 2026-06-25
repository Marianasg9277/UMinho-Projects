# GTFS — Próximos passos implementados

## Versão

Esta versão parte da fase em que já existiam:

- linhas GTFS reais em `linhas`;
- paragens GTFS reais em `paragens`;
- percursos por linha/sentido em `linha_paragens`;
- shapes GTFS em `rota_linha_ponto` com `shape_id`;
- frontend `ver_linhas.html` com mapa baseado no traçado real.

## Implementado agora

### 1. Simulador de autocarros baseado em shapes GTFS

O script `scripts/simular_autocarro.py` deixou de usar:

- `LINHA_ID = 5784` hardcoded;
- `linha90_routes.json`;
- KML da linha 90;
- paragens antigas de fallback como fonte principal.

Agora usa o backend:

```http
GET /api/linhas
GET /api/mapa/rotas/{linhaId}/{sentido}
POST /api/simulacao/autocarros/posicao
```

Exemplos:

```bash
python scripts/simular_autocarro.py --linha-numero 90 --sentido IDA --loop
python scripts/simular_autocarro.py --linha-numero 90 --sentido VOLTA --loop
python scripts/simular_autocarro.py --linha-id 6259 --sentido IDA --velocidade 30 --intervalo 1.5
```

Para testar outra linha:

```bash
python scripts/simular_autocarro.py --linha-numero 5 --sentido IDA --loop
```

### 2. `mapa_autocarros.html` deixou de depender do ID 5784

A página `frontend/mapa_autocarros.html` agora resolve a linha dinamicamente.

URLs suportados:

```text
/mapa_autocarros.html
/mapa_autocarros.html?linha=90
/mapa_autocarros.html?linha=90&sentido=VOLTA
/mapa_autocarros.html?linhaId=6259&sentido=IDA
```

Por defeito, se não forem passados parâmetros, abre a linha 90 em IDA.

A página usa:

```http
GET /api/mapa/rotas/{linhaId}/{sentido}
GET /api/autocarros/posicoes?linhaId={linhaId}
```

### 3. Removido o indicador visual `DEBUG / TESTE`

A etiqueta de debug foi removida do mapa de autocarros.

## Testes recomendados

### Backend

```bash
cd backend
mvnw.cmd clean spring-boot:run
```

### Validar shapes da linha 90

```sql
SELECT l.numero, r.sentido, r.shape_id, COUNT(*) AS pontos
FROM rota_linha_ponto r
JOIN linhas l ON l.id = r.linha_id
WHERE l.numero = '90'
GROUP BY l.numero, r.sentido, r.shape_id
ORDER BY r.sentido;
```

### Validar endpoint da rota

```text
http://localhost:8080/api/mapa/rotas/6259/IDA
http://localhost:8080/api/mapa/rotas/6259/VOLTA
```

### Validar simulador

```bash
python scripts/simular_autocarro.py --linha-numero 90 --sentido IDA --loop
```

Depois abrir:

```text
http://localhost:8080/ver_linhas.html
```

Abrir a linha 90 e confirmar que aparece um autocarro no mapa.

Também testar:

```text
http://localhost:8080/mapa_autocarros.html?linha=90&sentido=IDA
```

## Nota

A posição do autocarro continua ligada à linha, não ao sentido, porque a tabela atual `autocarro_ultima_posicao` não tem campo `sentido`. O simulador move o autocarro no sentido escolhido, mas o endpoint público filtra apenas por `linhaId`.

Uma melhoria futura seria adicionar `sentido` em `autocarro_ultima_posicao` e nos DTOs de simulação.
