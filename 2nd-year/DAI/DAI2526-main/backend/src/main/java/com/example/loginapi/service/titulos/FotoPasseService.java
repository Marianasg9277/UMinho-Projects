package com.example.loginapi.service.titulos;

import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.repository.clientes.ClienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.example.loginapi.model.titulos.Passe;


/**
 * Gestão segura de fotografia para passe digital.
 *
 * Segurança:
 * - Apenas JPEG e PNG aceites (sem PDF)
 * - MIME verificado via magic bytes, não via Content-Type
 * - Extensão validada contra whitelist e consistente com MIME real
 * - Nome do ficheiro gerado por UUID (sem input do cliente no path)
 * - Path traversal prevenido
 * - Ficheiro antigo só é removido após nova foto guardada e cliente atualizado
 */
@Service
public class FotoPasseService {

    private static final Logger log = LoggerFactory.getLogger(FotoPasseService.class);

    private static final Set<String> EXTENSOES_ACEITES = Set.of(".jpg", ".jpeg", ".png");

    private static final Map<String, String> EXTENSAO_PARA_MIME = Map.of(
            ".jpg",  "image/jpeg",
            ".jpeg", "image/jpeg",
            ".png",  "image/png"
    );

    private static final Map<String, String> MIME_PARA_EXTENSAO = Map.of(
            "image/jpeg", ".jpg",
            "image/png",  ".png"
    );

    @Value("${tub.fotos-passe.storage-dir:./uploads/fotos-passe}")
    private String storageDir;

    @Value("${tub.fotos-passe.max-size-bytes:5242880}")
    private long maxSizeBytes;

    @Autowired
    private ClienteRepository clienteRepo;

    /**
     * Valida, guarda a nova foto e atualiza o cliente.
     * O ficheiro antigo só é removido após o cliente ser atualizado com sucesso.
     *
     * @return MIME type da imagem guardada (para Content-Type no GET)
     */
    @Transactional
    public String guardarFoto(Cliente cliente, MultipartFile foto) throws IOException {
        // 1. Ficheiro não vazio
        if (foto == null || foto.isEmpty()) {
            throw new IllegalArgumentException("O ficheiro está vazio.");
        }

        // 2. Tamanho máximo
        if (foto.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(
                    "O ficheiro excede o tamanho máximo de " + (maxSizeBytes / 1_048_576) + " MB.");
        }

        // 3. Extensão
        String extensao = extrairExtensao(foto.getOriginalFilename()).toLowerCase();
        if (!EXTENSOES_ACEITES.contains(extensao)) {
            throw new IllegalArgumentException(
                    "Apenas imagens JPEG e PNG são aceites como fotografia de passe.");
        }

        // 4. MIME real via magic bytes
        String mimeDetectado = detectarMimePorMagicBytes(foto);
        if (mimeDetectado == null || !MIME_PARA_EXTENSAO.containsKey(mimeDetectado)) {
            throw new IllegalArgumentException(
                    "Conteúdo do ficheiro não reconhecido. Apenas JPEG e PNG são aceites.");
        }

        // 5. Consistência extensão ↔ MIME real
        if (!mimeDetectado.equals(EXTENSAO_PARA_MIME.get(extensao))) {
            throw new IllegalArgumentException(
                    "A extensão do ficheiro não corresponde ao seu conteúdo real.");
        }

        // 6. Preparar destino com UUID — sem qualquer input do cliente no path
        String nomeStorage = UUID.randomUUID() + MIME_PARA_EXTENSAO.get(mimeDetectado);
        String subDir = "cliente_" + cliente.getId();
        Path storageDirAbsoluto = Paths.get(storageDir).toAbsolutePath().normalize();
        Path dirPath = storageDirAbsoluto.resolve(subDir).normalize();

        // Prevenir path traversal
        if (!dirPath.startsWith(storageDirAbsoluto)) {
            throw new IllegalArgumentException("Caminho de destino inválido.");
        }

        Files.createDirectories(dirPath);
        Path novoFicheiro = dirPath.resolve(nomeStorage);

        // 7. Guardar novo ficheiro no disco
        try (InputStream is = foto.getInputStream()) {
            Files.copy(is, novoFicheiro, StandardCopyOption.REPLACE_EXISTING);
        }

        // 8. Registar path antigo antes de atualizar
        String pathAntigo = cliente.getFotoPassePath();

        // 9. Atualizar cliente na BD com novo path (relativo ao storageDir)
        String novoPath = subDir + "/" + nomeStorage;
        cliente.setFotoPassePath(novoPath);
        clienteRepo.save(cliente);

        // 10. Só agora remover ficheiro antigo (cliente já foi atualizado com sucesso)
        if (pathAntigo != null) {
            tentarEliminarFicheiroAntigo(pathAntigo);
        }

        log.info("Foto de passe atualizada para cliente id={}", cliente.getId());
        return mimeDetectado;
    }

