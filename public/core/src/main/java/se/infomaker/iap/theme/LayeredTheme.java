package se.infomaker.iap.theme;

import java.util.List;

import se.infomaker.iap.theme.alignment.ThemeAlignment;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.font.ThemeFont;
import se.infomaker.iap.theme.image.ThemeImage;
import se.infomaker.iap.theme.letterspacing.ThemeLetterSpacing;
import se.infomaker.iap.theme.linespacing.ThemeLineSpacing;
import se.infomaker.iap.theme.size.ThemeSize;
import se.infomaker.iap.theme.style.text.ThemeTextStyle;
import se.infomaker.iap.theme.transforms.ThemeTransforms;
import timber.log.Timber;

/**
 * Theme that can be defined in layers supporting references between the layers
 */
public class LayeredTheme extends BaseTheme {

    private final Resolver<ThemeColor> colorResolver;
    private final Resolver<ThemeSize> sizeResolver;
    private final Resolver<ThemeFont> fontResolver;
    private final Resolver<ThemeTextStyle> textStyleResolver;
    private final Resolver<ThemeImage> imageResolver;
    private final Resolver<ThemeLineSpacing> lineSpacingResolver;
    private final Resolver<ThemeTransforms> transformsResolver;
    private final Resolver<ThemeAlignment> alignmentResolver;
    private final Resolver<ThemeLetterSpacing> letterSpacingResolver;

    public LayeredTheme(LayeredTheme parent, Resolver<ThemeColor> colorResolver,
                        Resolver<ThemeSize> sizeResolver,
                        Resolver<ThemeFont> fontResolver,
                        Resolver<ThemeTextStyle> textStyleResolver,
                        Resolver<ThemeImage> imageResolver,
                        Resolver<ThemeLineSpacing> lineSpacingResolver,
                        Resolver<ThemeTransforms> transformsResolver,
                        Resolver<ThemeAlignment> alignmentResolver,
                        Resolver<ThemeLetterSpacing> letterSpacingResolver){
        this.colorResolver = colorResolver;
        this.sizeResolver = sizeResolver;
        this.fontResolver = fontResolver;
        this.textStyleResolver = textStyleResolver;
        this.imageResolver = imageResolver;
        this.lineSpacingResolver = lineSpacingResolver;
        this.transformsResolver = transformsResolver;
        this.alignmentResolver = alignmentResolver;
        this.letterSpacingResolver = letterSpacingResolver;

        if (parent != null) {
            try {
                colorResolver.setParent(parent.colorResolver);
            } catch (CircleReferenceException e) {
                Timber.e(e, "Failed to set parent");
            }
            try {
                sizeResolver.setParent(parent.sizeResolver);
            } catch (CircleReferenceException e) {
                Timber.e(e, "Failed to set parent");
            }
            try {
                fontResolver.setParent(parent.fontResolver);
            } catch (CircleReferenceException e) {
                Timber.e(e, "Failed to set parent");
            }
            try {
                lineSpacingResolver.setParent(parent.lineSpacingResolver);
            } catch (CircleReferenceException e) {
                Timber.e(e, "Failed to set parent");
            }
            try {
                textStyleResolver.setParent(parent.textStyleResolver);
            } catch (CircleReferenceException e) {
                Timber.e(e, "Failed to set parent");
            }
            try {
                imageResolver.setParent(parent.imageResolver);
            } catch (CircleReferenceException e) {
                Timber.e(e, "Failed to set parent");
            }
            try {
                transformsResolver.setParent(parent.transformsResolver);
            } catch (CircleReferenceException e) {
                Timber.e(e, "Failed to set parent");
            }
            try {
                alignmentResolver.setParent(parent.alignmentResolver);
            } catch (CircleReferenceException e) {
                Timber.e(e, "Failed to set parent");
            }
            try {
                letterSpacingResolver.setParent(parent.letterSpacingResolver);
            } catch (CircleReferenceException e) {
                Timber.e(e, "Failed to set parent");
            }
        }
    }

    @Override
    public ThemeColor getColor(String name, ThemeColor fallback) {
        return colorResolver.get(name, fallback);
    }

    @Override
    public ThemeColor getColor(List<String> names, ThemeColor fallback) {
        if (names == null) {
            return fallback;
        }
        for (String name : names) {
            ThemeColor value = getColor(name, null);
            if (value != null) {
                return value;
            }
        }
        return fallback;
    }

