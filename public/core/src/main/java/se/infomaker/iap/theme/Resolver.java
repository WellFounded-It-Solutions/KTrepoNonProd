package se.infomaker.iap.theme;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

/**
 * Resolves attributes by following internal references from the leaf up the stem
 * @param <T> to resolve to
 */
public class Resolver <T>{
    private Resolver<T> parent;
    private final Map<String, T> values;
    private final Map<String, String> references;
    private final Map<String, T> cache = new HashMap<>();

    public Resolver(Resolver<T> parent) {
        this.parent = parent;
        this.values = new HashMap<>();
        this.references = new HashMap<>();
    }

    public Resolver(Map<String, T> values, Map<String, String> references) {
        this.values = values;
        this.references = references;
    }

    public T get(String key, T fallback) {
        return get(null, key, fallback);
    }

    private T get(Resolver<T> leaf, String key, T fallback) {
        if (values.containsKey(key)) {
            return values.get(key);
        }
        if (leaf == null && cache.containsKey(key)) {
            return cache.get(key);
        }
        if (references.containsKey(key)) {
            try {
                if (leaf != null) {
                    return leaf.resolve(leaf, references.get(key), null);
                }
                else {
                    // Cache resolved values in leafs
                    T resolvedValue = resolve(this, references.get(key), null);
                    cache.put(key, resolvedValue);
                    return resolvedValue;
                }
            } catch (ThemeException e) {
                Timber.e(e, "Failed to resolve attribute");
            }
        } else if (parent != null) {
            return parent.get(leaf != null ? leaf : this, key, fallback);
        }
        return fallback;
    }

    private T resolve(Resolver<T> leaf, String key, Set<String> usedReferences) throws ThemeException {
        if (usedReferences != null && usedReferences.contains(key)) {
            throw new CircleReferenceException("Could not resolve value for key: " + key);
        }
        if (values.containsKey(key)) {
            return values.get(key);
        }
        if (references.containsKey(key)) {
            String reference = references.get(key);
            Set<String> myUsedReferences = usedReferences != null ? usedReferences : new HashSet<String>();
            myUsedReferences.add(key);
            return leaf.resolve(leaf, reference, myUsedReferences);
        }
        if (parent != null) {
            return parent.resolve(leaf, key, usedReferences);
        }
        throw new UndefinedException("No value defined for " + key);
    }

    /**
     * Sets the parent resolver for the resolver
     * @param parent to set
     * @throws CircleReferenceException if any parent in parent chain exists more than once
     */
    public void setParent(Resolver<T> parent) throws CircleReferenceException {
        if (parent != this) {
            Resolver<T> ancestor = parent.parent;
            HashSet<Resolver<T>> hierarchy = new HashSet<>();
            hierarchy.add(this);
            hierarchy.add(parent);
            while (ancestor != null) {
                if (hierarchy.contains(ancestor)) {
                    throw new CircleReferenceException("Failed to set parent");
                }
                hierarchy.add(ancestor);
                ancestor = ancestor.parent;
            }
            this.parent = parent;
        }
    }
}