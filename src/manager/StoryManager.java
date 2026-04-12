package manager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoryManager {

    // key: "prologue.story" , "floor1.battle_boss_win" 등
    // value: 해당 태그의 대사 목록
    private Map<String, List<String>> dialogues = new HashMap<>();

    // ============================================
    // 파일 로드 (층별 txt 파일)
    // ============================================
    public void loadFile(String filePath, String prefix) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentTag = null;
            List<String> currentLines = null;

            while ((line = br.readLine()) != null) {
                line = line.stripTrailing();

                // 태그 라인: [story], [battle_boss_win] 등
                if (line.startsWith("[") && line.endsWith("]")) {
                    // 이전 태그 저장
                    if (currentTag != null && currentLines != null) {
                        dialogues.put(prefix + "." + currentTag, currentLines);
                    }

                    currentTag = line.substring(1, line.length() - 1);  // [] 제거
                    currentLines = new ArrayList<>();
                } else if (currentTag != null) {
                    // 빈 줄도 포함 (연출용 빈 줄)
                    currentLines.add(line);
                }
            }

            // 마지막 태그 저장
            if (currentTag != null && currentLines != null) {
                dialogues.put(prefix + "." + currentTag, currentLines);
            }

        } catch (IOException e) {
            System.out.println("[StoryManager] 파일 로드 실패: " + filePath + " - " + e.getMessage());
        }
    }

    // ============================================
    // 전체 스토리 파일 로드
    // ============================================
    public void loadAll(String basePath) {
        loadFile(basePath + "/prologue.txt",   "prologue");
        loadFile(basePath + "/floor1.txt",     "floor1");
        loadFile(basePath + "/floor2.txt",     "floor2");
        loadFile(basePath + "/floor3.txt",     "floor3");
        loadFile(basePath + "/floor4.txt",     "floor4");
        loadFile(basePath + "/floor5.txt",     "floor5");
        loadFile(basePath + "/floor6.txt",     "floor6");
        loadFile(basePath + "/floor7.txt",     "floor7");
        loadFile(basePath + "/heap.txt",       "heap");
        loadFile(basePath + "/floor8.txt",     "floor8");
        loadFile(basePath + "/system.txt",     "system");
        loadFile(basePath + "/ascii_art.txt",  "ascii_art");
    }

    // ============================================
    // 대사 조회
    // ============================================
    public List<String> get(String key) {
        return dialogues.getOrDefault(key, List.of("..."));
    }

    public List<String> get(String prefix, String tag) {
        return get(prefix + "." + tag);
    }

    // ============================================
    // 태그 존재 여부 확인
    // ============================================
    public boolean has(String key) {
        return dialogues.containsKey(key);
    }

    public boolean has(String prefix, String tag) {
        return has(prefix + "." + tag);
    }
}
