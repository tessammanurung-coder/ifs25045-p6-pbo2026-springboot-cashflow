package org.delcom.ifs25045springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Kontrak Generic Repository Port untuk operasi CRUD dan pencarian.
 *
 * @param <T>  tipe entitas domain
 * @param <K> tipe data identifier entitas
 */
public interface IRepository<T, K> {
    List<T> findAll();

    Optional<T> findById(K id);

    T save(T entity);

    /** Menghapus entitas berdasarkan ID. Mengembalikan true jika ada yang terhapus. */
    boolean deleteById(K id);

    void update(T entity);

    List<T> findBy(Predicate<T> predicate);
}
