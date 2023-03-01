package au.com.blueoak.portal.utility;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Filter used so transaction status can be displayed in the logs. <br>
 * <br>
 * (c)2013 Blue Oak Solutions Pty Ltd. All rights reserved.
 * 
 * @see http://java.dzone.com/articles/monitoring-declarative-transac?page=0,0
 */
public class TransactionIndicatingFilter extends Filter {

	@Override
	public int decide(LoggingEvent loggingEvent) {

		loggingEvent.setProperty("xaName", TransactionIndicatingUtil.getTransactionStatus(true));
		loggingEvent.setProperty("xaStatus", TransactionIndicatingUtil.getTransactionStatus(false));
		return Filter.NEUTRAL;
	}
}