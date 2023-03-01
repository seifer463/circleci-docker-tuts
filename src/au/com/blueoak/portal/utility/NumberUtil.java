package au.com.blueoak.portal.utility;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Working with numbers utility class.
 * <br><br>
 * <b>(c)2008 Blue Oak Solutions Pty Ltd. All rights reserved.<br>
 */
public class NumberUtil
{
    /** number of decimal places to round dollar amounts */
    private static final int AMOUNT_ROUND_DECIMALS = 2;
    /** weights used in calculating the ABN checksum */
    private static final int[] WEIGHTS_ABN = {10, 1, 3, 5, 7, 9, 11, 13, 15, 17, 19};
    /** weights used in calculating the ACN checksum */
    private static final int[] WEIGHTS_ACN = {8, 7, 6, 5, 4, 3, 2, 1, 0};
    /** weights used in calculating the MOD9 check digit */
    private static final int[] WEIGHTS_MOD9 = {5, 3, 2, 1, 13, 11, 7, 5, 3, 2};
    private static final int WEIGHTS_MOD9_LEN = WEIGHTS_MOD9.length;
    
    /**
     * Represents one Mega Byte. The actual number of bytes from a megabyte.<br>
     * 1048 bytes == 2^10<br>
     * 1048 bytes == 1MB
     */
    public static final BigDecimal ONE_MB = new BigDecimal(Math.pow(2, 10));

    /**
     * 
     *
     * @param number
     * @param decimals
     * @return
     */
    public static double getRound( double number, int decimals )
    {
        return( BigDecimal.valueOf( number ).setScale( decimals, RoundingMode.HALF_UP ).doubleValue() );
    }
    
    /**
     * Round the amount to the right number of decimal places.
     *
     * @param amount
     * @return
     */
    public static double getRoundedAmount( double amount )
    {
        return( getRound(amount, AMOUNT_ROUND_DECIMALS) );
    }
    
    /**
     * 
     *
     * @param amount
     * @return
     */
    public static BigDecimal getAmountCentsRounding( BigDecimal amount )
    {
        return( amount == null ? 
            BigDecimal.ZERO : new BigDecimal(getAmountCentsRounding(amount.doubleValue())) );
    }
    
    /**
     * Round down the specified amount to the nearest 5 cents.
     *
     * @param amount that needs to be rounded down
     * @return the amount by which the rounding is to occur
     */
    public static double getAmountCentsRounding( double amount )
    {
        // rounding amount that needs to be done
        double roundingAmount = 0.0;
        
        // get the cents component of the amount (e.g. 12.34 get the 0.04 cents part)
        double cents = ( ((amount * 10.0d) - ((int) (amount * 10))) / 10 );
        // check if the cent amount needs to be adjusted if the amount is negative
        if( cents < 0 )
            cents = 0.10 + cents;
        // check the limits on the cents
        if( cents < 0.05 )
            roundingAmount = cents;
        else if( cents > 0.05 )
            roundingAmount = (cents - 0.05);
        
        // return the rounding amount
        return( roundingAmount );
    }
    
    /**
     * Convert a number to the specified length of numbers and letter. This is like a decimal to
     * hexdecimal conversion except the base is going to be 62 instead of 16. The base will be
     * 0-9, a-z and A-Z which is based 62.
     *
     * @param number
     * @param length
     * @return
     */
    public static String toBase62String( int number, int length )
        throws IllegalArgumentException
    {
        return( toBase62String((long)number, length) );
    }
    
    public static String toBase62String( long number, int length )
        throws IllegalArgumentException
    {
        // check that the number is positive
        if( number < 0 )
            throw( new IllegalArgumentException("negative numbers conversion not supported") );
        
        // string use to build the converted version of the number  
        StringBuilder strBuilder = new StringBuilder();
        // check that the number is not zero
        if( number == 0 )
        {   // the number is zero so no calculation is required
            strBuilder.append( "0" );
        }
        else
        {   // number is greater then zero so do the base 62 conversion
            while( number != 0 )
            {   // get the base 62 remainder and convert it
                strBuilder.insert( 0, toBase62Char(number % 62) );
                // calculate the next number in the line
                number = (number / 62);
            }
        }
        
        // check any leading zeros needs to be added to the converted number
        if( strBuilder.length() < length )
        {   // need to add some leading zeros as the length is not meet yet
            length = length - strBuilder.length();
            for( ; length > 0; length-- )
                strBuilder.insert( 0, "0" );
        }
        
        // return the string version of the result
        return( strBuilder.toString() );
    }
    
