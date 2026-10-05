package com.fast.ai.controller;

import com.fast.ai.domain.SearchKeyword;
import com.fast.ai.mapper.SearchKeywordMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/ai/keyword")
public class SearchKeywordController {

    @Resource
    private SearchKeywordMapper keywordMapper;

    @GetMapping("/list")
    public List<SearchKeyword> list() {
        return keywordMapper.selectList(null);
    }

    @PostMapping("/add")
    public String add(@RequestBody SearchKeyword kw) {
        kw.setId(null);
        kw.setEnabled(kw.getEnabled() == null ? 1 : kw.getEnabled());
        kw.setSortOrder(kw.getSortOrder() == null ? 0 : kw.getSortOrder());
        kw.setCreateTime(new Date());
        keywordMapper.insert(kw);
        return "ok";
    }

    @PostMapping("/update")
    public String update(@RequestBody SearchKeyword kw) {
        keywordMapper.updateById(kw);
        return "ok";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        keywordMapper.deleteById(id);
        return "ok";
    }
}