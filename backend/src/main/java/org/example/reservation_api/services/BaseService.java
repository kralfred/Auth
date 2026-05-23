package org.example.reservation_api.services;

import lombok.RequiredArgsConstructor;
import org.example.reservation_api.entities.BaseEntity;
import org.example.reservation_api.repositories.BaseRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class BaseService<T extends BaseEntity, R extends BaseRepository<T>> {

    protected final R repository;


    public List<T> findAll() {
        return repository.dbFindAll();
    }

    public T update(UUID id, T source) {
        return repository.dbFindById(id)
                .map(target -> {
                    copyNonNullProperties(source, target);
                    return repository.dbUpdate(target);
                })
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + id));
    }

    // Helper to copy only fields that aren't null
    private void copyNonNullProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        // Always ignore ID
        emptyNames.add("id");

        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        return emptyNames.toArray(new String[0]);
    }
}
