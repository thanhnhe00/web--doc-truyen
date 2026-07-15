package com.storyreading.storyreadingbackend.service;

import com.storyreading.storyreadingbackend.dto.CategoryRequest;
import com.storyreading.storyreadingbackend.entity.Category;
import com.storyreading.storyreadingbackend.repository.CategoryRepository;
import com.storyreading.storyreadingbackend.repository.StoryCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final StoryCategoryRepository storyCategoryRepository;

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Thể loại đã tồn tại");
        }
        Category category = new Category();
        category.setName(request.getName());
        return categoryRepository.save(category);
    }

    public Category update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thể loại"));
        category.setName(request.getName());
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thể loại");
        }
        if (!storyCategoryRepository.findByCategory_CategoryId(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thể loại đang được sử dụng bởi truyện, không thể xóa.");
        }
        categoryRepository.deleteById(id);
    }
}