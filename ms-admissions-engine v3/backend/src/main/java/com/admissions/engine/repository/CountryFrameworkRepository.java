package com.admissions.engine.repository;

import com.admissions.engine.model.CountryFramework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryFrameworkRepository extends JpaRepository<CountryFramework, Integer> {
    Optional<CountryFramework> findByRegion(String region);
}
