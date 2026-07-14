package com.storyreading.storyreadingbackend.repository;

import com.storyreading.storyreadingbackend.entity.ThemeCollection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThemeCollectionRepository extends JpaRepository<ThemeCollection, Long> {
}
