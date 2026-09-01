package org.example.reservation_api.repositories;

import org.example.reservation_api.entities.BaseEntity;
import org.example.reservation_api.entities.Identifiable;

import org.example.reservation_api.entities.BaseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BaseRepository<T extends BaseEntity> {
    List<T> dbFindAll();
    Optional<T> dbFindById(UUID id);
    T dbUpdate(T entity);
    boolean existsById(UUID id);
}
