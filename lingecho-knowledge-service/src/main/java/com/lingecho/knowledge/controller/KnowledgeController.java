package com.lingecho.knowledge.controller;

import com.lingecho.common.core.result.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识库控制器
 */
@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    @GetMapping("/documents")
    public Result<List<DocumentDTO>> listDocuments() {
        // TODO: 实现文档列表查询
        List<DocumentDTO> documents = new ArrayList<>();
        return Result.success(documents);
    }

    @PostMapping("/documents")
    public Result<DocumentDTO> uploadDocument(@RequestParam("file") MultipartFile file) {
        // TODO: 实现文档上传和解析
        DocumentDTO document = new DocumentDTO();
        return Result.success(document);
    }

    @GetMapping("/documents/{id}")
    public Result<DocumentDTO> getDocument(@PathVariable Long id) {
        // TODO: 实现文档详情查询
        DocumentDTO document = new DocumentDTO();
        document.setId(id);
        return Result.success(document);
    }

    @DeleteMapping("/documents/{id}")
    public Result<Void> deleteDocument(@PathVariable Long id) {
        // TODO: 实现文档删除
        return Result.success();
    }

    @PostMapping("/search")
    public Result<List<SearchResult>> search(@RequestBody SearchRequest request) {
        // TODO: 实现知识库搜索
        List<SearchResult> results = new ArrayList<>();
        return Result.success(results);
    }

    @Data
    static class DocumentDTO {
        private Long id;
        private String name;
        private String type;
        private Long size;
        private String status;
    }

    @Data
    static class SearchRequest {
        private String query;
        private Integer topK;
    }

    @Data
    static class SearchResult {
        private String documentId;
        private String content;
        private Double score;
    }
}

