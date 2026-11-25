package org.delcom.ifs25045springbootcashflow.adapter.repository;

import org.delcom.ifs25045springbootcashflow.domain.entity.Role;
import org.delcom.ifs25045springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25045springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25045springbootcashflow.domain.entity.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcRepositoryTest {

    private JdbcUserRepository userRepository;
    private JdbcTransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        DataSource dataSource = createDataSource();
        SchemaInitializer.initialize(dataSource);
        SchemaInitializer.initialize(dataSource); // idempotent (CREATE TABLE IF NOT EXISTS)
        userRepository = new JdbcUserRepository(dataSource);
        transactionRepository = new JdbcTransactionRepository(dataSource);
    }

    private DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:jdbc_repo_" + System.nanoTime() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    @Test
    void userCrudAndFinders() {
        User saved = userRepository.saveNew("andi", "hash", "Andi", Role.USER);
        assertTrue(saved.getId() > 0);

        assertTrue(userRepository.findByUsername("ANDI").isPresent());
        assertEquals(1, userRepository.findAll().size());
        assertTrue(userRepository.findById(saved.getId()).isPresent());
        assertEquals(1, userRepository.findBy(user -> user.getUsername().equals("andi")).size());

        saved.changeFullName("Andi Baru");
        saved.changePhotoPath("profiles/a.png");
        userRepository.update(saved);
        assertEquals("Andi Baru", userRepository.findById(saved.getId()).orElseThrow().getFullName());

        User viaSave = userRepository.save(new User(0, "budi", "h", "Budi", Role.ADMIN, null));
        assertTrue(viaSave.getId() > 0);
        assertEquals(Role.ADMIN, userRepository.findById(viaSave.getId()).orElseThrow().getRole());

        assertTrue(userRepository.deleteById(viaSave.getId()));
        assertFalse(userRepository.deleteById(viaSave.getId()));
        assertTrue(userRepository.findByUsername("tidakada").isEmpty());
        assertTrue(userRepository.findById(999).isEmpty());
    }

    @Test
    void transactionCrudAndFinders() {
        User owner = userRepository.saveNew("owner", "hash", "Owner", Role.USER);
        User other = userRepository.saveNew("other", "hash", "Other", Role.USER);

        Transaction created = transactionRepository.save(owner.getId(), "Gaji", 5_000_000, TransactionType.INCOME);
        assertTrue(created.getId() > 0);
        assertNull(created.getProofPath());
        transactionRepository.save(owner.getId(), "Makan", 25_000, TransactionType.EXPENSE);
        transactionRepository.save(other.getId(), "Milik lain", 1, TransactionType.INCOME);

        Transaction viaEntity = transactionRepository.save(
                new Transaction(0, owner.getId(), "Via entity", 777, TransactionType.EXPENSE, "proofs/p.png", LocalDateTime.now()));
        assertEquals("proofs/p.png", transactionRepository.findById(viaEntity.getId()).orElseThrow().getProofPath());

        assertEquals(4, transactionRepository.findAll().size());
        assertEquals(3, transactionRepository.findByUserId(owner.getId()).size());
        assertEquals(1, transactionRepository.findBy(tx -> tx.getAmount() == 25_000).size());
        assertTrue(transactionRepository.findById(999).isEmpty());

        created.changeDescription("Gaji Baru");
        created.changeAmount(6_000_000);
        created.changeType(TransactionType.INCOME);
        created.changeProofPath("proofs/x.png");
        transactionRepository.update(created);
        Transaction updated = transactionRepository.findById(created.getId()).orElseThrow();
        assertEquals("Gaji Baru", updated.getDescription());
        assertEquals(6_000_000, updated.getAmount());
        assertEquals("proofs/x.png", updated.getProofPath());

        updated.changeProofPath(null);
        transactionRepository.update(updated);
        assertNull(transactionRepository.findById(created.getId()).orElseThrow().getProofPath());

        assertTrue(transactionRepository.deleteById(created.getId()));
        assertFalse(transactionRepository.deleteById(created.getId()));
        assertEquals(2, transactionRepository.findByUserId(owner.getId()).size());
    }

    @Test
    void transactionsAreOrderedNewestFirstByDefault() {
        User owner = userRepository.saveNew("urut", "hash", "Urut", Role.USER);
        Transaction first = transactionRepository.save(owner.getId(), "Pertama", 1, TransactionType.INCOME);
        Transaction second = transactionRepository.save(owner.getId(), "Kedua", 2, TransactionType.INCOME);

        List<Transaction> list = transactionRepository.findByUserId(owner.getId());
        assertEquals(second.getId(), list.get(0).getId());
        assertEquals(first.getId(), list.get(1).getId());
        assertEquals(second.getId(), transactionRepository.findAll().get(0).getId());
    }
}
