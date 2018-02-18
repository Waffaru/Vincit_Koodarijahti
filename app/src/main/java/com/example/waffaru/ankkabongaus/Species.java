package com.example.waffaru.ankkabongaus;

/**
 * Created by Waffaru on 11.2.2018.
 */

public enum Species {
    mallard("Mallard"), redhead("Redhead"), gadwall("Gadwall"), canvasback("Canvasback"),
    lesser_scaup("Lesser Scaup");

    private final String name;

    Species(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static String[] getAsString() {
        return new String[]{"mallard", "redhead", "gadwall", "canvasback", "lesser scaup"};
    }
}