    @Override
    public ThemeColor getColor(ThemeColor fallback, String... names) {
        if (names == null) {
            return fallback;
        }
        for (String name : names) {
            ThemeColor value = getColor(name, null);
            if (value != null) {
                return value;
            }
        }
        return fallback;
    }

    @Override
    public ThemeSize getSize(String name, ThemeSize fallback) {
        return sizeResolver.get(name, fallback);
    }

    @Override
    public ThemeSize getSize(List<String> names, ThemeSize fallback) {
        if (names == null) {
            return fallback;
        }
        for (String name : names) {
            ThemeSize value = getSize(name, null);
            if (value != null) {
                return value;
            }
        }
        return fallback;
    }

    @Override
    public ThemeSize getSize(ThemeSize fallback, String... names) {
        if (names == null) {
            return fallback;
        }
        for (String name : names) {
            ThemeSize value = getSize(name, null);
            if (value != null) {
                return value;
            }
        }
        return fallback;
    }

    @Override
    public ThemeFont getFont(String name, ThemeFont fallback) {
        return fontResolver.get(name, fallback);
    }

    @Override
    public ThemeFont getFont(List<String> names, ThemeFont fallback) {
        if (names == null) {
            return fallback;
        }
        for (String name : names) {
            ThemeFont value = getFont(name, null);
            if (value != null) {
                return value;
            }
        }
        return fallback;
    }

    @Override
    public ThemeFont getFont(ThemeFont fallback, String... names) {
        if (names == null) {
            return fallback;
        }
        for (String name : names) {
            ThemeFont value = getFont(name, null);
            if (value != null) {
                return value;
            }
        }
        return fallback;
    }

    @Override
    public ThemeLineSpacing getLineSpacing(String name, ThemeLineSpacing fallback) {
        return lineSpacingResolver.get(name, fallback);
    }

    @Override
    public ThemeLineSpacing getLineSpacing(List<String> names, ThemeLineSpacing fallback) {
        if (names == null) {
            return fallback;
        }
        for (String name : names) {
            ThemeLineSpacing value = getLineSpacing(name, null);
            if (value != null) {
                return value;
            }
        }
        return fallback;
    }

    @Override
    public ThemeLineSpacing getLineSpacing(ThemeLineSpacing fallback, String... names) {
        if (names == null) {
            return fallback;
        }
        for (String name : names) {
            ThemeLineSpacing value = getLineSpacing(name, null);
            if (value != null) {
                return value;
            }
        }
        return fallback;
    }

    @Override
    public ThemeTextStyle getText(String name, ThemeTextStyle fallback) {
        return textStyleResolver.get(name, fallback);
    }

    @Override
    public ThemeTextStyle getText(List<String> names, ThemeTextStyle fallback) {
        if (names == null) {
            return fallback;
        }
        for (String name : names) {
            ThemeTextStyle value = getText(name, null);
            if (value != null) {
                return value;
            }
        }
        return fallback;
    }

    @Override
    public ThemeTextStyle getText(ThemeTextStyle fallback, String... names) {
        if (names == null) {
            return fallback;
        }
        for (String name : names) {
            ThemeTextStyle value = getText(name, null);
            if (value != null) {
                return value;
            }
        }
        return fallback;
    }

    @Override
    public ThemeImage getImage(String name, ThemeImage fallback) {
        return imageResolver.get(name, fallback);
    }

    @Override
    public ThemeImage getImage(List<String> names, ThemeImage fallback) {
        if (names == null) {
            return fallback;
        }
        for (String name : names) {
            ThemeImage value = getImage(name, null);
            if (value != null) {
                return value;
            }
        }
        return fallback;
    }

    @Override
    public ThemeImage getImage(ThemeImage fallback, String... names) {
        if (names == null) {
            return fallback;
        }
        for (String name : names) {
            ThemeImage value = getImage(name, null);
            if (value != null) {
                return value;
            }
        }
        return fallback;
    }

    @Override
    public ThemeTransforms getTransforms(String name, ThemeTransforms fallback) {
        return transformsResolver.get(name, fallback);
    }

    @Override
    public ThemeAlignment getAlignment(String name, ThemeAlignment fallback) {
        return alignmentResolver.get(name, fallback);
    }

    @Override
    public ThemeLetterSpacing getLetterSpacing(String name, ThemeLetterSpacing fallback) {
        return letterSpacingResolver.get(name, fallback);
    }
}
