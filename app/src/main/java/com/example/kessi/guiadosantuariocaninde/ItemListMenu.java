package com.example.kessi.guiadosantuariocaninde;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by kessi on 08/09/16.
 */
@IgnoreExtraProperties
public class ItemListMenu {

    public String name;

    public ItemListMenu() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public ItemListMenu(String name){
        this.name = name;
    }




}
