package view;

import dao.ParticleDAO;
import model.*;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class GameView {

    private Scanner scanner;

    public GameView(Scanner scanner) {
        this.scanner = scanner;
    }

    // ============================================
    // 화면 제어
    // ============================================

    /** 콘솔 화면 클리어 */
    public void clearScreen() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception e) {
            // cmd 실행 불가 시 ANSI 폴백
            System.out.print("\033[3J\033[2J\033[H");
            System.out.flush();
        }
    }

    /** Enter 입력 대기. 's' 입력 시 true 반환 (스킵 요청) */
    public boolean waitForEnter() {
        System.out.println("\n[ Enter: 계속 ]");
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("s");
    }

    /** 버퍼에 쌓인 입력을 비운다 */
    private void flushInput() {
        try {
            while (System.in.available() > 0) {
                System.in.read();
            }
        } catch (IOException ignored) {}
    }

    /**
     * 텍스트를 한 글자씩 출력. 도중 키 입력 시 나머지를 즉시 출력.
     * @return 's' 입력으로 전체 스킵이 요청되면 true
     */
    public boolean printSlow(String text, int delayMs) {
        flushInput();
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            System.out.print(chars[i]);
            try {
                Thread.sleep(delayMs);
                if (System.in.available() > 0) {
                    System.out.print(text.substring(i + 1));
                    boolean skip = checkSkipInput();
                    System.out.println();
                    return skip;
                }
            } catch (IOException | InterruptedException ignored) {}
        }
        System.out.println();
        return false;
    }

    /** 텍스트를 한 글자씩 출력 (기본 30ms) */
    public boolean printSlow(String text) {
        return printSlow(text, 30);
    }

    /** 입력 버퍼를 읽어서 's' 포함 여부 반환 */
    private boolean checkSkipInput() {
        try {
            if (System.in.available() > 0) {
                byte[] buf = new byte[System.in.available()];
                System.in.read(buf);
                return new String(buf).trim().toLowerCase().contains("s");
            }
        } catch (IOException ignored) {}
        return false;
    }

    /** 지정 시간(ms) 대기. 도중 's' 입력 시 true 반환 (스킵) */
    private boolean sleepWithSkipCheck(int ms) {
        try {
            int elapsed = 0;
            while (elapsed < ms) {
                Thread.sleep(50);
                elapsed += 50;
                if (System.in.available() > 0) {
                    return checkSkipInput();
                }
            }
        } catch (IOException | InterruptedException ignored) {}
        return false;
    }

    // ============================================
    // 스토리 / 대화
    // ============================================

    /**
     * 대사 목록을 연속 출력.
     * - 한 줄씩 타이핑 효과 후 0.5초 대기
     * - 타이핑 도중 또는 줄 대기 중 's' + Enter → 나머지 즉시 출력
     * - 모든 출력 후 Enter 대기
     */
    public void showDialogue(List<String> lines) {
        clearScreen();
        flushInput();
        boolean skip = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (skip) {
                System.out.println(line);
                continue;
            }

            if (line.isEmpty()) {
                System.out.println();
                skip = sleepWithSkipCheck(300);
            } else {
                skip = printSlow(line);
            }
            // 줄 사이 0.3초 대기
            if (!skip && i < lines.size() - 1) {
                skip = sleepWithSkipCheck(300);
            }
        }

        waitForEnter();
    }

    // ============================================
    // 선택지
    // ============================================

    /** 번호 선택지를 출력하고 유효한 입력을 받아 반환 (1-based) */
    public int showChoice(String... options) {
        System.out.println();
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + ". " + options[i]);
        }
        System.out.print("\n>> ");

        while (true) {
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= options.length) {
                    return choice;
                }
            } catch (NumberFormatException ignored) {}
            System.out.print("1~" + options.length + " 중에 선택해주세요.\n>> ");
        }
    }

    // ============================================
    // 전투 화면
    // ============================================

    /** 커서 위치 저장 후 아래 영역만 클리어 */
    private void saveCursor() {
        System.out.print("\033[s");  // 커서 위치 저장
        System.out.flush();
    }

    /** 저장된 커서로 복귀 후 아래 전부 지움 */
    private void clearBelow() {
        System.out.print("\033[u");  // 커서 복원
        System.out.print("\033[J");  // 커서 아래 전부 클리어
        System.out.flush();
    }

    /** 전투 시작 연출 */
    public void showBattleStart(String enemyName, int enemyPp) {
        clearScreen();
        printSlow("=================================");
        printSlow("  " + enemyName + ParticleDAO.lg(enemyPp) + " 나타났다!");
        printSlow("=================================");
        System.out.println();
    }

    /** 전투 상태 표시 (HP 바) + 커서 저장 */
    public void showBattleStatus(Entity player, Entity enemy, String enemyName) {
        clearScreen();
        System.out.println("[나] " + buildHpBar(player) + "  "
                + player.getCurrentHealth() + "/" + player.getHealth());
        System.out.println("[" + enemyName + "] " + buildHpBar(enemy) + "  "
                + enemy.getCurrentHealth() + "/" + enemy.getHealth());
        System.out.println();
        saveCursor();  // 이 아래가 교체 가능 영역
    }

    /** HP 비율에 따른 바 생성 */
    private String buildHpBar(Entity entity) {
        int max = entity.getHealth();
        int current = entity.getCurrentHealth();
        int barLength = 20;
        int filled = (max > 0) ? (current * barLength / max) : 0;

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            sb.append(i < filled ? "█" : "░");
        }
        sb.append("]");
        return sb.toString();
    }

    /** 플레이어 행동 선택 메뉴 */
    public int showPlayerMenu(int cardCount, int itemCount) {
        clearBelow();
        System.out.println("행동을 선택하세요:");
        System.out.println("1. 카드 사용 (" + cardCount + "장)");
        System.out.println("2. 아이템 사용 (" + itemCount + "개)");
        System.out.println("3. 도망가기");
        System.out.print(">> ");

        while (true) {
            String input = scanner.nextLine().trim();
            switch (input) {
                case "1": return 1;
                case "2": return 2;
                case "3": return 3;
                default:
                    System.out.print("1~3 중에 선택해주세요.\n>> ");
            }
        }
    }

    /** 카드 목록 출력 및 선택 (0: 취소) */
    public int showCardList(List<Card> cards) {
        clearBelow();
        if (cards.isEmpty()) {
            System.out.println("보유한 카드가 없습니다!");
            return -1;
        }

        System.out.println("사용할 카드를 선택하세요 (0: 취소):");
        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            if (c.getCPower() > 0) {
                System.out.println((i + 1) + ". " + c.getCName() + " (ATK:" + c.getCPower() + ")");
            } else {
                System.out.println((i + 1) + ". " + c.getCName());
            }
        }
        System.out.print(">> ");

        while (true) {
            String input = scanner.nextLine().trim();
            try {
                int idx = Integer.parseInt(input);
                if (idx == 0) return 0;
                if (idx >= 1 && idx <= cards.size()) return idx;
            } catch (NumberFormatException ignored) {}
            System.out.print("올바른 번호를 입력해주세요.\n>> ");
        }
    }

    /** 아이템 목록 출력 및 선택 (0: 취소) */
    public int showItemList(List<Item> items) {
        clearBelow();
        if (items.isEmpty()) {
            System.out.println("보유한 아이템이 없습니다!");
            return -1;
        }

        System.out.println("사용할 아이템을 선택하세요 (0: 취소):");
        for (int i = 0; i < items.size(); i++) {
            Item it = items.get(i);
            System.out.println((i + 1) + ". " + it.getIName());
        }
        System.out.print(">> ");

        while (true) {
            String input = scanner.nextLine().trim();
            try {
                int idx = Integer.parseInt(input);
                if (idx == 0) return 0;
                if (idx >= 1 && idx <= items.size()) return idx;
            } catch (NumberFormatException ignored) {}
            System.out.print("올바른 번호를 입력해주세요.\n>> ");
        }
    }

    /** 공격 결과 출력 */
    public void showAttackResult(String attackerName, int attackerPp,
                                 Card card,
                                 String targetName, int targetPp,
                                 int damage) {
        clearBelow();
        printSlow(attackerName + ParticleDAO.lg(attackerPp) + " "
                + card.getCName() + ParticleDAO.er(card.getPp()) + " 사용했다.");
        if (card.getCUseMsg() != null && !card.getCUseMsg().isEmpty()) {
            printSlow(card.getCUseMsg());
        }
        printSlow(targetName + ParticleDAO.lg(targetPp) + " " + damage + "의 대미지를 입었다!");
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }

    /** 아이템 사용 결과 출력 */
    public void showItemUseResult(Item item) {
        clearBelow();
        printSlow(item.getIName() + ParticleDAO.er(item.getPp()) + " 사용했다.");
        if (item.getIUseMsg() != null && !item.getIUseMsg().isEmpty()) {
            printSlow(item.getIUseMsg());
        }
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }

    /** 전투 승리 */
    public void showBattleWin(String enemyName, int enemyPp) {
        clearBelow();
        printSlow("=================================");
        printSlow("  " + enemyName + ParticleDAO.er(enemyPp) + " 쓰러뜨렸다!");
        printSlow("=================================");
        waitForEnter();
    }

    /** 전투 패배 */
    public void showBattleLose() {
        clearBelow();
        printSlow("=================================");
        printSlow("  쓰러지고 말았다...");
        printSlow("=================================");
        waitForEnter();
    }

    /** 게임 오버 화면 */
    public void showGameOver() {
        clearScreen();
        printSlow("=================================");
        printSlow("       GAME  OVER");
        printSlow("=================================");
        waitForEnter();
    }

    /** 도망 */
    public void showFlee() {
        clearBelow();
        printSlow("도망쳤다!");
    }

    /** 적이 도망 */
    public void showEnemyFled(String enemyName, int enemyPp) {
        clearBelow();
        printSlow(enemyName + ParticleDAO.lg(enemyPp) + " 도망쳤다!");
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }

    /** Scanner 방어 성공 */
    public void showScannerBlock(int useCount) {
        clearBelow();
        printSlow("Scanner로 적의 공격을 막았다! (" + useCount + "/3)");
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }

    /** Scanner 흡수 — 혜진 전투 종료 */
    public void showScannerCapture() {
        clearBelow();
        printSlow("어라..? 혜진이 Scanner로 빨려들어갔다!");
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
    }

    /** 적 공격 막힘 */
    public void showBlockedAttack(String enemyName) {
        clearBelow();
        printSlow(enemyName + "의 공격이 막혔다!");
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }

    // ============================================
    // 맵 탐색
    // ============================================

    /** 5x5 그리드 맵 출력 (현재 위치 표시) */

    public void showMap(Floor floor, Stage currentPos) {
       /* clearScreen();

        System.out.println(floor.getFloorLevel() + "층 - 현재 위치: "
                + currentPos.getColumn() + "_" + currentPos.getRow());
        System.out.println();

        // 열 번호 헤더
        System.out.print("    ");
        for (int c = 1; c <= 5; c++) {
            System.out.print(" " + c + "  ");
        }
        System.out.println();

        // 행 (a~e)
        char[] rowLabels = {'a', 'b', 'c', 'd', 'e'};
        for (char r : rowLabels) {
            System.out.print(" " + r + "  ");
            for (int c = 1; c <= 5; c++) {
                boolean isHere = currentPos.getRow() == c
                        && currentPos.getColumn().equals(String.valueOf(r));
                if (isHere) {
                    System.out.print(" @  ");
                } else {
                    // 해당 위치의 Stage를 찾아 이벤트 타일 여부 확인
                    final int col = c;
                    final String row = String.valueOf(r);
                    Stage tile = floor.getStages().stream()
                            .filter(s -> s.getRow() == col && s.getColumn().equals(row))
                            .findFirst().orElse(null);
                    boolean isEvent = tile != null && tile.getEventId() != null;
                    System.out.print(isEvent ? " !  " : " .  ");
                }
            }
            System.out.println();
        }

        System.out.println();
        System.out.println("이동: w(상) a(좌) s(하) d(우)");*/

        clearScreen();
        System.out.println(floor.getFloorLevel() + "층 - 현재 위치: "
            + currentPos.getColumn() + "_" + currentPos.getRow());
        System.out.println();

// 범례
        System.out.println("  @ 현재위치  N 몬스터  I 아이템  E 이벤트  F 출구  S 시작");
        System.out.println();

// 열 헤더 (a~e)
        System.out.print("      ");
        char[] colLabels = {'a', 'b', 'c', 'd', 'e'};
        for (char c : colLabels) {
            System.out.print("  " + c + "  ");
        }
        System.out.println();

// 행 (1~5)
        for (int r = 1; r <= 5; r++) {
            final int currentRow = r;
            System.out.print("  " + r + "  ");
            for (char c : colLabels) {
                final String currentCol = String.valueOf(c);
                boolean isHere = currentPos.getRow() == currentRow
                    && currentPos.getColumn().equals(currentCol);

                if (isHere) {
                    System.out.print("[ @ ]");
                } else {
                    Stage s = floor.getStages().stream()
                        .filter(st -> st.getRow() == currentRow
                            && st.getColumn().equals(currentCol))
                        .findFirst()
                        .orElse(null);

                    String symbol = getSymbol(s);
                    System.out.print("[ " + symbol + " ]");
                }
            }
            System.out.println();
        }

        System.out.println();
        System.out.println("이동: w(상) a(좌) s(하) d(우)  |  i: 인벤토리");
    }
    /** s_type → 맵 심볼 변환 */
    private String getSymbol(Stage s) {
        if (s == null) return "?";
        if (s.getS_type() == null) return ".";

        switch (s.getS_type()) {
            case "start":   return "S";
            case "finish":  return "F";
            case "npc_i":   return "N";  // 몬스터
            case "w":       return "■";  // 벽
            case "v":       return " ";  // 빈 칸
            default:
                if (s.getS_type().startsWith("event_")) return "E";
                return " ";
        }
    }

    /** 이동 입력 받기 */
    public String getMovementInput() {
        System.out.print(">> ");
        return scanner.nextLine().trim().toLowerCase();
    }

    /** 탐색 중 인벤토리 조회 (읽기 전용) */
    public void showInventoryView(List<Card> cards, List<Item> items) {
        clearScreen();
        System.out.println("========== 인벤토리 ==========");

        System.out.println("\n[ 카드 ]");
        if (cards.isEmpty()) {
            System.out.println("  보유한 카드가 없습니다.");
        } else {
            for (int i = 0; i < cards.size(); i++) {
                Card c = cards.get(i);
                if (c.getCPower() > 0) {
                    System.out.printf("  %d. %-20s ATK: %d%n", i + 1, c.getCName(), c.getCPower());
                } else {
                    System.out.printf("  %d. %s%n", i + 1, c.getCName());
                }
            }
        }

        System.out.println("\n[ 아이템 ]");
        if (items.isEmpty()) {
            System.out.println("  보유한 아이템이 없습니다.");
        } else {
            for (int i = 0; i < items.size(); i++) {
                Item it = items.get(i);
                System.out.printf("  %d. %s%n", i + 1, it.getIName());
            }
        }

        System.out.println("\n==============================");
        System.out.println("[ Enter: 돌아가기 ]");
        scanner.nextLine();
    }

    /** 맵 경고 메시지 (경계 밖 이동 등) */
    public void showMapAlert(String message) {
        System.out.println(message);
    }

    /** 층 이동 연출 */
    public void showFloorTransition(int nextFloorLevel) {
        clearScreen();
        printSlow("다음 층으로 이동합니다...");
        printSlow(nextFloorLevel + "층에 도착했다.");
        waitForEnter();
    }

    // ============================================
    // 도감 (Collection)
    // ============================================

    /** 도감 카테고리 헤더 */
    public void showCollectionHeader(String title) {
        System.out.println("=== " + title + " ===");
    }

    /** 도감 항목 (해금/미해금) */
    public void showCollectionEntry(String name, String detail, boolean unlocked) {
        if (unlocked) {
            System.out.println("[" + name + "] " + detail);
        } else {
            System.out.println("[???] 미해금");
        }
    }

    /** 도감 진행률 */
    public void showProgress(int unlocked, int total) {
        System.out.println("\n수집 진행률: " + unlocked + " / " + total);
    }

    /** 도감 메뉴 */
    public int showCollectionMenu() {
        clearScreen();
        System.out.println("=== 도감 ===");
        return showChoice("카드 도감", "아이템 도감", "보스 도감", "엔딩 도감", "돌아가기");
    }

    // ============================================
    // 시스템 메시지
    // ============================================

    /** 일반 시스템 메시지 */
    public void showMessage(String message) {
        System.out.println(message);
    }

    /** 카드/아이템 획득 연출 */
    public void showAcquire(String itemName) {
        printSlow("『" + itemName + "』을(를) 획득했다!");
        waitForEnter();
    }

    /** 엔딩 타이틀 출력 */
    public void showEndingTitle(String endingName) {
        clearScreen();
        System.out.println();
        printSlow("──────────────────────────────", 20);
        printSlow("  ENDING: " + endingName, 50);
        printSlow("──────────────────────────────", 20);
        System.out.println();
    }
}
