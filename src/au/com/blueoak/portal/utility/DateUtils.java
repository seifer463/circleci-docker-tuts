package au.com.blueoak.portal.utility;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

/**
 * Date utility class.
 * <br><br>
 * <b>(c)2008 Blue Oak Solutions Pty Ltd. All rights reserved.<br>
 */
public class DateUtils extends org.apache.commons.lang.time.DateUtils
{
    /** pattern used to extract date details from a date/time string (e.g. dd/MM/yyyy or yyyy-MM-dd) */
    private static final Pattern REGEX_PATTERN_DATE = 
        Pattern.compile("([0-9]{4}[-,/]{1}[0-9]{1,2}[-,/][0-9]{1,2})|([0-9]{1,2}[-,/][0-9]{1,2}[-,/][0-9]{4})");
    /** joda date formatter used to do the actual string to date object conversion */
    private static final DateTimeFormatter JODA_DATE_FORMATTER = new DateTimeFormatterBuilder()
        .append( null, new DateTimeParser[] {
            DateTimeFormat.forPattern("yyyy-MM-dd").getParser(),
            DateTimeFormat.forPattern("yyyy/MM/dd").getParser(),
            DateTimeFormat.forPattern("dd/MM/yyyy").getParser(),
            DateTimeFormat.forPattern("dd-MM-yyyy").getParser()
        } )
        .toFormatter()
        .withZoneUTC();
    /** the smallest ever date supported in the system */
    public static final Date FIRST_DATE = new Date(Long.MIN_VALUE);
    /** the largest ever date supported in the system */
    public static final Date LAST_DATE = new Date(Long.MAX_VALUE);

    /** default time zone used in the system */
    public static final TimeZone DEFAULT_TIME_ZONE = org.apache.commons.lang.time.DateUtils.UTC_TIME_ZONE; 
    
    /**
     * "dd/MM/yyyy"
     */
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    
    /**
     * "HH:mm"
     */
    public static final String TIME_FORMAT = "HH:mm:ss";
    
    /**
     * {@link DateUtils#DATE_FORMAT} + {@link DateUtils#TIME_FORMAT}
     */
    public static final String DATE_AND_TIME_FORMAT = new StringBuilder(DATE_FORMAT).append(" ").append(TIME_FORMAT).toString();
    
    /**
     * Clear the time component of the specified calendar object.
     *
     * @param calendar
     */
    public static void clearTimeFromDate( Calendar calendar )
    {
        if( calendar != null )
        {   // set the time to be mid-night on the specified date
            calendar.set( Calendar.HOUR_OF_DAY, 0 );
            calendar.set( Calendar.MINUTE, 0 );
            calendar.set( Calendar.SECOND, 0 );
            calendar.set( Calendar.MILLISECOND, 0 );
        }
    }
    
    /**
     * Remove any time component of the specified date.
     *
     * @param aDate
     * @return
     */
    public static Date getDateWithNoTime( Date aDate )
    {
        // only bother going any further if there is an object to work with
        if( aDate == null )
            return( null );
        
        // create a calendar to do the work with
        final GregorianCalendar workCalendar = new GregorianCalendar();
        workCalendar.setTime( aDate );
        
        // set the time to be mid-night on the specified date
        clearTimeFromDate( workCalendar );
        
        // return the date with all the time components removed
        return( workCalendar.getTime() );
    }
    
    /**
     * Get the current date in a specific time zone.
     *
     * @param timeZone
     * @return
     */
    public static Date getCurrentDate(TimeZone timeZone)
    {
        return(getDateInTimeZone(null, timeZone));
    }
    
    /**
     * Get the specified date at the specific time zone.
     *
     * @param date
     * @param timeZone
     * @return
     */
    public static Date getDateInTimeZone(Date date, TimeZone timeZone)
    {
        // ensure we have something to work with
        if(timeZone == null)
            timeZone = DateUtils.DEFAULT_TIME_ZONE;
        // convert the specified date and time to be at the specific time zone, 
        DateTime dateTime = (date == null) ? 
            new DateTime(DateTimeZone.forTimeZone(timeZone)) : new DateTime(date.getTime(), DateTimeZone.forTimeZone(timeZone));
        // extract the date component of the local time zone (month needs to be zero based)
        return (new GregorianCalendar(dateTime.getYear(), dateTime.getMonthOfYear() - 1, dateTime.getDayOfMonth())
            .getTime());
    }
    
