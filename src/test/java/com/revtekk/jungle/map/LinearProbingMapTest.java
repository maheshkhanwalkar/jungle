package com.revtekk.jungle.map;

import org.junit.Test;

import static org.junit.Assert.*;

public class LinearProbingMapTest
{
    @Test
    public void simplePutGet()
    {
        LinearProbingMap<Integer, Integer> map = new LinearProbingMap<>();
        final int NUM_ENTRIES = 1000;

        for(int i = 0; i < NUM_ENTRIES; i++)
            map.put(i, i);

        for(int i = 0; i < NUM_ENTRIES; i++)
        {
            assertTrue(map.containsKey(i));
            assertEquals((int)map.get(i), i);
        }

        assertEquals(map.size(), NUM_ENTRIES);
    }

    @Test
    public void testPutDelete()
    {
        LinearProbingMap<Integer, Integer> map = new LinearProbingMap<>();

        final int START = 800;
        final int MID = START * 2;
        final int END = MID * 2;
        final int NEXT = END * 2;

        for(int i = START; i < END; i++)
            map.put(i, i);

        for(int i = START; i < END; i++)
            assertEquals((int)map.get(i), i);

        for(int i = MID; i < END; i++)
            map.remove(i);

        for(int i = END; i < NEXT; i++)
            map.put(i, i);

        for(int i = START; i < NEXT; i++)
        {
            if(i < MID || i >= END)
            {
                assertTrue(map.containsKey(i));
                assertEquals((int)map.get(i), i);
            }
            else
            {
                assertFalse(map.containsKey(i));
                assertNull(map.get(i));
            }
        }
    }

    @Test
    public void testDeleteSkipping()
    {
        LinearProbingMap<Integer, Integer> map = new LinearProbingMap<>(256);

        for(int i = 0; i < 100; i++)
            map.put(i, i);

        map.put(262, 262);
        map.remove(8);

        assertTrue(map.containsKey(262));
    }

    @Test
    public void testWrapAround()
    {
        LinearProbingMap<Integer, Integer> map = new LinearProbingMap<>(256);

        // Fill up entries 10..255 in the table
        for(int i = 200; i < 256; i++)
            map.put(i, i);

        map.put(457, 457);

        assertTrue(map.containsKey(457));
        assertEquals((int)map.get(457), 457);
    }
}
