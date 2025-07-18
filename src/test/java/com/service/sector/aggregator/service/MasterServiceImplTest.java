package com.service.sector.aggregator.service;

import com.service.sector.aggregator.data.dto.CreateMasterRequest;
import com.service.sector.aggregator.data.entity.Master;
import com.service.sector.aggregator.data.repositories.MasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MasterServiceImplTest {

    @Mock
    private MasterRepository repo;

    @InjectMocks
    private MasterServiceImpl service;

    private Master john;
    private Master jane;

    @BeforeEach
    void setUp() {
        john = new Master(1L, "John Doe", "plumber");
        jane = new Master(2L, "Jane Roe", "hairdresser");
    }

    /* ------------------------------------------------------------------ *
     * getAllMasters()                                                    *
     * ------------------------------------------------------------------ */

    @Test
    void getAllMasters_returnsListFromRepository() {
        when(repo.findAll()).thenReturn(List.of(john, jane));

        List<Master> result = service.getAllMasters();

        assertEquals(2, result.size());
        assertEquals(john, result.get(0));
        assertEquals(jane, result.get(1));
        verify(repo).findAll();
    }

    /* ------------------------------------------------------------------ *
     * getMasterById()                                                    *
     * ------------------------------------------------------------------ */

    @Test
    void getMasterById_found_returnsOptionalWithEntity() {
        when(repo.findById(1L)).thenReturn(Optional.of(john));

        Optional<Master> result = service.getMasterById(1L);

        assertTrue(result.isPresent());
        assertEquals(john, result.get());
        verify(repo).findById(1L);
    }

    @Test
    void getMasterById_notFound_returnsEmptyOptional() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        Optional<Master> result = service.getMasterById(99L);

        assertTrue(result.isEmpty());
        verify(repo).findById(99L);
    }

    /* ------------------------------------------------------------------ *
     * createMaster()                                                     *
     * ------------------------------------------------------------------ */

    @Test
    void createMaster_savesMappedEntityAndReturnsSaved() {
        // given
        CreateMasterRequest req = new CreateMasterRequest("Alice", "electrician");
        Master saved = new Master(10L, "Alice", "electrician");

        // repo should return the entity with generated ID
        when(repo.save(any(Master.class))).thenReturn(saved);

        // when
        Master result = service.createMaster(req);

        // then
        // verify mapping
        ArgumentCaptor<Master> captor = ArgumentCaptor.forClass(Master.class);
        verify(repo).save(captor.capture());

        Master passedToRepo = captor.getValue();
        assertNull(passedToRepo.getId(), "ID must be null before save");
        assertEquals(req.name(), passedToRepo.getName());
        assertEquals(req.speciality(), passedToRepo.getSpeciality());

        // verify return value
        assertEquals(saved, result);
    }
}