package com.baedal.support.rag;

import com.baedal.support.observability.AgentMetrics;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Round 4 — RAG 설정 (Vector Store + 청킹 + QuestionAnswerAdvisor).
 *
 * <h3>Advisor 체인 순서</h3>
 * <pre>
 *   MessageChatMemoryAdvisor   (order=10)   — Round 3: 이전 대화 이력 주입
 *   QuestionAnswerAdvisor      (order=20)   — Round 4: RAG 검색 결과 주입
 *   PerformanceLoggingAdvisor  (order=100)  — Round 1: 최종 호출 시간 집계
 * </pre>
 * QuestionAnswerAdvisor의 검색어는 이번 턴 사용자 문장({@code prompt.getUserMessage()})뿐이라,
 * Memory가 앞에 있어도 "그 주문" 같은 지시어가 검색어에서 풀리지는 않는다(docs/4주차/06).
 * Memory를 앞에 두는 이유는 RAG가 만든 증강 프롬프트가 Memory에 저장되는 오염을 막기 위해서다(docs/4주차/03).
 *
 * @see KnowledgeLoader FAQ/정책 문서를 VectorStore에 적재하는 ApplicationRunner
 */
@Configuration
public class RagConfig {

    /**
     * similaritySearch가 돌려줄 상위 N건.
     * 정책 문서가 7건 내외라 4면 "환불 + 지연" 같은 복합 질문도 관련 조항을 덮으면서
     * 프롬프트 토큰 폭증을 피한다. (1=관련 정책 놓침, 10=원문 10개로 입력 토큰 폭증)
     */
    private static final int TOP_K = 4;

    /**
     * COSINE_SIMILARITY 기준. 이 값 미만은 "관련 없음"으로 버린다.
     * embeddinggemma(768차원) 기준으로 23문장을 재 보니, 잡혀야 할 정책 질문의 top-1 최저점은 0.525(쿠폰),
     * 정책이 아닌 질문(인사·주문 상태·사장님 번호·날씨·맛집)의 최고점은 0.447이었다. 그 사이인 0.5로 둔다.
     * qwen3-embedding:0.6b 때는 0.42였다(도메인 밖 0.389 vs 놓친 정책 질문 0.426). 임베딩 모델을 바꾸면 이 값도 다시 재야 한다.
     * raw는 docs/6주차/실측-raw/rag-threshold-점수분포.md.
     */
    static final double SIMILARITY_THRESHOLD = 0.5;

    /**
     * 문서를 토큰 단위 청크로 쪼개는 Splitter.
     * 배달 정책 문서는 조항 단위로 이미 끊겨 있어 800/350 기본값으로 대체로 잘 동작한다.
     */
    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter(
                800,    // chunkSize: 청크 한 개의 목표 토큰 수
                350,    // minChunkSizeChars: 이보다 작으면 앞 청크에 병합
                5,      // minChunkLengthToEmbed: 이보다 짧으면 임베딩 제외
                10_000, // maxNumChunks
                true    // keepSeparator (문단 구분자 유지)
        );
    }

    /**
     * 사용자 질문을 자동으로 벡터화 → VectorStore 검색 → Top-K 결과를 프롬프트에 주입하는 Advisor.
     * order(20)으로 Memory(10) 뒤, Performance(100) 앞에 놓는다.
     * 검색 저장소 장애가 전체 응답 실패로 번지지 않게 {@link FailSoftVectorStore}로 감싼다.
     */
    @Bean
    public QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore, AgentMetrics metrics) {
        SearchRequest searchRequest = SearchRequest.builder()
                .topK(TOP_K)
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .build();

        return QuestionAnswerAdvisor.builder(new FailSoftVectorStore(vectorStore, metrics))
                .searchRequest(searchRequest)
                .order(20)
                .build();
    }
}
