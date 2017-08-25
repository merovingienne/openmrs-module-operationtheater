package org.openmrs.module.operationtheater;

import java.time.format.DateTimeFormatter;

public class OTMetadata {

	public static final String DEFAULT_AVAILABLE_TIME_BEGIN_UUID = "4e051aeb-a19d-49e0-820f-51ae591ec41f";

	public static final String DEFAULT_AVAILABLE_TIME_END_UUID = "a9d9ec55-e992-4d04-aebe-808be50aa87a";

	public static final String CALENDAR_COLOR_UUID = "aefe79d0-aa24-4216-ad00-41ba073f7a39";

	public static final DateTimeFormatter AVAILABLE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMMM YYYY hh:mm a");

	public static final String LOCATION_TAG_OPERATION_THEATER_UUID = "af3e9ed5-2de2-4a10-9956-9cb2ad5f84f2";

	public static final String APPT_TYPE_UUID = "93263567-286d-4567-8596-0611d9800206";

	public static final String PLACEHOLDER_PATIENT_UUID = "76f68fa5-8df4-4c51-a5cf-70039dbf5de4";

	public static final String PLACEHOLDER_PROCEDURE_UUID = "e1f3d8c3-f533-499d-afa0-357f06509cca";

	public static final String OPENMRS_ID_NAME = "OpenMRS ID";

	//Global Properties

	public static final String GP_CONTINUOUS_PLANNING_WINDOW = "operationtheater.continuousPlanningWindow";
}
