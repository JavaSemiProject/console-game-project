package view;

import java.util.Scanner;

public class MainMenuView {

    private Scanner scanner;

    public MainMenuView(Scanner scanner) {
        this.scanner = scanner;
    }

    /** 메인 메뉴 출력 및 선택 */
    public int showMainMenu() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception e) {
            System.out.print("\033[3J\033[2J\033[H");
            System.out.flush();
        }

        System.out.println();
        System.out.println("  ╔═══════════════════════════════════╗");
        System.out.println("  ║                                   ║");
        System.out.println("  ║   코드 세계에 갇힌 영균의 탈출기       ║");
        System.out.println("  ║                                   ║");
        System.out.println("  ╚═══════════════════════════════════╝");
        System.out.println();
        System.out.println("  1. 새 게임");
        System.out.println("  2. 이어하기");
        System.out.println("  3. 도감");
        System.out.println("  4. 종료");
        System.out.print("\n  >> ");

        while (true) {
            String input = scanner.nextLine().trim();
            switch (input) {
                case "1": return 1;
                case "2": return 2;
                case "3": return 3;
                case "4": return 4;
                default:
                    System.out.print("  1~4 중에 선택해주세요.\n  >> ");
            }
        }
    }

    /** 저장된 데이터가 없을 때 안내 */
    public void showNoSaveData() {
        System.out.println("\n  저장된 데이터가 없습니다.");
        System.out.println("  [Enter를 눌러 돌아가기]");
        scanner.nextLine();
    }
}
