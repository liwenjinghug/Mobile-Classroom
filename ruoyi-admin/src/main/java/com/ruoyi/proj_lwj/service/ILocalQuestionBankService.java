package com.ruoyi.proj_lwj.service;

import com.ruoyi.proj_lwj.domain.LocalQuestionBank;
import java.util.List;

public interface ILocalQuestionBankService {
    List<LocalQuestionBank> search(String keyword, Integer questionType, Integer difficulty, String category, Integer status, int page, int size);
    LocalQuestionBank get(Long id);
    int add(LocalQuestionBank q);
    int edit(LocalQuestionBank q);
    int remove(Long id);
    List<String> categories();
    int countEnabled();
    int countAll();
}

