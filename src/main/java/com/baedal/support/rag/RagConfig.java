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
 * Memory가 먼저 "아까 그 주문"의 orderId를 복원해야 RAG가 "그 주문의 환불 정책"을
 * 검색할 수 있다. 순서를 바꾸면 어떤 품질 저하가 생기는지는 숙제 3단계에서 관찰한다.
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
     * 처음엔 0.5로 뒀는데, "비 오는 날 늦게 오면 보상 받나요?"가 delay-compensation 0.456 /
     * weather-delay 0.426으로 전부 탈락했다. 같은 표본에서 도메인 밖 질문("치킨 맛집 추천해줘",
     * "오늘 날씨 어때요?", 인사, 주문 상태 문의)의 최고점은 0.389였다. 그래서 둘 사이인 0.42로 내렸다.
     * 표본이 20문장 남짓이고 간격이 좁아서(0.389 vs 0.426), 문서가 늘면 다시 재야 한다. raw는 docs/6주차/실측-raw.
     */
    static final double SIMILARITY_THRESHOLD = 0.42;

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
