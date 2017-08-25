package org.openmrs.module.operationtheater.scheduler.solver;

import java.time.ZonedDateTime;
import java.util.Comparator;

/**
 * Created by lukas on 28.07.14.
 */
public class StartDateComparator implements Comparator<ZonedDateTime> {

	@Override
	public int compare(ZonedDateTime dateTime, ZonedDateTime dateTime2) {
		return dateTime.equals(dateTime2) ? 0 : dateTime.isBefore(dateTime2) ? -1 : 1;
	}
}
