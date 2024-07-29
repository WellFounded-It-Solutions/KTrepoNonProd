package se.infomaker.frtutilities.mainmenutoolbarsettings;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import se.infomaker.frtutilities.TextUtils;

public class ToolbarConfig {
    public enum Position {
        LEFT,
        RIGHT,
        CENTER,
        NONE,
    }

    public enum ToolbarVisibility {
        HIDDEN,
        TRANSPARENT,
        FLAT,
        VISIBLE,
    }

    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String CENTER = "center";
    private static final String NONE = "none";
    private static final String FLAT = "flat";
    private static final String VISIBLE = "visible";
    private static final String HIDDEN = "hidden";
    private static final String TRANSPARENT = "transparent";
    private static final String DEFAULT_LOGO_RESOURCE = "toolbar_logo";

    private String visibility = VISIBLE;
    private String logoPosition = NONE;
    private String logoResource = DEFAULT_LOGO_RESOURCE;
    private String titlePosition = LEFT;

    private List<ButtonConfig> buttons = new ArrayList<>();

    public ToolbarConfig() {
        this(new Builder());
    }

    public ToolbarConfig(Builder builder) {
        if (!TextUtils.isEmpty(builder.visibility)) {
            visibility = builder.visibility;
        }
        if (!TextUtils.isEmpty(builder.logoPosition)) {
            logoPosition = builder.logoPosition;
        }
        if (!TextUtils.isEmpty(builder.logoResource)) {
            logoResource = builder.logoResource;
        }
        if (!TextUtils.isEmpty(builder.titlePosition)) {
            titlePosition = builder.titlePosition;
        }
        if (builder.buttons != null) {
            buttons = builder.buttons;
        }
    }

    public ToolbarVisibility visibility() {
        switch (visibility) {
            case HIDDEN: {
                return ToolbarVisibility.HIDDEN;
            }
            case TRANSPARENT: {
                return ToolbarVisibility.TRANSPARENT;
            }
            case FLAT: {
                return ToolbarVisibility.FLAT;
            }
            default: {
                //visible | translucent | filled
                return ToolbarVisibility.VISIBLE;
            }
        }
    }

    public List<ButtonConfig> getButtons() {
        return buttons;
    }

    public Position getLogoPosition() {
        return resolvePosition(logoPosition, Position.RIGHT);
    }

    public String getLogoResource() {
        return logoResource;
    }

    public Position getTitlePosition() {
        return resolvePosition(titlePosition, Position.LEFT);
    }

    private static Position resolvePosition(String positionString, Position defaultPosition) {
        switch (positionString) {
            case LEFT:
                return Position.LEFT;
            case RIGHT:
                return Position.RIGHT;
            case CENTER:
                return Position.CENTER;
            case NONE:
                return Position.NONE;
            default:
                return defaultPosition;
        }
    }

    public static class ButtonConfig {
        private String position = RIGHT;
        @Nullable
        private String icon;
        @Nullable
        private String text;
        @Nullable
        private JsonObject click;

        public Position getPosition() {
            return resolvePosition(position, Position.RIGHT);
        }

        @Nullable
        public String getIcon() {
            return icon;
        }

        @Nullable
        public String getText() {
            return text;
        }

        @Nullable
        public JsonObject getClick() {
            return click;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String visibility;
        private String logoResource;
        private String logoPosition;
        private String titlePosition;
        private List<ButtonConfig> buttons;

        private Builder() {
        }

        public Builder visibility(String visibility) {
            this.visibility = visibility;
            return this;
        }

        public Builder logoResource(String logoResource) {
            this.logoResource = logoResource;
            return this;
        }

        public Builder logoPosition(String logoPosition) {
            this.logoPosition = logoPosition;
            return this;
        }

        public Builder titlePosition(String titlePosition) {
            this.titlePosition = titlePosition;
            return this;
        }

        public Builder buttons(List<ButtonConfig> buttons) {
            this.buttons = buttons;
            return this;
        }

        public ToolbarConfig build() {
            return new ToolbarConfig(this);
        }
    }
}
