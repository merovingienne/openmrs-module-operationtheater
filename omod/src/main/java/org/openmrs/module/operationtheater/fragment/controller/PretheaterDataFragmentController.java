package org.openmrs.module.operationtheater.fragment.controller;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.operationtheater.PastProcedure;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.Time;
import org.openmrs.module.operationtheater.Util.SurgeryObsUtil;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.openmrs.module.operationtheater.Drug;


/**
 * Controller class for handling data in
 * pretheater data collection page.
 */


public class PretheaterDataFragmentController {

    private final Time time = new Time();

    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    protected final Log log = LogFactory.getLog(getClass());


    /**
     * Method to fetch past procedures of a patient.
     * @param UI
     * @param patient required
     * @return JSON objects with past procedure data.
     * @see omod/src/main/webapp/resources/scripts/preTheaterForm_page/surgeryNote.js
     */

    public List<SimpleObject> getPastProcedures(UiUtils UI,
                                                @RequestParam("patient") Patient patient
    ){

        if (patient == null){
            throw new IllegalArgumentException("Patient doesn't exist");
        }

        List<PastProcedure> pastProceduresObsGroup = PastProcedure.getAllPastProcedures(patient);


        return SimpleObject.fromCollection(pastProceduresObsGroup, UI,"name", "date", "comment"  );

    }

    /**
     * Add new past procedure record to the patient as obs.
     * @param ui
     * @param patient
     * @param pastProcedureName
     * @param pastProcedureDate
     * @param pastProcedureComment
     * @see org.openmrs.module.operationtheater.OperationTheaterModuleActivator#setupInitialConcepts(ConceptService)
     */


    public FragmentActionResult addPastProcedureRecord(UiUtils ui,
                                                       @RequestParam("patient")  Patient patient,
                                                       @RequestParam("pastProcedureName") String pastProcedureName,
                                                       @RequestParam("pastProcedureDate") String pastProcedureDate,
                                                       @RequestParam("pastProcedureComment") String pastProcedureComment
    ){



        if (patient == null){
            return new FailureResult(ui.message("operationtheater.patient.notFound"));
        }

        if (pastProcedureName == null){
            return new FailureResult(ui.message("operationtheater.pastProcedure.invalidName"));
        }


        ConceptService conceptService = Context.getConceptService();
        ObsService obsService = Context.getObsService();

        /**
         * Procedure history grouping obs.
         *
         */
        Obs parentObs = new Obs();
        parentObs.setConcept(conceptService.getConcept(160714));
        parentObs.setObsDatetime(new Date());
        parentObs.setPerson(patient);
        parentObs.setLocation(new Location(1));
        parentObs.setCreator(Context.getAuthenticatedUser());


        Obs procedurePerformed = new Obs();
        procedurePerformed.setConcept(conceptService.getConcept(1651));
        procedurePerformed.setValueText(pastProcedureName);
        procedurePerformed.setObsDatetime(new Date());
        procedurePerformed.setPerson(patient);
        procedurePerformed.setLocation(new Location(1));


        Obs procedureDate = new Obs();
        procedureDate.setConcept(conceptService.getConcept(160715));


        try {
            procedureDate.setValueDate(formatter.parse(pastProcedureDate));
        } catch (ParseException e) {
            return new FailureResult(ui.message("operationtheater.pastProcedure.invalidDate"));
        }


        procedureDate.setObsDatetime(new Date());
        procedureDate.setPerson(patient);
        procedureDate.setLocation(new Location(1));


        Obs procedureComment = new Obs();
        procedureComment.setConcept(conceptService.getConcept(160716));
        procedureComment.setValueText(pastProcedureComment);
        procedureComment.setObsDatetime(new Date());
        procedureComment.setPerson(patient);
        procedureComment.setLocation(new Location(1));


        parentObs.addGroupMember(procedurePerformed);
        parentObs.addGroupMember(procedureDate);
        parentObs.addGroupMember(procedureComment);


        obsService.saveObs(parentObs, null);

        Obs savedObs = obsService.getObs(parentObs.getObsId());

        if (savedObs.getId() == null || savedObs.getConcept().getConceptId() != parentObs.getConcept().getConceptId()){
            return new FailureResult(ui.message("operationtheater.pastProcedure.failedToAdd"));
        }

        return new SuccessResult(ui.message("operationtheater.pastProcedure.successfullyAdded"));

    }


