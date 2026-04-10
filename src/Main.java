import view.GameView;
import view.MainMenuView;
import manager.GameManager;

import java.io.PrintStream;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // Windows 콘솔 UTF-8 설정
        try {
            new ProcessBuilder("cmd", "/c", "chcp 65001").inheritIO().start().waitFor();
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (Exception ignored) {}

        Scanner scanner = new Scanner(System.in);
        GameView gameView = new GameView(scanner);
        MainMenuView menuView = new MainMenuView(scanner);

        GameManager gameManager = new GameManager(scanner, gameView, menuView);
        gameManager.run();
    }
}
