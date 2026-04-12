package model;

/**
 * DB 기반 도감 목록 항목. 발견 여부 포함.
 * firstTry == -1 이면 아직 미발견.
 */
public class CollectionEntry {
    private String id;
    private String name;
    private int firstTry;   // -1 = 미발견
    private int count;      // 총 획득/조우 횟수

    public CollectionEntry(String id, String name, int firstTry, int count) {
        this.id = id;
        this.name = name;
        this.firstTry = firstTry;
        this.count = count;
    }

    public String getId()      { return id; }
    public String getName()    { return name; }
    public int getFirstTry()   { return firstTry; }
    public int getCount()      { return count; }
    public boolean isFound()   { return firstTry != -1; }
}