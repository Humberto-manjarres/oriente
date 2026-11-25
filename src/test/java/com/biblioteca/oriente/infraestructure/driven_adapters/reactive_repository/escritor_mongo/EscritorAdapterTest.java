package com.biblioteca.oriente.infraestructure.driven_adapters.reactive_repository.escritor_mongo;

import com.biblioteca.oriente.domain.model.escritor.Escritor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EscritorAdapterTest {

    @Mock
    private EscritorRepository repository;

    private EscritorAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new EscritorAdapter(repository, Mappers.getMapper(EscritorTransformerRepository.class));
    }

    @Test
    void crearEscritorDebeGuardarEntityYRetornarDominio() {
        Escritor nuevo = new Escritor(null, "1001", "Gabriel García Márquez", "Colombiana");
        EscritorEntity guardada = new EscritorEntity("abc123", "1001", "Gabriel García Márquez", "Colombiana");
        when(repository.save(any(EscritorEntity.class))).thenReturn(Mono.just(guardada));

        StepVerifier.create(adapter.crearEscritor(nuevo))
                .expectNext(new Escritor("abc123", "1001", "Gabriel García Márquez", "Colombiana"))
                .verifyComplete();

        ArgumentCaptor<EscritorEntity> captor = ArgumentCaptor.forClass(EscritorEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
        assertThat(captor.getValue().getIdentificacion()).isEqualTo("1001");
        assertThat(captor.getValue().getNombre()).isEqualTo("Gabriel García Márquez");
        assertThat(captor.getValue().getNacionalidad()).isEqualTo("Colombiana");
    }

    @Test
    void crearEscritorDebePropagarErrorDelRepositorio() {
        Escritor nuevo = new Escritor(null, "1001", "Gabriel García Márquez", "Colombiana");
        when(repository.save(any(EscritorEntity.class))).thenReturn(Mono.error(new IllegalStateException("fallo mongo")));

        StepVerifier.create(adapter.crearEscritor(nuevo))
                .expectErrorMessage("fallo mongo")
                .verify();
    }

    @Test
    void consultarEscritorDebeRetornarDominioCuandoExiste() {
        EscritorEntity entity = new EscritorEntity("abc123", "1001", "Gabriel García Márquez", "Colombiana");
        when(repository.findByIdentificacion("1001")).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.consultarEscritor("1001"))
                .expectNext(new Escritor("abc123", "1001", "Gabriel García Márquez", "Colombiana"))
                .verifyComplete();
    }

    @Test
    void consultarEscritorDebeCompletarVacioCuandoNoExiste() {
        when(repository.findByIdentificacion("9999")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.consultarEscritor("9999"))
                .verifyComplete();
    }

    @Test
    void consultarEscritorDebePropagarErrorDelRepositorio() {
        when(repository.findByIdentificacion("1001")).thenReturn(Mono.error(new IllegalStateException("fallo mongo")));

        StepVerifier.create(adapter.consultarEscritor("1001"))
                .expectErrorMessage("fallo mongo")
                .verify();
    }
}