    /**
     * Get the current date and time in specified time zone. <br>
     * If time zone is null it returns the time in UTC ({@link DateUtils#DEFAULT_TIME_ZONE}).
     *
     * @param timeZone
     * @return
     */
    public static Date getCurrentDateTime(TimeZone timeZone)
    {
        if(timeZone == null)
            timeZone = DateUtils.DEFAULT_TIME_ZONE;
        DateTime dateTime = new DateTime(DateTimeZone.forTimeZone(timeZone));

        GregorianCalendar utcTime = new GregorianCalendar();
        utcTime.set(Calendar.YEAR, dateTime.getYear());
        utcTime.set(Calendar.MONTH, dateTime.getMonthOfYear() - 1);
        utcTime.set(Calendar.DAY_OF_MONTH, dateTime.getDayOfMonth());
        utcTime.set(Calendar.HOUR_OF_DAY, dateTime.getHourOfDay());
        utcTime.set(Calendar.MINUTE, dateTime.getMinuteOfHour());
        utcTime.set(Calendar.SECOND, dateTime.getSecondOfMinute());
        return utcTime.getTime();
    }

    /**
     * Get the date but without any time component from the specified string. This is flexible to extract 
     * only the date element even if time component is supplied. The date can be formatted as dd/MM/yyyy
     * or yyyy-MM-dd where the element separator of '/' and '-' can be used interchangeably.
     * 
     * The returned date is in UTC timezone.
     *
     * @param aDate
     * @return the date that was found or NULL is nothing could be found
     */
    public static Date getDateWithNoTime( String aDate )
        throws java.lang.IllegalArgumentException
    {
        if( StringUtils.hasText(aDate) )
        {
            aDate = StringUtils.trim(aDate);
            final Matcher dateMatcher = REGEX_PATTERN_DATE.matcher(aDate);
            if( dateMatcher.find() )
                return( JODA_DATE_FORMATTER.parseLocalDate( dateMatcher.group() ).toDate() );
        }
        
        return( null );
    }
    
    /**
     * Check if the two dates are the same. The checking is only down to the second.
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSame( Date date1, Date date2 )
    {
        boolean same = false;
        
        if( (date1 == null) && (date2 == null) )
            same = true;
        else if( (date1 != null) && (date2 != null) )
            same = ( ((long) (date1.getTime() / 1000)) == ((long) (date2.getTime() / 1000)) );

        return( same );
    }
    
    /**
     * Adjust the specified date and time by a specific number of minutes using local time zone.
     *
     * @param aDate
     * @param minutes
     * @return
     */
    public static Date getDateTimeWithMinuteAdjustment( Date aDate, int minutes )
    {
        // return the date with all the time components removed
        return( getDateTimeWithMinuteAdjustment(aDate, minutes, false) );
    }

    /**
     * Adjust the specified date and time by a specific number of minutes while specifying if either local or UTC time 
     * zone is to be used. 
     *
     * @param aDate
     * @param minutes
     * @param utcTime
     * @return
     */
    public static Date getDateTimeWithMinuteAdjustment( Date aDate, int minutes, boolean utcTime )
    {
        // check the specified parameter
        Assert.notNull( aDate, "date needs to be specified" );
        
        // create a calendar to do the work with
        final GregorianCalendar workCalendar = 
            (utcTime ? new GregorianCalendar(UTC_TIME_ZONE) : new GregorianCalendar());
        workCalendar.setTime( aDate );
        // adjust the specify date and time by the number of minutes
        workCalendar.add( Calendar.MINUTE, minutes );
        
        // return the date with all the time components removed
        return( workCalendar.getTime() );
    }

    /**
     * Get the specified time to the minute (removing any seconds and milliseconds) while specifying if either local or 
     * UTC time zone is to be used.  
     *
     * @param aDate
     * @param utcTime
     * @return
     */
    public static Date getDateTimeToMinute( Date aDate, boolean utcTime )
    {
        // check the specified parameter
        Assert.notNull( aDate, "date needs to be specified" );

        // create a calendar to do the work with
        final GregorianCalendar workCalendar = 
            (utcTime ? new GregorianCalendar(UTC_TIME_ZONE) : new GregorianCalendar());
        workCalendar.setTime( aDate );
        // clear the second and the millisecond elements
        workCalendar.set( Calendar.SECOND, 0 );
        workCalendar.set( Calendar.MILLISECOND, 0 );
        
        // return the date with all the time components removed
        return( workCalendar.getTime() );
    }
    
