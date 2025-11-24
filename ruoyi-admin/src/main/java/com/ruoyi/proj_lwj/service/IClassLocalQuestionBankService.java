package com.ruoyi.proj_lwj.service;

import com.ruoyi.proj_lwj.domain.ClassLocalQuestionBank;
import java.util.List;

public interface IClassLocalQuestionBankService {
    List<ClassLocalQuestionBank> search(String keyword, Integer questionType, Integer difficulty, String category, Integer status, int page, int size);
    ClassLocalQuestionBank get(Long id);
    int add(ClassLocalQuestionBank q);
    int edit(ClassLocalQuestionBank q);
    int remove(Long id);
    List<String> categories();
    int countEnabled();
    int countAll();
}

