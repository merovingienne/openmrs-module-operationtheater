package org.openmrs.module.operationtheater.fragment.controller;

import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.module.appui.TestUiUtils;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Tests {@link PostTheaterDataFragmentController}
 */
public class PostTheaterDataFragmentControllerTest {

	/**
	 * @verifies throw IllegalArgumentException if surgery is null
	 * @see PostTheaterDataFragmentController#getTheaterDrugs(UiUtils, Surgery, String)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getTheaterDrugs_shouldThrowIllegalArgumentExceptionIfSurgeryIsNull() throws Exception {

		new PostTheaterDataFragmentController().getTheaterDrugs(new TestUiUtils(), null, "2");
	}

	/**
	 * @verifies throw IllegalArgumentException if workflow stage is null
	 * @see PostTheaterDataFragmentController#getTheaterDrugs(UiUtils, Surgery, String)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getTheaterDrugs_shouldThrowIllegalArgumentExceptionIfWorkflowPositionIsNull() throws Exception {
		Surgery surgery = new Surgery();
		new PostTheaterDataFragmentController().getTheaterDrugs(new TestUiUtils(), surgery, null);
	}


	/**
	 * @verifies throw IllegalArgumentException if workflow stage is empty
	 * @see PostTheaterDataFragmentController#getTheaterDrugs(UiUtils, Surgery, String)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getTheaterDrugs_shouldThrowIllegalArgumentExceptionIfWorkflowPositionIsEmpty() throws Exception {
		Surgery surgery = new Surgery();
		new PostTheaterDataFragmentController().getTheaterDrugs(new TestUiUtils(), surgery, "");
	}


	/**
	 * @verifies throw IllegalArgumentException if workflow stage is not a number
	 * @see PostTheaterDataFragmentController#getTheaterDrugs(UiUtils, Surgery, String)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getTheaterDrugs_shouldThrowIllegalArgumentExceptionIfWorkflowPositionIsNotANumber() throws Exception {
		Surgery surgery = new Surgery();
		new PostTheaterDataFragmentController().getTheaterDrugs(new TestUiUtils(), surgery, "asd");
	}


	/**
	 * @verifies throw IllegalArgumentException if workflow stage is not 2 or 3
	 * these values denote in-theater and post-theater stages respectively.
	 * @see PostTheaterDataFragmentController#getTheaterDrugs(UiUtils, Surgery, String)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getTheaterDrugs_shouldThrowIllegalArgumentExceptionIfWorkflowPositionIsNotValid() throws Exception {
		Surgery surgery = new Surgery();
		new PostTheaterDataFragmentController().getTheaterDrugs(new TestUiUtils(), surgery, "5");
	}




	/**
	 * @verifies throw IllegalArgumentException if patient is null.
	 * @see PostTheaterDataFragmentController#addTheaterDrugPrescription(UiUtils, Patient, Surgery, String, String, String, String, OperationTheaterService)
	 */
	@Test
	public void addTheaterDrugPrescription_shouldReturnFailureResultIfPatientIsNull() throws Exception {

		Surgery surgery = new Surgery();

		OperationTheaterService otService = Mockito.mock(OperationTheaterService.class);
		when(otService.saveSurgery(any(Surgery.class))).thenReturn(null);

		//call method under test
		FragmentActionResult result = new PostTheaterDataFragmentController()
				.addTheaterDrugPrescription(new TestUiUtils(), null, surgery, "2", "1", "test", "none", otService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.patient.notFound"));
	}


	/**
	 * @verifies throw IllegalArgumentException if surgery is null.
	 * @see PostTheaterDataFragmentController#addTheaterDrugPrescription(UiUtils, Patient, Surgery, String, String, String, String, OperationTheaterService)
	 */
	@Test
	public void addTheaterDrugPrescription_shouldReturnFailureResultIfSurgeryIsNull() throws Exception {

		Patient patient = new Patient();

		OperationTheaterService otService = Mockito.mock(OperationTheaterService.class);
		when(otService.saveSurgery(any(Surgery.class))).thenReturn(null);

		//call method under test
		FragmentActionResult result = new PostTheaterDataFragmentController()
				.addTheaterDrugPrescription(new TestUiUtils(), patient, null, "2", "1", "test", "none", otService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.surgery.notFound"));
	}


	/**
	 * verifies return failure result if drug concept id is null.
	 * @throws Exception
	 */
	@Test
	public void addTheaterDrugPrescription_shouldReturnFailureResultIfDrugConceptIdIsNull() {

		Patient patient = new Patient();
		Surgery surgery = new Surgery();

		OperationTheaterService otService = Mockito.mock(OperationTheaterService.class);
		when(otService.saveSurgery(any(Surgery.class))).thenReturn(null);

		//call method under test
		FragmentActionResult result = new PostTheaterDataFragmentController()
				.addTheaterDrugPrescription(new TestUiUtils(), patient, surgery, "2", null, "test", "none", otService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.drug.invalidDrug"));

	}

	/**
	 * verifies return failure result if drug quantity is null.
	 * @throws Exception
	 */
	@Test
	public void addTheaterDrugPrescription_shouldReturnFailureResultIfDrugQuantityIsNull() {

		Patient patient = new Patient();
		Surgery surgery = new Surgery();

		OperationTheaterService otService = Mockito.mock(OperationTheaterService.class);
		when(otService.saveSurgery(any(Surgery.class))).thenReturn(null);

		//call method under test
		FragmentActionResult result = new PostTheaterDataFragmentController()
				.addTheaterDrugPrescription(new TestUiUtils(), patient, surgery,"2", "13579", null, "none", otService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("operationtheater.drug.invalidQuantity"));

	}


	/**
	 * @verifies throw IllegalArgumentException if surgery is null
	 * @see PostTheaterDataFragmentController#getSurgeryNote(UiUtils, Surgery)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getSurgeryNote_shouldThrowIllegalArgumentExceptionIfSurgeryIsNull() throws Exception {

		new PostTheaterDataFragmentController().getSurgeryNote(new TestUiUtils(), null);
	}


	/**
	 * @verifies throw IllegalArgumentException if patient is null
	 * @see PostTheaterDataFragmentController#addOrUpdateSurgeryNote(UiUtils, Patient, Surgery, String, OperationTheaterService)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void addOrUpdateSurgeryNote_shouldThrowIllegalArgumentExceptionIfPatientIsNull() throws Exception {

		Surgery surgery = new Surgery();

		OperationTheaterService otService = Mockito.mock(OperationTheaterService.class);
		when(otService.saveSurgery(any(Surgery.class))).thenReturn(null);

		new PostTheaterDataFragmentController().addOrUpdateSurgeryNote(new TestUiUtils(), null, surgery, "test note", otService);

	}


	/**
	 * @verifies throw IllegalArgumentException if surgery is null
	 * @see PostTheaterDataFragmentController#addOrUpdateSurgeryNote(UiUtils, Patient, Surgery, String, OperationTheaterService)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void addOrUpdateSurgeryNote_shouldThrowIllegalArgumentExceptionIfSurgeryIsNull() throws Exception {

		Patient patient = new Patient();

		OperationTheaterService otService = Mockito.mock(OperationTheaterService.class);
		when(otService.saveSurgery(any(Surgery.class))).thenReturn(null);

		new PostTheaterDataFragmentController().addOrUpdateSurgeryNote(new TestUiUtils(), patient, null, "test note", otService);

	}

	/**
	 * verifies return failure result if drug quantity is null.
	 * @throws Exception
	 */
	@Test
	public void addOrUpdateSurgeryNote_shouldReturnFailureResultIfSurgeryNoteIsNull() {

		Patient patient = new Patient();
		Surgery surgery = new Surgery();

		OperationTheaterService otService = Mockito.mock(OperationTheaterService.class);
		when(otService.saveSurgery(any(Surgery.class))).thenReturn(null);

		//call method under test
		FragmentActionResult result = new PostTheaterDataFragmentController()
				.addOrUpdateSurgeryNote(new TestUiUtils(), patient, surgery,null, otService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("Invalid surgery note."));

	}


	/**
	 * verifies return failure result if drug quantity is empty.
	 * @throws Exception
	 */
	@Test
	public void addOrUpdateSurgeryNote_shouldReturnFailureResultIfSurgeryNoteIsEmpty(){

		Patient patient = new Patient();
		Surgery surgery = new Surgery();

		OperationTheaterService otService = Mockito.mock(OperationTheaterService.class);
		when(otService.saveSurgery(any(Surgery.class))).thenReturn(null);

		//call method under test
		FragmentActionResult result = new PostTheaterDataFragmentController()
				.addOrUpdateSurgeryNote(new TestUiUtils(), patient, surgery,"", otService);

		//verify
		assertThat(result, instanceOf(FailureResult.class));
		assertThat(((FailureResult) result).getSingleError(), is("Invalid surgery note."));

	}



}
