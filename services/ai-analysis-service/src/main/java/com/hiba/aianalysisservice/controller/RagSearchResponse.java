package com.hiba.aianalysisservice.controller;

import com.hiba.aianalysisservice.rag.RagSearchResult;

import java.util.List;

public record RagSearchResponse(String query, List<Result> results) {
    public static RagSearchResponse from(String query, List<RagSearchResult> results) {
        return new RagSearchResponse(query, results.stream().map(Result::from).toList());
    }

    public record Result(String documentId, String title, String content, String source,
                         String documentType, int chunkIndex, double similarity) {
        static Result from(RagSearchResult result) {
            var document = result.document();
            return new Result(document.documentId(), document.title(), document.content(), document.source(),
                    document.documentType().name(), document.chunkIndex(), result.similarity());
        }
    }
}
