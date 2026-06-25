package com.example.loginapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import com.example.loginapi.model.infraestrutura.Coroa;
import com.example.loginapi.model.infraestrutura.Linha;


public class CompraGuestRequestDTO {

    @NotNull(message = "tipoBilheteId é obrigatório")
    @Min(value = 1, message = "tipoBilheteId deve ser um identificador válido (>= 1)")
    private Long tipoBilheteId;

    @Min(value = 1, message = "linhaId deve ser um identificador válido (>= 1)")
    private Long linhaId;

    @NotBlank(message = "guestEmail é obrigatório")
    @Email(message = "guestEmail deve ser um endereço de e-mail válido")
    private String guestEmail;

    @NotBlank(message = "guestNome é obrigatório")
    private String guestNome;

    @Pattern(regexp = "\\d{9}", message = "guestNif deve ter exactamente 9 dígitos")
    private String guestNif;

    /** Coroa/zona tarifária (opcional — usar quando não há linha associada). */
    @Min(value = 1, message = "coroaId deve ser um identificador válido (>= 1)")
    private Long coroaId;

    public Long getTipoBilheteId() { return tipoBilheteId; }
    public void setTipoBilheteId(Long tipoBilheteId) { this.tipoBilheteId = tipoBilheteId; }

    public Long getLinhaId() { return linhaId; }
    public void setLinhaId(Long linhaId) { this.linhaId = linhaId; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
    public String getGuestNome() { return guestNome; }
    public void setGuestNome(String guestNome) { this.guestNome = guestNome; }
    public String getGuestNif() { return guestNif; }
    public void setGuestNif(String guestNif) { this.guestNif = guestNif; }
    public Long getCoroaId() { return coroaId; }
    public void setCoroaId(Long coroaId) { this.coroaId = coroaId; }
}
