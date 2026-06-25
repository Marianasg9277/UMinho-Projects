#!/usr/bin/env python3
"""
Simulador de autocarros TUB baseado nos shapes GTFS importados no backend.

Agora o simulador NÃO usa o KML antigo nem o ID interno 5784 hardcoded.
Ele vai buscar a rota real ao endpoint:

    GET /api/mapa/rotas/{linhaId}/{sentido}

Uso recomendado:
    python scripts/simular_autocarro.py --linha-numero 90 --sentido IDA
    python scripts/simular_autocarro.py --linha-numero 90 --sentido VOLTA
    python scripts/simular_autocarro.py --linha-id 6259 --sentido IDA

Também pode ser usado para qualquer linha GTFS que tenha shapes importados.
"""

import argparse
import math
import sys
import time
from datetime import datetime
from typing import Dict, List, Optional, Sequence, Tuple
import random

try:
    import requests
except ImportError:
    print("Modulo 'requests' nao encontrado. Instale com:")
    print("  pip install requests")
    sys.exit(1)

Point = Tuple[float, float]


def normalizar_url(base_url: str) -> str:
    return base_url.rstrip('/')


def get_json(url: str, timeout: float = 15.0):
    resp = requests.get(url, timeout=timeout)
    resp.raise_for_status()
    return resp.json()


def resolver_linha(base_url: str, linha_id: Optional[int], linha_numero: str) -> Dict:
    """Resolve o ID interno da linha a partir de --linha-id ou --linha-numero."""
    if linha_id is not None:
        linhas = get_json(f"{base_url}/api/linhas")
        for linha in linhas:
            if int(linha.get('id')) == int(linha_id):
                return linha
        return {"id": linha_id, "numero": str(linha_id), "nome": f"Linha {linha_id}"}

    linhas = get_json(f"{base_url}/api/linhas")
    for linha in linhas:
        if str(linha.get('numero', '')).strip() == str(linha_numero).strip():
            return linha

    disponiveis = ', '.join(str(l.get('numero')) for l in linhas[:20])
    raise RuntimeError(
        f"Linha numero {linha_numero!r} nao encontrada em /api/linhas. "
        f"Primeiras linhas disponiveis: {disponiveis}"
    )


def carregar_rota_gtfs(base_url: str, linha_id: int, sentido: str) -> List[Point]:
    """Carrega os pontos da rota real GTFS importada em rota_linha_ponto."""
    url = f"{base_url}/api/mapa/rotas/{linha_id}/{sentido}"
    pontos = get_json(url)
    polyline: List[Point] = []
    for p in pontos:
        lat = p.get('latitude')
        lon = p.get('longitude')
        if lat is not None and lon is not None:
            polyline.append((float(lat), float(lon)))
    return polyline


def carregar_fallback_paragens(base_url: str, linha_id: int, sentido: str) -> List[Point]:
    """Fallback: usa coordenadas das paragens se a rota GTFS não existir."""
    url = f"{base_url}/api/linhas/{linha_id}/detalhe?sentido={sentido}"
    detalhe = get_json(url)
    pontos: List[Point] = []
    for p in detalhe.get('percurso', []):
        lat = p.get('latitude')
        lon = p.get('longitude')
        if lat is not None and lon is not None:
            pontos.append((float(lat), float(lon)))
    return pontos


