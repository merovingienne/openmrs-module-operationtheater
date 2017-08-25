package org.openmrs.module.operationtheater;

import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * Class used to obtain DateTime object of the current time<br />
 * Test friendly design - can be easily mocked
 */
public class Time {

//	public DateTime now() {
//		return new DateTime();
//	}

	public ZonedDateTime now() {
		return ZonedDateTime.now();
	}
}
