
package au.com.blueoak.portal.utility;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {insert details}
 * <br><br>
 * <b>(c)2008 Blue Oak Solutions Pty Ltd. All rights reserved.<br>
 */
public class StringUtils 
    extends org.apache.commons.lang3.StringUtils 
{
    /** ABN prefix definitions */
    public static final String PREFIX_ABN = "ABN ";
    /** ACN prefix definitions */
    public static final String PREFIX_ACN = "ACN ";
    /** format used to group the ABN details */
    public static final byte[] GROUPING_ABN_NUM = {2, 3, 3, 3};
    /** format used to group the ACN details */
    public static final byte[] GROUPING_ABC_NUM = {3, 3, 3};    
    
    /**
     * Check whether the given String is empty.
     * <p>This method accepts any Object as an argument, comparing it to
     * {@code null} and the empty String. As a consequence, this method
     * will never return {@code true} for a non-null non-String object.
     * <p>The Object signature is useful for general attribute handling code
     * that commonly deals with Strings but generally has to iterate over
     * Objects since attributes may e.g. be primitive value objects as well.
     * @param str the candidate String
     * @since 3.2.1
     */
    public static boolean isEmpty(Object str) {
        return (str == null || "".equals(str));
    }

    /**
     * Check that the given CharSequence is neither {@code null} nor of length 0.
     * Note: Will return {@code true} for a CharSequence that purely consists of whitespace.
     * <p><pre>
     * StringUtils.hasLength(null) = false
     * StringUtils.hasLength("") = false
     * StringUtils.hasLength(" ") = true
     * StringUtils.hasLength("Hello") = true
     * </pre>
     * @param str the CharSequence to check (may be {@code null})
     * @return {@code true} if the CharSequence is not null and has length
     * @see #hasText(String)
     */
    public static boolean hasLength(CharSequence str) {
        return (str != null && str.length() > 0);
    }

    /**
     * Check that the given String is neither {@code null} nor of length 0.
     * Note: Will return {@code true} for a String that purely consists of whitespace.
     * @param str the String to check (may be {@code null})
     * @return {@code true} if the String is not null and has length
     * @see #hasLength(CharSequence)
     */
    public static boolean hasLength(String str) {
        return hasLength((CharSequence) str);
    }

    /**
     * Check whether the given CharSequence has actual text.
     * More specifically, returns {@code true} if the string not {@code null},
     * its length is greater than 0, and it contains at least one non-whitespace character.
     * <p><pre>
     * StringUtils.hasText(null) = false
     * StringUtils.hasText("") = false
     * StringUtils.hasText(" ") = false
     * StringUtils.hasText("12345") = true
     * StringUtils.hasText(" 12345 ") = true
     * </pre>
     * @param str the CharSequence to check (may be {@code null})
     * @return {@code true} if the CharSequence is not {@code null},
     * its length is greater than 0, and it does not contain whitespace only
     * @see Character#isWhitespace
     */
    public static boolean hasText(CharSequence str) {
        if (!hasLength(str)) {
            return false;
        }
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the given String has actual text.
     * More specifically, returns {@code true} if the string not {@code null},
     * its length is greater than 0, and it contains at least one non-whitespace character.
     * @param str the String to check (may be {@code null})
     * @return {@code true} if the String is not {@code null}, its length is
     * greater than 0, and it does not contain whitespace only
     * @see #hasText(CharSequence)
     */
    public static boolean hasText(String str) {
        return hasText((CharSequence) str);
    }
    
    /**
     * Clean the specified string. If after the string contains no text null is returned.
     *
     * @param str
     * @return
     */
    public static String trim( String str )
    {
        if( str != null )
        {
            str = str.trim();
            if( str.length() <= 0 )
                str = null;
        }
        
        return( str );
    }

    /**
     * Delete all the whitespaces from a string, including any within the string (not just leading and trailing). This
     * works that same was as {@link StringUtils#deleteWhitespace(String)} but additionally ensures that an empty result
     * is null.
     *
     * @param str  the String to delete whitespace from, may be null
     * @return the String without whitespaces, {@code null} if null String input
     */    
    public static String deleteWhitespaceToNull( String str )
    {
        return( trim(deleteWhitespace(str)) );
    }
    
    /**
     * Remove leading and trailing whitespace and then replacing sequences of whitespace characters by a single space.
     * This works the same way as {@link StringUtils#normalizeSpace(String)} but additionally ensures that an empty
     * result (after normalization) is null.
     *
     * @param str
     * @return
     */
    public static String normalizeSpaceToNull( String str )
    {
        return( trim(normalizeSpace(str)) );
    }
    
    /**
     * This normalizes spaces to NULL using {@link #normalizeSpaceToNull(String)} but after normalization the string
     * length will be not longer then maxWidth using {@link StringUtils#abbreviate(String, int, int)}.
     *
     * @param str
     * @param maxWidth
     * @return
     */
    public static String normalizeSpaceToNullAbbreviate( String str, int maxWidth )
    {
        return( StringUtils.abbreviate(StringUtils.normalizeSpaceToNull(str), maxWidth) );
    }
    
    /**
     * Trim the specified string to be a singular (if it's plural). The rules that are implemented are general in 
     * nature and follow these rules:
     * 
     * (1) replace 'ies' ending with 'y' (e.g. babies -> baby)
     * (2) remove 'es' at the end where the -es is preceded by s, z, x, sh, ch. If it's an s or z, it may be doubled 
     *  (e.g. foxes -> fox, buses or busses -> bus, quizzes -> quiz)
     * (3) remove 's' at the end (e.g. dogs -> dog) 
     *
     * @param str
     * @return
     */
    public static String trimToSingular( String str )
    {
        if( hasText(str) )
        {
            if( endsWithIgnoreCase(str, "ies") )
            {
                str = str.substring( 0, str.length() - 3 ) + "y";
            }
            else if( endsWithIgnoreCase(str, "es") )
            {
// TODO complete 'es' plural to singular convertion                
            }
            else if( endsWithIgnoreCase(str, "s") )
            {
                str = str.substring( 0, str.length() - 1 );
            }
        }
        
        return( str );
    }
    
    /**
     * Check if both are the same. The two specified parameter are the same if they
     * are both null or if they are both actually the same (ignoring case).
     *
     * @param text1
     * @param text2
     * @return
     */
    public static boolean isSame( String text1, String text2 )
    {
        return( isSame(text1, text2, true) );
    }
    
    /**
     * Check if both are the same. The two specified parameter are the same if they
     * are both null or if they are both actually the same. Case checking on the string
     * could be ignored if specified.
     *
     * @param text1
     * @param text2
     * @param ignoreCase
     * @return
     */
    public static boolean isSame( String text1, String text2, boolean ignoreCase )
    {
        // details being the same
        boolean same = false;

        // check if both are null
        if( !hasText(text1) && !hasText(text2) )
        {   // no string specified so they are the same
            same = true;
        }
        else if( hasText(text1) && hasText(text2) )
        {   // both have values, check if they are the same
            if( ignoreCase )
                same = text1.equalsIgnoreCase( text2 );
            else
                same = text1.equals( text2 );
        }
        
        // the details are same
        return( same );
    }

    /**
     * Append the specified string to the specified buffer. If the specified buffer is 
     * not available a new one is created. If a separator is specified it will be appended
     * to the buffer if it already contains some data before the specified string is
     * appended.
     *
     * @param strBuilder
     * @param str
     * @return
     */
    public static StringBuilder appendStr( StringBuilder strBuilder, String str, String separator )
    {
        if( strBuilder == null )
        {   // no buffer is available so create one and add the specified string
            strBuilder = new StringBuilder( str );
        }
        else
        {   // buffer is available, check if the separator needs to be added
            if( hasLength(separator) && strBuilder.length() > 0 )
                strBuilder.append( separator );
            // add the specified string
            strBuilder.append( str );
        }
        
        return( strBuilder );
    }
    
    /**
     * 
     *
     * @param str1
     * @param str2
     * @param separator
     * @return
     */
    public static String combineStr( String str1, String str2, String separator )
    {
        String result = null;
        
        if( hasText(str1) && !hasText(str2) )
            result = str1;
        else if( !hasText(str1) && hasText(str2) )
            result = str2;
        else if( hasText(str1) && hasText(str2) )
            result = (new StringBuilder( trim(str1) )).
                append( separator ).append( trim(str2) ).toString();
        
        return( result );
    }

    /**
     * Append the specified collection of strings to the specified buffer. If the specified buffer is 
     * not available a new one is created. If a separator is specified it will be appended between each of
     * the string found in the collection. Note, the separator will not be added to the end of the specified
     * even if it contains some data.
     *
     * @param strBuilder
     * @param strCollection
     * @param separator
     * @return
     */
    public static StringBuilder combineStr( StringBuilder strBuilder, Collection<String> strCollection, String separator )
    {
        // check that there is something to work with
        if( strCollection != null && !strCollection.isEmpty() )
        {   // there is something to work with, create the string builder if required
            if( strBuilder == null )
                strBuilder = new StringBuilder();
            // go through the collection and add them together
            boolean addSeparator = false;
            for( String string : strCollection )
            {
                if( hasText(string) )
                {
                    if( addSeparator && hasLength(separator) )
                        strBuilder.append( separator);
                    strBuilder.append( string );
                    addSeparator = true;
                }
            }
        }
        
        // return the result
        return( strBuilder );
    }
    
    /**
     * 
     *
     * @param groupPattern
     * @param rawText
     * @param groups
     * @return
     */
    private static String group( String groupPattern, String rawText, int groups )
    {
        // the specified texted formatted 
        String formattedText = null;
        
        // format the number using the specified groupping pattern
        Pattern pattern = Pattern.compile( groupPattern );
        Matcher matcher = pattern.matcher( rawText );
        
        // get the intial string back for now
        if( matcher.find() )
        {   // the patterns has been found so go and create the groups
            StringBuffer groupedString = new StringBuffer();
            for( int i = 1; i <= groups; i++ )
                groupedString.append( matcher.group(i) ).append( " " );
            formattedText = groupedString.toString().trim();
        }

        // return the formatted number (if the pattern matched)
        return( (formattedText == null) ? rawText : formattedText );
    }

    /**
     * 
     * 
     *
     * @param number
     * @param totalDigits
     * @param pattern
     * @return
     */
    public static String group( long number, byte[] grouping )
    {
        // the specified number format as string 
        String numberAsString = null;
        
        // check that the specified parameters are correct
        if( (grouping != null) && (grouping.length != 0) )
        {   // there is some sort of grouping defined so try to group things

            // construct pattern for the groups
            StringBuilder groupPattern = new StringBuilder();
            StringBuilder numberFormatPattern = new StringBuilder();
            int groups = grouping.length;
            int digitCount = 0;
            for( int i = 0; i < groups; i++ )
            {   // append to the grouping patterm
                groupPattern.append( "(\\d{" ).append( grouping[i] ).append( "})" );
                // update the number format pattern
                for( digitCount = grouping[i]; digitCount > 0; digitCount-- )
                    numberFormatPattern.append( "0" );
            }

            // format the number into the required number of digits
            final DecimalFormat decimalFormat = new DecimalFormat( numberFormatPattern.toString() );
            String rawText = decimalFormat.format( number );
            // now group the number as specified by the pattern
            numberAsString = group( groupPattern.toString(), rawText, groups );
        }
        
        // return the formatted number (if the pattern matched)
        return( (numberAsString == null) ? Long.toString(number) : numberAsString );
    }
    
    /**
     * 
     * 
     *
     * @param number
     * @param totalDigits
     * @param pattern
     * @return
     */
    public static String group( String text, byte[] grouping )
    {
        // the specified texted formatted 
        String formattedText = null;
        
        // check that the specified parameters are correct
        if( (grouping != null) && (grouping.length != 0) )
        {   // there is some sort of grouping defined so try to group things

            // construct pattern for the groups
            StringBuilder groupPattern = new StringBuilder();
            int groups = grouping.length;
            for( int i = 0; i < groups; i++ )
            {   // append to the grouping patterm
                groupPattern.append( "(\\p{ASCII}{" ).append( grouping[i] ).append( "})" );
            }

            // now group the number as specified by the pattern
            formattedText = group( groupPattern.toString(), text, groups );
        }
        
        // return the formatted number (if the pattern matched)
        return( (formattedText == null) ? text : formattedText );
    }
    
    /**
     * Compare one string against the other. The result is based on str1.compareTo( str2 ).
     * If either one is null then the one who is null will be greater and if they are both null
     * then both are simply the same. 
     *
     * @param str1
     * @param str2
     * @return
     */
    public static int compare( String str1, String str2 )
    {
        // result of the comparison
        int result = 0;
        
        if( hasText(str1) && hasText(str2) )
        {   // both have some text specified, comparison is based on the upper case version
            result = trim(str1).compareToIgnoreCase( trim(str2) );
        }
        else if( !hasText(str1) && hasText(str2) )
        {   // str1 is null/empty but str2 has some text so str1 is greater
            result = -1;
        }
        else if( hasText(str1) && !hasText(str2) )
        {   // str1 is not empty but str2 is null or empty so str2 is greater
            result = 1;
        }
        
        // return the result of the comparison
        return( result );
    }
    
    /**
     * Get the formatted version of the specified legal identifier if it's one of the know identifiers (ABN/ACN). If
     * the details are not then a clean version (leading and trailing spaces removed) is returned.
     *
     * @param legalId
     * @param includePrefix should prefix be added to the result if known
     * @return
     */
    public static String getFormattedLegalIdentifier( String legalId, boolean includePrefix )
    {
        // format the legal identifier details
        String result = null;
        if( StringUtils.hasText(legalId) )
        {   // remove any whitespaces, etc from the details before trying to see if it'sa number
            final String tmpLegalStr = StringUtils.deleteWhitespaceToNull(legalId);
            try
            {   // try to parse the legal id as a number (checking if it just contains numbers)
                long tmpLegalNum = Long.parseLong(tmpLegalStr);
                // if it go here it means that we have a number to work with no see if it's any of the supported
                // Australian business identifier
                if( NumberUtil.isValidABN(tmpLegalNum) )
                    result = (includePrefix ? PREFIX_ABN : "")
                        + StringUtils.group(tmpLegalNum, GROUPING_ABN_NUM);
                else if( NumberUtil.isValidACN(tmpLegalNum) )
                    result = (includePrefix ? PREFIX_ACN : "")
                        + StringUtils.group(tmpLegalNum, GROUPING_ABC_NUM);
            }
            catch (NumberFormatException ex)
            {}  // problem converting the legal id into a number so do not do anything special with it
        }

        // return the result, if nothing has been set/formatted yet just use the original details
        return( StringUtils.hasText(result) ? result : StringUtils.trim(legalId));
    }
    
    /**
     * Convert the array of bytes into HEX values.
     *
     * @param value
     * @return
     */
    public static String getHexString(byte[] value)
    {
        String result = null;
        
        if(value != null && value.length > 0)
        {
            final StringBuilder strBldr = new StringBuilder();
            for(int i = 0; i < value.length; i++)
                strBldr.append(Integer.toHexString((value[i] &0xFF) | 0x100).substring(1, 3));
            result = strBldr.toString().toUpperCase();
        }
        
        return( result );
    }
}
