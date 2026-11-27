package org.delcom.ifs25045springbootcashflow.adapter.repository;

import org.delcom.ifs25045springbootcashflow.domain.repository.IRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Base generic in-memory repository yang mengimplementasikan {@link IRepository}.
 * Menghilangkan duplikasi kode penyimpanan list dan pencarian untuk berbagai entitas.
 *
 * @param <T>  tipe entitas domain
 * @param <K> tipe data identifier unik entitas
 */
public abstract class InMemoryRepository<T, K> implements IRepository<T, K> {
    /** Penyimpanan data entitas di memori. */
    protected final List<T> data = new ArrayList<>();

    /** Fungsi untuk mengekstrak ID dari objek entitas. */
    private final Function<T, K> idExtractor;

    protected InMemoryRepository(Function<T, K> idExtractor) {
        this.idExtractor = idExtractor;
    }

    @Override
    public List<T> findAll() {
        // Salinan defensif agar data internal tidak termutasi dari luar
        return new ArrayList<>(data);
    }

    @Override
    public Optional<T> findById(K id) {
        return data.stream()
                .filter(item -> idExtractor.apply(item).equals(id))
                .findFirst();
    }

    @Override
    public T save(T entity) {
        data.add(entity);
        return entity;
    }

    @Override
    public boolean deleteById(K id) {
        return data.removeIf(item -> idExtractor.apply(item).equals(id));
    }

    @Override
    public void update(T entity) {
        // Objek mutable disimpan by-reference dalam memori.
    }

    @Override
    public List<T> findBy(Predicate<T> predicate) {
        return data.stream()
                .filter(predicate)
                .toList();
    }
}
