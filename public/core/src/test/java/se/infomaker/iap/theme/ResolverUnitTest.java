package se.infomaker.iap.theme;

import junit.framework.Assert;

import org.junit.Test;

import java.util.HashMap;

@SuppressWarnings("SameParameterValue")
public class ResolverUnitTest {

    private static final String MATCH_VALUE = "match";
    private static final String FALLBACK_VALUE = "fallback";

    @Test
    public void testMatch(){
        Resolver<String> resolver = resolverWithKeyValuePair("one", MATCH_VALUE);
        Assert.assertEquals(MATCH_VALUE, resolver.get("one", FALLBACK_VALUE));
    }

    @Test
    public void fallbackInSameLayer() {
        Resolver<String> resolver = resolverWithKeyValuePair("one", MATCH_VALUE);
        String result = resolver.get("two", FALLBACK_VALUE);
        Assert.assertEquals(FALLBACK_VALUE, result);
    }

    @Test
    public void referenceInSameLayer() {
        HashMap<String, String> values = new HashMap<>();
        values.put("one", MATCH_VALUE);
        HashMap<String, String> references = new HashMap<>();
        references.put("two", "one");
        Resolver<String> resolver = new Resolver<>(values, references);
        Assert.assertEquals(MATCH_VALUE, resolver.get("two", FALLBACK_VALUE));
    }

    @Test
    public void valueInParent() throws CircleReferenceException {
        Resolver<String> parent = resolverWithKeyValuePair("one", MATCH_VALUE);
        Resolver<String> resolver = emptyResolver();
        Assert.assertEquals(FALLBACK_VALUE, resolver.get("one", FALLBACK_VALUE));
        resolver.setParent(parent);
        Assert.assertEquals(MATCH_VALUE, resolver.get("one", FALLBACK_VALUE));
    }

    @Test
    public void referenceFromParentToChild() throws CircleReferenceException {
        Resolver<String> parent = resolverWithReference("one", "two");
        Resolver<String> resolver = resolverWithKeyValuePair("two", MATCH_VALUE);
        Assert.assertEquals(FALLBACK_VALUE, resolver.get("one", FALLBACK_VALUE));
        resolver.setParent(parent);
        Assert.assertEquals(MATCH_VALUE, resolver.get("one", FALLBACK_VALUE));
    }

    @Test
    public void referenceFromChildToParent() throws CircleReferenceException {
        Resolver<String> parent = resolverWithKeyValuePair("one", MATCH_VALUE);
        Resolver<String> resolver = resolverWithReference("two", "one");
        Assert.assertEquals(FALLBACK_VALUE, resolver.get("two", FALLBACK_VALUE));
        resolver.setParent(parent);
        Assert.assertEquals(MATCH_VALUE, resolver.get("two", FALLBACK_VALUE));
    }

    @Test
    public void referenceWithoutValue() {
        Resolver<String> resolver = resolverWithReference("one", MATCH_VALUE);
        Assert.assertEquals(FALLBACK_VALUE, resolver.get("one", FALLBACK_VALUE));
    }

    @Test
    public void detectCircleParentReference() throws CircleReferenceException {
        Resolver<String> parent1 = emptyResolver();
        Resolver<String> parent2 = emptyResolver();
        Resolver<String> parent3 = emptyResolver();
        parent1.setParent(parent2);
        parent2.setParent(parent3);
        Resolver<String> resolver = emptyResolver();
        parent3.setParent(resolver);
        try{
            resolver.setParent(parent1);
        }
        catch (CircleReferenceException e) {
            return;
        }
        Assert.fail("Circle reference not detected");
    }

    private Resolver<String> resolverWithKeyValuePair(String key, String value) {
        HashMap<String, String> values = new HashMap<>();
        values.put(key, value);
        return new Resolver<>(values, new HashMap<String, String>());
    }

    private Resolver<String> resolverWithReference(String key, String value) {
        HashMap<String, String> references = new HashMap<>();
        references.put(key, value);
        return new Resolver<>(new HashMap<String, String>(), references);
    }

    private Resolver<String> emptyResolver() {
        return new Resolver<>(new HashMap<String, String>(), new HashMap<String, String>());
    }
}