    /**
     * Get the date from a date and time component. The creation of the date can be done in either local or UTC time zone.
     *
     * @param date
     * @param time
     * @param utcTime
     * @return
     */
    public static Date getDateTime( Date date, Date time, boolean utcTime )
    {
        // check the specified parameter
        Assert.notNull( date, "date needs to be specified" );
        Assert.notNull( time, "time needs to be specified" );

        // create the calendars to do the work with
        final GregorianCalendar workCldr1 = 
            (utcTime ? new GregorianCalendar(UTC_TIME_ZONE) : new GregorianCalendar());
        workCldr1.setTime( date );
        final GregorianCalendar workCldr2 = 
            (utcTime ? new GregorianCalendar(UTC_TIME_ZONE) : new GregorianCalendar());
        workCldr2.setTime( time );

        // combine the calendars together to produce the required result
        workCldr1.set( Calendar.HOUR_OF_DAY, workCldr2.get(Calendar.HOUR_OF_DAY) );
        workCldr1.set( Calendar.MINUTE, workCldr2.get(Calendar.MINUTE) );
        // clear the second and the millisecond elements
        workCldr1.set( Calendar.SECOND, 0 );
        workCldr1.set( Calendar.MILLISECOND, 0 );
        
        // return the date with all the time components removed
        return( workCldr1.getTime() );
    }
    
    /**
     * Get the next day from the specified date.
     *
     * @param aDate
     * @return
     * @throws IllegalArgumentException
     */
    public static Date getNextDay( Date aDate )
        throws IllegalArgumentException
    {
        // return the date with all the time components removed
        return( getDateWithDayAdjustment(aDate, 1, false) );
    }
    
    /**
     * Get the previous day from the specified date.
     *
     * @param aDate
     * @return
     * @throws IllegalArgumentException
     */
    public static Date getPreviousDay( Date aDate )
        throws IllegalArgumentException
    {
        // return the date with all the time components removed
        return( getDateWithDayAdjustment(aDate, -1, false) );
    }
    
    /**
     * 
     *
     * @param aDate
     * @param days
     * @param clearTime
     * @return
     */
    public static Date getDateWithDayAdjustment( Date aDate, int days, boolean clearTime )
    {
        // check the specified parameter
        Assert.notNull( aDate, "date needs to be specified" );

        // create a calendar to do the work with
        final GregorianCalendar workCalendar = new GregorianCalendar();
        workCalendar.setTime( aDate );

        // adjust the specify date by the number of days
        workCalendar.add( Calendar.DAY_OF_MONTH, days );
        // clear the time component if required
        if( clearTime )
            clearTimeFromDate( workCalendar );
        
        // return the date with all the time components removed
        return( workCalendar.getTime() );
    }
    
    /**
     * 
     *
     * @param aDate
     * @param months number of month by which to adjust the date
     * @param clearTime should the time component be cleared from the date
     * @return
     * @throws IllegalArgumentException
     */
    public static Date getDateWithMonthAdjustment( Date aDate, int months, boolean clearTime )
        throws IllegalArgumentException
    {
        // check the specified parameter
        Assert.notNull( aDate, "date needs to be specified" );

        // create a calendar to do the work with
        final GregorianCalendar workCalendar = new GregorianCalendar();
        workCalendar.setTime( aDate );

        // adjust the specify date by the number of months
        workCalendar.add( Calendar.MONTH, months );
        // clear the time component if required
        if( clearTime )
            clearTimeFromDate( workCalendar );
        
        // return the date with all the time components removed
        return( workCalendar.getTime() );
    }
    
    /**
     * 
     *
     * @param aDate
     * @param clearTime
     * @return
     */
    public static Date getDateAtMonthStart( Date aDate, boolean clearTime )
        throws IllegalArgumentException
    {
        // check the specified parameter
        Assert.notNull( aDate, "date needs to be specified" );

        // create a calendar to do the work with
        final GregorianCalendar workCalendar = new GregorianCalendar();
        workCalendar.setTime( aDate );

        // adjust the specify date by the number of months
        workCalendar.set( Calendar.DAY_OF_MONTH, 1 );
        // clear the time component if required
        if( clearTime )
            clearTimeFromDate( workCalendar );
        
        // return the date with all the time components removed
        return( workCalendar.getTime() );
    }
    
