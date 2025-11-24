package com.ruoyi.proj_lwj.service.impl;

import com.ruoyi.proj_lwj.domain.ClassLocalQuestionBank;
import com.ruoyi.proj_lwj.mapper.ClassLocalQuestionBankMapper;
import com.ruoyi.proj_lwj.service.IClassLocalQuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassLocalQuestionBankServiceImpl implements IClassLocalQuestionBankService {

    @Autowired
    private ClassLocalQuestionBankMapper mapper;

    @Override
    public List<ClassLocalQuestionBank> search(String keyword, Integer questionType, Integer difficulty, String category, Integer status, int page, int size) {
        int offset = Math.max(0, page) * size;
        int limit = size;
        return mapper.search(keyword, questionType, difficulty, category, status, offset, limit);
    }

    @Override
    public ClassLocalQuestionBank get(Long id) { return mapper.selectById(id); }

    @Override
    public int add(ClassLocalQuestionBank q) { return mapper.insert(q); }

    @Override
    public int edit(ClassLocalQuestionBank q) { return mapper.update(q); }

    @Override
    public int remove(Long id) { return mapper.delete(id); }

    @Override
    public List<String> categories() { return mapper.selectCategories(); }

    @Override
    public int countEnabled() { return mapper.countEnabled(); }

    @Override
    public int countAll() { return mapper.countAll(); }
}

