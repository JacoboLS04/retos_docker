package co.edu.uniquindio.retos.service;

import co.edu.uniquindio.retos.model.SaludoRegistro;
import co.edu.uniquindio.retos.repository.SaludoRegistroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class SaludoRegistroService {
    private final SaludoRegistroRepository repo;

    public SaludoRegistroService(SaludoRegistroRepository repo) {
        this.repo = repo;
    }

    public SaludoRegistro registrar(String nombre) {
        SaludoRegistro r = new SaludoRegistro(nombre, OffsetDateTime.now());
        return repo.save(r);
    }

    public Page<SaludoRegistro> listar(Pageable pageable) {
        return repo.findAll(pageable);
    }
}
