package edu.kosa.terrainproject.noise;

import java.util.Random;

public final class PermutationTable {
    private static final int TABLE_SIZE = 256;
    private static final int EXTENDED_SIZE = TABLE_SIZE * 2;
    private final int[] table;

    public PermutationTable(long seed) {
        this.table = new int[EXTENDED_SIZE];
        initializeTable(seed);
    }

    private void initializeTable(long seed) {
        int[] base = new int[TABLE_SIZE];
        for (int i = 0; i < TABLE_SIZE; i++) {
            base[i] = i;
        }
        Random rand = new Random(seed);
        for (int i = TABLE_SIZE - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = base[i];
            base[i] = base[j];
            base[j] = temp;
        }
        for (int i = 0; i < TABLE_SIZE; i++) {
            table[i] = table[i + TABLE_SIZE] = base[i];
        }
    }

    public int get(int index) {
        return table[index & (TABLE_SIZE - 1)];
    }
}