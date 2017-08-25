package org.openmrs.module.operationtheater.fragment.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.LocationTag;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.module.appointmentscheduling.AppointmentBlock;
import org.openmrs.module.appointmentscheduling.AppointmentType;
import org.openmrs.module.appointmentscheduling.api.AppointmentService;
import org.openmrs.module.appui.TestUiUtils;
import org.openmrs.module.operationtheater.MockUtil;
import org.openmrs.module.operationtheater.OTMetadata;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.SchedulingData;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.module.operationtheater.scheduler.Scheduler;
import org.openmrs.module.operationtheater.validator.SchedulingDataValidator;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Tests {@link SchedulingFragmentController}
 * also see {@see SchedulingFragmentControllerTest2} which doesn't use Powermock and extends BaseModuleContextSensitiveTest
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Scheduler.class })
public class SchedulingFragmentControllerTest {

	private void mockValidateUtil(SchedulingFragmentController controller, final boolean validationShouldPass)
			throws Exception {
		Validator validator = MockUtil
				.mockValidator(validationShouldPass, SchedulingDataValidator.class, SchedulingData.class, "field", "code");
		org.mockito.internal.util.reflection.Whitebox.setInternalState(controller, "schedulingDataValidator", validator);
	}

	/**
	 * @verifies return scheduled surgeries and available times for all operating theaters
	 * @see SchedulingFragmentController#getEvents(org.openmrs.ui.framework.UiUtils, java.util.Date, java.util.Date, java.util.List, org.openmrs.api.LocationService, org.openmrs.module.appointmentscheduling.api.AppointmentService)
	 */
	@Test
	public void getEvents_shouldReturnScheduledSurgeriesAndAvailableTimesForAllOperatingTheaters() throws Exception {
		//FIXME refactor this test and use dbunit!! - turned out that BaseModuleContextSensitiveTest doesn't work with Powermockito

		//prepare parameters
		Date start = Date.from(LocalDateTime.of(2014, 6, 9, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
		Date end = Date.from(LocalDateTime.of(2014, 6, 9, 23, 59).atZone(ZoneId.systemDefault()).toInstant());
		List<String> resources = new ArrayList<String>();
		resources.add("ot 1");
		resources.add("ot 2");
		resources.add("ot 3");

		//prepare service layer return objects
		LocationTag tag = new LocationTag();
		List<Location> locations = new ArrayList<Location>();

		LocationAttributeType beginAttributeType = new LocationAttributeType();
		beginAttributeType.setUuid(OTMetadata.DEFAULT_AVAILABLE_TIME_BEGIN_UUID);
		LocationAttributeType endAttributeType = new LocationAttributeType();
		endAttributeType.setUuid(OTMetadata.DEFAULT_AVAILABLE_TIME_END_UUID);
		LocationAttributeType calendarColorType = new LocationAttributeType();
		calendarColorType.setUuid(OTMetadata.CALENDAR_COLOR_UUID);

		LocationAttribute defaultBeginAttr = Mockito.spy(new LocationAttribute());
		defaultBeginAttr.setValue("08:45");
		Mockito.doReturn(beginAttributeType).when(defaultBeginAttr).getDescriptor();
		LocationAttribute defaultEndAttr = Mockito.spy(new LocationAttribute());
		Mockito.doReturn(endAttributeType).when(defaultEndAttr).getDescriptor();
		defaultEndAttr.setValue("19:16");
		LocationAttribute calendarColor = Mockito.spy(new LocationAttribute());
		Mockito.doReturn(calendarColorType).when(calendarColor).getDescriptor();
		calendarColor.setValue("#00ffff");
		Set<LocationAttribute> attributes = new HashSet<LocationAttribute>();
		attributes.add(defaultBeginAttr);
		attributes.add(defaultEndAttr);
		attributes.add(calendarColor);

		Location ot1 = new Location();
		ot1.setId(1);
		ot1.setName("ot 1");
		ot1.setAttributes(attributes);
		locations.add(ot1);

		Location ot2 = new Location();
		ot2.setId(2);
		ot2.setName("ot 2");
		ot2.setAttributes(attributes);
		locations.add(ot2);

		Location ot3 = new Location();
		ot3.setId(3);
		ot3.setName("ot 3");
		ot3.setAttributes(attributes);
		locations.add(ot3);

		List<AppointmentBlock> blocks = new ArrayList<AppointmentBlock>();
		AppointmentBlock blockOt2 = new AppointmentBlock();
		blockOt2.setLocation(ot2);
		LocalDateTime blockStartDate = LocalDateTime.of(2014, 6, 9, 7, 35);
		blockOt2.setStartDate(Date.from(blockStartDate.atZone(ZoneId.systemDefault()).toInstant()));
		LocalDateTime blockEndDate = LocalDateTime.of(2014, 6, 9, 20, 55);
		blockOt2.setEndDate(Date.from(blockEndDate.atZone(ZoneId.systemDefault()).toInstant()));
		blocks.add(blockOt2);

		//ot3 is not available for this day
		AppointmentBlock blockOt3 = new AppointmentBlock();
		blockStartDate = blockStartDate.truncatedTo(ChronoUnit.DAYS);
		blockOt3.setLocation(ot3);
		blockOt3.setStartDate(Date.from(blockStartDate.atZone(ZoneId.systemDefault()).toInstant()));
		blockOt3.setEndDate(Date.from(blockStartDate.atZone(ZoneId.systemDefault()).toInstant()));
		blocks.add(blockOt3);

		List<Surgery> surgeries = new ArrayList<Surgery>();
		Surgery surgery = new Surgery();
		surgery.setUuid("uuid");
		Procedure procedure = new Procedure();
		procedure.setName("Procedure Name");
		Patient patient = Mockito.mock(Patient.class);
		when(patient.getFamilyName()).thenReturn("family name");
		when(patient.getGivenName()).thenReturn("given name");
		when(patient.getUuid()).thenReturn("patient_uuid");
		String pattern = "yyyy-MM-dd HH:mm";
		LocalDateTime begin = LocalDateTime.parse("2014-06-09 12:00", DateTimeFormatter.ofPattern(pattern));
		LocalDateTime finish = LocalDateTime.parse("2014-06-09 13:30", DateTimeFormatter.ofPattern(pattern));

		surgery.setProcedure(procedure);
		surgery.setPatient(patient);
		SchedulingData scheduling = new SchedulingData();
		scheduling.setStart(begin);
		scheduling.setEnd(finish);
		scheduling.setLocation(ot1);
		scheduling.setDateLocked(true);
		surgery.setSchedulingData(scheduling);
		surgeries.add(surgery);

		//mock service layer
		LocationService locationService = Mockito.mock(LocationService.class);
		AppointmentService appointmentService = Mockito.mock(AppointmentService.class);
		OperationTheaterService otService = Mockito.mock(OperationTheaterService.class);

		doReturn(tag).when(locationService).getLocationTagByUuid(OTMetadata.LOCATION_TAG_OPERATION_THEATER_UUID);
		doReturn(locations).when(locationService).getLocationsByTag(tag);
		doReturn(blocks).when(appointmentService).getAppointmentBlocks(start, end, "1,2,3,", null, null);
		doReturn(surgeries).when(otService).getScheduledSurgeries(
				eq(LocalDateTime.ofInstant(start.toInstant(), ZoneId.systemDefault())),
				eq(LocalDateTime.ofInstant(end.toInstant(), ZoneId.systemDefault())));

		//call function under test
		List<SimpleObject> result = new SchedulingFragmentController().getEvents(new TestUiUtils(),
				start, end, resources, locationService, appointmentService, otService);

		//verify
		for (SimpleObject o : result) {
			System.err.println(o.toJson());
		}

		assertThat(result, hasSize(6));
		int i = 0;
		assertThat(result.get(0).toJson(),
				is("{\"title\":\"\",\"start\":\"2014-06-09 00:00\",\"end\":\"2014-06-09 08:45\",\"availableStart\":\"2014-06-09 08:45\",\"availableEnd\":\"2014-06-09 19:16\",\"surgeryUuid\":null,\"patientUuid\":null,\"dateLocked\":false,\"resourceId\":1,\"allDay\":false,\"editable\":false,\"annotation\":true,\"color\":\"grey\",\"state\":null}"));
		assertThat(result.get(1).toJson(), is(
				"{\"title\":\"\",\"start\":\"2014-06-09 19:16\",\"end\":\"2014-06-09 23:59\",\"availableStart\":\"2014-06-09 08:45\",\"availableEnd\":\"2014-06-09 19:16\",\"surgeryUuid\":null,\"patientUuid\":null,\"dateLocked\":false,\"resourceId\":1,\"allDay\":false,\"editable\":false,\"annotation\":true,\"color\":\"grey\",\"state\":null}"));

		assertThat(result.get(2).toJson(), is(
				"{\"title\":\"\",\"start\":\"2014-06-09 00:00\",\"end\":\"2014-06-09 07:35\",\"availableStart\":\"2014-06-09 07:35\",\"availableEnd\":\"2014-06-09 20:55\",\"surgeryUuid\":null,\"patientUuid\":null,\"dateLocked\":false,\"resourceId\":2,\"allDay\":false,\"editable\":false,\"annotation\":true,\"color\":\"grey\",\"state\":null}"));
		assertThat(result.get(3).toJson(), is(
				"{\"title\":\"\",\"start\":\"2014-06-09 20:55\",\"end\":\"2014-06-09 23:59\",\"availableStart\":\"2014-06-09 07:35\",\"availableEnd\":\"2014-06-09 20:55\",\"surgeryUuid\":null,\"patientUuid\":null,\"dateLocked\":false,\"resourceId\":2,\"allDay\":false,\"editable\":false,\"annotation\":true,\"color\":\"grey\",\"state\":null}"));

		assertThat(result.get(4).toJson(), is(
				"{\"title\":\"\",\"start\":\"2014-06-09 00:00\",\"end\":\"2014-06-09 23:59\",\"availableStart\":\"2014-06-09 00:00\",\"availableEnd\":\"2014-06-09 00:00\",\"surgeryUuid\":null,\"patientUuid\":null,\"dateLocked\":false,\"resourceId\":3,\"allDay\":false,\"editable\":false,\"annotation\":true,\"color\":\"grey\",\"state\":null}"));

		assertThat(result.get(5).toJson(), is(
				"{\"title\":\"Procedure Name - family name given name\",\"start\":\"2014-06-09 12:00\",\"end\":\"2014-06-09 13:30\",\"availableStart\":\"\",\"availableEnd\":\"\",\"surgeryUuid\":\"uuid\",\"patientUuid\":\"patient_uuid\",\"dateLocked\":true,\"resourceId\":1,\"allDay\":false,\"editable\":false,\"annotation\":false,\"color\":\"#00ffff\",\"state\":null}"));
	}

	/**
	 * @verifies create appointment block if available times differ from default ones
	 * @see SchedulingFragmentController#adjustAvailableTimes(org.openmrs.ui.framework.UiUtils, String, boolean, java.util.Date, java.util.Date, org.openmrs.api.LocationService, org.openmrs.module.appointmentscheduling.api.AppointmentService)
	 */
	@Test
	public void adjustAvailableTimes_shouldCreateAppointmentBlockIfAvailableTimesDifferFromDefaultOnes() throws Exception {
		String uuid = "1";
		Location location = new Location();
		location.setUuid(uuid);

		Date start = Date.from(LocalDateTime.of(2014, 6, 9, 7, 12).atZone(ZoneId.systemDefault()).toInstant());
		Date end = Date.from(LocalDateTime.of(2014, 6, 9, 20, 55).atZone(ZoneId.systemDefault()).toInstant());

		//mock service layer
		LocationService locationService = Mockito.mock(LocationService.class);
		AppointmentService appointmentService = Mockito.mock(AppointmentService.class);

		doReturn(location).when(locationService).getLocationByUuid(uuid);
		doReturn(null).when(appointmentService)
				.getAppointmentBlocks(Matchers.any(Date.class), Matchers.any(Date.class), eq("1"),
						Matchers.any(Provider.class), Matchers.any(AppointmentType.class));

		ArgumentCaptor<AppointmentBlock> argumentCaptor = ArgumentCaptor.forClass(AppointmentBlock.class);
		when(appointmentService.saveAppointmentBlock(argumentCaptor.capture())).thenReturn(null);

		//call function under test
		new SchedulingFragmentController()
				.adjustAvailableTimes(new TestUiUtils(), uuid, true, start, end, locationService, appointmentService);

		assertThat(argumentCaptor.getValue().getLocation(), equalTo(location));
		assertThat(argumentCaptor.getValue().getStartDate(), equalTo(start));
		assertThat(argumentCaptor.getValue().getEndDate(), equalTo(end));

	}

	/**
	 * @verifies update appointment block if entry already exists
	 * @see SchedulingFragmentController#adjustAvailableTimes(org.openmrs.ui.framework.UiUtils, String, boolean, java.util.Date, java.util.Date, org.openmrs.api.LocationService, org.openmrs.module.appointmentscheduling.api.AppointmentService)
	 */
	@Test
	public void adjustAvailableTimes_shouldUpdateAppointmentBlockIfEntryAlreadyExists() throws Exception {
		String uuid = "afd1b8c2-eac7-4db4-b689-911615cebf80";
		Location location = new Location();
		location.setUuid(uuid);
		location.setId(10);

		List<AppointmentBlock> blocks = new ArrayList<AppointmentBlock>();
		AppointmentBlock block = new AppointmentBlock();
		block.setUuid("2");
		blocks.add(block);

		Date start = Date.from(LocalDateTime.of(2014, 6, 9, 7, 12).atZone(ZoneId.systemDefault()).toInstant());
		Date end = Date.from(LocalDateTime.of(2014, 6, 9, 20, 55).atZone(ZoneId.systemDefault()).toInstant());

		//mock service layer
		LocationService locationService = Mockito.mock(LocationService.class);
		AppointmentService appointmentService = Mockito.mock(AppointmentService.class);

		doReturn(location).when(locationService).getLocationByUuid(uuid);
		doReturn(blocks).when(appointmentService)
				.getAppointmentBlocks(Matchers.any(Date.class), Matchers.any(Date.class), eq(uuid),
						Matchers.any(Provider.class), Matchers.any(AppointmentType.class));

		ArgumentCaptor<AppointmentBlock> argumentCaptor = ArgumentCaptor.forClass(AppointmentBlock.class);
		when(appointmentService.saveAppointmentBlock(argumentCaptor.capture())).thenReturn(null);

		//call function under test
		new SchedulingFragmentController()
				.adjustAvailableTimes(new TestUiUtils(), uuid, true, start, end, locationService, appointmentService);

		assertThat(argumentCaptor.getValue().getId(), is(block.getId()));
		assertThat(argumentCaptor.getValue().getLocation(), equalTo(location));
		assertThat(argumentCaptor.getValue().getStartDate(), equalTo(start));
		assertThat(argumentCaptor.getValue().getEndDate(), equalTo(end));
	}

	/**
	 * @verifies update SchedulingData with provided values and return SuccessResult
	 * @see SchedulingFragmentController#adjustSurgerySchedule(String, String, java.util.Date, boolean, org.openmrs.api.LocationService, org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void adjustSurgerySchedule_shouldUpdateSchedulingDataWithProvidedValuesAndReturnSuccessResult() throws Exception {
		String surgeryUuid = "surgeryUuid";
		String locationUuid = "locationUuid";
		Date scheduledDateTime = new Date();
		boolean dateLocked = false;

		Surgery surgery = new Surgery();
		Location location = new Location();

		Procedure procedure = new Procedure();
		procedure.setOtPreparationDuration(12);
		procedure.setInterventionDuration(34);
		surgery.setProcedure(procedure);

		SchedulingData schedulingData = new SchedulingData();
		surgery.setSchedulingData(schedulingData);

		//mock service layer
		LocationService locationService = Mockito.mock(LocationService.class);
		OperationTheaterService otService = Mockito.mock(OperationTheaterService.class);
		when(otService.getSurgeryByUuid(surgeryUuid)).thenReturn(surgery);
		when(locationService.getLocationByUuid(locationUuid)).thenReturn(location);
		ArgumentCaptor<Surgery> captor = ArgumentCaptor.forClass(Surgery.class);
		when(otService.saveSurgery(captor.capture())).thenReturn(null);

		SchedulingFragmentController controller = new SchedulingFragmentController();
		mockValidateUtil(controller, true);

		//call function under test
		FragmentActionResult result = controller
				.adjustSurgerySchedule(new TestUiUtils(), surgeryUuid, locationUuid, scheduledDateTime, dateLocked,
						locationService, otService);

		//verify
		Surgery captured = captor.getValue();
		assertThat(captured, is(surgery));
		assertThat(captured.getSchedulingData(), is(schedulingData));
		assertThat(captured.getSchedulingData().getLocation(), is(location));
		assertThat(captured.getSchedulingData().getStart(),
				equalTo(LocalDateTime.ofInstant(scheduledDateTime.toInstant(),
						ZoneId.systemDefault())));
		assertThat(captured.getSchedulingData().getEnd(),
				equalTo(LocalDateTime.ofInstant(scheduledDateTime.toInstant(),
						ZoneId.systemDefault()).plusMinutes(12 + 34)));
		assertThat(captured.getSchedulingData().getDateLocked(), is(dateLocked));

		assertThat(result, instanceOf(SuccessResult.class));
		assertThat(((SuccessResult) result).getMessage(), equalTo(
				"operationtheater.scheduling.page.surgeryAdjustedSuccessfully"));
	}

	/**
	 * @verifies return FailureResult if SchedulingData validation fails
	 * @see SchedulingFragmentController#adjustSurgerySchedule(String, String, java.util.Date, boolean, org.openmrs.api.LocationService, org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void adjustSurgerySchedule_shouldReturnFailureResultIfSchedulingDataValidationFails() throws Exception {
		String surgeryUuid = "surgeryUuid";
		String locationUuid = "locationUuid";
		Date scheduledDateTime = new Date();
		boolean dateLocked = false;

		Surgery surgery = new Surgery();
		Location location = new Location();
		Procedure procedure = new Procedure();
		procedure.setInterventionDuration(12);
		procedure.setOtPreparationDuration(34);
		surgery.setProcedure(procedure);

		SchedulingData schedulingData = new SchedulingData();
		surgery.setSchedulingData(schedulingData);

		//mock service layer
		LocationService locationService = Mockito.mock(LocationService.class);
		OperationTheaterService otService = Mockito.mock(OperationTheaterService.class);
		when(otService.getSurgeryByUuid(surgeryUuid)).thenReturn(surgery);
		when(locationService.getLocationByUuid(locationUuid)).thenReturn(location);
		ArgumentCaptor<Surgery> captor = ArgumentCaptor.forClass(Surgery.class);
		when(otService.saveSurgery(captor.capture())).thenReturn(null);

		SchedulingFragmentController controller = new SchedulingFragmentController();
		mockValidateUtil(controller, false);

		//call function under test
		FragmentActionResult result = controller
				.adjustSurgerySchedule(new TestUiUtils(), surgeryUuid, locationUuid, scheduledDateTime, dateLocked,
						locationService, otService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getErrors().getFieldErrorCount(), is(1));
		assertThat(((FailureResult) result).getErrors().getFieldErrors().get(0).getField(), is("field"));
		assertThat(((FailureResult) result).getErrors().getFieldErrors().get(0).getCode(), is("code"));
	}

	/**
	 * @verifies throw IllegalArgumentException if there is no Surgery for the given uuid
	 * @see SchedulingFragmentController#adjustSurgerySchedule(org.openmrs.ui.framework.UiUtils, String, String, java.util.Date, boolean, org.openmrs.api.LocationService, org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void adjustSurgerySchedule_shouldThrowIllegalArgumentExceptionIfThereIsNoSurgeryForTheGivenUuid()
			throws Exception {

		String surgeryUuid = "invalidUuid";
		String locationUuid = "";
		UiUtils ui = new TestUiUtils();

		LocationService locationService = Mockito.mock(LocationService.class);
		OperationTheaterService otService = Mockito.mock(OperationTheaterService.class);
		when(otService.getSurgeryByUuid(surgeryUuid)).thenReturn(null);
		when(locationService.getLocationByUuid(locationUuid)).thenReturn(null);

		new SchedulingFragmentController()
				.adjustSurgerySchedule(ui, surgeryUuid, locationUuid, new Date(), true, locationService,
						otService);
	}

	/**
	 * @verifies return SuccessResult if solve method doesnt throw an IllegalStateException
	 * @see SchedulingFragmentController#schedule(org.openmrs.ui.framework.UiUtils)
	 */
	@Test
	public void schedule_shouldReturnSuccessResultIfSolveMethodDoesntThrowAnIllegalStateException() throws Exception {
		Scheduler mockInstance = PowerMockito.mock(Scheduler.class);
		Whitebox.setInternalState(Scheduler.class, "INSTANCE", mockInstance);
		PowerMockito.doNothing().when(mockInstance).solve(anyInt());

		AdministrationService administrationService = Mockito.mock(AdministrationService.class);
		when(administrationService.getGlobalProperty("operationtheater.continuousPlanningWindow")).thenReturn("7");

		//call method under test
		FragmentActionResult result = new SchedulingFragmentController().schedule(new TestUiUtils(), administrationService);

		//verify
		assertThat(result, instanceOf(SuccessResult.class));
		assertThat(((SuccessResult) result).getMessage(), is("operationtheater.scheduling.startedSuccessfully"));
	}

	/**
	 * @verifies return FailureResult if solve method throws an IllegalStateException
	 * @see SchedulingFragmentController#schedule(org.openmrs.ui.framework.UiUtils)
	 */
	@Test
	public void schedule_shouldReturnFailureResultIfSolveMethodThrowsAnIllegalStateException() throws Exception {
		Scheduler mockInstance = PowerMockito.mock(Scheduler.class);
		Whitebox.setInternalState(Scheduler.class, "INSTANCE", mockInstance);
		PowerMockito.doThrow(new IllegalStateException()).when(mockInstance).solve(anyInt());

		AdministrationService administrationService = Mockito.mock(AdministrationService.class);
		when(administrationService.getGlobalProperty("operationtheater.continuousPlanningWindow")).thenReturn("7");

		//call method under test
		FragmentActionResult result = new SchedulingFragmentController().schedule(new TestUiUtils(), administrationService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.scheduling.schedulerAlreadyRunning"));
	}

	/**
	 * @verifies return SuccessResult if status is running or succeeded
	 * @see SchedulingFragmentController#getSolverStatus(org.openmrs.ui.framework.UiUtils)
	 */
	@Test
	public void getSolverStatus_shouldReturnSuccessResultIfStatusIsRunningOrSucceeded() throws Exception {
		Scheduler mockInstance = PowerMockito.mock(Scheduler.class);
		Whitebox.setInternalState(Scheduler.class, "INSTANCE", mockInstance);
		when(mockInstance.getStatus()).thenReturn(Scheduler.Status.RUNNING).thenReturn(Scheduler.Status.SUCCEEDED);

		//call method under test
		FragmentActionResult result_running = new SchedulingFragmentController().getSolverStatus(new TestUiUtils());
		FragmentActionResult result_succeeded = new SchedulingFragmentController().getSolverStatus(new TestUiUtils());

		//verify
		assertThat(result_running, instanceOf(SuccessResult.class));
		assertThat(((SuccessResult) result_running).getMessage(), is("running"));
		assertThat(result_succeeded, instanceOf(SuccessResult.class));
		assertThat(((SuccessResult) result_succeeded).getMessage(), is("operationtheater.scheduling.finishedSuccessfully"));
	}

	/**
	 * @verifies return FailureResult if status is failed, pristine or any other
	 * @see SchedulingFragmentController#getSolverStatus(org.openmrs.ui.framework.UiUtils)
	 */
	@Test
	public void getSolverStatus_shouldReturnFailureResultIfStatusIsFailedPristineOrAnyOther() throws Exception {
		Scheduler mockInstance = PowerMockito.mock(Scheduler.class);
		Whitebox.setInternalState(Scheduler.class, "INSTANCE", mockInstance);
		when(mockInstance.getStatus()).thenReturn(Scheduler.Status.FAILED).thenReturn(Scheduler.Status.PRISTINE);

		//call method under test
		FragmentActionResult result_failed = new SchedulingFragmentController().getSolverStatus(new TestUiUtils());
		FragmentActionResult result_pristine = new SchedulingFragmentController().getSolverStatus(new TestUiUtils());

		//verify
		assertThat(result_failed, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result_failed).getSingleError(), is("operationtheater.scheduling.schedulingFailed"));
		assertThat(result_pristine, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result_pristine).getSingleError(),
				is("operationtheater.scheduling.schedulerNotStarted"));
	}
}
