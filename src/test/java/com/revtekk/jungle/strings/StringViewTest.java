package com.revtekk.jungle.strings;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringViewTest
{
    private static final String orig = "hello world, this is an example string";

    @Test
    public void testCharAtForwards()
    {
        StringView view = new StringView(orig);

        for(int i = 0; i < orig.length(); i++)
            assertEquals(orig.charAt(i), view.charAt(i));
    }

    @Test
    public void testCharAtBackwards()
    {
        StringView view = new StringView(orig, true);
        String rev = doSBReverse();

        for(int i = 0; i < rev.length(); i++)
            assertEquals(rev.charAt(i), view.charAt(i));
    }

    @Test
    public void testReverseReset()
    {
        StringView view = new StringView(orig);
        assertTrue(view.equalsString(orig));

        view.reverse();
        assertTrue(view.equalsString(doSBReverse()));

        view.reverse();
        assertTrue(view.equalsString(orig));

        view.reverse();
        view.reset();

        assertTrue(view.equalsString(orig));
    }

    @Test
    public void testEqualsString()
    {
        StringView small = new StringView("hello");
        StringView big = new StringView("hello world");

        assertFalse(small.equalsString(orig));
        assertFalse(big.equalsString("hello"));
        assertFalse(small.equalsString("seven"));
    }

    @Test
    public void testEqualsAndHash()
    {
        StringView view = new StringView(orig);
        StringView copy = new StringView(view);

        assertEquals(view, view);

        assertEquals(view, copy);
        assertEquals(view.hashCode(), copy.hashCode());

        copy.reverse();

        assertNotEquals(view, copy);
    }

    /**
     * Get the reverse string using StringBuilder
     * @return the reversed string
     */
    private static String doSBReverse()
    {
        StringBuilder builder = new StringBuilder(orig);
        return builder.reverse().toString();
    }
}
