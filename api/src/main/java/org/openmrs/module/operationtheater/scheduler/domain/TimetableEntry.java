package org.openmrs.module.operationtheater.scheduler.domain;

//import org.joda.time.DateTime;
import org.openmrs.Location;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;

import java.time.ZonedDateTime;

/**
 * Common interface for a timetable entry<br />
 * Will be implemented by {@link Anchor} and {@link PlannedSurgery}
 */
public interface TimetableEntry {

	Location getLocation();

	ZonedDateTime getStart();

	ZonedDateTime getEnd();

	/**
	 * @return length of the chain starting at this element in minutes
	 */
	int getChainLengthInMinutes();

	@InverseRelationShadowVariable(sourceVariableName = "previousTimetableEntry")
	TimetableEntry getNextTimetableEntry();

	void setNextTimetableEntry(TimetableEntry entry);
}
