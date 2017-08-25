package org.openmrs.module.operationtheater.fragment.controller;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.api.ProviderService;
import org.openmrs.module.appui.TestUiUtils;
import org.openmrs.module.operationtheater.MockUtil;
import org.openmrs.module.operationtheater.OTMetadata;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.SchedulingData;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.module.operationtheater.validator.SurgeryValidator;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link SurgeryFragmentController}
 */
public class SurgeryFragmentControllerTest {

	/**
	 * @verifies throw IllegalArgumentException if surgery is null
	 * @see SurgeryFragmentController#getSurgicalTeam(UiUtils, Surgery)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getSurgicalTeam_shouldThrowIllegalArgumentExceptionIfSurgeryIsNull() throws Exception {

		//call method under test
		new SurgeryFragmentController().getSurgicalTeam(new TestUiUtils(), null);
	}

	/**
	 * @verifies return names of providers that are assigned to this surgery
	 * @see SurgeryFragmentController#getSurgicalTeam(UiUtils, Surgery)
	 */
	@Test
	public void getSurgicalTeam_shouldReturnNamesOfProvidersThatAreAssignedToThisSurgery() throws Exception {

		Surgery surgery = new Surgery();
		Set<Provider> surgicalTeam = new HashSet<Provider>();
		Provider provider1 = new Provider();
		provider1.setName("name1");
		provider1.setUuid("uuid1");
		Provider provider2 = new Provider();
		provider2.setName("name2");
		provider2.setUuid("uuid2");

		surgery.setSurgicalTeam(new HashSet<Provider>(Arrays.asList(new Provider[] { provider1, provider2 })));

		//call method under test
		List<SimpleObject> result = new SurgeryFragmentController().getSurgicalTeam(new TestUiUtils(), surgery);

		//verify
		assertThat(result, hasSize(2));
		List<String> jsonList = new ArrayList<String>();
		jsonList.add(result.get(0).toJson());
		jsonList.add(result.get(1).toJson());

		assertThat(jsonList, containsInAnyOrder(
				new String[] { "{\"name\":\"name1\",\"uuid\":\"uuid1\"}", "{\"name\":\"name2\",\"uuid\":\"uuid2\"}" }));
	}

