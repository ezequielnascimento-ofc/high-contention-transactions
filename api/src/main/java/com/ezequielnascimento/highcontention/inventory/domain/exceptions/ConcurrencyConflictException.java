package com.ezequielnascimento.highcontention.inventory.domain.exceptions;

import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;

/**
 * The decrease kept losing to concurrent writers and gave up after the
 * configured number of attempts. Nothing was changed; the client may retry.
 */
public class ConcurrencyConflictException extends InventoryDomainException {

    public ConcurrencyConflictException(InventoryId inventoryId, int attempts) {
        super("Gave up decreasing inventory " + inventoryId.value() + " after " + attempts
                + " attempts because of concurrent updates; retry later");
    }
}