    /**
     * Convert a number to a base 62 encoding. 
     * The format is 0-9, a-z and A-Z. 
     *
     * @param number
     * @return
     * @throws IllegalArgumentException if the specified number is greater then 61
     */
    private static char toBase62Char( long number )
        throws IllegalArgumentException
    {
        if( (number >= 0) && (number < 10) )
            // convert to simple digit between 0 and 9
            return( (char) (48 + number) );
        else if( (number >= 10) && (number <= 35) )
            // convert to a character between 'a' and 'z'
            return( (char) (97 + (number - 10)) );
        else if( (number >= 36) && (number <= 61) )
            // convert to a character between 'A' and 'Z'
            return( (char) (65 + (number - 36)) );
        else
            throw( new IllegalArgumentException("number must be [0, 61]") );
    }

    /**
     * Compare two values to determine order.
     *
     * @param value1
     * @param value2
     * @return negative value is value1 is before value2, positive is value2 is before value1, zero if both values are the same
     */
    public static int compare( int value1, int value2 )
    {
        if( value1 < value2 )
            return( -1 );
        else if( value1 > value2 )
            return( 1 );
        else
            return( 0 );
    }
    
    /**
     * Compare two values to determine order.
     *
     * @param value1
     * @param value2
     * @return negative value is value1 is before value2, positive is value2 is before value1, zero if both values are the same
     */
    public static int compare( long value1, long value2 )
    {
        if( value1 < value2 )
            return( -1 );
        else if( value1 > value2 )
            return( 1 );
        else
            return( 0 );
    }

    /**
     * Compare two values to determine order.
     *
     * @param value1
     * @param value2
     * @return negative value is value1 is before value2, positive is value2 is before value1, zero if both values are the same
     */
    public static int compare( double value1, double value2 )
    {
        if( value1 < value2 )
            return( -1 );
        else if( value1 > value2 )
            return( 1 );
        else
            return( 0 );
    }
    
    /**
     * Compare two different BigDecimal value for being the same value. This is a NULL safe compare and uses the 
     * BigDecimal.compareTo to do the actual comparison.
     *
     * @param val1
     * @param val2
     * @return
     * @see BigDecimal#compareTo(BigDecimal)
     */
    public static boolean isSame( BigDecimal val1, BigDecimal val2 )
    {
        if( (val1 == null && val2 != null) || (val1 != null && val2 == null) )
            return( false );
        
        return(val1 == null ? true : val1.compareTo(val2) == 0);
    }
    
    /**
     * MOD9 based check digit.
     *
     * @param number
     * @return
     * @see #mod9CheckDigit(String)
     */
    public static int mod9CheckDigit( long number )
    {
        return( mod9CheckDigit(Long.toString(number)) );
    }
    
    /**
     * MOD9 base check digit. This to work the number must be not more than 9 digits long.
     *
     * @param number
     * @return
     * @throws IllegalArgumentException
     */
    public static int mod9CheckDigit( String number )
        throws IllegalArgumentException
    {
        // ensure we have something to work with and correct length.
        Assert.state(StringUtils.hasText(number) && StringUtils.trim(number).length() <= WEIGHTS_MOD9_LEN, 
            "need a number that is not more than " + WEIGHTS_MOD9_LEN + " digits long to calculate the MOD9 on");
        
        // convert the specified number into an array to get each digit
        byte[] accountDigits = number.getBytes();
        // get the length of the number on which the check digit is to be calculated
        int numLen = accountDigits.length;
        // go through all the digits and apply the (product and summing process)
        int sumOfProducts = 0;
        int tmpDigit = 0;
        for( int i = 0; i < numLen; i++ )
        {   // get the next digit and from the number array 
            tmpDigit = (accountDigits[numLen - 1 - i] - 48);
            // multiple the digit with the predefined weight
            tmpDigit *= WEIGHTS_MOD9[WEIGHTS_MOD9_LEN - 1 - i];
            // sum the temp digit to the total product
            sumOfProducts += tmpDigit;
        }
    
        // calculate the final check digit and return it
        return( 9 - (sumOfProducts % 9) );
    }
    
