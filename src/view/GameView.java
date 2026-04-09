package view;

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

    /** 텍스트를 한 글자씩 출력. 도중 키 입력 시 나머지를 즉시 출력. */
    public void printSlow(String text, int delayMs) {
        flushInput();
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            System.out.print(chars[i]);
            try {
                Thread.sleep(delayMs);
                if (System.in.available() > 0) {
                    // 나머지 한번에 출력
                    System.out.print(text.substring(i + 1));
                    flushInput();
                    break;
                }
            } catch (IOException | InterruptedException ignored) {}
        }
        System.out.println();
    }

    /** 텍스트를 한 글자씩 출력 (기본 30ms) */
    public void printSlow(String text) {
        printSlow(text, 30);
    }

    // ============================================
    // 스토리 / 대화
    // ============================================

    /**
     * 대사 목록을 타이핑 효과로 출력.
     * - 타이핑 도중 Enter → 현재 줄 즉시 완성
     * - 대기 중 's' 입력 → 남은 대사 전부 즉시 출력
     */
    public void showDialogue(List<String> lines) {
        clearScreen();
        boolean skip = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (skip) {
                System.out.println(line);
                continue;
            }

            if (line.isEmpty()) {
                System.out.println();
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            } else {
                printSlow(line);
            }

            // 5줄마다 입력 대기 (스킵 기회 제공)
            if ((i + 1) % 5 == 0 && i < lines.size() - 1) {
                skip = waitForEnter();
            }
        }

        if (!skip) {
            waitForEnter();
        }
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
    public void showBattleStart(String enemyName) {
        clearScreen();
        printSlow("=================================");
        printSlow("  " + enemyName + "이(가) 나타났다!");
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
            System.out.println((i + 1) + ". " + c.getCName() + " (ATK:" + c.getCPower() + ")");
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
    public void showAttackResult(String attackerName, String cardName, int damage) {
        clearBelow();
        printSlow(attackerName + "의 " + cardName + "! " + damage + " 데미지!");
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }

    /** 아이템 사용 결과 출력 */
    public void showItemUseResult(String itemName) {
        clearBelow();
        printSlow(itemName + "을(를) 사용했다!");
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }

    /** 전투 승리 */
    public void showBattleWin(String enemyName) {
        clearBelow();
        printSlow("=================================");
        printSlow("  " + enemyName + "을(를) 쓰러뜨렸다!");
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

    /** 도망 */
    public void showFlee() {
        clearBelow();
        printSlow("도망쳤다!");
    }

    // ============================================
    // 맵 탐색
    // ============================================

    /** 5x5 그리드 맵 출력 (현재 위치 표시) */
    public void showMap(Floor floor, Stage currentPos) {
        clearScreen();

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
                System.out.print(isHere ? " @  " : " .  ");
            }
            System.out.println();
        }

        System.out.println();
        System.out.println("이동: w(상) a(좌) s(하) d(우)");
    }

    /** 이동 입력 받기 */
    public String getMovementInput() {
        System.out.print(">> ");
        return scanner.nextLine().trim().toLowerCase();
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
