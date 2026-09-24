package com.baedal.support.rag;

import com.baedal.support.observability.AgentMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FailSoftVectorStoreTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final VectorStore delegate = mock(VectorStore.class);
    private final FailSoftVectorStore store = new FailSoftVectorStore(delegate, new AgentMetrics(registry));

    @Test
    @DisplayName("검색이 성공하면 결과를 그대로 돌려준다")
    void passesThroughResults() {
        List<Document> docs = List.of(new Document("정책"));
        when(delegate.similaritySearch(any(SearchRequest.class))).thenReturn(docs);

        assertThat(store.similaritySearch(SearchRequest.builder().query("q").build())).isSameAs(docs);
    }

    @Test
    @DisplayName("PgVector 연결 실패는 빈 Context로 바꾸고 rag.failure를 올린다")
    void searchFailureBecomesEmptyContext() {
        when(delegate.similaritySearch(any(SearchRequest.class)))
                .thenThrow(new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection"));

        assertThat(store.similaritySearch(SearchRequest.builder().query("q").build())).isEmpty();
        assertThat(registry.get("baedal.agent.rag.failure").counter().count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("적재(add) 실패는 삼키지 않는다")
    void addFailureIsNotSwallowed() {
        doThrow(new IllegalStateException("down")).when(delegate).add(anyList());

        assertThatThrownBy(() -> store.add(List.of(new Document("x")))).isInstanceOf(IllegalStateException.class);
    }
}