def haversine_m(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    R = 6_371_000
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    dphi = math.radians(lat2 - lat1)
    dlam = math.radians(lon2 - lon1)
    a = math.sin(dphi / 2) ** 2 + math.cos(phi1) * math.cos(phi2) * math.sin(dlam / 2) ** 2
    return 2 * R * math.atan2(math.sqrt(a), math.sqrt(1 - a))


def bearing_deg(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    dlam = math.radians(lon2 - lon1)
    x = math.sin(dlam) * math.cos(phi2)
    y = math.cos(phi1) * math.sin(phi2) - math.sin(phi1) * math.cos(phi2) * math.cos(dlam)
    return (math.degrees(math.atan2(x, y)) + 360) % 360


def interpolate_point(lat1: float, lon1: float, lat2: float, lon2: float, frac: float) -> Point:
    return lat1 + (lat2 - lat1) * frac, lon1 + (lon2 - lon1) * frac


def calcular_distancias_acumuladas(polyline: Sequence[Point]) -> List[float]:
    cumulative = [0.0]
    for i in range(1, len(polyline)):
        d = haversine_m(polyline[i - 1][0], polyline[i - 1][1], polyline[i][0], polyline[i][1])
        cumulative.append(cumulative[-1] + d)
    return cumulative


def calcular_sub_estado(minutos_reais: float, minutos_esperados: float) -> str:
    """Calcula PONTUAL / ATRASADO / ADIANTADO com base no diagrama de estados."""
    diferenca = minutos_reais - minutos_esperados
    if diferenca > 5:
        return "ATRASADO"
    elif diferenca < -2:
        return "ADIANTADO"
    else:
        return "PONTUAL"


def nova_ocupacao(ocupacao_atual: int, capacidade: int) -> int:
    """Simula entrada e saída de passageiros ao passar por uma paragem."""
    saem = random.randint(0, min(ocupacao_atual, 10))
    entram = random.randint(0, min(capacidade - (ocupacao_atual - saem), 15))
    return max(0, min(ocupacao_atual - saem + entram, capacidade))


def posicao_na_distancia(polyline: Sequence[Point], cumulative: Sequence[float], dist_m: float) -> Point:
    total = cumulative[-1]
    dist_m = max(0.0, min(dist_m, total))
    for i in range(1, len(cumulative)):
        if cumulative[i] >= dist_m:
            seg_start = cumulative[i - 1]
            seg_len = cumulative[i] - seg_start
            if seg_len < 0.001:
                return polyline[i]
            frac = (dist_m - seg_start) / seg_len
            return interpolate_point(polyline[i - 1][0], polyline[i - 1][1], polyline[i][0], polyline[i][1], frac)
    return polyline[-1]


def simular(args) -> None:
    base_url = normalizar_url(args.url)
    sentido = args.sentido.upper().strip()
    if sentido not in {"IDA", "VOLTA"}:
        raise RuntimeError("--sentido deve ser IDA ou VOLTA")

    linha = resolver_linha(base_url, args.linha_id, args.linha_numero)
    linha_id = int(linha['id'])
    linha_numero = str(linha.get('numero', linha_id))
    linha_nome = str(linha.get('nome', f'Linha {linha_numero}'))

    polyline = carregar_rota_gtfs(base_url, linha_id, sentido)
    origem_rota = "GTFS shapes"
    if len(polyline) < 2:
        polyline = carregar_fallback_paragens(base_url, linha_id, sentido)
        origem_rota = "fallback paragens"

    if len(polyline) < 2:
        raise RuntimeError(
            f"Nao ha pontos suficientes para simular a linha {linha_numero} ({linha_id}) no sentido {sentido}."
        )

    cumulative = calcular_distancias_acumuladas(polyline)
    total_dist = cumulative[-1]
    velocidade_ms = args.velocidade / 3.6
    dist_por_tick = velocidade_ms * args.intervalo

    codigo = args.codigo or f"BUS-{linha_numero}-{sentido}-01"
    nome = args.nome or f"Autocarro Linha {linha_numero} {sentido}"

    print("=" * 72)
    print("Simulador de autocarro TUB com rota GTFS")
    print("=" * 72)
    print(f"Backend:      {base_url}")
    print(f"Linha:        {linha_numero} | id={linha_id} | {linha_nome}")
    print(f"Sentido:      {sentido}")
    print(f"Rota:         {origem_rota} | {len(polyline)} pontos | {total_dist:.0f} m")
    print(f"Autocarro:    {codigo} | {nome}")
    print(f"Velocidade:   {args.velocidade:.1f} km/h")
    print(f"Intervalo:    {args.intervalo:.1f}s")
    print("=" * 72)

    api_url = f"{base_url}/api/simulacao/autocarros/posicao"
    headers = {"Content-Type": "application/json", "X-SIMULATOR-API-KEY": args.key}

    dist_atual = 0.0
    prev_lat, prev_lon = polyline[0]
    ciclo = 1

    # ── Estado, sub-estado e ocupação ──────────────────────────────────────
    estado_atual = "ARMAZENADO"
    sub_estado_atual = None
    ocupacao_atual = 0
    capacidade = args.capacidade
    primeira_paragem_atingida = False
    tempo_inicio_servico = None
    ultima_paragem_dist = -9999.0  # para evitar atualizar a mesma paragem múltiplas vezes

    # Carregar paragens da linha para cálculo de sub-estado e ocupação
    try:
        url_detalhe = f"{base_url}/api/linhas/{linha_id}/detalhe?sentido={sentido}"
        detalhe = get_json(url_detalhe)
        paragens_linha = [
            {
                "lat": float(p["latitude"]),
                "lon": float(p["longitude"]),
                "minutos": int(p.get("minutosDesdeInicio", 0)),
                "nome": p.get("nome", ""),
            }
            for p in detalhe.get("percurso", [])
            if p.get("latitude") and p.get("longitude")
        ]
        print(f"[INFO] {len(paragens_linha)} paragens carregadas para cálculo de estado")
    except Exception as e:
        paragens_linha = []
        print(f"[AVISO] Não foi possível carregar paragens: {e}")

    try:
        while True:
            if dist_atual >= total_dist:
                if not args.loop:
                    print("[OK] Fim do percurso.")
                    break
                dist_atual = 0.0
                prev_lat, prev_lon = polyline[0]
                ciclo += 1
                print(f"[CICLO] Reiniciar ciclo {ciclo}")

            lat, lon = posicao_na_distancia(polyline, cumulative, dist_atual)
            if dist_atual == 0 and len(polyline) >= 2:
                direcao = int(bearing_deg(polyline[0][0], polyline[0][1], polyline[1][0], polyline[1][1])) % 360
            else:
                direcao = int(bearing_deg(prev_lat, prev_lon, lat, lon)) % 360

            pct = (dist_atual / total_dist * 100) if total_dist > 0 else 0
            vel_report = max(0.0, args.velocidade + math.sin(dist_atual / 150) * 2.5)

            # ── Gerir estado ──────────────────────────────────────────────────
            # Só muda estado se o autocarro não estiver em MANUTENCAO ou AVARIADO
            # (esses estados são geridos pelo admin no backoffice)
            if estado_atual not in ("MANUTENCAO", "AVARIADO"):

                # ARMAZENADO → EM_TRANSITO ao primeiro movimento
                if estado_atual == "ARMAZENADO" and dist_atual > 0:
                    estado_atual = "EM_TRANSITO"
                    sub_estado_atual = None
                    print("[ESTADO] ARMAZENADO -> EM_TRANSITO")

                # Verificar se passou por alguma paragem (distância < 80m)
                for par in paragens_linha:
                    dist_a_paragem = haversine_m(lat, lon, par["lat"], par["lon"])
                    if dist_a_paragem < 80 and abs(dist_atual - ultima_paragem_dist) > 200:
                        ultima_paragem_dist = dist_atual

                        # Primeira paragem → EM_SERVICO
                        if not primeira_paragem_atingida:
                            primeira_paragem_atingida = True
                            estado_atual = "EM_SERVICO"
                            tempo_inicio_servico = time.time()
                            print(f"[ESTADO] EM_TRANSITO -> EM_SERVICO (paragem: {par['nome']})")

                        # Atualizar ocupação
                        ocupacao_atual = nova_ocupacao(ocupacao_atual, capacidade)

                        # Calcular sub-estado
                        if estado_atual == "EM_SERVICO" and tempo_inicio_servico is not None:
                            minutos_reais = (time.time() - tempo_inicio_servico) / 60.0
                            sub_estado_atual = calcular_sub_estado(minutos_reais, par["minutos"])
                            print(f"[PARAGEM] {par['nome']} | Ocupação: {ocupacao_atual}/{capacidade} | {sub_estado_atual}")
                        break

                # Simular avaria aleatória (0.1% de probabilidade por tick)
                if estado_atual in ("EM_TRANSITO", "EM_SERVICO") and random.random() < 0.001:
                    estado_atual = "AVARIADO"
                    sub_estado_atual = None
                    print("[ESTADO] AVARIA detectada! O estado passa a AVARIADO.")

                # Fim de percurso → ARMAZENADO
                if dist_atual >= total_dist and not args.loop:
                    estado_atual = "ARMAZENADO"
                    sub_estado_atual = None
                    ocupacao_atual = 0
                    print("[ESTADO] Fim de percurso -> ARMAZENADO")

            # Converter inicio_servico para ISO string se disponivel
            inicio_servico_iso = (
                datetime.fromtimestamp(tempo_inicio_servico).strftime("%Y-%m-%dT%H:%M:%S")
                if estado_atual == "EM_SERVICO" and tempo_inicio_servico is not None
                else None
            )

            payload = {
                "codigoAutocarro": codigo,
                "nome": nome,
                "linhaId": linha_id,
                "latitude": round(lat, 6),
                "longitude": round(lon, 6),
                "velocidade": round(vel_report, 1),
                "direcao": direcao,
                "timestampReportado": datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),
                "estado": estado_atual,
                "subEstado": sub_estado_atual,
                "ocupacao": ocupacao_atual,
                "capacidade": capacidade,
                "inicioServico": inicio_servico_iso,
                "sentido": sentido,
            }

            try:
                resp = requests.post(api_url, headers=headers, json=payload, timeout=8)
                status = resp.status_code
                ok = "OK" if status == 200 else "ERRO"
                print(f"[{ok}] {status} | {pct:5.1f}% | ({lat:.6f}, {lon:.6f}) | {vel_report:.1f} km/h | {direcao}° | {estado_atual}{(' / ' + sub_estado_atual) if sub_estado_atual else ''} | {ocupacao_atual}/{capacidade} pax")
                if status != 200:
                    print("     ", resp.text[:220])
            except requests.exceptions.RequestException as exc:
                print(f"[ERRO] Rede: {exc}")

            prev_lat, prev_lon = lat, lon
            dist_atual += dist_por_tick
            time.sleep(args.intervalo)

    except KeyboardInterrupt:
        print("\n[STOP] Simulacao terminada pelo utilizador.")


def main() -> None:
    parser = argparse.ArgumentParser(description="Simulador de autocarro TUB baseado nos shapes GTFS")
    parser.add_argument("--url", default="http://localhost:8080", help="URL base do backend")
    parser.add_argument("--key", default="dev-simulator-key", help="API key do simulador")
    parser.add_argument("--linha-id", type=int, default=None, help="ID interno da linha na BD")
    parser.add_argument("--linha-numero", default="90", help="Numero público da linha, usado se --linha-id não for fornecido")
    parser.add_argument("--sentido", default="IDA", choices=["IDA", "VOLTA", "ida", "volta"], help="Sentido a simular")
    parser.add_argument("--codigo", default=None, help="Codigo do autocarro simulado")
    parser.add_argument("--nome", default=None, help="Nome do autocarro simulado")
    parser.add_argument("--intervalo", type=float, default=2.0, help="Intervalo entre envios, em segundos")
    parser.add_argument("--velocidade", type=float, default=25.0, help="Velocidade em km/h")
    parser.add_argument("--loop", action="store_true", help="Reiniciar no fim do percurso")
    parser.add_argument("--capacidade", type=int, default=60, help="Capacidade máxima de passageiros do autocarro")
    args = parser.parse_args()
    simular(args)


if __name__ == "__main__":
    main()
