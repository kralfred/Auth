package org.example.reservation_api.controllers;

import org.example.reservation_api.entities.BaseEntity;
import org.example.reservation_api.services.BaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

public abstract class BaseController<T extends BaseEntity> {

    protected final BaseService<T, ?> service;

    protected BaseController(BaseService<T, ?> service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<T>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<T> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<T> update(@PathVariable UUID id, @RequestBody T entity) {
        T updatedEntity = service.update(id, entity);
        return ResponseEntity.ok(updatedEntity);
    }
}