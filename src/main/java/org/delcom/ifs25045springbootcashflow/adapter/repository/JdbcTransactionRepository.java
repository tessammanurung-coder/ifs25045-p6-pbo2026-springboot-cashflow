package org.delcom.ifs25045springbootcashflow.adapter.repository;

import org.delcom.ifs25045springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25045springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25045springbootcashflow.domain.repository.ITransactionRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Repository transaksi berbasis JDBC (PostgreSQL).
 */
public class JdbcTransactionRepository implements ITransactionRepository {
    private static final String COLUMNS = "id, user_id, description, amount, type, proof_path, created_at";

    private final DataSource dataSource;

    public JdbcTransactionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Transaction> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM transactions ORDER BY created_at DESC, id DESC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<Transaction> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal membaca daftar transaksi", ex);
        }
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        String sql = "SELECT " + COLUMNS
                + " FROM transactions WHERE user_id = ? ORDER BY created_at DESC, id DESC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                List<Transaction> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal membaca transaksi pengguna", ex);
        }
    }

    @Override
    public Optional<Transaction> findById(Integer id) {
        String sql = "SELECT " + COLUMNS + " FROM transactions WHERE id = ?";
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            rs = statement.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal membaca transaksi", ex);
        } finally {
            closeQuietly(rs);
            closeQuietly(statement);
            closeQuietly(connection);
        }
    }

    @Override
    public Transaction save(Transaction entity) {
        return insert(entity.getUserId(), entity.getDescription(), entity.getAmount(),
                entity.getType(), entity.getProofPath());
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type) {
        return insert(userId, description, amount, type, null);
    }

    private Transaction insert(int userId, String description, long amount, TransactionType type, String proofPath) {
        String sql = """
                INSERT INTO transactions (user_id, description, amount, type, proof_path, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        LocalDateTime createdAt = LocalDateTime.now();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, userId);
            statement.setString(2, description);
            statement.setLong(3, amount);
            statement.setString(4, type.name());
            statement.setString(5, proofPath);
            statement.setTimestamp(6, Timestamp.valueOf(createdAt));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException("Gagal mendapatkan ID transaksi baru");
                }
                return new Transaction(keys.getInt(1), userId, description, amount, type, proofPath, createdAt);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal menyimpan transaksi", ex);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal menghapus transaksi", ex);
        }
    }

    @Override
    public void update(Transaction entity) {
        String sql = """
                UPDATE transactions
                SET description = ?, amount = ?, type = ?, proof_path = ?
                WHERE id = ?
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getDescription());
            statement.setLong(2, entity.getAmount());
            statement.setString(3, entity.getType().name());
            statement.setString(4, entity.getProofPath());
            statement.setInt(5, entity.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal mengubah transaksi", ex);
        }
    }

    @Override
    public List<Transaction> findBy(Predicate<Transaction> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
            // best-effort close; primary SQLException already handled above
        }
    }

    private static Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("description"),
                rs.getLong("amount"),
                TransactionType.valueOf(rs.getString("type")),
                rs.getString("proof_path"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
