package manager;

public class EventManager {

    // ============================================
    // 이벤트 결과
    // ============================================
    public enum EventResult {
        NOTHING,            // 아무 일 없음
        SHOW_DIALOGUE,      // 대사 출력
        GET_CARD,           // 카드 획득
        GET_ITEM,           // 아이템 획득
        START_BATTLE,       // 전투 돌입
        DOOR_LOCKED,        // 문 잠겨있음 (조건 미충족)
        ENDING_FALL,       // 넘어짐 엔딩 (1층 도망)
        ENDING_SHORTCUT,    // 최단거리 엔딩 (3층 세미콜론 문)
        ENDING_BETRAYAL,    // 선혁 뒷통수 엔딩 (4층 패배)
        ENDING_GC,          // GC 엔딩 (히든맵 혜진 루트)
        ENDING_DEFEAT,      // 패배 엔딩 (8층 } 카드 없음)
        ENDING_HYEJIN,      // 혜진 엔딩
        ENDING_NO_HYEJIN    // 혜진 X 엔딩
    }

    // ============================================
    // 이벤트 ID 상수
    // ============================================
    public static final String TUTORIAL_FLEE       = "TUTORIAL_FLEE";        // 1층: 도망가기
    public static final String SEMICOLON_FIND      = "SEMICOLON_FIND";       // 2층: [ ; ] 카드 발견
    public static final String COMMENT_BRANCH      = "COMMENT_BRANCH";       // 2층: 나뭇가지 주석
    public static final String SEMICOLON_DOOR      = "SEMICOLON_DOOR";       // 3층: 세미콜론 문
    public static final String INTERPRETER_ROBOT   = "INTERPRETER_ROBOT";    // 3층: 인터프리터/JIT 로봇
    public static final String SUSPECT_SUNHYUK     = "SUSPECT_SUNHYUK";      // 4층: 선혁 의심 이벤트
    public static final String CACHE_BATTLE        = "CACHE_BATTLE";         // 6층: 캐시 전투
    public static final String HEAP_ENTRY          = "HEAP_ENTRY";           // 7층: 힙 영역 진입
    public static final String HYEJIN_REUNION      = "HYEJIN_REUNION";       // 히든맵: 혜진 재회
    public static final String FINAL_BRACKET       = "FINAL_BRACKET";        // 8층: } 카드 사용

    // ============================================
    // 게임 상태 참조 (GameManager에서 주입)
    // ============================================
    private boolean hasSemicolon;
    private boolean hasBracket;
    private boolean commentBranchDone;     // 2층: 주석 나뭇가지 이벤트 완료
    private boolean interpreterRobotDone;  // 3층: 실행엔진 이벤트 완료
    private int visitCount;                // 특정 구역 방문 횟수 (세미콜론 이벤트용)

    public void updateState(boolean hasSemicolon, boolean hasBracket) {
        this.hasSemicolon = hasSemicolon;
        this.hasBracket = hasBracket;
    }

    public void setCommentBranchDone(boolean done) {
        this.commentBranchDone = done;
    }

    public void setInterpreterRobotDone(boolean done) {
        this.interpreterRobotDone = done;
    }

    public void setVisitCount(int count) {
        this.visitCount = count;
    }

    // ============================================
    // 이벤트 트리거 (이벤트 ID → 결과 반환)
    // ============================================
    public EventResult trigger(String eventId) {
        switch (eventId) {

            // --- 1층: 설산 ---
            case TUTORIAL_FLEE:
                return EventResult.ENDING_FALL;

            // --- 2층: 숲 ---
            case SEMICOLON_FIND:
                if (visitCount >= 3) {
                    return EventResult.GET_CARD;    // [ ; ] 카드 획득
                }
                return EventResult.NOTHING;

            case COMMENT_BRANCH:
                return EventResult.SHOW_DIALOGUE;   // 주석 나뭇가지 대사

            // --- 3층: 들판 ---
            case SEMICOLON_DOOR:
                if (hasSemicolon) {
                    return EventResult.ENDING_SHORTCUT;
                }
                return EventResult.DOOR_LOCKED;

            case INTERPRETER_ROBOT:
                return EventResult.SHOW_DIALOGUE;   // 로봇 목격 대사

            // --- 4층: 사막 ---
            case SUSPECT_SUNHYUK:
                if (commentBranchDone && interpreterRobotDone) {
                    return EventResult.START_BATTLE;    // 두 이벤트 모두 경험 → 선혁 의심 활성화
                }
                return EventResult.NOTHING;

            // --- 6층: 심해 ---
            case CACHE_BATTLE:
                return EventResult.START_BATTLE;    // 캐시 전투

            // --- 7층: 화산 ---
            case HEAP_ENTRY:
                // TODO: 힙 영역 진입 조건 확인
                return EventResult.SHOW_DIALOGUE;

            // --- 히든맵 ---
            case HYEJIN_REUNION:
                return EventResult.SHOW_DIALOGUE;   // 혜진 재회 대사 후 선택지

            // --- 8층: 딸깍이 ---
            case FINAL_BRACKET:
                if (hasBracket) {
                    return EventResult.SHOW_DIALOGUE;   // } 사용 → 최종 선택지로
                }
                return EventResult.ENDING_DEFEAT;       // } 없음 → 패배 엔딩

            default:
                return EventResult.NOTHING;
        }
    }
}