    /**
     * Return all pre-theater drugs prescribed for this surgery.
     * @param UI
     * @param surgery
     * @return JSON objects of pre-theater drugs.
     */


    public List<SimpleObject> getPreTheaterDrugs(UiUtils UI, @RequestParam("surgery") Surgery surgery){

        if (surgery == null){
            throw new IllegalArgumentException("Surgery doesn't exist");
        }

        List<Drug> drugList = Drug.getAllDrugs(surgery, 1);

        if (drugList == null){
            drugList = Collections.emptyList();
            List<SimpleObject> emptyList = Collections.emptyList();
            return emptyList;
        }


        return SimpleObject.fromCollection(drugList, UI,"name", "quantity", "notes"  );

    }


    /**
     * Add new pre-theater drug prescription as obs.
     * @param ui
     * @param patient
     * @param surgery
     * @param drugConceptId
     * @param drugQuantity
     * @param prescriptionNotes
     * @param otService
     * @return
     */


    public FragmentActionResult addPretheaterDrugPrescription(UiUtils ui,
                                                              @RequestParam("patient")  Patient patient,
                                                              @RequestParam("surgery") Surgery surgery,
                                                              @RequestParam("drugConceptId") String drugConceptId,
                                                              @RequestParam("drugQuantity") String drugQuantity,
                                                              @RequestParam("prescriptionNotes") String prescriptionNotes,
                                                              @SpringBean OperationTheaterService otService)
    {


        if (patient == null){
            return new FailureResult(ui.message("operationtheater.patient.notFound"));
        }

        if (drugConceptId == null || drugConceptId.length() == 0){
            return new FailureResult(ui.message("Drug not found."));
        }

        if (drugQuantity == null || drugQuantity.length() == 0){
            return new FailureResult(ui.message("Invalid drug quantity"));
        }


        ConceptService conceptService = Context.getConceptService();
        ObsService obsService = Context.getObsService();


        Concept drug = conceptService.getConcept(Integer.parseInt(drugConceptId));

        Obs surgeryInfoObs = SurgeryObsUtil.getSurgeryObs(surgery, patient);

        Obs preTheaterDrug = new Obs();
        preTheaterDrug.setConcept(drug);
        preTheaterDrug.setPerson(patient);
        preTheaterDrug.setObsDatetime(new Date());
        preTheaterDrug.setValueNumeric(Double.parseDouble(drugQuantity));
        preTheaterDrug.setLocation(new Location(1));
        preTheaterDrug.setComment(prescriptionNotes);


        boolean hasPreTheaterParent = false;
        Obs preTheaterDrugsObsGroup = null;



        if (surgeryInfoObs.hasGroupMembers()){

            Set<Obs> memberObs = surgeryInfoObs.getGroupMembers();

            for (Obs ob: memberObs){
                if (ob.getConcept().getConceptId() == 200002){
                    preTheaterDrugsObsGroup = ob;
                    hasPreTheaterParent = true;
                }
            }

            if (!hasPreTheaterParent){
                preTheaterDrugsObsGroup = createPretheaterDrugsParentObs(patient);
            }

        } else {
            preTheaterDrugsObsGroup = createPretheaterDrugsParentObs(patient);

        }

        preTheaterDrugsObsGroup.addGroupMember(preTheaterDrug);

        if (!hasPreTheaterParent){
            surgeryInfoObs.addGroupMember(preTheaterDrugsObsGroup);
        }


        try {
            obsService.saveObs(surgeryInfoObs, "added pre-theater drug");
            surgery.setSurgeryObsGroup(surgeryInfoObs);
            otService.saveSurgery(surgery);
        } catch (Exception e){
            System.out.println(e.getMessage());
            return new FailureResult(ui.message("Failed to add drug prescription"));
        }


        log.info("Pre-theater prescription successfully added.");

        return new SuccessResult(ui.message("Drug prescription successfully added."));
    }


    /**
     * Create pre-theater drugs obs group if null.
     * @param patient
     * @return
     */


    public Obs createPretheaterDrugsParentObs(Patient patient){


        log.warn("Doesn't have pre-theater drugs parent.");

        Obs preTheaterDrugsObsGroup = new Obs();
        preTheaterDrugsObsGroup.setConcept(Context.getConceptService().getConcept(200002));
        preTheaterDrugsObsGroup.setPerson(patient);
        preTheaterDrugsObsGroup.setObsDatetime(new Date());
        preTheaterDrugsObsGroup.setLocation(new Location(1));

        return  preTheaterDrugsObsGroup;

    }

}