    /**
     * 
     *
     * @param aDate
     * @param clearTime
     * @return
     */
    public static Date getDateAtMonthEnd( Date aDate, boolean clearTime )
        throws IllegalArgumentException
    {
        // check the specified parameter
        Assert.notNull( aDate, "date needs to be specified" );

        // create a calendar to do the work with
        final GregorianCalendar workCalendar = new GregorianCalendar();
        workCalendar.setTime( aDate );

        // adjust the specify date by the number of months
        workCalendar.set( Calendar.DAY_OF_MONTH, workCalendar.getActualMaximum(Calendar.DAY_OF_MONTH) );
        // clear the time component if required
        if( clearTime )
            clearTimeFromDate( workCalendar );
        
        // return the date with all the time components removed
        return( workCalendar.getTime() );
    }
    
    /**
     * Check if the specified date has passed (before) the current system time. The testing could include the specified 
     * date or not. For example if the date is inclusive, and the specified date is on or before the current time, then 
     * true is returned.  
     * 
     * @param aDate
     * @param inclusive
     * @param includeTime if the current system time should also be considered and not just the date
     * @return
     * @throws IllegalArgumentException
     */
    public static boolean isDatePassed( Date aDate, boolean inclusive, boolean includeTime )
        throws IllegalArgumentException
    {
        return( isDatePassed(aDate, includeTime ? new Date(): getDateWithNoTime(new Date()), inclusive) );
    }

    /**
     * Check if bDate is on (inclusive = true) or after aDate. Note, the comparison does take time into consideration.
     *
     * @param aDate
     * @param bDate
     * @param inclusive
     * @return <b>true</b> if aDate is before bDate, otherwise <b>false</b> 
     * @throws IllegalArgumentException
     */
    public static boolean isDatePassed( Date aDate, Date bDate, boolean inclusive )
        throws IllegalArgumentException
    {
        // check parameter
        Assert.notNull( aDate, "need a date to check" );
        Assert.notNull( bDate, "need b date to check againts" );
        
        // status of the specified date having passed
        boolean passed = false;
        // check if the date has passed
        if( inclusive )
            passed = (aDate.getTime() <= bDate.getTime());
        else
            passed = (aDate.getTime() < bDate.getTime());
        
        // now return the status of the current date is being after the period
        return( passed );
    }

    /**
     * Check to see if there are any gaps between the two specified date. A gap
     * will only be reported (i.e. not continues) if bDate is not right after
     * the aDate. Checking is only factored into the comparison if includeTime is true
     * and then the comparison is only at the minute level.  
     *
     * @param aDate
     * @param bDate
     * @param includeTime
     * @return
     * @throws IllegalArgumentException
     */
    public static boolean isContinuesDates( Date aDate, Date bDate, boolean includeTime )
        throws IllegalArgumentException
    {
        // check parameter
        Assert.notNull( aDate, "need date a to work with" );
        Assert.notNull( bDate, "need date b to work with" );

        // clear the time time component of the dates if time is not to be included
        if( !includeTime )
        {
            aDate = getDateWithNoTime( aDate );
            bDate = getDateWithNoTime( bDate );
        }
        
        // the two dates are continues only if the number of day between them
        // is either one or less days - the function used for calculating days
        // assumes both dates are inclusive so this is why the checking is done
        // at two days apart because number of days from 1/2/04 to 2/2/04 is two
        // days when dates are inclusive but this is a continues date
        final int diff =
            (int) Math.abs(Math.round(
                (double) (aDate.getTime() - bDate.getTime()) 
                / 
                (includeTime ? DateUtils.MILLIS_PER_MINUTE : DateUtils.MILLIS_PER_DAY)
                )) + 1;
        
        // return the status of the dates being continues
        return( diff <= 2 );
    }

    /**
     * Get the number of days between two dates. The returned number of days is an absolute difference
     * and it assumes both dates are inclusive in the calculation.
     *
     * @param aDate
     * @param bDate
     * @return
     */
    public static int getDays( Date aDate, Date bDate )
        throws IllegalArgumentException
    {
        // check parameter
        Assert.notNull( aDate, "need date a to work with" );
        Assert.notNull( bDate, "need date b to work with" );
        
        // remove the times from the date to avoid incorrect results
        aDate = getDateWithNoTime( aDate );
        bDate = getDateWithNoTime( bDate );
        
        // do the calculation and return the results - note, the dates are inclusive
    	return( (int) Math.abs( Math.round(((double) (aDate.getTime() - bDate.getTime()) / DateUtils.MILLIS_PER_DAY)) ) + 1 );
    }
    
