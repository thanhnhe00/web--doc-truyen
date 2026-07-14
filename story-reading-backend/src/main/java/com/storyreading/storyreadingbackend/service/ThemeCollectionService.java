package com.storyreading.storyreadingbackend.service;

import com.storyreading.storyreadingbackend.dto.ThemeCollectionRequest;
import com.storyreading.storyreadingbackend.entity.*;
import com.storyreading.storyreadingbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ThemeCollectionService {

    private final ThemeCollectionRepository themeCollectionRepository;
    private final CollectionStoryRepository collectionStoryRepository;
    private final StoryRepository storyRepository;

    public ThemeCollection create(ThemeCollectionRequest request) {
        ThemeCollection collection = new ThemeCollection();
        collection.setName(request.getName());
        collection.setDescription(request.getDescription());
        ThemeCollection saved = themeCollectionRepository.save(collection);

        if (request.getStoryIds() != null) {
            for (Long storyId : request.getStoryIds()) {
                Story story = storyRepository.findById(storyId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy truyện id=" + storyId));
                CollectionStory cs = new CollectionStory();
                cs.setCollection(saved);
                cs.setStory(story);
                collectionStoryRepository.save(cs);
            }
        }
        return saved;
    }

    public List<Story> getStoriesByCollection(Long collectionId) {
        if (!themeCollectionRepository.existsById(collectionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bộ sưu tập");
        }
        return collectionStoryRepository.findByCollection_CollectionId(collectionId)
                .stream()
                .map(CollectionStory::getStory)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<ThemeCollection> getAll() {
        return themeCollectionRepository.findAll();
    }

    public ThemeCollection update(Long id, ThemeCollectionRequest request) {
        ThemeCollection collection = themeCollectionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bộ sưu tập"));
        collection.setName(request.getName());
        collection.setDescription(request.getDescription());
        ThemeCollection saved = themeCollectionRepository.save(collection);

        if (request.getStoryIds() != null) {
            collectionStoryRepository.findByCollection_CollectionId(id)
                    .forEach(collectionStoryRepository::delete);
            for (Long storyId : request.getStoryIds()) {
                Story story = storyRepository.findById(storyId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy truyện id=" + storyId));
                CollectionStory cs = new CollectionStory();
                cs.setCollection(saved);
                cs.setStory(story);
                collectionStoryRepository.save(cs);
            }
        }
        return saved;
    }

    public void delete(Long id) {
        if (!themeCollectionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bộ sưu tập");
        }
        themeCollectionRepository.deleteById(id);
    }
}