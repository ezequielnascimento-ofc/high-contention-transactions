package com.ezequielnascimento.highcontention.unit.inventory.domain;

import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InventoryIdTest {

    @Nested
    class InventoryIdCreation {

        @Test
        void shouldCreateInventoryIdFromGivenUuid() {
            UUID uuid = UUID.randomUUID();

            InventoryId id = new InventoryId(uuid);

            assertEquals(uuid, id.value());
        }

        @Test
        void shouldRejectCreationWithNullValue() {
            assertThrows(IllegalArgumentException.class, () -> new InventoryId(null));
        }
    }

    @Nested
    class InventoryIdGeneration {

        @Test
        void shouldGenerateNonNullId() {
            InventoryId id = InventoryId.generate();

            assertNotNull(id);
            assertNotNull(id.value());
        }

        @Test
        void shouldGenerateDistinctIdsOnEachCall() {
            InventoryId first = InventoryId.generate();
            InventoryId second = InventoryId.generate();

            assertNotEquals(first, second);
        }
    }

    @Nested
    class InventoryIdEquality {

        @Test
        void shouldConsiderIdsEqualWhenUnderlyingValueMatches() {
            UUID value = UUID.randomUUID();

            InventoryId first = new InventoryId(value);
            InventoryId second = new InventoryId(value);

            assertEquals(first, second);
            assertEquals(first.hashCode(), second.hashCode());
        }

        @Test
        void shouldConsiderIdsDifferentWhenUnderlyingValueDiffers() {
            InventoryId first = new InventoryId(UUID.randomUUID());
            InventoryId second = new InventoryId(UUID.randomUUID());

            assertNotEquals(first, second);
        }
    }
}