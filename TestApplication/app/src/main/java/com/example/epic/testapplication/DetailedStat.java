package com.example.epic.testapplication;

/**
 * Created by henryshangguan on 6/19/15.
 */
public class DetailedStat {
    private String name;
    private int data;

    public DetailedStat(String string, int num) {
        name = string;
        data = num;
    }

    public String getName() {
        return name;
    }

    public int getData() {
        return data;
    }


}
