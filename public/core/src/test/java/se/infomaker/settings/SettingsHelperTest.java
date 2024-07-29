package se.infomaker.settings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettingsHelperTest {
    List<Integer> list;
    @Before
    public void setUp() throws Exception {
        list = new ArrayList<>();
        list.add(-5);
        list.add(0);
        list.add(2);
        list.add(10);
        list.add(20);
        list.add(30);
    }

    /*
    The first method run in this test class takes a long time. We don't want any of the real tests
    to take this long time. Hence we have this empty warm up "test".
     */
    @Test
    public void aWarmup() throws Exception {
        SettingsHelper.getClosestValue(42, list);
    }

    @Test(expected = InvalidParameterException.class)
    public void noList() {
        SettingsHelper.getClosestValue(0, new ArrayList<>());
    }

    @Test
    public void higherThanMax() throws Exception {
        int closestValue = SettingsHelper.getClosestValue(42, list);
        Assert.assertEquals(30, closestValue);
    }

    @Test
    public void lowerThanMin() throws Exception {
        int closestValue = SettingsHelper.getClosestValue(-100, list);
        Assert.assertEquals(-5, closestValue);
    }

    @Test
    public void onAValue() throws Exception {
        int closestValue = SettingsHelper.getClosestValue(10, list);
        Assert.assertEquals(10, closestValue);
    }

    @Test
    public void exactlyInBetween() throws Exception {
        int closestValue = SettingsHelper.getClosestValue(0, list);
        Assert.assertEquals(0, closestValue);
    }

    @Test
    public void exactlyInBetweenReversedList() throws Exception {
        Collections.reverse(list);
        int closestValue = SettingsHelper.getClosestValue(0, list);
        Assert.assertEquals(0, closestValue);
    }

    @Test
    public void sameValueInListTwice() throws Exception {
        list.add(10);
        int closestValue = SettingsHelper.getClosestValue(10, list);
        Assert.assertEquals(10, closestValue);
    }
}