	/**
	 * @verifies return FailureResult if surgery is null
	 * @see SurgeryFragmentController#addProviderToSurgicalTeam(UiUtils, Surgery, String, OperationTheaterService, ProviderService)
	 */
	@Test
	public void addProviderToSurgicalTeam_shouldReturnFailureResultIfSurgeryIsNull() throws Exception {

		ProviderService providerService = Mockito.mock(ProviderService.class);
		String uuid = "uuid";
		when(providerService.getProviderByUuid(uuid)).thenReturn(new Provider());

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.addProviderToSurgicalTeam(new TestUiUtils(), null, uuid, null, providerService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.surgery.notFound"));
	}

	/**
	 * @verifies return FailureResult if provider is null
	 * @see SurgeryFragmentController#addProviderToSurgicalTeam(UiUtils, Surgery, String, OperationTheaterService, ProviderService)
	 */
	@Test
	public void addProviderToSurgicalTeam_shouldReturnFailureResultIfProviderIsNull() throws Exception {

		ProviderService providerService = Mockito.mock(ProviderService.class);
		String uuid = "uuid";
		when(providerService.getProviderByUuid(uuid)).thenReturn(null);

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.addProviderToSurgicalTeam(new TestUiUtils(), new Surgery(), uuid, null, providerService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.provider.notFound"));
	}

	/**
	 * @verifies return FailureResult if provider is already part of surgical team
	 * @see SurgeryFragmentController#addProviderToSurgicalTeam(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.Surgery, String, org.openmrs.module.operationtheater.api.OperationTheaterService, org.openmrs.api.ProviderService)
	 */
	@Test
	public void addProviderToSurgicalTeam_shouldReturnFailureResultIfProviderIsAlreadyPartOfSurgicalTeam() throws Exception {
		ProviderService providerService = Mockito.mock(ProviderService.class);
		String uuid = "uuid";
		Provider provider = new Provider();
		provider.setUuid(uuid);
		provider.setName("name");
		when(providerService.getProviderByUuid(uuid)).thenReturn(provider);

		Surgery surgery = new Surgery();
		surgery.setSurgicalTeam(new HashSet<Provider>(Arrays.asList(provider)));

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.addProviderToSurgicalTeam(new TestUiUtils(), surgery, uuid, null, providerService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(),
				is("operationtheater.surgery.providerAlreadyPartOfSurgicalTeam:name"));
	}

	/**
	 * @verifies return SuccessResult if provider has been added
	 * @see SurgeryFragmentController#addProviderToSurgicalTeam(UiUtils, Surgery, String, OperationTheaterService, ProviderService)
	 */
	@Test
	public void addProviderToSurgicalTeam_shouldReturnSuccessResultIfProviderHasBeenAdded() throws Exception {

		OperationTheaterService service = Mockito.mock(OperationTheaterService.class);
		when(service.saveSurgery(any(Surgery.class))).thenReturn(null);

		Provider provider = new Provider();
		String providerUuid = "uuid";
		provider.setName("name");

		ProviderService providerService = Mockito.mock(ProviderService.class);
		when(providerService.getProviderByUuid(providerUuid)).thenReturn(provider);

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.addProviderToSurgicalTeam(new TestUiUtils(), new Surgery(), providerUuid, service, providerService);

		//verify
		ArgumentCaptor<Surgery> captor = ArgumentCaptor.forClass(Surgery.class);
		verify(service).saveSurgery(captor.capture());

		assertThat(captor.getValue().getSurgicalTeam(), hasSize(1));
		assertTrue(captor.getValue().getSurgicalTeam().contains(provider));

		assertThat(result, instanceOf(SuccessResult.class));
		assertThat(((SuccessResult) result).getMessage(),
				is("operationtheater.surgery.addedProviderToSurgicalTeam:name"));
	}

	/**
	 * @verifies return FailureResult if surgery is null
	 * @see SurgeryFragmentController#removeProviderFromSurgicalTeam(UiUtils, Surgery, String, OperationTheaterService, ProviderService)
	 */
	@Test
	public void removeProviderFromSurgicalTeam_shouldReturnFailureResultIfSurgeryIsNull() throws Exception {

		ProviderService providerService = Mockito.mock(ProviderService.class);
		String uuid = "uuid";
		when(providerService.getProviderByUuid(uuid)).thenReturn(null);

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.removeProviderFromSurgicalTeam(new TestUiUtils(), null, "uuid", null, providerService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.surgery.notFound"));
	}

	/**
	 * @verifies return FailureResult if provider is null
	 * @see SurgeryFragmentController#removeProviderFromSurgicalTeam(UiUtils, Surgery, String, OperationTheaterService, ProviderService)
	 */
	@Test
	public void removeProviderFromSurgicalTeam_shouldReturnFailureResultIfProviderIsNull() throws Exception {

		ProviderService providerService = Mockito.mock(ProviderService.class);
		String uuid = "uuid";
		when(providerService.getProviderByUuid(uuid)).thenReturn(null);

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.removeProviderFromSurgicalTeam(new TestUiUtils(), new Surgery(), uuid, null, providerService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.provider.notFound"));
	}

	/**
	 * @verifies return FailureResult if provider is not part of surgical team
	 * @see SurgeryFragmentController#removeProviderFromSurgicalTeam(UiUtils, Surgery, String, OperationTheaterService, ProviderService)
	 */
	@Test
	public void removeProviderFromSurgicalTeam_shouldReturnFailureResultIfProviderIsNotPartOfSurgicalTeam()
			throws Exception {

		ProviderService providerService = Mockito.mock(ProviderService.class);
		String uuid = "uuid";
		Provider provider = new Provider();
		provider.setName("name");
		when(providerService.getProviderByUuid(uuid)).thenReturn(provider);

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.removeProviderFromSurgicalTeam(new TestUiUtils(), new Surgery(), uuid, null, providerService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(),
				is("operationtheater.surgery.providerNotPartOfSurgicalTeam:name"));
	}

	/**
	 * @verifies return SuccessResult if provider has been removed
	 * @see SurgeryFragmentController#removeProviderFromSurgicalTeam(UiUtils, Surgery, String, OperationTheaterService, ProviderService)
	 */
	@Test
	public void removeProviderFromSurgicalTeam_shouldReturnSuccessResultIfProviderHasBeenRemoved() throws Exception {
		OperationTheaterService service = Mockito.mock(OperationTheaterService.class);
		when(service.saveSurgery(any(Surgery.class))).thenReturn(null);

		Surgery surgery = new Surgery();
		Provider provider = new Provider();
		String uuid = "uuid";
		provider.setName("name");
		surgery.setSurgicalTeam(new HashSet<Provider>(Arrays.asList(provider)));

		ProviderService providerService = Mockito.mock(ProviderService.class);
		when(providerService.getProviderByUuid(uuid)).thenReturn(provider);

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.removeProviderFromSurgicalTeam(new TestUiUtils(), surgery, uuid, service, providerService);

		//verify
		ArgumentCaptor<Surgery> captor = ArgumentCaptor.forClass(Surgery.class);
		verify(service).saveSurgery(captor.capture());

		assertThat(captor.getValue().getSurgicalTeam(), hasSize(0));

		assertThat(result, instanceOf(SuccessResult.class));
		assertThat(((SuccessResult) result).getMessage(),
				is("operationtheater.surgery.removedProviderFromSurgicalTeam:name"));
	}

	/**
	 * @verifies return FailureResult if surgery is null
	 * @see SurgeryFragmentController#updateProcedure(UiUtils, Surgery, Procedure, OperationTheaterService)
	 */
	@Test
	public void updateProcedure_shouldReturnFailureResultIfSurgeryIsNull() throws Exception {

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.updateProcedure(new TestUiUtils(), null, new Procedure(), null);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.surgery.notFound"));
	}

	/**
	 * @verifies return FailureResult if procedure is null
	 * @see SurgeryFragmentController#updateProcedure(UiUtils, Surgery, Procedure, OperationTheaterService)
	 */
	@Test
	public void updateProcedure_shouldReturnFailureResultIfProcedureIsNull() throws Exception {

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.updateProcedure(new TestUiUtils(), new Surgery(), null, null);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.procedure.notFound"));
	}

	/**
	 * @verifies return SuccessResult if procedure has been successfully updated
	 * @see SurgeryFragmentController#updateProcedure(UiUtils, Surgery, Procedure, OperationTheaterService)
	 */
	@Test
	public void updateProcedure_shouldReturnSuccessResultIfProcedureHasBeenSuccessfullyUpdated() throws Exception {

		OperationTheaterService service = Mockito.mock(OperationTheaterService.class);
		when(service.saveSurgery(any(Surgery.class))).thenReturn(null);

		Procedure procedure = new Procedure();
		procedure.setUuid("procedureUuid");
		procedure.setName("procedureName");

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.updateProcedure(new TestUiUtils(), new Surgery(), procedure, service);

		//verify
		ArgumentCaptor<Surgery> captor = ArgumentCaptor.forClass(Surgery.class);
		verify(service).saveSurgery(captor.capture());
		assertThat(captor.getValue().getProcedure(), is(procedure));

		assertThat(result, instanceOf(SuccessResult.class));
		assertThat(((SuccessResult) result).getMessage(),
				is("operationtheater.surgery.updated.procedure:procedureName"));
	}

	/**
	 * @verifies throw IllegalArgumentException if surgery is null
	 * @see SurgeryFragmentController#getSurgeryTimes(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.Surgery)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getSurgeryTimes_shouldThrowIllegalArgumentExceptionIfSurgeryIsNull() throws Exception {

		//call method under test
		new SurgeryFragmentController().getSurgeryTimes(new TestUiUtils(), null);
	}

	/**
	 * @verifies return all surgery times of this surgery if it has already been finished
	 * @see SurgeryFragmentController#getSurgeryTimes(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.Surgery)
	 */
	@Test
	public void getSurgeryTimes_shouldReturnAllSurgeryTimesOfThisSurgeryIfItHasAlreadyBeenFinished() throws Exception {
		LocalDateTime refDate = LocalDateTime.of(2014, 8, 10, 17, 0, 0);
		LocalDateTime created = refDate.minusDays(1);
		LocalDateTime started = refDate;
		LocalDateTime finished = refDate.plusHours(1);

		Surgery surgery = new Surgery();
		surgery.setDateCreated(Date.from(created.atZone(ZoneId.systemDefault()).toInstant()));
		surgery.setDateStarted(started);
		surgery.setDateFinished(finished);

		//call method under test
		List<SimpleObject> result = new SurgeryFragmentController().getSurgeryTimes(new TestUiUtils(), surgery);

		//verify
		assertThat(result, hasSize(3));
		int i = 0;
		assertThat((String) result.get(i).get("type"), is("CREATED"));
		assertThat((String) result.get(i).get("displayName"), is("operationtheater.surgery.dateCreated.displayName"));
		assertThat((String) result.get(i).get("dateTimeStr"), is("9 August 2014 05:00 PM"));
		i++;
		assertThat((String) result.get(i).get("type"), is("STARTED"));
		assertThat((String) result.get(i).get("displayName"), is("operationtheater.surgery.dateStarted.displayName"));
		assertThat((String) result.get(i).get("dateTimeStr"), is("10 August 2014 05:00 PM"));
		i++;
		assertThat((String) result.get(i).get("type"), is("FINISHED"));
		assertThat((String) result.get(i).get("displayName"), is("operationtheater.surgery.dateFinished.displayName"));
		assertThat((String) result.get(i).get("dateTimeStr"), is("10 August 2014 06:00 PM"));
	}

	/**
	 * @verifies return created and start times of this surgery if it hasn't been finished
	 * @see SurgeryFragmentController#getSurgeryTimes(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.Surgery)
	 */
	@Test
	public void getSurgeryTimes_shouldReturnCreatedAndStartTimesOfThisSurgeryIfItHasntBeenFinished() throws Exception {
		LocalDateTime refDate = LocalDateTime.of(2014, 8, 10, 17, 0, 0);
		LocalDateTime created = refDate.minusDays(1);
		LocalDateTime started = refDate;

		Surgery surgery = new Surgery();
		surgery.setDateCreated(Date.from(created.atZone(ZoneId.systemDefault()).toInstant()));
		surgery.setDateStarted(started);

		//call method under test
		List<SimpleObject> result = new SurgeryFragmentController().getSurgeryTimes(new TestUiUtils(), surgery);

		//verify
		assertThat(result, hasSize(2));
		int i = 0;
		assertThat((String) result.get(i).get("type"), is("CREATED"));
		assertThat((String) result.get(i).get("displayName"), is("operationtheater.surgery.dateCreated.displayName"));
		assertThat((String) result.get(i).get("dateTimeStr"), is("9 August 2014 05:00 PM"));
		i++;
		assertThat((String) result.get(i).get("type"), is("STARTED"));
		assertThat((String) result.get(i).get("displayName"), is("operationtheater.surgery.dateStarted.displayName"));
		assertThat((String) result.get(i).get("dateTimeStr"), is("10 August 2014 05:00 PM"));
	}

	/**
	 * @verifies return created time of this surgery if it hasn't been started
	 * @see SurgeryFragmentController#getSurgeryTimes(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.Surgery)
	 */
	@Test
	public void getSurgeryTimes_shouldReturnCreatedTimeOfThisSurgeryIfItHasntBeenStarted() throws Exception {
		LocalDateTime refDate = LocalDateTime.of(2014, 8, 10, 17, 0, 0);
		LocalDateTime created = refDate.minusDays(1);

		Surgery surgery = new Surgery();
		surgery.setDateCreated(Date.from(created.atZone(ZoneId.systemDefault()).toInstant()));

		//call method under test
		List<SimpleObject> result = new SurgeryFragmentController().getSurgeryTimes(new TestUiUtils(), surgery);

		//verify
		assertThat(result, hasSize(1));
		int i = 0;
		assertThat((String) result.get(i).get("type"), is("CREATED"));
		assertThat((String) result.get(i).get("displayName"), is("operationtheater.surgery.dateCreated.displayName"));
		assertThat((String) result.get(i).get("dateTimeStr"), is("9 August 2014 05:00 PM"));
	}

	/**
	 * @verifies return FailureResult if surgery is null
	 * @see SurgeryFragmentController#startSurgery(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.Surgery, org.openmrs.module.operationtheater.api.OperationTheaterService, org.openmrs.module.operationtheater.validator.SurgeryValidator)
	 */
	@Test
	public void startSurgery_shouldReturnFailureResultIfSurgeryIsNull() throws Exception {
		//call method under test
		FragmentActionResult result = new SurgeryFragmentController().startSurgery(new TestUiUtils(), null, null, null);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.surgery.notFound"));
	}

	/**
	 * @verifies return FailureResult if validation fails
	 * @see SurgeryFragmentController#startSurgery(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.Surgery, org.openmrs.module.operationtheater.api.OperationTheaterService, org.openmrs.module.operationtheater.validator.SurgeryValidator)
	 */
	@Test
	public void startSurgery_shouldReturnFailureResultIfValidationFails() throws Exception {
		Surgery surgery = new Surgery();

		//mock validator - add validation error on field procedure
		SurgeryValidator validator = (SurgeryValidator) MockUtil
				.mockValidator(false, SurgeryValidator.class, Surgery.class, "procedure", "errorCode");
		OperationTheaterService service = Mockito.mock(OperationTheaterService.class);

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.startSurgery(new TestUiUtils(), surgery, service, validator);

		//verify
		verify(validator).validate(Mockito.eq(surgery), Mockito.any(Errors.class));
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getErrors().getFieldErrors("procedure").get(0).getCode(), is("errorCode"));
	}

	/**
	 * @verifies return FailureResult if surgery has already been finished
	 * @see SurgeryFragmentController#startSurgery(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.Surgery, org.openmrs.module.operationtheater.api.OperationTheaterService, org.openmrs.module.operationtheater.validator.SurgeryValidator)
	 */
	@Test
	public void startSurgery_shouldReturnFailureResultIfSurgeryHasAlreadyBeenFinished() throws Exception {
		Surgery surgery = new Surgery();
		surgery.setDateStarted(LocalDateTime.now().minusMinutes(2));
		surgery.setDateFinished(LocalDateTime.now().minusMinutes(1));

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController().startSurgery(new TestUiUtils(), surgery, null, null);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.surgery.alreadyFinished"));
	}

	/**
	 * @verifies return FailureResult if surgery has already been started
	 * @see SurgeryFragmentController#startSurgery(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.Surgery, org.openmrs.module.operationtheater.api.OperationTheaterService, org.openmrs.module.operationtheater.validator.SurgeryValidator)
	 */
	@Test
	public void startSurgery_shouldReturnFailureResultIfSurgeryHasAlreadyBeenStarted() throws Exception {
		Surgery surgery = new Surgery();
		surgery.setDateStarted(LocalDateTime.now().minusMinutes(1));

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController().startSurgery(new TestUiUtils(), surgery, null, null);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.surgery.alreadyStarted"));
	}

	/**
	 * @verifies return SuccessResult if dateStarted has been successfully set
	 * @see SurgeryFragmentController#startSurgery(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.Surgery, org.openmrs.module.operationtheater.api.OperationTheaterService, org.openmrs.module.operationtheater.validator.SurgeryValidator)
	 */
	@Test
	public void startSurgery_shouldReturnSuccessResultIfDateStartedHasBeenSuccessfullySet() throws Exception {
		Surgery surgery = new Surgery();
		Location location = new Location();
		location.setName("locationName");
		SchedulingData schedulingData = new SchedulingData();
		schedulingData.setLocation(location);
		surgery.setSchedulingData(schedulingData);

		SurgeryValidator validator = (SurgeryValidator) MockUtil
				.mockValidator(true, SurgeryValidator.class, Surgery.class, null, null);
		OperationTheaterService service = Mockito.mock(OperationTheaterService.class);
		when(service.saveSurgery(eq(surgery))).thenReturn(surgery);

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.startSurgery(new TestUiUtils(), surgery, service, validator);

		ArgumentCaptor<Surgery> captor = ArgumentCaptor.forClass(Surgery.class);
		verify(service).saveSurgery(captor.capture());

		//verify
		verify(validator).validate(Mockito.eq(surgery), Mockito.any(Errors.class));
		assertThat(captor.getValue().getDateStarted().truncatedTo(ChronoUnit.SECONDS),
				is(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)));
		assertThat(result, instanceOf(SuccessResult.class));
		assertThat(((SuccessResult) result).getMessage(), is("operationtheater.surgery.started:locationName"));
	}

	/**
	 * @verifies return FailureResult if surgery is null
	 * @see SurgeryFragmentController#finishSurgery(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.Surgery, org.openmrs.module.operationtheater.api.OperationTheaterService, org.openmrs.module.operationtheater.validator.SurgeryValidator)
	 */
	@Test
	public void finishSurgery_shouldReturnFailureResultIfSurgeryIsNull() throws Exception {
		//call method under test
		FragmentActionResult result = new SurgeryFragmentController().finishSurgery(new TestUiUtils(), null, null, null);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.surgery.notFound"));
	}

	/**
	 * @verifies return FailureResult if validation fails
	 * @see SurgeryFragmentController#finishSurgery(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.Surgery, org.openmrs.module.operationtheater.api.OperationTheaterService, org.openmrs.module.operationtheater.validator.SurgeryValidator)
	 */
	@Test
	public void finishSurgery_shouldReturnFailureResultIfValidationFails() throws Exception {
		Surgery surgery = new Surgery();

		//mock validator - add validation error on field procedure
		SurgeryValidator validator = (SurgeryValidator) MockUtil
				.mockValidator(false, SurgeryValidator.class, Surgery.class, "procedure", "errorCode");
		OperationTheaterService service = Mockito.mock(OperationTheaterService.class);

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.finishSurgery(new TestUiUtils(), surgery, service, validator);

		//verify
		verify(validator).validate(Mockito.eq(surgery), Mockito.any(Errors.class));
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getErrors().getFieldErrors("procedure").get(0).getCode(), is("errorCode"));
	}

	/**
	 * @verifies return FailureResult if surgery has already been finished
	 * @see SurgeryFragmentController#finishSurgery(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.Surgery, org.openmrs.module.operationtheater.api.OperationTheaterService, org.openmrs.module.operationtheater.validator.SurgeryValidator)
	 */
	@Test
	public void finishSurgery_shouldReturnFailureResultIfSurgeryHasAlreadyBeenFinished() throws Exception {
		Surgery surgery = new Surgery();
		surgery.setDateFinished(LocalDateTime.now().minusMinutes(1));

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController().finishSurgery(new TestUiUtils(), surgery, null, null);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.surgery.alreadyFinished"));
	}

	/**
	 * @verifies return SuccessResult if dateFinished has been successfully set
	 * @see SurgeryFragmentController#finishSurgery(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.Surgery, org.openmrs.module.operationtheater.api.OperationTheaterService, org.openmrs.module.operationtheater.validator.SurgeryValidator)
	 */
	@Test
	public void finishSurgery_shouldReturnSuccessResultIfDateFinishedHasBeenSuccessfullySet() throws Exception {
		Surgery surgery = new Surgery();
		Location location = new Location();
		location.setName("locationName");
		SchedulingData schedulingData = new SchedulingData();
		schedulingData.setLocation(location);
		surgery.setSchedulingData(schedulingData);

		SurgeryValidator validator = (SurgeryValidator) MockUtil
				.mockValidator(true, SurgeryValidator.class, Surgery.class, null, null);
		OperationTheaterService service = Mockito.mock(OperationTheaterService.class);
		when(service.saveSurgery(eq(surgery))).thenReturn(surgery);

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.finishSurgery(new TestUiUtils(), surgery, service, validator);

		ArgumentCaptor<Surgery> captor = ArgumentCaptor.forClass(Surgery.class);
		verify(service).saveSurgery(captor.capture());

		//verify
		verify(validator).validate(Mockito.eq(surgery), Mockito.any(Errors.class));
		assertThat(captor.getValue().getDateFinished().truncatedTo(ChronoUnit.SECONDS),
				is(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)));
		assertThat(result, instanceOf(SuccessResult.class));
		assertThat(((SuccessResult) result).getMessage(), is("operationtheater.surgery.finished"));
	}

