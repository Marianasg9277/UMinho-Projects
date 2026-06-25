package com.example.loginapi.service.comunicacao;

import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.titulos.Transacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.comunicacao.Aviso;
import com.example.loginapi.model.infraestrutura.Linha;


/**
 * Serviço de envio de emails.
 *
 * Por defeito (tub.email.enabled=false) apenas regista no log — útil
 * para desenvolvimento/CI sem servidor SMTP.
 * Para activar o envio real, definir EMAIL_ENABLED=true e configurar
 * as propriedades spring.mail.* (ver application.properties).
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${tub.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${spring.mail.username:noreply@tub.pt}")
    private String remetente;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    // ─────────────────────────────────────────────────────────────────────────
    // Envio de bilhete (guest ou autenticado)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Envia email de confirmação de compra de bilhete com QR code inline e PDF do bilhete em anexo.
     *
     * @param imagemQrBase64  data URI base64 da imagem QR (pode ser null)
     * @param pdfBilhete      bytes do PDF do bilhete (pode ser null — envia sem anexo)
     */
    public void enviarBilhete(Transacao transacao, String imagemQrBase64, byte[] pdfBilhete) {
        String destinatario = transacao.getGuestEmail() != null
                ? transacao.getGuestEmail()
                : (transacao.getCliente() != null ? transacao.getCliente().getUtilizador().getEmail() : null);
        if (destinatario == null) {
            log.warn("[EMAIL] enviarBilhete: sem destinatário para transação id={}", transacao.getId());
            return;
        }

        String nome = transacao.getGuestNome() != null
                ? transacao.getGuestNome()
                : (transacao.getCliente() != null ? transacao.getCliente().getNomeCompleto() : "Cliente");

        String assunto = "TUB – O seu bilhete #" + transacao.getId();
        String corpo = construirCorpoEmailBilhete(transacao, nome, imagemQrBase64);

        if (pdfBilhete != null) {
            enviarComAnexo(destinatario, assunto, corpo, pdfBilhete,
                    "bilhete-" + transacao.getId() + ".pdf", "application/pdf");
        } else {
            enviar(destinatario, assunto, corpo);
        }
        log.info("[EMAIL] Bilhete id={} enviado para {}", transacao.getId(), destinatario);
    }

    /** Compatibilidade — sem PDF em anexo. */
    public void enviarBilhete(Transacao transacao, String imagemQrBase64) {
        enviarBilhete(transacao, imagemQrBase64, null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Envio de fatura
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Envia fatura HTML por email.
     *
     * @param destinatario email do destinatário
     * @param assunto      assunto do email
     * @param corpoHtml    conteúdo HTML da fatura
     * @param pdfBytes     bytes do PDF (pode ser null — nesse caso não anexa)
     * @param faturaNumero número da fatura (para nomear o ficheiro)
     */
    public void enviarFatura(String destinatario, String assunto,
                              String corpoHtml, byte[] pdfBytes, String faturaNumero) {
        if (pdfBytes != null && emailEnabled && mailSender != null) {
            enviarComAnexo(destinatario, assunto, corpoHtml, pdfBytes,
                    "fatura-" + faturaNumero + ".pdf", "application/pdf");
        } else {
            enviar(destinatario, assunto, corpoHtml);
        }
        log.info("[EMAIL] Fatura {} enviada para {}", faturaNumero, destinatario);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Notificações de passe
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Email de confirmação de passe ativado.
     */
    public void enviarConfirmacaoPasse(String emailDestino, Passe passe) {
        String assunto = "TUB – Passe ativado com sucesso";
        String corpo = "<h2>O seu passe foi ativado!</h2>"
                + "<p>Tipo: <strong>" + passe.getTipoPasse().getNome() + "</strong></p>"
                + "<p>Zona: <strong>" + passe.getCoroa().getNome() + "</strong></p>"
                + "<p>Válido de <strong>" + passe.getDataInicio() + "</strong> a <strong>"
                + passe.getDataFim() + "</strong></p>"
                + "<p>Preço pago: <strong>" + passe.getPrecoAplicado() + " €</strong></p>"
                + "<p>Fatura: <strong>" + passe.getCodigoQr() + "</strong></p>"
                + "<br><p>Obrigado por escolher a TUB.</p>";
        enviar(emailDestino, assunto, corpo);
        log.info("[EMAIL] Confirmação de passe id={} enviada para {}", passe.getId(), emailDestino);
    }

    /**
     * Email de aviso de expiração próxima.
     */
    public void enviarAvisoExpiracao(String emailDestino, Passe passe, int diasRestantes) {
        String assunto = "TUB – O seu passe expira em " + diasRestantes + " dia(s)";
        String corpo = "<h2>Atenção: o seu passe está prestes a expirar</h2>"
                + "<p>O passe <strong>" + passe.getTipoPasse().getNome() + "</strong> ("
                + passe.getCoroa().getNome() + ") expira em <strong>"
                + passe.getDataFim() + "</strong> (" + diasRestantes + " dia(s)).</p>"
                + "<p>Aceda à aplicação TUB para o renovar antes que expire.</p>";
        enviar(emailDestino, assunto, corpo);
        log.info("[EMAIL] Aviso de expiração passe id={} ({} dias) enviado para {}",
                passe.getId(), diasRestantes, emailDestino);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Métodos internos
    // ─────────────────────────────────────────────────────────────────────────

    private void enviar(String destinatario, String assunto, String corpoHtml) {
        if (!emailEnabled || mailSender == null) {
            log.info("[EMAIL-SIMULADO] Para={} | Assunto={}", destinatario, assunto);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
            helper.setFrom(remetente);
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(corpoHtml, true);
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("[EMAIL] Simulação/Erro ao enviar para {}: SMTP indisponível ou {}", destinatario, e.getMessage());
        }
    }

    private void enviarComAnexo(String destinatario, String assunto, String corpoHtml,
                                 byte[] anexoBytes, String nomeAnexo, String tipoAnexo) {
        if (!emailEnabled || mailSender == null) {
            log.info("[EMAIL-SIMULADO] Para={} | Assunto={} | Anexo={}", destinatario, assunto, nomeAnexo);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(remetente);
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(corpoHtml, true);
            helper.addAttachment(nomeAnexo,
                    () -> new java.io.ByteArrayInputStream(anexoBytes), tipoAnexo);
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("[EMAIL] Simulação/Erro ao enviar com anexo para {}: SMTP indisponível ou {}", destinatario, e.getMessage());
        }
    }

    private String construirCorpoEmailBilhete(Transacao t, String nome, String imagemQrBase64) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>Confirmação de compra — TUB</h2>");
        sb.append("<p>Olá, <strong>").append(nome).append("</strong>!</p>");
        sb.append("<p>O seu bilhete foi emitido com sucesso.</p>");
        sb.append("<table style='border-collapse:collapse;width:100%;max-width:500px'>");
        sb.append("<tr><td><strong>Bilhete nº</strong></td><td>#").append(t.getId()).append("</td></tr>");
        sb.append("<tr><td><strong>Tipo</strong></td><td>").append(t.getTipoBilhete().getNome()).append("</td></tr>");
        if (t.getLinha() != null) {
            sb.append("<tr><td><strong>Linha</strong></td><td>")
              .append(t.getLinha().getOrigem()).append(" → ").append(t.getLinha().getDestino())
              .append("</td></tr>");
        }
        sb.append("<tr><td><strong>Preço</strong></td><td>").append(t.getPreco()).append(" €</td></tr>");
        sb.append("<tr><td><strong>Data compra</strong></td><td>").append(t.getDataCompra()).append("</td></tr>");
        if (t.getValidoAte() != null) {
            sb.append("<tr><td><strong>Válido até</strong></td><td>").append(t.getValidoAte()).append("</td></tr>");
        }
        sb.append("<tr><td><strong>Código QR</strong></td><td><code>")
          .append(t.getCodigoQr()).append("</code></td></tr>");
        if (t.getFaturaNumero() != null) {
            sb.append("<tr><td><strong>Fatura</strong></td><td>").append(t.getFaturaNumero()).append("</td></tr>");
        }
        sb.append("</table>");
        if (imagemQrBase64 != null && !imagemQrBase64.isBlank()) {
            sb.append("<br><p><strong>QR Code:</strong></p>");
            sb.append("<img src='").append(imagemQrBase64).append("' alt='QR Code' style='width:200px;height:200px'/>");
        }
        sb.append("<br><p>Boas viagens! — TUB Transportes Urbanos de Braga</p>");
        return sb.toString();
    }
}
