package com.ezequielnascimento.highcontention.unit.inventory.domain;

import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InventoryIdTest {

    @Test
    void shouldGenerateInventoryId() {
        InventoryId id = InventoryId.generate();

        assertNotNull(id);
        assertNotNull(id.value());
    }

    @Test
    void shouldCreateInventoryIdFromUuid() {
        UUID uuid = UUID.randomUUID();

        InventoryId id = new InventoryId(uuid);

        assertEquals(uuid, id.value());
    }

    @Test
    void shouldRejectNullValue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new InventoryId(null)
        );
    }
}