	/**
	 * @verifies return FailureResult if surgeryUuid is null
	 * @see SurgeryFragmentController#createNewSurgery(org.openmrs.ui.framework.UiUtils, String, org.openmrs.Patient, org.openmrs.module.operationtheater.Procedure, org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void createNewSurgery_shouldReturnFailureResultIfSurgeryUuidIsNull() throws Exception {
		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.createNewSurgery(new TestUiUtils(), null, new Patient(), new Procedure(), null);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.surgery.nullUuid"));
	}

	/**
	 * @verifies return FailureResult if patient is null
	 * @see SurgeryFragmentController#createNewSurgery(org.openmrs.ui.framework.UiUtils, String, org.openmrs.Patient, org.openmrs.module.operationtheater.Procedure, org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void createNewSurgery_shouldReturnFailureResultIfPatientIsNull() throws Exception {
		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.createNewSurgery(new TestUiUtils(), "surgeryUuid", null, new Procedure(), null);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.patient.notFound"));
	}

	/**
	 * @verifies return FailureResult if procedure is null
	 * @see SurgeryFragmentController#createNewSurgery(org.openmrs.ui.framework.UiUtils, String, org.openmrs.Patient, org.openmrs.module.operationtheater.Procedure, org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void createNewSurgery_shouldReturnFailureResultIfProcedureIsNull() throws Exception {
		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.createNewSurgery(new TestUiUtils(), "surgeryUuid", new Patient(), null, null);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.procedure.notFound"));
	}

	/**
	 * @verifies return FailureResult if surgery already exists
	 * @see SurgeryFragmentController#createNewSurgery(org.openmrs.ui.framework.UiUtils, String, org.openmrs.Patient, org.openmrs.module.operationtheater.Procedure, org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void createNewSurgery_shouldReturnFailureResultIfSurgeryAlreadyExists() throws Exception {
		//prepare
		String surgeryUuid = "surgeryUuid";
		OperationTheaterService otService = Mockito.mock(OperationTheaterService.class);
		when(otService.getSurgeryByUuid(surgeryUuid)).thenReturn(new Surgery());

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.createNewSurgery(new TestUiUtils(), surgeryUuid, new Patient(), new Procedure(), otService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.surgery.alreadyExists"));
	}

	/**
	 * @verifies return SuccessResult if surgery has been created successfully
	 * @see SurgeryFragmentController#createNewSurgery(org.openmrs.ui.framework.UiUtils, String, org.openmrs.Patient, org.openmrs.module.operationtheater.Procedure, org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void createNewSurgery_shouldReturnSuccessResultIfSurgeryHasBeenCreatedSuccessfully() throws Exception {
		//prepare
		String surgeryUuid = "surgeryUuid";
		OperationTheaterService otService = Mockito.mock(OperationTheaterService.class);
		when(otService.getSurgeryByUuid(surgeryUuid)).thenReturn(null);

		Patient patient = new Patient();
		Procedure procedure = new Procedure();

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.createNewSurgery(new TestUiUtils(), surgeryUuid, patient, procedure, otService);

		ArgumentCaptor<Surgery> captor = ArgumentCaptor.forClass(Surgery.class);

		//verify
		verify(otService).saveSurgery(captor.capture());
		assertThat(result, instanceOf(SuccessResult.class));
		assertThat(((SuccessResult) result).getMessage(), is("operationtheater.surgery.createdSuccessfully"));
		assertThat(captor.getValue().getUuid(), equalTo(surgeryUuid));
		assertThat(captor.getValue().getPatient(), equalTo(patient));
		assertThat(captor.getValue().getProcedure(), equalTo(procedure));
	}

	/**
	 * @verifies return FailureResult if surgery is null
	 * @see SurgeryFragmentController#replaceEmergencyPlaceholderPatient(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.Surgery, org.openmrs.Patient, org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void replaceEmergencyPlaceholderPatient_shouldReturnFailureResultIfSurgeryIsNull() throws Exception {
		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.replaceEmergencyPlaceholderPatient(new TestUiUtils(), null, new Patient(), null);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.surgery.notFound"));
	}

	/**
	 * @verifies return FailureResult if patient is null
	 * @see SurgeryFragmentController#replaceEmergencyPlaceholderPatient(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.Surgery, org.openmrs.Patient, org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void replaceEmergencyPlaceholderPatient_shouldReturnFailureResultIfPatientIsNull() throws Exception {
		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.replaceEmergencyPlaceholderPatient(new TestUiUtils(), new Surgery(), null, null);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.patient.notFound"));
	}

	/**
	 * @verifies return FailureResult if current patient is not the emergency placeholder patient
	 * @see SurgeryFragmentController#replaceEmergencyPlaceholderPatient(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.Surgery, org.openmrs.Patient, org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void replaceEmergencyPlaceholderPatient_shouldReturnFailureResultIfCurrentPatientIsNotTheEmergencyPlaceholderPatient()
			throws Exception {
		//prepare
		Surgery surgery = new Surgery();
		Patient patient = new Patient();
		patient.setUuid("not emergency placeholder patient uuid");
		PersonName personName = new PersonName("given", "middle", "family");
		patient.setNames(new HashSet<PersonName>(Arrays.asList(personName)));
		surgery.setPatient(patient);

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.replaceEmergencyPlaceholderPatient(new TestUiUtils(), surgery, new Patient(), null);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(),
				is("operationtheater.surgery.notPlaceholderPatient:family, given"));
	}

	/**
	 * @verifies return SuccessResult if replacement was successful
	 * @see SurgeryFragmentController#replaceEmergencyPlaceholderPatient(org.openmrs.ui.framework.UiUtils, org.openmrs.module.operationtheater.Surgery, org.openmrs.Patient, org.openmrs.module.operationtheater.api.OperationTheaterService)
	 */
	@Test
	public void replaceEmergencyPlaceholderPatient_shouldReturnSuccessResultIfReplacementWasSuccessful() throws Exception {
		//prepare
		Surgery surgery = new Surgery();
		Patient patient = new Patient();
		patient.setUuid(OTMetadata.PLACEHOLDER_PATIENT_UUID);
		surgery.setPatient(patient);

		Patient newPatient = new Patient();

		OperationTheaterService service = Mockito.mock(OperationTheaterService.class);

		//call method under test
		FragmentActionResult result = new SurgeryFragmentController()
				.replaceEmergencyPlaceholderPatient(new TestUiUtils(), surgery, newPatient, service);

		ArgumentCaptor<Surgery> captor = ArgumentCaptor.forClass(Surgery.class);

		//verify
		verify(service).saveSurgery(captor.capture());
		assertThat(captor.getValue(), is(surgery));
		assertThat(captor.getValue().getPatient(), is(newPatient));
		assertThat(result, instanceOf(SuccessResult.class));
		assertThat(((SuccessResult) result).getMessage(),
				is("operationtheater.surgery.replacedPlaceholderPatientSuccessfully"));
	}
}
