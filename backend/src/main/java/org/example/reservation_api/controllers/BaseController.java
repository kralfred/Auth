package org.example.reservation_api.controllers;

import org.example.reservation_api.entities.BaseEntity;
import org.example.reservation_api.services.BaseService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

public abstract class BaseController<T extends BaseEntity, S extends BaseService<T, ?>> {

    protected final S service;

    protected BaseController(S service) {
        this.service = service;
    }


    @GetMapping
    public ResponseEntity<List<T>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<T> update(@PathVariable UUID id, @RequestBody T entity) {
        // Calls the generic update method you added to BaseService
        T updatedEntity = service.update(id, entity);
        return ResponseEntity.ok(updatedEntity);
    }


    @GetMapping("/ids")
    public List<UUID> getAllIds() {
        return service.findAll().stream()
                .map(BaseEntity::getId)
                .toList();
    }
}