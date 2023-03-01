package au.com.blueoak.portal.utility;

import java.util.Collection;
import java.util.Map;

/**
 * Collection utility class.
 * <br><br>
 * <b>(c)2008 Blue Oak Solutions Pty Ltd. All rights reserved.<br>
 * Source Control:</b> $Id: CollectionUtils.java 132 2008-04-02 04:37:48Z attilam $
 */
public class CollectionUtils
    extends org.springframework.util.CollectionUtils
{
    /**
     * Private constructor as this is a utility class.
     */
    private CollectionUtils()
    {}

    /**
     * 
     *
     * @param a
     * @return
     */
    public static boolean isEmpty( Object[] a ) 
    {
        return( a == null || a.length <= 0 );
    }
    
    /**
     * Get the size of the collection; zero is returned if the collection is null.
     *
     * @param a
     * @return
     */
    public static int size( Object[] a )
    {
        return( a == null ? 0 : a.length );
    }
    
    /**
     * Get the size of the collection; zero is returned if the collection is null.
     *
     * @param a
     * @return
     */
    public static int size( Collection<?> a )
    {
        return( a == null ? 0 : a.size() );
    }
    
    /**
     * Get the size of the collection; zero is returned if the collection is null.
     *
     * @param a
     * @return
     */
    public static int size( Map<?,?> a )
    {
        return( a == null ? 0 : a.size() );
    }
    
    /**
     * Compares a collection of classes that have implemented {@link com.bluebilling.common.util.CustomComparable} interface.  
     *
     * @param <T>
     * @param a
     * @param b
     * @return
     */
    public static <T extends CustomComparable<T>> boolean isSame( Collection<? extends T> a, Collection<? extends T> b )
    {
        // checking status
        boolean same = false;

        // check if there is anything to work with
        if( (a == null || a.isEmpty()) && (b == null || b.isEmpty()) )
        {   // either nothing was specified or both collections are empty
            same = true;
        }
        else if( a != null && !a.isEmpty() && b != null && !b.isEmpty() )
        {   // there are something in both collections, check their content
            if( isSameElements( a, b ) )
                same = isSameElements( b, a );
        }

        // return the checking status
        return( same );
    }
    
    /**
     * Check if the content of the first parameter is in the second one. The checking is only done one way.
     *
     * @param a
     * @param b
     * @return
     */
    private static <T extends CustomComparable<T>> boolean isSameElements( Collection<? extends T> a, Collection<? extends T> b )
    {
        boolean same = false;
        
        for( T itemA : a )
        {   // get the next item and see if it's in the second list
            same = false;
            for( T itemB : b )
            {   // get the next item and is if they are the same 
                if( itemA.isSame(itemB) )
                {   // they are same
                    same = true;
                    // no need to look further in the current list
                    break;
                }
            }
            
            // see if item 1 was NOT found in the second list
            if( !same )
            {   // it was NOT found so there is no point going any further as we know
                // the two list do not contain the same items
                break;
            }
        }
        
        return( same );
    }
}
