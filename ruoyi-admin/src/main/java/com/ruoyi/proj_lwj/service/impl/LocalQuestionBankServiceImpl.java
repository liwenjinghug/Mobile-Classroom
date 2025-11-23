package com.ruoyi.proj_lwj.service.impl;

import com.ruoyi.proj_lwj.domain.LocalQuestionBank;
import com.ruoyi.proj_lwj.mapper.LocalQuestionBankMapper;
import com.ruoyi.proj_lwj.service.ILocalQuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocalQuestionBankServiceImpl implements ILocalQuestionBankService {

    @Autowired
    private LocalQuestionBankMapper mapper;

    @Override
    public List<LocalQuestionBank> search(String keyword, Integer questionType, Integer difficulty, String category, Integer status, int page, int size) {
        int offset = Math.max(0, page) * size;
        int limit = size;
        return mapper.search(keyword, questionType, difficulty, category, status, offset, limit);
    }

    @Override
    public LocalQuestionBank get(Long id) { return mapper.selectById(id); }

    @Override
    public int add(LocalQuestionBank q) { return mapper.insert(q); }

    @Override
    public int edit(LocalQuestionBank q) { return mapper.update(q); }

    @Override
    public int remove(Long id) { return mapper.delete(id); }

    @Override
    public List<String> categories() { return mapper.selectCategories(); }

    @Override
    public int countEnabled() { return mapper.countEnabled(); }

    @Override
    public int countAll() { return mapper.countAll(); }
}

