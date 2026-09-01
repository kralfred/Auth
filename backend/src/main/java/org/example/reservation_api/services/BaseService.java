package org.example.reservation_api.services;

import lombok.RequiredArgsConstructor;
import org.example.reservation_api.entities.BaseEntity;
import org.example.reservation_api.messages.ResourceNotFoundException; // Use custom exception
import org.example.reservation_api.repositories.BaseRepository;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class BaseService<T extends BaseEntity, R extends BaseRepository<T>> {

    protected final R repository;

    public List<T> findAll() {
        return repository.dbFindAll();
    }

    public T findById(UUID id) {
        return repository.dbFindById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + id));
    }

    public T update(UUID id, T entity) {
        // Ensure entity exists before attempting update
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Resource not found with ID: " + id);
        }
        return repository.dbUpdate(entity);
    }
}