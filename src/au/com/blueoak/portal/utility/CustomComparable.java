package au.com.blueoak.portal.utility;

/**
 * Interface used to impose rules for checking if two objects are the same.
 * This is different to the equals as isSame typically requires all data 
 * members of the object to be the same before true is returned. 
 * <br><br>
 * <b>(c)2008 Blue Oak Solutions Pty Ltd. All rights reserved.<br>
 * Source Control:</b> $Id: CustomComparable.java 122 2008-03-17 17:57:37Z attilam $
 */
public interface CustomComparable<T extends CustomComparable<T>>
{
    /**
     * 
     *
     * @param that
     * @return
     */
    public boolean isSame( T that );
}
