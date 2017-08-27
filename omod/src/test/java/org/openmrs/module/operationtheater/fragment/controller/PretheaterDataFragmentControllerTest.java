package org.openmrs.module.operationtheater.fragment.controller;

import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.module.appui.TestUiUtils;
import org.openmrs.module.operationtheater.*;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Tests {@link PretheaterDataFragmentController}
 */
public class PretheaterDataFragmentControllerTest {

	/**
	 * @verifies throw IllegalArgumentException if patient is null
	 * @see PretheaterDataFragmentController#getPastProcedures(UiUtils, Patient)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getPastProcedures_shouldThrowIllegalArgumentExceptionIfPatientIsNull() throws Exception {

		new PretheaterDataFragmentController().getPastProcedures(new TestUiUtils(), null);
	}


	/**
	 * @verifies return failure result if patient is null.
	 * @see PretheaterDataFragmentController#addPastProcedureRecord(UiUtils, Patient, String, String, String)
	 * @throws Exception
	 */
	@Test
	public void addPastProcedureRecord_shouldReturnFailureResultIfPatientIsNull() throws Exception {

		ObsService obsService = Mockito.mock(ObsService.class);
		when(obsService.saveObs(new Obs(), "test")).thenReturn(null);

		ConceptService conceptService= Mockito.mock(ConceptService.class);
		when(conceptService.getConcept(anyInt())).thenReturn(new Concept());


		//call method under test
		FragmentActionResult result = new PretheaterDataFragmentController()
				.addPastProcedureRecord(new TestUiUtils(), null, "test proc 1", "2017-08-25", "test");

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.patient.notFound"));

	}

	/**
	 * @verifies return failure result if date is null.
	 * @see PretheaterDataFragmentController#addPastProcedureRecord(UiUtils, Patient, String, String, String)
	 * @throws Exception
	 */
	@Test
	public void addPastProcedureRecord_shouldReturnFailureResultIfPastProcedureNameIsNull() throws Exception {

		Patient patient = new Patient();

		//call method under test
		FragmentActionResult result = new PretheaterDataFragmentController()
				.addPastProcedureRecord(new TestUiUtils(), patient, null, "2017-08-25", "test");

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.pastProcedure.invalidName"));

	}

	/**
	 * @verifies return failure result if date is null.
	 * @see PretheaterDataFragmentController#addPastProcedureRecord(UiUtils, Patient, String, String, String)
	 * @throws Exception
	 */

	@Test
	public void addPastProcedureRecord_shouldReturnFailureResultIfPastProcedureDateIsNull() throws Exception {

		Patient patient = new Patient();

		//call method under test
		FragmentActionResult result = new PretheaterDataFragmentController()
				.addPastProcedureRecord(new TestUiUtils(), patient, "test proc 1", null, "test");

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.pastProcedure.invalidDate"));

	}


	/**
	 * @verifies return failure result if comment is null.
	 * @see PretheaterDataFragmentController#addPastProcedureRecord(UiUtils, Patient, String, String, String)
	 * @throws Exception
	 */

	@Test
	public void addPastProcedureRecord_shouldReturnFailureResultIfPastProcedureCommentIsNull() throws Exception {

		Patient patient = new Patient();

		//call method under test
		FragmentActionResult result = new PretheaterDataFragmentController()
				.addPastProcedureRecord(new TestUiUtils(), patient, "test proc 1", "2017-08-25", null);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.pastProcedure.invalidComment"));

	}

	// addPastProcedureRecord success can't be tested at this time due to Context services constraints.



	@Test(expected = IllegalArgumentException.class)
	public void getPreTheaterDrugs_shouldThrowIllegalArgumentExceptionIfSurgeryIsNull(){

		new PretheaterDataFragmentController().getPreTheaterDrugs(new TestUiUtils(), null);

	}

	/**
	 * @verifies throw IllegalArgumentException if patient is null.
	 * @see PretheaterDataFragmentController#addPretheaterDrugPrescription(UiUtils, Patient, Surgery, String, String, String, OperationTheaterService)
	 */
	@Test
	public void addPretheaterDrugPrescription_shouldReturnFailureResultIfPatientIsNull() throws Exception {

		Surgery surgery = new Surgery();

		OperationTheaterService otService = Mockito.mock(OperationTheaterService.class);
		when(otService.saveSurgery(any(Surgery.class))).thenReturn(null);

		//call method under test
		FragmentActionResult result = new PretheaterDataFragmentController()
				.addPretheaterDrugPrescription(new TestUiUtils(), null, surgery, "13547", "1", "test", otService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.patient.notFound"));
	}


	/**
	 * @verifies throw IllegalArgumentException if patient is null.
	 * @see PretheaterDataFragmentController#addPretheaterDrugPrescription(UiUtils, Patient, Surgery, String, String, String, OperationTheaterService)
	 */
	@Test
	public void addPretheaterDrugPrescription_shouldReturnFailureResultIfSurgeryIsNull() throws Exception {

		Patient patient = new Patient();

		OperationTheaterService otService = Mockito.mock(OperationTheaterService.class);
		when(otService.saveSurgery(any(Surgery.class))).thenReturn(null);

		//call method under test
		FragmentActionResult result = new PretheaterDataFragmentController()
				.addPretheaterDrugPrescription(new TestUiUtils(), patient, null, "13547", "1", "test", otService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.surgery.notFound"));
	}


	/**
	 * verifies return failure result if drug concept id is null.
	 * @throws Exception
	 */
	@Test
	public void addPretheaterDrugPrescription_shouldReturnFailureResultIfDrugConceptIdIsNull() throws Exception {

		Patient patient = new Patient();
		Surgery surgery = new Surgery();

		OperationTheaterService otService = Mockito.mock(OperationTheaterService.class);
		when(otService.saveSurgery(any(Surgery.class))).thenReturn(null);

		//call method under test
		FragmentActionResult result = new PretheaterDataFragmentController()
				.addPretheaterDrugPrescription(new TestUiUtils(), patient, surgery,null, "1", "test", otService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.drug.invalidDrug"));

	}

	/**
	 * verifies return failure result if drug quantity id is null.
	 * @throws Exception
	 */
	@Test
	public void addPretheaterDrugPrescription_shouldReturnFailureResultIfDrugQuantityIsNull() throws Exception {

		Patient patient = new Patient();
		Surgery surgery = new Surgery();

		OperationTheaterService otService = Mockito.mock(OperationTheaterService.class);
		when(otService.saveSurgery(any(Surgery.class))).thenReturn(null);

		//call method under test
		FragmentActionResult result = new PretheaterDataFragmentController()
				.addPretheaterDrugPrescription(new TestUiUtils(), patient, surgery,"13579", null, "test", otService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.drug.invalidQuantity"));

	}



}
