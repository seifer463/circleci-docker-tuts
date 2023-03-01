package au.com.blueoak.portal.utility;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Period object that hold details about when the period started and may have finished.
 * Note time component is used, period is always assumed to be defined over days and every
 * date is assumed to be inclusive.<br>
 * <br>
 * For any date comparison if a date is not defined it assumed to mean infinity.
 * <br><br>
 * <b>(c)2008 Blue Oak Solutions Pty Ltd. All rights reserved.<br>
 */
public class Period
    implements Comparable<Period>
{
    /** date formatter used to format a date into text */
    private static final String DATE_FORMAT_BILLING_PERIOD = "MMM-yyyy";
    private static final String DATE_FORMAT_DATE = "dd-MM-yyy";
    private static final String DATE_FORMAT_TIME = "HH:mm";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( DATE_FORMAT_BILLING_PERIOD );
    /** number of days assumed in a month */
    private static final BigDecimal DAYS_IN_MONTH = new BigDecimal( 31 );
    
    /** indicates if time is to be kept with the date element */
    private boolean keepTime = false;
    /** date and time on which the period starts */
    private Date startDate;
    /** date and time on which the period ends */
    private Date endDate;

    /**
     * Construct an empty object. 
     */
    public Period()
    {}
    
    /**
     * 
     *
     * @param keepTime
     */
    public Period( boolean keepTime )
    {
        setKeepTime( keepTime );
    }

    /**
     * Create a period using the specified details. By default any time specified is automatically removed.
     * 
     * @param startDate
     * @param endDate
     * @see #Period(boolean, Date, Date) if you want to keep the time component as well
     */
    public Period( Date startDate, Date endDate )
    {
        setStartDate( startDate );
        setEndDate( endDate );
    }

    /**
     * 
     *
     * @param keepTime
     * @param startDate
     * @param endDate
     */
    public Period( boolean keepTime, Date startDate, Date endDate )
    {
        setKeepTime( keepTime );
        setStartDate( startDate );
        setEndDate( endDate );
    }
    
    /**
     * 
     *
     * @param period
     */
    public Period( Period period )
    {
        setPeriod( period );
    }

    /**
     *
     * @return the keepTime
     */
    public boolean isKeepTime()
    {
        return keepTime;
    }

    /**
     *
     * @param keepTime the keepTime to set
     */
    private void setKeepTime( boolean keepTime )
    {
        this.keepTime = keepTime;
    }

    /**
     * @hibernate.property
     *      column = "start_date"
     *      type = "date"
     * 		not-null = "true"
     * 
     * @return start date of the period; this is a mutable object
     */
    public Date getStartDate()
    {
        // ensure the dates are the right way around
        fixDates();
        // return a copy of the start date
        return( startDate );
    }
    
    /**
     * A copy of the specified date is stored internally.
     *
     * @param startDate The startDate to set.
     */
    public void setStartDate( Date startDate )
    {
        // store the date specified if valid (remove the time component)
        if( startDate != null )
        {   // date has been specified, remove the time component before storing it
        	this.startDate = (keepTime ? new Date(startDate.getTime()) : DateUtils.getDateWithNoTime(startDate)); 
        }
        else
        {   // just clear the currently set date
            this.startDate = null;
        }
    }
    
    /**
     * See if the start date is set to infinite (i.e. it equals null).
     *
     * @return
     */
    public boolean isStartInfinate()
    {
        return( this.startDate == null );
    }
    
    /**
     * @hibernate.property
     *      column = "end_date"
     *      type = "date"
     * 		not-null = "true"
     * 
     * @return end date of the period; this is a mutable object
     */
    public Date getEndDate()
    {
        // ensure the dates are the right way around
        fixDates();
        // return a copy of the end date
        return( endDate );
    }
    
    /**
     * A copy of the specified date is stored internally. 
     *
     * @param endDate The endDate to set.
     */
    public void setEndDate( Date endDate )
    {
        // store the date specified if valid (remove the time component)
        if( endDate != null )
        {   // date has been specified, remove the time component before storing it
            this.endDate = (keepTime ? new Date(endDate.getTime()) : DateUtils.getDateWithNoTime(endDate));
        }
        else
        {   // just clear the currently set date
            this.endDate = null;
        }
    }
    
    /**
     * See if the end date is set to infinite (i.e. it equals null).
     *
     * @return
     */
    public boolean isEndInfinate()
    {
        return( this.endDate == null );
    }
    
    /**
     * See if the period defined as being infinite (i.e. both the start and end of the period is not set).
     *
     * @return true is only returned if both start and end date elements are not set
     */
    public boolean isInfinite()
    {
        return( this.startDate == null && this.endDate == null );
    }

    /**
     * Overwrite the start and end dates of this period with the details in the specified period.
     *
     * @param period
     */
    public void setPeriod( Period period )
    {
        setStartDate( period == null ? null : period.getStartDate() );
        setEndDate( period == null ? null : period.getEndDate() );
    }
    
    /**
     * Extends this period with the period specified. This means if the specified date is earlier then the start date 
     * the start date will be changed. Also, if the end date is before the specified date it will be adjusted to the
     * specified date.
     *
     * @param period
     * @param excludeInfinity indicates if infinity should be excluded during the evaluation, e.g. infinity
     *      would overwrite what ever date specified if infinity is not excluded
     */
    public void extendPeriod( Period period, boolean excludeInfinity )
    {
        extendPeriod( period, excludeInfinity, true, true );
    }
    
    /**
     * Adjust the period either the start or end date or both based on the specified date.
     *
     * @param period
     * @param excludeInfinity
     * @param adjStart
     * @param adjEnd
     */
    public void extendPeriod( Period period, boolean excludeInfinity, boolean adjStart, boolean adjEnd )
    {
        if( excludeInfinity )
        {   // exclude infinity, adjust the START date
            if( adjStart && this.startDate == null && period != null && period.startDate != null )
                this.setStartDate( period.startDate );
            // adjust the END date
            if( adjEnd && this.endDate == null && period != null && period.endDate != null )
                this.setEndDate( period.endDate );
        }
        else
        {   // do not exclude infinity, adjust the START date
            if( adjStart && (period == null || period.startDate == null) )
                this.startDate = null;
            // adjust the END date
            if( adjEnd && (period == null || period.endDate == null) )
                this.endDate = null;
        }
        
        // adjust the start of the period as allowed and required
        if( adjStart && this.startDate != null && period != null && period.startDate != null )
            this.startDate.setTime( Math.min(this.startDate.getTime(), period.startDate.getTime()) );
        
        // adjust the end of the period as allowed and required
        if( adjEnd && this.endDate != null && period != null && period.endDate != null )
            this.endDate.setTime( Math.max(this.endDate.getTime(), period.endDate.getTime()) );
    }
    
    /**
     * Adjust the period either the start or end date or both based on the specified date.
     *
     * @param aDate
     * @param excludeInfinity
     * @param adjStart
     * @param adjEnd
     */
    public void extendPeriod( Date aDate, boolean excludeInfinity, boolean adjStart, boolean adjEnd )
    {
        if( adjStart )
        {
            if( aDate == null && !excludeInfinity )
                this.setStartDate( null );
            else if( aDate != null && this.startDate == null && excludeInfinity )
                this.setStartDate( aDate );
            
            if( aDate != null && this.startDate != null )
                this.startDate.setTime( Math.min(this.startDate.getTime(), aDate.getTime()) );
        }

        if( adjEnd )
        {
            if( aDate == null && !excludeInfinity )
                this.setEndDate( null );
            else if( aDate != null && this.endDate == null && excludeInfinity )
                this.setEndDate( aDate );
            
            if( aDate != null && this.endDate != null )
                this.endDate.setTime( Math.max(this.endDate.getTime(), aDate.getTime()) );
        }
    }
    
    /**
     * Reduce this period with the period specified. This means which ever of the start date is
     * the later the start date will be adjusted to that. Also, which ever end date is earlier it will
     * be adjusted to that.  
     *
     * @param period
     */
    public void contractPeriod( Period period )
    {
        contractPeriod( period, true, true );
    }
    
    /**
     * 
     *
     * @param period
     * @param excludeInfinity
     * @param adjStart
     * @param adjEnd
     */
    public void contractPeriod( Period period, boolean adjStart, boolean adjEnd )
    {
        // check if there is something to work with
        if( period != null )
        {   // there is something there, make adjustment to dates
            
            // adjust the start of the period as allowed and required
            if( adjStart )
                if( this.startDate == null && period.startDate != null )
                    this.setStartDate( period.startDate );
                else if( this.startDate != null && period.startDate != null )
                    this.startDate.setTime( Math.max(this.startDate.getTime(), period.startDate.getTime()) );
            
            // adjust the end of the period as allowed and required
            if( adjEnd )
                if( this.endDate == null && period.endDate != null )
                    this.setEndDate( period.endDate );
                else if( this.endDate != null && period.endDate != null )
                    this.endDate.setTime( Math.min(this.endDate.getTime(), period.endDate.getTime()) );
            
            // adjust the dates of the period just in case they crossed over
            fixDates();
        }
    }
    
    /**
     * Adjust the period either the start or end date or both based on the specified date.
     *
     * @param aDate
     * @param adjStart
     * @param adjEnd
     */
    public void contractPeriod( Date aDate, boolean adjStart, boolean adjEnd )
    {
        if( aDate != null )
        {   // a date has been specified, adjust the dates
            if( adjStart )
                if( this.startDate == null )
                    this.setStartDate( aDate );
                else
                    this.startDate.setTime( Math.max(this.startDate.getTime(), aDate.getTime()) );
            if( adjEnd )
                if( this.endDate == null )
                    this.setEndDate( aDate );
                else
                    this.endDate.setTime( Math.min(this.endDate.getTime(), aDate.getTime()) );

            // adjust the dates of the period just in case they crossed over
            fixDates();
        }
    }
    
    /**
     * Get the number of milliseconds between the reported period. If no time keeping is enabled the system will
     * calculate the difference between the dates as dates being inclusive. 
     *
     * @return the number of milliseconds, -1 indicates that it's infinite number of time (either start or end is infinite)
     */
    public long getTime()
    {
        return( isStartInfinate() || isEndInfinate() ? -1 : 
            Math.abs(getStartDate().getTime() - getEndDate().getTime()) + (isKeepTime() ? 0 : DateUtils.MILLIS_PER_DAY) );  
    }
    
    /**
     * Get the total number of days in the specified period.
     *
     * @return the number of days, -1 indicates that it's infinite number of days
     */
    public int getDays()
    {
        return( isStartInfinate() || isEndInfinate() ? -1 : DateUtils.getDays( getEndDate(), getStartDate() ) );
    }
    
    /**
     * Get the total number of months in the specified period.
     * 
     * @return returns the number of months, -1 indicates that it's infinite number of months
     */
    public int getMonths()
    {
        return( isStartInfinate() || isEndInfinate() ? 
            -1 : Math.max(new BigDecimal(getDays()).divide(DAYS_IN_MONTH, RoundingMode.HALF_UP).intValue(), 1) );
    }    
    
    /**
     * Ensures that the start date is on or before the end date if both are defined.
     */
    private void fixDates()
    {
        // check that both dates are set, if they are, check if they are in the wrong order
        if( startDate != null && endDate != null && DateUtils.isDatePassed(this.endDate, this.startDate, false) )
        {   // both dates are set but the end date is before the start date (i.e. wrong way around) switch them
            final Date tmpDate = startDate;
            this.startDate = this.endDate;
            this.endDate = tmpDate;            
        }
    }
    
    /**
     * Check if the specified date is within this period. No time component of the specified date is used in the 
     * comparison to ensure accurate inclusive date check is performed. If the specified date is NULL (infinite) the
     * result will be TRUE.
     *
     * @param aDate
     * @return
     */
    public boolean isDateWithin( Date aDate )
    {
        boolean within = false;
        
        // check if there is something to work with
        if( aDate == null )
        {   // date specified is infinite is it within the period
            within = true;
        }
        else
        {   // there is something there, remove the time component from the date
            final Date adjDate = (keepTime ? aDate : DateUtils.getDateWithNoTime(aDate)); 
            // ensure the dates are the right way around
            fixDates();
            // check if the specified date is on or after this period starts
            if( (within = (startDate == null || DateUtils.isDatePassed(startDate, adjDate, true))) )
            {   // the date is on or after the start of this period, now if the end date is set, check
                // that the date is also on or before the end for this period
                within = (endDate == null || DateUtils.isDatePassed(adjDate, endDate, true));
            }
        }

        return( within );
    }
    
    /**
     * Check if the specified period within this period. The specified period can only be within this period if it 
     * starts on or after this period and it ends on or before this period. However, if the specified period is NULL
     * (infinite) the result will be TRUE. 
     *
     * @param period
     * @return
     */
    public boolean isPeriodWithin( Period period )
    {
        // indicates if the specific period is within this one
        boolean within = false;
        
        // check if there is something to work with
        if( period == null )
        {   // period specified is infinite so it is within the period
            within = true;
        }
        else
        {   // all the core details are available to work with, ensure the dates are the right way around
            fixDates();
            period.fixDates();

            //check if the start date of the specified period is on or after the start date of this period
            if( (startDate == null && period.startDate == null) ||
                (startDate == null && period.startDate != null) ||
                (startDate != null && period.startDate != null && DateUtils.isDatePassed(startDate, period.startDate, true)) )
            {   // the specified period starts on or after this period, now check this period's end date has been set
                within = (endDate == null && period.endDate == null) ||
                    (endDate == null && period.endDate != null) ||
                    (endDate != null && period.endDate != null && DateUtils.isDatePassed(period.endDate, endDate, true));
            }
        }
        
        return( within );
    }
    
    /**
     * Check if the specified period is a continuation of this period. The dates are considered continues if the two
     * periods either overlap (see {@link #isOverlapping(Period)}) or one period ends and the next period starts on
     * the following day. Time component is taken into consideration during the comparison. If the specified period is
     * null (infinite) the result will be true.
     *          
     * @param period
     * @return
     */
    public boolean isContinues( Period period )
    {
        // indicates if the this and the specified period are continues (if the no period was specified it is assumed 
        // that the specified period is representing infinity and hence infinity is a continuation of any period)
        boolean continues = true;
        
        // check if there is something to work with
        if( period != null )
        {   // there is something to work with, ensure the dates are the right way around 
            fixDates();
            period.fixDates();
            
            // check if any of the start or end dates have been specified
            if( (startDate == null && period.startDate == null) || (endDate == null && period.endDate == null) )
            {   // either no start or end dates has been specified so by default they are continues, as there is no
                // start or end in sight
                continues = true;
            }
            else if( (this.startDate == null && period.startDate != null) || 
                    (this.startDate != null && period.startDate != null && DateUtils.isDatePassed(this.startDate, period.startDate, true)) )
            {   // either this period does not have a start but the specified period does or both period have a start   
                // and this period starts on or before the specified period, check if this period has end date
                if( this.endDate == null )
                {   // this period does not have an end date and it starts before the specified period so
                    // the two periods are continues (i.e. there is no gap between them)
                    continues = true;
                }
                else
                {   // there is an end date specified, now the period can still be continues if the end date
                    // on or after the specified period starts
                    if( !(continues = DateUtils.isDatePassed( period.startDate, this.endDate, true)) )
                    {   // it looks like the end date is before the start date, see if the dates are continues
                        // as the end date could be just a day before the start date which means that the periods
                        // are still continues as dates are considered inclusive
                        continues = DateUtils.isContinuesDates( this.endDate, period.startDate, keepTime );
                    }
                }
            }
            else
            {   // this period starts after the specified period, see if the specified period has end date
                if( period.endDate == null )
                {   // specified period does not have an end date and it starts before this period so the
                    // two periods are continues (i.e. there is no gap between them)
                    continues = true;
                }
                else
                {   // there is an end date specified in the specified period, the periods can still be continues
                    // if the specified period ends on or after this period starts
                    if( !(continues = DateUtils.isDatePassed( this.startDate, period.endDate, true)) )
                    {   // it looks like the end date is before the start date, see if the dates are continues
                        // as the end date could be just a day before the start date which means that the periods
                        // are still continues as dates are considered inclusive
                        continues = DateUtils.isContinuesDates( period.endDate, this.startDate, keepTime );
                    }
                }
            }
        }
        
        // return the result of the specified date being continue of this one or either versa
        return( continues );
    }
    
    /**
     * Check if the two periods are overlapping. The two periods are overlapping if any part of the two periods are on
     * or within each other. Note, the checking on the dates are inclusive and time component. If the specified period 
     * is null (infinite) the result will be true.
     *
     * @param period
     * @return
     */
    public boolean isOverlapping( Period period )
    {
        // indicates if the this and the specified period are overlapping (if the no period was specified it is assumed 
        // that the specified period is representing infinity and hence infinity overlaps any period)
        boolean overlapping = true;
        
        // check if there is something to work with
        if( period != null )
        {   // there is something to work with, ensure the dates are the right way around
            fixDates();
            period.fixDates();
            
            // check if any of the end dates have been specified
            if( (this.startDate == null && period.startDate == null) || (this.endDate == null && period.endDate == null) )
            {   // either no start or end dates has been specified so by default they are overlapping as both point to infinity
                overlapping = true;
            }
            else if( (this.startDate == null && period.startDate != null) || 
                    (this.startDate != null && period.startDate != null && DateUtils.isDatePassed(this.startDate, period.startDate, true)) )
            {   // either this period does not has a start but the specified period does or both period have a start   
                // and this period starts on or before the specified period, check if this period has end date
                if( this.endDate == null )
                {   // this period does not have an end date and it starts before the specified period so
                    // the two periods overlap (i.e. there is no gap between them)
                    overlapping = true;
                }
                else
                {   // there is an end date specified, now the period can still overlap if the end date
                    // on or after the specified period starts
                    overlapping = DateUtils.isDatePassed( period.startDate, this.endDate, true );
                }
            }
            else
            {   // this period starts after the specified period, see if the specified period has end date
                if( period.endDate == null )
                {   // specified period does not have an end date and it starts before this period so the
                    // two periods overlap (i.e. there is no gap between them)
                    overlapping = true;
                }
                else
                {   // there is an end date specified in the specified period, the periods can still overlap
                    // if the specified period ends on or after this period starts
                    overlapping = DateUtils.isDatePassed( this.startDate, period.endDate, true );
                }
            }
        }
        
        // return the result of the specified specified period overlap this period
        return( overlapping );
    }

    /**
     * Get the period over which this period and the one specified overlap. The two periods are overlapping if any part 
     * of the two periods are on or within each other. Note, the checking on the dates are inclusive. 
     *
     * @param period
     * @return
     */
    public Period getOverlappingPeriod( Period period )
    {
        Period overlappingPeriod = null;
        
        // check if this period and the specified one overlap
        if( this.isOverlapping(period) )
        {   // the two period are overlapping, create a new period that covers the overlapping period
            overlappingPeriod = new Period( this );
            overlappingPeriod.contractPeriod( period, true, true );
        }
        
        return( overlappingPeriod );
    }
    
    /**
     * Calculates the gap period between 'this' and the input 'period'. <br>
     * If the periods are continuous this method will return null as there is no gap between them.
     *
     * @param period The gap between 'this' and 'period'
     */
    public Period getGapPeriod(Period period)
    {
        Period gapPeriod = null;
        // check if we have a gap between the periods (the periods are not continues)
        if(!this.isContinues(period))
        {
            // if 'this' period ends before the 'period' starts
            if(this.compareTo(period) < 0)
            {
                gapPeriod = new Period(this.getEndDate(), period.getStartDate());
            }
            else if(this.compareTo(period) > 0)
            {
                // if 'period' ends before 'this' period starts
                gapPeriod = new Period(period.getEndDate(), this.getStartDate());
            }
        }
        return gapPeriod;
    }
    
    /**
     * Get the number of days this period is overlapping the specified period. The two periods are overlapping if any 
     * part of the two periods are on or within each other. Note, the checking on the dates are inclusive. 
     *
     * @param period
     * @return
     * @see #isOverlapping(Period)
     */
    public int getOverlappingDays( Period period )
    {
        final Period overlappingPeriod = getOverlappingPeriod( period );
        return( overlappingPeriod != null ? overlappingPeriod.getDays() : 0 );
    }
    
    /**
     * 
     * 
     * @param object
     */
    public boolean equals( Object object )
    {
        boolean equals = false;
        
        if( !(equals = (this == object)) )
        {   // there is something to work with, check if the right instance
            if( object instanceof Period )
            {   // object is the right type, make it easier to work with
                final Period that = (Period) object;
                // see if the period start and end dates are the same
                equals = isSame( that );
            }
            else
            {   // see if the specified object is null
                equals = (object == null ? this.isInfinite() : false);
                
            }
        }
        
        return( equals );
    }
    
    /**
     * 
     *
     * @return
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        // calculate the hash code details
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.startDate == null) ? 0 : this.startDate.hashCode());
        result = prime * result + ((this.endDate == null) ? 0 : this.endDate.hashCode());
        // return the calculated hash code
        return result;
    }
    
    /**
     * 
     *
     * @param period
     * @return
     */
    public boolean isSame( Period period )
    {
        boolean same = false;
        
        // check that there is something to work with
        if( period != null )
        {   // there is something there, check its content
            if( DateUtils.isSame(startDate, period.startDate) )
                same = DateUtils.isSame( endDate, period.endDate );
        }
        else
        {   // specified period is null, see if this period is infinite (null and infinate is the same)
            same = this.isInfinite();
        }
        
        return( same );
    }

    /**
     * Compare two dates to determine their order. The logic here is different to DateUtils. Here infinity can be 
     * specified if it is before an actual date or after an actual date.
     *
     * @param a
     * @param b
     * @param infiniteBefore
     * @return
     */
    private int compareDates( Date aDate, Date bDate, boolean infiniteBefore )
    {
        int result = 0;
        
        if( aDate == null && bDate != null )
            result = (infiniteBefore ? -1 : 1);
        else if( aDate != null && bDate == null )
            result = (infiniteBefore ? 1 : -1);
        else if( aDate != null && bDate != null )
            result = aDate.compareTo( bDate );
        
        return( result );
    }
    
    /**
     * Compare this period to the specified one to determine their order chronologically (ascending order).
     *
     * @param period
     * @return
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo( Period period )
    {
        // result of the comparison
        int result = 0;
        
        // check if the specified period represents infinity
        if( period == null || period.isInfinite() )
        {   // specified period is infinity, if this period is infinity then both are the same, otherwise no matter
            // what this period is it does not infinity, so the specified period is before this period (another 
            // words this period is after the specified one)
            result = (this.isInfinite() ? 0 : -1);
        }
        else if( this.isInfinite() )
        {   // this period is infinity, if the specified period is infinity then both are the same, otherwise no matter
            // what the specified period is it does not bean infinity, so this period is before the specified period
            result = (period == null || period.isInfinite() ? 0 : 1);
        }
        else
        {   // both periods have something specified, ensure the dates are the right way around
            fixDates();
            period.fixDates();
            // check where each of the period start
            if( (result = compareDates( startDate, period.startDate, true)) == 0 )
            {   // both start dates are the same, now compare the end dates
                result = compareDates( endDate, period.endDate, false );
            }
        }

        // return the result of the comparison
        return( result );
    }
    
    /**
     * Get the descriptive string version of the period.
     *
     * @return
     */
    public String toStringAsBillingPeriod()
    {
        // descriptive text version of the period
        String text = null;
        
        // ensure that dates are the right way around
        fixDates();
        // see which part of the period has been specified
        if( startDate == null && endDate == null )
        {   // both start and end has not be set, this means that the period is infinity
            text = "infinity";
        }
        else if( startDate == null && endDate != null )
        {   // start period has not been set but the end has been set
            synchronized( DATE_FORMAT )
            {   // need to synchronise of the format object as it's not thread safe
                DATE_FORMAT.applyPattern( DATE_FORMAT_BILLING_PERIOD );
                text = (new StringBuilder( "up to and " )).append( DATE_FORMAT.format(endDate) ).toString();
            }
        }
        else if( startDate != null && endDate == null )
        {   // end period has not been set but the start has been set
            synchronized( DATE_FORMAT )
            {   // need to synchronise of the format object as it's not thread safe
                DATE_FORMAT.applyPattern( DATE_FORMAT_BILLING_PERIOD );
                text = (new StringBuilder( DATE_FORMAT.format(startDate) )).append( " and bejond" ).toString();
            }
        }
        else
        {   // both start and end dates of the period has been set, now see the duration the period
            GregorianCalendar workCalendar = new GregorianCalendar();
            
            // get the month details of the start date
            workCalendar.setTime( startDate );
            int startMonth = workCalendar.get( Calendar.MONTH );
            // get the month details from the end date
            workCalendar.setTime( endDate );
            int endMonth = workCalendar.get( Calendar.MONTH );
            
            // need to synchronise of the format object as it's not thread safe
            synchronized( DATE_FORMAT )
            {   // check if the start and end months are the same - period is for the month
                DATE_FORMAT.applyPattern( DATE_FORMAT_BILLING_PERIOD );
                if( startMonth == endMonth )
                    text = DATE_FORMAT.format( startDate );
                else
                    text = (new StringBuilder( DATE_FORMAT.format( startDate ) )).
                        append( " to " ).append( DATE_FORMAT.format( endDate ) ).toString(); 
            }
        }
        
        // return the descriptive text that was constructed
        return( text );
    }
    
    /**
     * 
     *
     * @param datePattern
     * @return
     */
    public String toStringAsSpecified( String datePattern )
    {
        // descriptive text version of the period
        String text = null;

        // construct a default date pattern is none has been specified
        if( !StringUtils.hasText(datePattern) )
            datePattern = new StringBuilder(DATE_FORMAT_DATE).append(" ").append(DATE_FORMAT_TIME).toString(); 
        
        // ensure that dates are the right way around
        fixDates();
        // see which part of the period has been specified
        if( startDate == null && endDate == null )
        {   // both start and end has not be set, this means that the period is infinity
            text = "infinity";
        }
        else if( startDate == null && endDate != null )
        {   // start period has not been set but the end has been set
            synchronized( DATE_FORMAT )
            {   // need to synchronise of the format object as it's not thread safe
                DATE_FORMAT.applyPattern( datePattern );
                text = (new StringBuilder( "up to and " )).append( DATE_FORMAT.format(endDate) ).toString();
            }
        }
        else if( startDate != null && endDate == null )
        {   // end period has not been set but the start has been set
            synchronized( DATE_FORMAT )
            {   // need to synchronise of the format object as it's not thread safe
                DATE_FORMAT.applyPattern( datePattern );
                text = (new StringBuilder( DATE_FORMAT.format(startDate) )).append( " and bejond" ).toString();
            }
        }
        else
        {   // both start and end dates of the period has been set
            synchronized( DATE_FORMAT )
            {   // check if the start and end months are the same - period is for the month
                DATE_FORMAT.applyPattern( datePattern );
                text = (new StringBuilder( DATE_FORMAT.format( startDate ) )).
                    append( " to " ).append( DATE_FORMAT.format( endDate ) ).toString(); 
            }
        }
        
        // return the descriptive text that was constructed
        return( text );
    }
    
    /**
     * 
     * 
     */
    public String toString()
    {
        return( (new StringBuffer("[Period: startDate=").append(startDate).
                append(", endDate=").append(endDate).append("]")).toString() );
    }
}
