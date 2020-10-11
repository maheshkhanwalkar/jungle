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
}
