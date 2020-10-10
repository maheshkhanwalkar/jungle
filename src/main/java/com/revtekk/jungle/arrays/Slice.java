package com.revtekk.jungle.arrays;

import java.util.Iterator;

/**
 * The {@code Slice} class creates a lightweight "slice" on top of an underlying object arrays,
 * which allows for proper bounds checking and slice range modification.
 *
 * The underlying array can be modified by the slice -- it is not immutable -- but, the underlying
 * array cannot be resized larger than what is it initialised to.
 *
 * Since this class uses generics, this solution will only be very performant for object arrays.
 * The use of this class for primitive arrays will require boxing the array into the corresponding
 * object array, which does have a significant penalty if performed repeatedly.
 *
 * @param <T> array object type
 */
public class Slice<T> implements Iterable<T>
{
    private final T[] arr;
    private int start, end;

    /**
     * Create a slice over the entire array
     * @param arr underlying array
     */
    public Slice(T[] arr)
    {
        this.arr = arr;
        this.start = 0;
        this.end = arr.length;
    }

    /**
     * Create a slice over a portion of the array
     * The bounds of the slice are [start, end)
     *
     * @param arr underlying array
     * @param start starting index
     * @param end ending index (not inclusive)
     */
    public Slice(T[] arr, int start, int end)
    {
        this.arr = arr;
        this.start = start;
        this.end = end;
    }

    /**
     * Get the total slice capacity
     * This is the length of the underlying array
     *
     * @return the capacity
     */
    public int capacity()
    {
        return arr.length;
    }

    /**
     * Get the length of the slice
     * @return the length
     */
    public int size()
    {
        return end - start;
    }

    /**
     * Reset the slice to cover the entire array
     *
     * This method overwrites the starting and ending indices and sets
     * them to be the starting and ending bounds of the underlying array
     */
    public void reset()
    {
        this.start = 0;
        this.end = arr.length;
    }

    /**
     * Get the element at the specified index within the slice
     * @param index requested index
     * @return the element at that index within the slice
     * @throws IndexOutOfBoundsException if the provided index is beyond the ending bounds
     */
    public T get(int index)
    {
        if(index > size())
            throw new IndexOutOfBoundsException("index points to beyond end of slice");

        return arr[start + index];
    }

    /**
     * Set the element at the specified index
     * @param elem new element
     * @param index index to set
     * @throws IndexOutOfBoundsException if the provided index is beyond the ending bounds
     */
    public void set(T elem, int index)
    {
        if(index > size())
            throw new IndexOutOfBoundsException("index points to beyond end of slice");

        arr[start + index] = elem;
    }

    @Override
    public Iterator<T> iterator()
    {
        return new SliceIterator<>(this, 0);
    }
}
