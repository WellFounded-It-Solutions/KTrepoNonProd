package se.infomaker.frt.moduleinterface.model;

import java.util.ArrayList;

import se.infomaker.frtutilities.MainMenuItem;

public class ModulesConfig {

    private ArrayList<MainMenuItem> modules;

    public ModulesConfig() {

    }

    public ArrayList<MainMenuItem> getMainMenuItems() {
        return this.modules;
    }

    public void setMainMenuItems(ArrayList<MainMenuItem> mainMenuItems) {
        this.modules = mainMenuItems;
    }
}