    /**
     * MOD10 (or also known as Luhn algorithm) based check digit.
     *
     * @param number
     * @return
     */
    public static int mod10CheckDigit( long number ) 
    {
        return( mod10CheckDigit(Long.toString(number)) );
    }
    
    /**
     * MOD10 (or also known as Luhn algorithm) based check digit.
     *
     * @param number
     * @return
     * @throws IllegalArgumentException
     */
    public static int mod10CheckDigit( String number ) 
        throws IllegalArgumentException
    {
        // ensure we have something to work with
        Assert.hasText(number, "need a number to calculate the MOD10 on");
        
        // convert the specified number into an array to get each digit
        byte[] accountDigits = number.getBytes();
        // get the length of the number on which the check digit is to be calculated
        int numLen = accountDigits.length;
        // go through all the digits and apply the (product and summing process)
        int sumOfProducts = 0;
        int parity = numLen % 2;
        int tmpDigit = 0;
        for( int i = 0; i < numLen; i++ )
        {   // get the next digit and from the number array 
            tmpDigit = (accountDigits[i] - 48);
            // check if the digit is to be multiplied by two
            if( (i % 2) != parity )
                tmpDigit = tmpDigit * 2;
            // see if the digits needs to be added together
            if( tmpDigit > 9 )
                tmpDigit = tmpDigit - 9;
            // sum the temp digit to the total product
            sumOfProducts += tmpDigit;
        }
    
        // calculate the final check digit and return it
        return( ((sumOfProducts % 10) == 0) ? 0 : (10 - (sumOfProducts % 10)) );
    }
    
    /**
     * Check if the number is a valid ABN ({@link #isValidABN(long)} or ACN ({@link #isValidACN(long)).
     *
     * @param number
     * @return
     */
    public static boolean isValidABNorACN( long number )
    {
        return( isValidABN(number) || isValidACN(number) );
    }
    
    /**
     * Validate an Australian Business Number (ABN)
     * 
     * @link http://www.ato.gov.au/businesses/content.asp?doc=/content/13187.htm
     * @param abn
     * @return
     */
    public static boolean isValidABN( long abn )
    {
        // check that the specified number is 11 digits long
        String abnStr = Long.toString(abn);
        if(abnStr.length() > 11)
            return( false );
        else
            abnStr = StringUtils.leftPad(abnStr, 11, "0");
        
        // convert the specified number into an array to get each digit
        byte[] abnDigit = abnStr.getBytes();
        
        // subtract one from the first digit
        abnDigit[0] = (byte) (abnDigit[0] - 1);
        
        // sum of products
        int sumOfProducts = 0;
        int numLen = abnDigit.length;
        for( int i = 0; i < numLen; i++ )
            sumOfProducts += (abnDigit[i] - 48) * WEIGHTS_ABN[i];
        
        // using the sum of product to verify if the details are correct
        return(sumOfProducts % 89 == 0);
    }
    
    /**
     * Validate an Australian Company Number (ACN)
     *
     * @link http://www.asic.gov.au/asic/asic.nsf/byheadline/Australian+Company+Number+(ACN)+Check+Digit
     * @param acn
     * @return
     */
    public static boolean isValidACN( long acn )
    {
        // check that the specified number is 9 digits long
        String acnStr = Long.toString(acn);
        if(acnStr.length() > 9)
            return( false );
        else
            acnStr = StringUtils.leftPad(acnStr, 9, "0");
        
        // convert the specified number into an array to get each digit
        byte[] acnDigit = acnStr.getBytes();

        // sum of products
        int sumOfProducts = 0;
        int numLen = acnDigit.length;
        for( int i = 0; i < numLen; i++ )
            sumOfProducts += (acnDigit[i] - 48) * WEIGHTS_ACN[i];
        
        // get the complement of the remainder
        int remComp = 10 - (sumOfProducts % 10);
        if( remComp == 10 )
            remComp = 0;
        
        // verify that the remainder complement equals the specified checksum
        return( (acnDigit[8] - 48) == remComp );
    }
}
