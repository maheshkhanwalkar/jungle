package com.revtekk.jungle.string;

import java.util.Objects;

/**
 * The {@code StringView} class creates an immutable, low-cost "view" on top an
 * underlying string.
 *
 * The view itself can be modified - i.e. direction or bounds, but the underlying string
 * cannot be modified or swapped out and can always be recovered entirely but resetting
 * the view back to encompassing the entire string.
 *
 * TODO become feature-complete with String (for the most part)
 */
public class StringView
{
    private final String str;
    private boolean reverse;

    /**
     * Create a new (default) string view
     * This view encompasses the entire string in the forward direction
     *
     * @param str underlying string
     */
    public StringView(String str)
    {
        this.str = str;
        this.reverse = false;
    }

    /**
     * Create a new string view
     * This view encompasses the entire string with the direction specified by reverse
     *
     * @param str underlying string
     * @param reverse direction of the string, true = forwards, false = backwards
     */
    public StringView(String str, boolean reverse)
    {
        this.str = str;
        this.reverse = reverse;
    }

    /**
     * Create a copy of an existing string view
     * This view will be exactly the same as the view provided
     *
     * @param orig original string view
     */
    public StringView(StringView orig)
    {
        this.str = orig.str;
        this.reverse = orig.reverse;
    }

    /**
     * Reverses the current view (forward -> backwards, backwards -> forwards)
     */
    public void reverse()
    {
        reverse = !reverse;
    }

    /**
     * Reset the view back to encompassing the entire string in the forward direction
     * This allows the original string to be "recovered"
     */
    public void reset()
    {
        reverse = false;
    }

    /**
     * Get the character at the given index
     *
     * Depending on the current direction of the view, the index will either be
     * used as-is or modified to allow for backwards traversal.
     *
     * Therefore, using indices 0...len for charAt() when the view is backwards will
     * correspond to len - 1...0 for the actual underlying string
     *
     * @param index the index of the char value
     * @return the char value at the index of the given string
     */
    public char charAt(int index)
    {
        int actual = reverse ? str.length() - 1 - index : index;
        return str.charAt(actual);
    }

    /**
     * Check equality with a raw string
     * @param other string to check against
     * @return true if the view and the string are equal, false otherwise
     */
    public boolean equalsString(String other)
    {
        if(other.length() != str.length())
            return false;

        for(int i = 0; i < str.length(); i++)
        {
            if(other.charAt(i) != charAt(i))
                return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringView that = (StringView) o;
        return reverse == that.reverse &&
                Objects.equals(str, that.str);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(str, reverse);
    }
}