    /**
     * Compare two dates. NULL value is considered to be infinity.
     *
     * @param aDate
     * @param bDate
     * @return -1, 0 or 1 as aDate is less than (before), equal to, or greater than (after) bDate.
     * @throws IllegalArgumentException
     */
    public static int compareTo( Date aDate, Date bDate )
        throws IllegalArgumentException
    {
        int result = 0;
        
        if( aDate == null && bDate != null )
            result = 1;
        else if( aDate != null && bDate == null )
            result = -1;
        else if( aDate != null && bDate != null )
            result = aDate.compareTo( bDate );
        
        return( result );
    }
    
    /**
     * Compare two period. NULL value is considered to be infinity. This function relies on Period.compareTo(Period)
     * function but ensure that we operate in a NULL safe environment.
     *
     * @param aPeriod
     * @param bPeriod
     * @return -1, 0 or 1 as aPriod is less than (before), equal to, or greater than (after) bPeriod.
     */
    public static int compareTo( Period aPeriod, Period bPeriod )
    {
        int result = 0;
        
        // ensure we have the first period specified as comparison is relative to that object
        if( aPeriod == null )
            aPeriod = new Period();
            
        // compare the two period relative to the aPeriod
        result = aPeriod.compareTo(bPeriod);
        
        return( result );
    }
    
    /**
     * Check if the two periods are overlapping.
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean isPeriodOverlapping(Period a, Period b)
    {
        return( (a == null || b == null) ? true : a.isOverlapping(b) );
    }
    
    /**
     * Verify if any of the periods between the specified collections overlap. The validation is done both ways so all 
     * elements are compared against each other.
     *
     * @param periodColl1
     * @param periodColl2
     * @return
     */
    public static boolean isAnyPeriodOverlapping(Collection<Period> periodColl1, Collection<Period> periodColl2)
    {
        // if either of the collection is empty (hence infinite) the periods overlap
        boolean overlap = CollectionUtils.isEmpty(periodColl1) || CollectionUtils.isEmpty(periodColl2);
        if(!overlap )
        {   // at this stage nothing is overlapping and we have something to work with 
            for( Period period1 : periodColl1 )
            {   // get the next period in the first collection and compare it against all the periods in the other 
                for( Period period2 : periodColl2 )
                {   // see the periods overlap and if they do do not bother searching further
                    if( (overlap = isPeriodOverlapping(period1, period2)) )
                        break;
                }
                // there is no point going further if an overlap situations has been encountered
                if( overlap )
                    break;
            }
        }
        
        return( overlap );
    }
    
    /**
     * Verify that the collection of periods between keys do not overlap. The validation is done two way so all elements
     * are compared against each other. If there is only one set of periods (i.e. one key) they will be considered as
     * NOT being overlapping.
     * 
     *
     * @param periodMap
     * @return
     */
    public static boolean isAnyPeriodOverlapping(Map<Object, Collection<Period>> periodMap)
    {
        boolean overlap = false;
        
        if(CollectionUtils.size(periodMap) > 1)
        {   // there is something to work with, go through lists of periods and validate them
            final Set<Object> keys = periodMap.keySet();
            for(Object key1 : keys)
            {   // get the list periods stored against the next key
                final Collection<Period> firstPeriods = periodMap.get(key1);
                // go through all the other periods to check for overlapping
                for(Object key2 : keys)
                {   // compare the first list of periods against the second one but make sure we are not going to
                    // compare itself in the second list
                    if((key1 == null && key2 != null) || (key1 != null && key2 == null) || 
                            (key1 != null && key2 != null && !key1.equals(key2)))
                        overlap = isAnyPeriodOverlapping(firstPeriods, periodMap.get(key2));
                    // there is no point going further if an overlap situations has been encountered
                    if( overlap )
                        break;
                }
                // there is no point going further if an overlap situations has been encountered
                if( overlap )
                    break;                
            }
        }
        
        return( overlap );
    }
    
    /**
     * Copy the value of source date into destination date.
     *
     * @param destDate
     * @param srcDate
     * @return
     */
    public static Date copy( Date destDate, Date srcDate )
    {
        // check if a parameter has been set
        if( srcDate != null )
        {   // date was specified store parameter
            if( destDate == null )
                destDate = new Date( srcDate.getTime() );
            else
                destDate.setTime( srcDate.getTime() );
        }
        else
        { 
            destDate = null;
        }
        
        return( destDate );
    }
    
    /**
     * Get the specified date as string formatted as per the java.text.DateFormat.
     *
     * @param date
     * @param format
     * @return
     */
    public static String toString(Date date, int format)
    {
        return(date == null ? null : DateFormat.getDateInstance(format).format(date));
    }
}
