package com.swd392.baking.service;

import com.swd392.baking.model.Category;
import com.swd392.baking.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllParentWithChildren() {
        return categoryRepository.findByParentIsNull();
    }
}
