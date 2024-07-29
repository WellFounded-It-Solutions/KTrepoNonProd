package se.infomaker.frtutilities;

import android.content.Context;
import android.text.TextUtils;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import timber.log.Timber;

/**
 * Loads and caches templates
 */
public class TemplateManager {

    public static final String DEFAULT_ERROR_TEMPLATE = "default_error_template.mustache";

    private static final Map<String, TemplateManager> MANAGERS = new HashMap<>();

    public static TemplateManager getManager(Context context, String moduleId) {
        if (!MANAGERS.containsKey(moduleId)) {
            synchronized (MANAGERS) {
                if (!MANAGERS.containsKey(moduleId)) {
                    MANAGERS.put(moduleId, new TemplateManager(new ResourceManager(context.getApplicationContext(), moduleId)));
                }
            }
        }
        return MANAGERS.get(moduleId);
    }

    private ResourceManager resourceManager;
    private Map<String, Template> templates = new HashMap<>();

    public TemplateManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public Template getTemplate(String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        if (templates.containsKey(name)) {
            return templates.get(name);
        }
        InputStream is = null;
        try {
            is = resourceManager.getAssetStream(name);
            String templateString = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
            Template template = new Handlebars().compileInline(templateString);
            templates.put(name, template);
        } catch (IOException e) {
            Timber.e(e, "Could not load template");
        }
        finally {
            IOUtils.safeClose(is);
        }
        return templates.get(name);
    }

    public Template getTemplate(String name, String fallback) {
        Template template = null;
        if (!TextUtils.isEmpty(name)) {
            template = getTemplate(name);
        }
        if (template == null) {
            template = getTemplate(fallback);
        }
        return template;
    }
}