    /**
     * Remove a fotografia do passe do cliente.
     * A BD é limpa antes de apagar o ficheiro — se a deleção falhar,
     * o registo na BD fica null (foto órfã no disco, mas estado consistente).
     */
    @Transactional
    public void removerFoto(Cliente cliente) {
        String path = cliente.getFotoPassePath();
        if (path == null) {
            throw new IllegalStateException("O cliente não tem fotografia registada.");
        }

        // 1. Limpar na BD primeiro — garantia de estado consistente
        cliente.setFotoPassePath(null);
        clienteRepo.save(cliente);

        // 2. Apagar ficheiro físico (após save com sucesso)
        tentarEliminarFicheiroAntigo(path);

        log.info("Foto de passe removida para cliente id={}", cliente.getId());
    }

    /**
     * Lê os bytes da foto do cliente para servir no GET.
     *
     * @return array de bytes da imagem
     * @throws IOException se o ficheiro não existir ou não for legível
     */
    public byte[] lerFoto(Cliente cliente) throws IOException {
        String path = cliente.getFotoPassePath();
        if (path == null) {
            throw new IllegalStateException("Cliente não tem fotografia registada.");
        }

        Path storageDirAbsoluto = Paths.get(storageDir).toAbsolutePath().normalize();
        Path ficheiroPath = storageDirAbsoluto.resolve(path).normalize();

        // Prevenir path traversal
        if (!ficheiroPath.startsWith(storageDirAbsoluto)) {
            throw new IllegalArgumentException("Caminho inválido.");
        }

        if (!Files.exists(ficheiroPath)) {
            throw new IOException("Ficheiro de fotografia não encontrado.");
        }

        return Files.readAllBytes(ficheiroPath);
    }

    /**
     * Infere o Content-Type a partir da extensão do path guardado na BD.
     */
    public String inferirContentType(String fotoPassePath) {
        if (fotoPassePath == null) return "image/jpeg";
        String ext = extrairExtensao(fotoPassePath).toLowerCase();
        return EXTENSAO_PARA_MIME.getOrDefault(ext, "image/jpeg");
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    private void tentarEliminarFicheiroAntigo(String pathRelativo) {
        try {
            Path storageDirAbsoluto = Paths.get(storageDir).toAbsolutePath().normalize();
            Path ficheiroAntigo = storageDirAbsoluto.resolve(pathRelativo).normalize();
            if (ficheiroAntigo.startsWith(storageDirAbsoluto)) {
                Files.deleteIfExists(ficheiroAntigo);
            }
        } catch (IOException e) {
            log.warn("Não foi possível eliminar foto antiga ({}): {}", pathRelativo, e.getMessage());
        }
    }

    private String detectarMimePorMagicBytes(MultipartFile file) throws IOException {
        byte[] header = new byte[8];
        int read;
        try (InputStream is = file.getInputStream()) {
            read = is.read(header, 0, 8);
        }
        if (read < 3) return null;

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

    private String extrairExtensao(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot >= 0 ? filename.substring(lastDot) : "";
    }
}
