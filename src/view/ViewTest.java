package view;

import model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ViewTest {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GameView gameView = new GameView(scanner);
        MainMenuView menuView = new MainMenuView(scanner);

        while (true) {
            System.out.print("\033[2J\033[H");
            System.out.flush();

            System.out.println("=== View 테스트 메뉴 ===");
            System.out.println("1. 메인 메뉴");
            System.out.println("2. 대사 출력 (스킵 테스트)");
            System.out.println("3. 선택지");
            System.out.println("4. 전투 화면");
            System.out.println("5. 맵 탐색");
            System.out.println("6. 도감");
            System.out.println("7. 엔딩 타이틀");
            System.out.println("0. 종료");
            System.out.print(">> ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1": testMainMenu(menuView); break;
                case "2": testDialogue(gameView); break;
                case "3": testChoice(gameView); break;
                case "4": testBattle(gameView, scanner); break;
                case "5": testMap(gameView); break;
                case "6": testCollection(gameView); break;
                case "7": testEnding(gameView); break;
                case "0": scanner.close(); return;
            }
        }
    }

    // 1. 메인 메뉴
    static void testMainMenu(MainMenuView menuView) {
        int choice = menuView.showMainMenu();
        System.out.println("\n선택한 메뉴: " + choice);
        System.out.println("[Enter로 돌아가기]");
        new Scanner(System.in).nextLine();
    }

    // 2. 대사 출력 + 스킵 테스트
    static void testDialogue(GameView gameView) {
        List<String> lines = Arrays.asList(
                "영균은 모니터 앞에 앉아 있었다.",
                "눈앞에는 끝없이 펼쳐진 코드의 바다.",
                "",
                "\"딸깍아, 이거 좀 해줘.\"",
                "영균은 익숙한 손놀림으로 AI에게 과제를 넘겼다.",
                "",
                "그 순간, 모니터에서 빛이 새어 나왔다.",
                "강렬한 빛이 방 전체를 뒤덮었다.",
                "",
                "\"...여기가 어디지?\"",
                "정신을 차려보니, 눈앞에 설산이 펼쳐져 있었다.",
                "차가운 바람이 뺨을 스쳤다.",
                "이곳은 코드 세계, 그 첫 번째 층."
        );

        System.out.println("[대사 테스트 - 타이핑 중 Enter로 줄 스킵, 대기 중 's'로 전체 스킵]");
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        gameView.showDialogue(lines);
    }

    // 3. 선택지
    static void testChoice(GameView gameView) {
        gameView.clearScreen();
        System.out.println("선혁이 수상한 행동을 보이고 있다...");
        int choice = gameView.showChoice("선혁을 공격한다", "선혁을 믿는다");
        System.out.println("\n>> 선택 결과: " + choice);
        gameView.waitForEnter();
    }

    // 4. 전투 화면
    static void testBattle(GameView gameView, Scanner scanner) {
        Hero hero = new Hero("h1", 100, 10, 20, 1, "c1");
        NPC boss = new NPC("n1", "미주", "2층 보스 - 숲의 수호자", 80,
                true, 8, 15, 1, "c2");

        // 전투 시작
        gameView.showBattleStart(boss.getNName());

        // 카드/아이템 준비
        List<Card> cards = new ArrayList<>();
        cards.add(new Card.Builder().cName("Scanner").cPower(15).cDesc("입력을 읽어들인다").build());
        cards.add(new Card.Builder().cName("println").cPower(10).cDesc("출력으로 공격한다").build());

        List<Item> items = new ArrayList<>();
        items.add(new Item.Builder().iName("커피").iDesc("HP를 회복한다").build());

        // 전투 루프 데모 (1턴만)
        gameView.showBattleStatus(hero, boss, boss.getNName());
        int action = gameView.showPlayerMenu(cards.size(), items.size());

        if (action == 1) {
            int cardIdx = gameView.showCardList(cards);
            if (cardIdx > 0) {
                Card selected = cards.get(cardIdx - 1);
                boss.takeDamage(selected.getCPower());
                gameView.showAttackResult("영균", selected.getCName(), selected.getCPower());
            }
        } else if (action == 2) {
            int itemIdx = gameView.showItemList(items);
            if (itemIdx > 0) {
                gameView.showItemUseResult(items.get(itemIdx - 1).getIName());
            }
        } else {
            gameView.showFlee();
        }

        // 상태 갱신 후 표시
        gameView.showBattleStatus(hero, boss, boss.getNName());

        // 승리 연출
        boss.setCurrentHealth(0);
        gameView.showBattleWin(boss.getNName());
    }

    // 5. 맵 탐색
    static void testMap(GameView gameView) {
        // 간이 Floor/Stage 생성
        Floor floor = new Floor(2);
        for (int r = 1; r <= 5; r++) {
            String[] cols = {"a", "b", "c", "d", "e"};
            for (String c : cols) {
                floor.addStage(new Stage(0, c + "_" + r, r, c, 2));
            }
        }

        // e_1에서 시작
        Stage currentPos = floor.getStages().stream()
                .filter(s -> s.getRow() == 1 && s.getColumn().equals("e"))
                .findFirst().orElse(null);

        // 3번 이동 데모
        for (int i = 0; i < 3; i++) {
            gameView.showMap(floor, currentPos);
            String move = gameView.getMovementInput();

            int nextRow = currentPos.getRow();
            char nextCol = currentPos.getColumn().charAt(0);

            if (move.equals("w")) nextCol--;
            else if (move.equals("s")) nextCol++;
            else if (move.equals("a")) nextRow--;
            else if (move.equals("d")) nextRow++;

            final int r = nextRow;
            final char c = nextCol;
            Stage next = floor.getStages().stream()
                    .filter(s -> s.getRow() == r && s.getColumn().equals(String.valueOf(c)))
                    .findFirst().orElse(null);

            if (next != null) {
                currentPos = next;
            } else {
                gameView.showMapAlert("더 이상 갈 수 없습니다.");
            }
        }

        gameView.showFloorTransition(3);
    }

    // 6. 도감
    static void testCollection(GameView gameView) {
        int menu = gameView.showCollectionMenu();
        gameView.clearScreen();

        if (menu == 5) return;

        String[] titles = {"카드 도감", "아이템 도감", "보스 도감", "엔딩 도감"};
        gameView.showCollectionHeader(titles[menu - 1]);
        System.out.println();

        gameView.showCollectionEntry("Scanner", "ATK:15 - 입력을 읽어들인다 (해금: 1트라이)", true);
        gameView.showCollectionEntry("println", "ATK:10 - 출력으로 공격한다 (해금: 1트라이)", true);
        gameView.showCollectionEntry(null, null, false);
        gameView.showCollectionEntry(null, null, false);

        gameView.showProgress(2, 4);
        gameView.waitForEnter();
    }

    // 7. 엔딩 타이틀
    static void testEnding(GameView gameView) {
        gameView.showEndingTitle("넘어짐 엔딩");

        List<String> endingLines = Arrays.asList(
                "영균은 설산에서 도망치다 발을 헛디뎠다.",
                "",
                "끝없이 추락하는 영균.",
                "코드 세계는 그렇게 그를 삼켰다.",
                "",
                "- END -"
        );
        for (String line : endingLines) {
            gameView.printSlow(line);
        }

        gameView.showAcquire("넘어짐 엔딩");
        gameView.waitForEnter();
    }
}
