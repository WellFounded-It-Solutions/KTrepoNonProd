package se.infomaker.frtutilities;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class MainMenuConfig {

    @SerializedName("modules")
    private ArrayList<MainMenuItem> mainMenuItems;

    public MainMenuConfig() {

    }

    public ArrayList<MainMenuItem> getMainMenuItems() {
        return this.mainMenuItems;
    }

    public void setMainMenuItems(ArrayList<MainMenuItem> mainMenuItems) {
        this.mainMenuItems = mainMenuItems;
    }
}