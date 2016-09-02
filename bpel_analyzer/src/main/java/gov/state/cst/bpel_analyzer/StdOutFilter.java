package gov.state.cst.bpel_analyzer;

import java.util.Arrays;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * @author BaydushLC
 *
 */
@SuppressWarnings("rawtypes")
public class StdOutFilter extends AbstractMatcherFilter {
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.qos.logback.core.filter.Filter#decide(java.lang.Object)
	 */
	@Override
	public FilterReply decide(final Object event) {
		FilterReply result = FilterReply.NEUTRAL;
		if (isStarted()) {

			final LoggingEvent loggingEvent = (LoggingEvent)event;
	
			final List<Level> eventsToKeep = Arrays.asList(Level.TRACE, Level.DEBUG,
					Level.INFO);
			if (!eventsToKeep.contains(loggingEvent.getLevel())) {
				result = FilterReply.DENY;
			}
		}
		return result;
	}
}
