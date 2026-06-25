package com.example.loginapi.service.clientes;

import com.example.loginapi.model.clientes.DocumentoEstatuto;
import com.example.loginapi.model.clientes.PedidoEstatuto;
import com.example.loginapi.model.clientes.enums.EstadoPedidoEstatuto;
import com.example.loginapi.repository.clientes.DocumentoEstatutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.example.loginapi.model.clientes.Cliente;


/**
 * Serviço de upload e gestão de documentos para pedidos de estatuto.
 *
 * Segurança:
 * - MIME type verificado via magic bytes (não confia no Content-Type do cliente)
 * - Extensão validada contra whitelist
 * - Nome do ficheiro sanitizado (previne path traversal)
 * - Tamanho máximo: 5MB (configurável)
 * - Tipos aceites: image/jpeg, image/png, application/pdf
 */
@Service
public class DocumentoService {

    private static final Set<String> TIPOS_ACEITES = Set.of(
            "image/jpeg", "image/png", "application/pdf"
    );

    private static final Set<String> EXTENSOES_ACEITES = Set.of(
            ".jpg", ".jpeg", ".png", ".pdf"
    );

    private static final Map<String, String> EXTENSAO_PARA_MIME = Map.of(
            ".jpg",  "image/jpeg",
            ".jpeg", "image/jpeg",
            ".png",  "image/png",
            ".pdf",  "application/pdf"
    );

    @Autowired
    private DocumentoEstatutoRepository documentoRepo;

    @Value("${tub.documentos.storage-dir:./uploads/estatutos}")
    private String storageDir;

    @Value("${tub.documentos.max-size-bytes:5242880}") // 5 MB
    private long maxSizeBytes;

    /**
     * Faz upload de um documento e associa ao pedido.
     */
    public DocumentoEstatuto upload(PedidoEstatuto pedido, MultipartFile file) throws IOException {
        // 1. Validar estado do pedido
        if (pedido.getEstado() != EstadoPedidoEstatuto.DRAFT
                && pedido.getEstado() != EstadoPedidoEstatuto.CORRECTION_REQUESTED) {
            throw new IllegalStateException(
                    "Só é possível adicionar documentos a pedidos em DRAFT ou CORRECTION_REQUESTED.");
        }

        // 2. Validar ficheiro não vazio
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("O ficheiro está vazio.");
        }

        // 3. Validar tamanho
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(
                    "O ficheiro excede o tamanho máximo permitido (" + (maxSizeBytes / 1_048_576) + "MB).");
        }

        // 4. Sanitizar e validar nome do ficheiro
        String nomeSanitizado = sanitizarNomeFicheiro(file.getOriginalFilename());
        String extensao = extrairExtensao(nomeSanitizado).toLowerCase();

        if (!EXTENSOES_ACEITES.contains(extensao)) {
            throw new IllegalArgumentException(
                    "Extensão de ficheiro não permitida. Tipos aceites: PDF, JPEG, PNG.");
        }

        // 5. Detectar MIME real via magic bytes (não confia no Content-Type do cliente)
        String mimeDetectado = detectarMimePorMagicBytes(file);
        if (mimeDetectado == null || !TIPOS_ACEITES.contains(mimeDetectado)) {
            throw new IllegalArgumentException(
                    "Conteúdo do ficheiro não reconhecido ou não permitido. Tipos aceites: JPEG, PNG, PDF.");
        }

        // 6. Verificar consistência extensão ↔ MIME real (previne disfarce de tipo)
        String mimeEsperadoPelaExtensao = EXTENSAO_PARA_MIME.get(extensao);
        if (!mimeDetectado.equals(mimeEsperadoPelaExtensao)) {
            throw new IllegalArgumentException(
                    "A extensão do ficheiro não corresponde ao seu conteúdo real.");
        }

        // 7. Guardar ficheiro no filesystem com nome gerado por UUID
        String nomeStorage = UUID.randomUUID() + extensao;
        String subDir = "pedido_" + pedido.getId();
        Path dirPath = Paths.get(storageDir, subDir).normalize();

        // Garantir que o diretório resolvido está dentro de storageDir (previne path traversal)
        Path storageDirAbsoluto = Paths.get(storageDir).toAbsolutePath().normalize();
        if (!dirPath.toAbsolutePath().normalize().startsWith(storageDirAbsoluto)) {
            throw new IllegalArgumentException("Caminho de destino inválido.");
        }

        Files.createDirectories(dirPath);
        Path filePath = dirPath.resolve(nomeStorage);

        try (InputStream is = file.getInputStream()) {
            Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        // 8. Guardar metadados na BD
        DocumentoEstatuto doc = new DocumentoEstatuto();
        doc.setPedido(pedido);
        doc.setNomeFicheiro(nomeSanitizado);
        doc.setTipoConteudo(mimeDetectado);
        doc.setTamanhoBytes(file.getSize());
        doc.setCaminhoStorage(subDir + "/" + nomeStorage);
        return documentoRepo.save(doc);
    }

    /**
     * Lista documentos de um pedido.
     */
    public List<DocumentoEstatuto> listarDocumentos(PedidoEstatuto pedido) {
        return documentoRepo.findByPedido(pedido);
    }

    /**
     * Obtém o caminho absoluto de um documento para download.
     */
    public Path obterCaminhoFisico(DocumentoEstatuto doc) {
        return Paths.get(storageDir, doc.getCaminhoStorage()).normalize();
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    /**
     * Detecta o MIME type real do ficheiro lendo os magic bytes.
     * Ignora o Content-Type declarado pelo cliente.
     */
    private String detectarMimePorMagicBytes(MultipartFile file) throws IOException {
        byte[] header = new byte[8];
        int read;
        try (InputStream is = file.getInputStream()) {
            read = is.read(header, 0, 8);
        }

        if (read < 3) {
            return null;
        }

        // PDF: 25 50 44 46 ("%PDF")
        if (read >= 4
                && header[0] == 0x25 && header[1] == 0x50
                && header[2] == 0x44 && header[3] == 0x46) {
            return "application/pdf";
        }

        // JPEG: FF D8 FF
        if ((header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (read >= 8
                && (header[0] & 0xFF) == 0x89
                && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47
                && header[4] == 0x0D && header[5] == 0x0A
                && (header[6] & 0xFF) == 0x1A && header[7] == 0x0A) {
            return "image/png";
        }

        return null;
    }

    /**
     * Sanitiza o nome do ficheiro:
     * - Remove componentes de caminho (previne path traversal)
     * - Remove caracteres de controlo e perigosos
     * - Limita o comprimento a 100 caracteres
     */
    private String sanitizarNomeFicheiro(String filename) {
        if (filename == null || filename.isBlank()) {
            return "documento";
        }
        // Extrair apenas o nome base (sem diretórios)
        String nome = Paths.get(filename).getFileName().toString();
        // Remover null bytes, caracteres de controlo e caracteres especiais perigosos
        nome = nome.replaceAll("[\\x00-\\x1F\\x7F/\\\\:*?\"<>|]", "_");
        // Limitar comprimento
        if (nome.length() > 100) {
            String ext = extrairExtensao(nome);
            String base = nome.substring(0, 100 - ext.length());
            nome = base + ext;
        }
        return nome.isBlank() ? "documento" : nome;
    }

    private String extrairExtensao(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot >= 0 ? filename.substring(lastDot) : "";
    }
}
