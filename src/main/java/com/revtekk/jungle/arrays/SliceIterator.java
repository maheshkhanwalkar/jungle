package com.revtekk.jungle.arrays;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Slice iterator
 *
 * This iterator allows iterating over the contents of the given slice,
 * under the bounds restrictions enforced by the slice.
 *
 * @param <T> slice element type
 */
public class SliceIterator<T> implements Iterator<T>
{
    private final Slice<T> slice;
    private int pos;

    public SliceIterator(Slice<T> slice, int pos)
    {
        this.slice = slice;
        this.pos = pos;
    }

    @Override
    public boolean hasNext()
    {
        return pos < slice.size();
    }

    @Override
    public T next()
    {
        if(!hasNext())
            throw new NoSuchElementException("reached end of slice");

        T elem = slice.get(pos);
        pos++;

        return elem;
    }
}
