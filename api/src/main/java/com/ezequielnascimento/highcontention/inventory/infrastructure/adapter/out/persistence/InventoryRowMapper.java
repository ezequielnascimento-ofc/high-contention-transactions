package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence;

import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/** Maps an {@code inventory} row (id, product_id, quantity, created_at, updated_at). */
public final class InventoryRowMapper implements RowMapper<Inventory> {

    public static final InventoryRowMapper INSTANCE = new InventoryRowMapper();

    /** Column list matching {@link #mapRow}, for SELECT and RETURNING clauses. */
    public static final String COLUMNS = "id, product_id, quantity, created_at, updated_at";

    private InventoryRowMapper() {
    }

    @Override
    public Inventory mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Inventory.reconstitute(
                new InventoryId(UUID.fromString(rs.getString("id"))),
                new ProductId(UUID.fromString(rs.getString("product_id"))),
                rs.getInt("quantity"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}
