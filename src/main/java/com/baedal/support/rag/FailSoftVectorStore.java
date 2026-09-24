package com.baedal.support.rag;

import com.baedal.support.observability.AgentMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

import java.util.List;

/**
 * Round 6 — 검색이 실패하면 "Context 없음"으로 내려앉는 VectorStore 래퍼.
 *
 * <p>PgVector를 멈추고 보니 RAG가 필요 없는 주문 조회까지 전부 Fallback으로 떨어졌다.
 * QuestionAnswerAdvisor가 검색 예외를 그대로 던지고, 그게 컨트롤러 catch까지 올라가기 때문이다.
 * 검색 실패를 빈 결과로 바꾸면 Tool·Memory는 계속 돌고, 정책 질문은 프롬프트의
 * "Context에서 답을 찾을 수 없으면 상담원 연결" 규칙으로 떨어진다. (관찰은 docs/6주차/01)</p>
 *
 * <p>QuestionAnswerAdvisor에만 이 래퍼를 준다. KnowledgeLoader의 적재 경로는 원본 VectorStore를 써서
 * 적재 실패가 조용히 묻히지 않게 둔다.</p>
 */
@Slf4j
class FailSoftVectorStore implements VectorStore {

    private final VectorStore delegate;
    private final AgentMetrics metrics;

    FailSoftVectorStore(VectorStore delegate, AgentMetrics metrics) {
        this.delegate = delegate;
        this.metrics = metrics;
    }

    @Override
    public List<Document> similaritySearch(SearchRequest request) {
        try {
            return delegate.similaritySearch(request);
        } catch (RuntimeException e) {
            metrics.ragFailure();
            log.warn("[RAG] 검색 실패 — Context 없이 진행 type={} message={}",
                    e.getClass().getSimpleName(), e.getMessage());
            return List.of();
        }
    }

    @Override
    public void add(List<Document> documents) {
        delegate.add(documents);
    }

    @Override
    public void delete(List<String> idList) {
        delegate.delete(idList);
    }

    @Override
    public void delete(Filter.Expression filterExpression) {
        delegate.delete(filterExpression);
    }

    @Override
    public String getName() {
        return "FailSoft(" + delegate.getName() + ")";
    }
}
