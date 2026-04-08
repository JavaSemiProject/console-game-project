package model;

import manager.BattleManager;
import manager.BattleManager.BattleResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BattleTest {
    public static void main(String[] args) {

        // ============================================
        // 히어로 3인 생성
        // ============================================
        Hero youngkyun = new Hero("H001", 100, 5, 10, 0, "C001");
        Hero hyejin    = new Hero("H002",  80, 8, 15, 1, "C002");
        Hero sunhyuk   = new Hero("H003",  70, 3,  8, 0, null);

        System.out.println("=== 히어로 ===");
        System.out.println("영균: " + youngkyun);
        System.out.println("혜진: " + hyejin);
        System.out.println("선혁: " + sunhyuk);

        // ============================================
        // 카드 생성 (effect 포함)
        // ============================================
        Card scannerCard = new Card.Builder()
                .cId("C001").pp(1).cName("Scanner")
                .method("SCAN_INPUT").cPower(10)
                .cDesc("사용자 입력을 읽어들이는 기본 도구.")
                .build();
        // build 후 effect 세팅 (Card ↔ Effect 순환 참조 해결)
        scannerCard.setEffect(new AttackEffect(10, scannerCard));

        Card randomCard = new Card.Builder()
                .cId("C002").pp(0).cName("Math.random")
                .method("RANDOM_ATK").cPower(25)
                .cDesc("랜덤 주사위를 굴려 공격한다.")
                .build();
        randomCard.setEffect(new AttackEffect(25, randomCard));

        Card arraySortCard = new Card.Builder()
                .cId("C004").pp(0).cName("Arrays.sort")
                .method("SORT_SLASH").cPower(30)
                .cDesc("배열을 정렬하며 적을 베어낸다.")
                .build();
        arraySortCard.setEffect(new AttackEffect(30, arraySortCard));

        // ============================================
        // 몬스터 생성 (2층 보스: 미주)
        // ============================================
        NPC miju = new NPC("M001", "미주", "영균아 실망이야 ☞☜", 40, false, 8, 12, 1, "C003");

        Card mijuCard = new Card.Builder()
                .cId("C003").pp(0).cName("System.out")
                .method("PRINT_MSG").cPower(5)
                .cDesc("콘솔에 메시지를 출력한다.")
                .build();
        mijuCard.setEffect(new AttackEffect(8, mijuCard));

        System.out.println("\n=== 적 ===");
        System.out.println("미주: " + miju);

        // ============================================
        // 플레이어 인벤토리
        // ============================================
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(scannerCard);
        playerCards.add(randomCard);
        playerCards.add(arraySortCard);

        List<Item> playerItems = new ArrayList<>();
        // 포션 아이템 추가
        Item potion = new Item.Builder()
                .iId("I001").iName("포션")
                .heal(30).power(0).hp(0).pp(0)
                .iDesc("HP를 30 회복하는 기본 포션.")
                .build();
        potion.setEffect(new ItemHealEffect(30, potion));
        playerItems.add(potion);

        // ============================================
        // 전투 시작! (영균 vs 미주)
        // ============================================
        System.out.println("\n========================================");
        System.out.println("  영균 vs 미주 - 전투 시작!");
        System.out.println("========================================\n");

        Scanner scanner = new Scanner(System.in);
        BattleManager battleManager = new BattleManager(scanner);

        BattleResult result = battleManager.startBattle(
                youngkyun, miju, playerCards, playerItems, mijuCard
        );

        System.out.println("\n========================================");
        switch (result) {
            case WIN:  System.out.println("  결과: 승리!"); break;
            case LOSE: System.out.println("  결과: 패배..."); break;
            case FLEE: System.out.println("  결과: 도망!"); break;
        }
        System.out.println("========================================");
        System.out.println("영균 최종 상태: " + youngkyun);
        System.out.println("미주 최종 상태: " + miju);

        scanner.close();
    }
}
