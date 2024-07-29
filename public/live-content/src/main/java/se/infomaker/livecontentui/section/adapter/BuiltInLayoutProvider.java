package se.infomaker.livecontentui.section.adapter;

import java.util.HashMap;

class BuiltInLayoutProvider implements LayoutProvider {
    private final HashMap<String, Integer> templates = new HashMap<>();
    public BuiltInLayoutProvider() {
        
    }
    @Override
    public int getLayout(String template) {
        return 0;
    }
}
