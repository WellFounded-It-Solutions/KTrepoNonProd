package se.infomaker.livecontentui.section.configuration;

public class DividerConfig {
    private String drawable = null;
    private String placement = "between"; //between | around

    public String getDrawable() {
        return drawable;
    }

    public Placement getPlacement() {
        switch (placement) {
            case "around":
                return Placement.AROUND;
            case "between":
            default:
                return Placement.BETWEEN;
        }
    }

    public enum Placement {
        BETWEEN, //We only have lines between the items, not before first and after last
        AROUND, //We have lines between the imtes, AND before first and after last
    }
}
