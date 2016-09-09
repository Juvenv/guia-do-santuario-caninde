package com.example.kessi.guiadosantuariocaninde;

import java.util.ArrayList;

/**
 * Created by kessi on 08/09/16.
 */
public class Group {
    private String name;
    private ArrayList<Child> items;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Child> getItems() {
        return items;
    }

    public void setItems(ArrayList<Child> items) {
        this.items = items;
    }
}
