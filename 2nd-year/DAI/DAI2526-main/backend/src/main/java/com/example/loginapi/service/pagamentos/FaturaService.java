package com.example.loginapi.service.pagamentos;

import com.example.loginapi.model.pagamentos.Pagamento;
import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.titulos.Transacao;
import java.math.BigDecimal;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.pagamentos.Conta;
import com.example.loginapi.model.infraestrutura.Linha;


/**
 * Serviço de geração de faturas.
 *
 * Gera faturas em HTML (sempre) e opcionalmente em PDF (via OpenPDF).
 * Cada fatura inclui número único, dados da entidade, do cliente e da transação.
 */
@Service
public class FaturaService {

    private static final Logger log = LoggerFactory.getLogger(FaturaService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ─────────────────────────────────────────────────────────────────────────
    // Faturas de Passes
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Gera HTML de fatura para um passe.
     */
    public String gerarHtmlFaturaPasse(Passe passe, Pagamento pagamento) {
        String nomeCliente = passe.getCliente().getNomeCompleto();
        String nif = passe.getCliente().getNif();
        String faturaNumero = pagamento.getFaturaNumero();
        String dataEmissao = pagamento.getFaturaEmitidaEm() != null
                ? pagamento.getFaturaEmitidaEm().format(FORMATTER)
                : LocalDateTime.now().format(FORMATTER);

        return construirHtmlFatura(
                faturaNumero,
                dataEmissao,
                nomeCliente,
                nif,
                passe.getTipoPasse().getNome() + " — " + passe.getCoroa().getNome(),
                "Passe de transporte",
                passe.getPrecoAplicado().toString(),
                passe.getDataInicio() != null ? passe.getDataInicio().toString() : "—",
                passe.getDataFim() != null ? passe.getDataFim().toString() : "—"
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Faturas de Carregamentos de Saldo
    // ─────────────────────────────────────────────────────────────────────────

    public String gerarHtmlFaturaCarregamento(String faturaNumero, String nomeCliente,
                                               String nif, BigDecimal valor,
                                               LocalDateTime emitidaEm) {
        String data = emitidaEm != null ? emitidaEm.format(FORMATTER) : LocalDateTime.now().format(FORMATTER);
        return construirHtmlFatura(
                faturaNumero, data, nomeCliente, nif != null ? nif : "—",
                "Carregamento de saldo — Conta TUB", "Carregamento de conta",
                valor.toPlainString(), data, "—");
    }

    public byte[] gerarPdfFaturaCarregamento(String faturaNumero, String nomeCliente,
                                              String nif, BigDecimal valor,
                                              LocalDateTime emitidaEm) {
        try {
            String data = emitidaEm != null ? emitidaEm.format(FORMATTER) : LocalDateTime.now().format(FORMATTER);
            return gerarPdf(faturaNumero, nomeCliente, nif != null ? nif : "—",
                    "Carregamento de saldo — Conta TUB", valor.toPlainString(), data, "—", emitidaEm);
        } catch (Exception e) {
            log.error("[FATURA] Erro ao gerar PDF carregamento | fatura={}: {}", faturaNumero, e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Faturas de Bilhetes
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Gera HTML de fatura para um bilhete (autenticado ou guest).
     */
    public String gerarHtmlFaturaBilhete(Transacao transacao) {
        String nomeCliente;
        String nif;

        if (transacao.getCliente() != null) {
            nomeCliente = transacao.getCliente().getNomeCompleto();
            nif = transacao.getCliente().getNif();
        } else {
            nomeCliente = transacao.getGuestNome() != null ? transacao.getGuestNome() : "—";
            nif = transacao.getGuestNif() != null ? transacao.getGuestNif() : "—";
        }

        String dataEmissao = transacao.getFaturaEmitidaEm() != null
                ? transacao.getFaturaEmitidaEm().format(FORMATTER)
                : LocalDateTime.now().format(FORMATTER);

        String linhaDetalhe = transacao.getLinha() != null
                ? transacao.getLinha().getOrigem() + " → " + transacao.getLinha().getDestino()
                : "Sem linha específica";

        String validoAte = transacao.getValidoAte() != null
                ? transacao.getValidoAte().format(FORMATTER)
                : "—";

        return construirHtmlFatura(
                transacao.getFaturaNumero(),
                dataEmissao,
                nomeCliente,
                nif,
                transacao.getTipoBilhete().getNome() + (transacao.getLinha() != null ? " — " + linhaDetalhe : ""),
                "Bilhete de transporte",
                transacao.getPreco() != null ? transacao.getPreco().toString() : "0.00",
                transacao.getDataCompra() != null ? transacao.getDataCompra().format(FORMATTER) : "—",
                validoAte
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Geração de PDF
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Gera PDF de fatura para um passe usando OpenPDF.
     *
     * @return bytes do PDF, ou null em caso de erro
     */
    public byte[] gerarPdfFaturaPasse(Passe passe, Pagamento pagamento) {
        try {
            return gerarPdf(
                    pagamento.getFaturaNumero(),
                    passe.getCliente().getNomeCompleto(),
                    passe.getCliente().getNif(),
                    passe.getTipoPasse().getNome() + " — " + passe.getCoroa().getNome(),
                    passe.getPrecoAplicado().toString(),
                    passe.getDataInicio() != null ? passe.getDataInicio().toString() : "—",
                    passe.getDataFim() != null ? passe.getDataFim().toString() : "—",
                    pagamento.getFaturaEmitidaEm()
            );
        } catch (Exception e) {
            log.error("[FATURA] Erro ao gerar PDF para passe id={}: {}", passe.getId(), e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PDF do Bilhete (documento de transporte com QR code)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Gera PDF do bilhete de transporte — distinto da fatura.
     * Inclui: QR code, código do bilhete, titular, tipo, linha, validade.
     *
     * @param transacao   transação paga
     * @param qrPng       bytes PNG do QR code (pode ser null — omite imagem)
     * @return bytes do PDF, ou null em caso de erro
     */
    public byte[] gerarPdfBilhete(Transacao transacao, byte[] qrPng) {
        try {
            String nome = transacao.getCliente() != null
                    ? transacao.getCliente().getNomeCompleto()
                    : (transacao.getGuestNome() != null ? transacao.getGuestNome() : "—");

            String linhaInfo = transacao.getLinha() != null
                    ? transacao.getLinha().getOrigem() + " → " + transacao.getLinha().getDestino()
                    : "—";

            String validoAteStr = transacao.getValidoAte() != null
                    ? transacao.getValidoAte().format(FORMATTER) : "—";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A5, 45, 45, 55, 45);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font fTitulo    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   16, Color.decode("#1e3a5f"));
            Font fSubtitulo = FontFactory.getFont(FontFactory.HELVETICA,        10, Color.DARK_GRAY);
            Font fNegrito   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   10, Color.BLACK);
            Font fNormal    = FontFactory.getFont(FontFactory.HELVETICA,        10, Color.BLACK);
            Font fCodigo    = FontFactory.getFont(FontFactory.COURIER,           8, Color.decode("#374151"));
            Font fNota      = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);

            // Cabeçalho
            Paragraph titulo = new Paragraph("TUB — Bilhete de Transporte", fTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(titulo);

            Paragraph subtitulo = new Paragraph(
                    transacao.getTipoBilhete().getNome() + (!"—".equals(linhaInfo) ? " — " + linhaInfo : ""),
                    fSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(14f);
            doc.add(subtitulo);

            // QR code
            if (qrPng != null) {
                com.lowagie.text.Image qrImg = com.lowagie.text.Image.getInstance(qrPng);
                qrImg.setAlignment(Element.ALIGN_CENTER);
                qrImg.scaleToFit(190, 190);
                doc.add(qrImg);
            }

            // Código abaixo do QR
            if (transacao.getCodigoQr() != null) {
                Paragraph codigo = new Paragraph(transacao.getCodigoQr(), fCodigo);
                codigo.setAlignment(Element.ALIGN_CENTER);
                codigo.setSpacingAfter(14f);
                doc.add(codigo);
            }

            // Dados do bilhete
            doc.add(new Paragraph("Titular: " + nome, fNegrito));
            doc.add(new Paragraph("Emitido em: " + (transacao.getDataCompra() != null
                    ? transacao.getDataCompra().format(FORMATTER) : "—"), fNormal));
            doc.add(new Paragraph("Válido até: " + validoAteStr, fNormal));
            if (transacao.getFaturaNumero() != null) {
                doc.add(new Paragraph("Ref. fatura: " + transacao.getFaturaNumero(), fNormal));
            }

            // Rodapé
            doc.add(new Paragraph(" "));
            Paragraph nota = new Paragraph(
                    "Bilhete válido para 1 viagem. Apresente o código QR ao motorista ou validador.", fNota);
            nota.setAlignment(Element.ALIGN_CENTER);
            doc.add(nota);

            Paragraph rodape = new Paragraph(
                    "TUB Transportes Urbanos de Braga, S.A. | NIF: 500000000 | www.tub.pt", fNota);
            rodape.setAlignment(Element.ALIGN_CENTER);
            doc.add(rodape);

            doc.close();
            log.info("[BILHETE] PDF gerado | transacao={} | titular={}", transacao.getId(), nome);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("[BILHETE] Erro ao gerar PDF bilhete id={}: {}", transacao.getId(), e.getMessage());
            return null;
        }
    }

    /**
     * Gera PDF de fatura para um bilhete usando OpenPDF.
     *
     * @return bytes do PDF, ou null em caso de erro
     */
    public byte[] gerarPdfFaturaBilhete(Transacao transacao) {
        try {
            String nomeCliente = transacao.getCliente() != null
                    ? transacao.getCliente().getNomeCompleto()
                    : (transacao.getGuestNome() != null ? transacao.getGuestNome() : "—");
            String nif = transacao.getCliente() != null
                    ? transacao.getCliente().getNif()
                    : (transacao.getGuestNif() != null ? transacao.getGuestNif() : "—");
            String descricao = transacao.getTipoBilhete().getNome()
                    + (transacao.getLinha() != null
                       ? " — " + transacao.getLinha().getOrigem() + " → " + transacao.getLinha().getDestino()
                       : "");
            String validoAte = transacao.getValidoAte() != null
                    ? transacao.getValidoAte().format(FORMATTER) : "—";

            return gerarPdf(
                    transacao.getFaturaNumero(),
                    nomeCliente,
                    nif,
                    descricao,
                    transacao.getPreco() != null ? transacao.getPreco().toString() : "0.00",
                    transacao.getDataCompra() != null ? transacao.getDataCompra().format(FORMATTER) : "—",
                    validoAte,
                    transacao.getFaturaEmitidaEm()
            );
        } catch (Exception e) {
            log.error("[FATURA] Erro ao gerar PDF para transação id={}: {}", transacao.getId(), e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers privados
    // ─────────────────────────────────────────────────────────────────────────

    private byte[] gerarPdf(String faturaNumero, String nomeCliente, String nif,
                             String descricao, String total, String dataInicio,
                             String dataFim, LocalDateTime emitidaEm) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50, 50, 60, 60);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        // Cabeçalho
        Font fonteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.decode("#1e3a5f"));
        Font fonteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
        Font fonteCampo = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        Font fonteCampoNegrito = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

        Paragraph titulo = new Paragraph("TUB — Transportes Urbanos de Braga", fonteTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(titulo);

        Paragraph subtitulo = new Paragraph("Fatura Simplificada", fonteSubtitulo);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(20f);
        doc.add(subtitulo);

        // Número e data
        doc.add(new Paragraph("Nº Fatura: " + faturaNumero, fonteCampoNegrito));
        String dataEmissaoStr = emitidaEm != null ? emitidaEm.format(FORMATTER) : LocalDateTime.now().format(FORMATTER);
        doc.add(new Paragraph("Data de Emissão: " + dataEmissaoStr, fonteCampo));

        doc.add(new Paragraph(" "));

        // Dados do cliente
        doc.add(new Paragraph("Dados do Cliente", fonteCampoNegrito));
        doc.add(new Paragraph("Nome: " + nomeCliente, fonteCampo));
        doc.add(new Paragraph("NIF: " + nif, fonteCampo));

        doc.add(new Paragraph(" "));

        // Detalhe do serviço
        doc.add(new Paragraph("Descrição do Serviço", fonteCampoNegrito));
        doc.add(new Paragraph("Serviço: " + descricao, fonteCampo));
        doc.add(new Paragraph("Período: " + dataInicio + " — " + dataFim, fonteCampo));

        doc.add(new Paragraph(" "));

        // Total
        Font fonteTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.decode("#1e3a5f"));
        Paragraph totalParagraph = new Paragraph("TOTAL (IVA incluído): " + total + " €", fonteTotal);
        totalParagraph.setSpacingBefore(10f);
        doc.add(totalParagraph);

        doc.add(new Paragraph(" "));

        // Rodapé
        Font fonteRodape = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);
        Paragraph rodape = new Paragraph(
                "TUB Transportes Urbanos de Braga, S.A. | NIF: 500000000 | www.tub.pt",
                fonteRodape);
        rodape.setAlignment(Element.ALIGN_CENTER);
        rodape.setSpacingBefore(30f);
        doc.add(rodape);

        doc.close();
        log.info("[FATURA] PDF gerado | fatura={} | cliente={}", faturaNumero, nomeCliente);
        return baos.toByteArray();
    }

    private String construirHtmlFatura(String faturaNumero, String dataEmissao,
                                        String nomeCliente, String nif,
                                        String descricao, String tipoServico,
                                        String total, String dataInicio, String dataFim) {
        return "<!DOCTYPE html><html lang='pt'><head><meta charset='UTF-8'>"
                + "<title>Fatura " + faturaNumero + "</title>"
                + "<style>body{font-family:Arial,sans-serif;max-width:700px;margin:40px auto;color:#333}"
                + "h1{color:#1e3a5f;border-bottom:2px solid #1e3a5f;padding-bottom:8px}"
                + "table{width:100%;border-collapse:collapse;margin-top:16px}"
                + "td{padding:8px 12px;border:1px solid #ddd}"
                + "td:first-child{font-weight:bold;background:#f5f5f5;width:35%}"
                + ".total{font-size:1.3em;font-weight:bold;color:#1e3a5f;border-top:2px solid #1e3a5f}"
                + ".footer{margin-top:40px;font-size:0.8em;color:#999;text-align:center}"
                + "</style></head><body>"
                + "<h1>TUB — Transportes Urbanos de Braga</h1>"
                + "<h2 style='color:#555'>Fatura Simplificada</h2>"
                + "<table>"
                + "<tr><td>Nº Fatura</td><td>" + faturaNumero + "</td></tr>"
                + "<tr><td>Data de Emissão</td><td>" + dataEmissao + "</td></tr>"
                + "<tr><td>Nome do Cliente</td><td>" + nomeCliente + "</td></tr>"
                + "<tr><td>NIF</td><td>" + nif + "</td></tr>"
                + "<tr><td>Tipo de Serviço</td><td>" + tipoServico + "</td></tr>"
                + "<tr><td>Descrição</td><td>" + descricao + "</td></tr>"
                + "<tr><td>Data Início</td><td>" + dataInicio + "</td></tr>"
                + "<tr><td>Data Fim / Validade</td><td>" + dataFim + "</td></tr>"
                + "<tr class='total'><td>TOTAL (IVA incl.)</td><td>" + total + " €</td></tr>"
                + "</table>"
                + "<p class='footer'>TUB Transportes Urbanos de Braga, S.A. | NIF: 500000000 | www.tub.pt</p>"
                + "</body></html>";
    }
}
