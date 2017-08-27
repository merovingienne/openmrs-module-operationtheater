package org.openmrs.module.operationtheater.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.operationtheater.Drug;
import org.openmrs.module.operationtheater.PastProcedure;
import org.openmrs.module.operationtheater.Surgery;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class PostTheaterDataFragmentController {

    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    protected final Log log = LogFactory.getLog(getClass());

    /**
     * Get the in-theater and post-theater drugs.
     * @param UI
     * @param surgery
     * @param workflowPos
     * @return
     */

    public List<SimpleObject> getTheaterDrugs(UiUtils UI,
                                              @RequestParam("surgery") Surgery surgery,
                                              @RequestParam("workflowPos") String workflowPos){

        if (surgery == null){
            throw new IllegalArgumentException("Surgery doesn't exist");
        }

        if (workflowPos == null || workflowPos.length() == 0){
            throw new IllegalArgumentException("Invalid workflow stage");
        }

        int workflowPosition;

        try {
            workflowPosition = Integer.parseInt(workflowPos);

            if (workflowPosition != 2 && workflowPosition != 3){
                throw new IllegalArgumentException("Invalid workflow stage");
            }

        } catch (NumberFormatException e){
            throw new IllegalArgumentException("Invalid workflow stage");
        }

        List<Drug> drugList = Drug.getAllDrugs(surgery, workflowPosition);

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


    public FragmentActionResult addTheaterDrugPrescription(UiUtils ui,
                                                           @RequestParam("patient") Patient patient,
                                                           @RequestParam("surgery") Surgery surgery,
                                                           @RequestParam("workflowPos") String workflowPos,
                                                           @RequestParam("drugConceptId") String drugConceptId,
                                                           @RequestParam("drugQuantity") String drugQuantity,
                                                           @RequestParam("prescriptionNotes") String prescriptionNotes,
                                                           @SpringBean OperationTheaterService otService)
    {


        if (patient == null){
            return new FailureResult(ui.message("operationtheater.patient.notFound"));
        }

        if (surgery == null){
            return new FailureResult(ui.message("operationtheater.surgery.notFound"));
        }

        if (!workflowPos.equalsIgnoreCase("2") && !workflowPos.equalsIgnoreCase("3")){
            return new FailureResult(ui.message("Invalid workflow position."));
        }

        if (drugConceptId == null || drugConceptId.length() == 0 || drugConceptId == "undefined"){
            return new FailureResult(ui.message("operationtheater.drug.invalidDrug"));
        }

        if (drugQuantity == null || drugQuantity.length() == 0){
            return new FailureResult(ui.message("operationtheater.drug.invalidQuantity"));
        }

        if (prescriptionNotes == null || prescriptionNotes.length() == 0){
            return new FailureResult(ui.message("operationtheater.drug.invalidNote"));
        }

        ConceptService conceptService = Context.getConceptService();
        ObsService obsService = Context.getObsService();

        Concept drug = null;


        try {
            drug = conceptService.getConcept(Integer.parseInt(drugConceptId));
        } catch (NumberFormatException e) {
            return new FailureResult(ui.message("Invalid drug."));
        }

        Obs surgeryInfoObs = SurgeryObsUtil.getSurgeryObs(surgery, patient);


        Obs xTheaterDrug = new Obs();
        xTheaterDrug.setConcept(drug);
        xTheaterDrug.setPerson(patient);
        xTheaterDrug.setObsDatetime(new Date());
        xTheaterDrug.setValueNumeric(Double.parseDouble(drugQuantity));
        xTheaterDrug.setLocation(new Location(1));
        xTheaterDrug.setComment(prescriptionNotes);


        boolean hasXTheaterParent = false;
        Obs xTheaterDrugsObsGroup = null;
        int parentConceptId = (Integer.parseInt(workflowPos) == 2 ? 200003 : 200004);

        if (surgeryInfoObs.hasGroupMembers()){

            Set<Obs> memberObs = surgeryInfoObs.getGroupMembers();

            for (Obs ob: memberObs){
                if (ob.getConcept().getConceptId() == parentConceptId){
                    xTheaterDrugsObsGroup = ob;
                    hasXTheaterParent = true;
                }
            }

            if (!hasXTheaterParent){
                xTheaterDrugsObsGroup = createXTheaterDrugsParentObs(patient, parentConceptId);
            }

        } else {
            xTheaterDrugsObsGroup = createXTheaterDrugsParentObs(patient, parentConceptId);

        }

        xTheaterDrugsObsGroup.addGroupMember(xTheaterDrug);

        if (!hasXTheaterParent){
            surgeryInfoObs.addGroupMember(xTheaterDrugsObsGroup);
        }


        try {
            obsService.saveObs(surgeryInfoObs, "added theater drug");
            surgery.setSurgeryObsGroup(surgeryInfoObs);
            otService.saveSurgery(surgery);
        } catch (Exception e){
            System.out.println(e.getMessage());
            return new FailureResult(ui.message("Failed to add drug prescription."));
        }


        log.info("post-theater prescription successfully added.");

        return new SuccessResult(ui.message("Drug prescription successfully added."));
    }



    public FragmentActionResult addOrUpdateSurgeryNote(UiUtils ui,
                                                       @RequestParam("patient") Patient patient,
                                                       @RequestParam("surgery") Surgery surgery,
                                                       @RequestParam("surgeryNote") String surgeryNote,
                                                       @SpringBean OperationTheaterService otService
    )
    {

        if (patient == null){
            throw new IllegalArgumentException("Patient not found");
        }

        if (surgery == null){
            throw new IllegalArgumentException("Surgery not found");
        }


        if (surgeryNote == null || surgeryNote.length() == 0){
            return new FailureResult("Invalid surgery note.");
        }

        ConceptService conceptService = Context.getConceptService();
        ObsService obsService = Context.getObsService();

        /*
         *  Check if surgery note already exists.
         */
        Obs procedureInfoObs = SurgeryObsUtil.getSurgeryObs(surgery, patient);
        Obs parentObs = null;
        boolean hasParent = false;
        Obs procedureDate = null;
        Obs procedureComment = null;

        if (procedureInfoObs.hasGroupMembers()){
            for (Obs ob : procedureInfoObs.getGroupMembers()){
                if (ob.getConcept().getConceptId() == 160714){
                    parentObs = ob;
                    hasParent = true;
                    break;
                }
            }
        }

        if (hasParent && parentObs!=null){
            for (Obs ob : parentObs.getGroupMembers()){
                if (ob.getConcept().getConceptId() == 160715 ){
                    procedureDate = ob;
                }
                if (ob.getConcept().getConceptId() == 160716 ){
                    procedureComment = ob;
                }
            }

            procedureDate.setValueDate(new Date());
            procedureComment.setValueText(surgeryNote);

            obsService.saveObs(parentObs, "Updated surgery note");


            return new SuccessResult(ui.message("Successfully updated surgery note."));

        }

        /**
         * Procedure history grouping obs.
         * We use it to store the current surgery's
         * outcome, so as to use it as a past procedure
         * in the future.
         *
         */
        parentObs = new Obs();
        parentObs.setConcept(conceptService.getConcept(160714));
        parentObs.setObsDatetime(new Date());
        parentObs.setPerson(patient);
        parentObs.setLocation(new Location(1));
        parentObs.setCreator(Context.getAuthenticatedUser());


        Obs procedurePerformed = new Obs();
        procedurePerformed.setConcept(conceptService.getConcept(1651));
        procedurePerformed.setValueText(surgery.getProcedure().getName());
        procedurePerformed.setObsDatetime(new Date());
        procedurePerformed.setPerson(patient);
        procedurePerformed.setLocation(new Location(1));


        procedureDate = new Obs();
        procedureDate.setConcept(conceptService.getConcept(160715));
        procedureDate.setValueDate(new Date());



        procedureDate.setObsDatetime(new Date());
        procedureDate.setPerson(patient);
        procedureDate.setLocation(new Location(1));


        procedureComment = new Obs();
        procedureComment.setConcept(conceptService.getConcept(160716));
        procedureComment.setValueText(surgeryNote);
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



        procedureInfoObs.addGroupMember(savedObs);

        obsService.saveObs(procedureInfoObs, "added surgery note");
        surgery.setSurgeryObsGroup(procedureInfoObs);
        otService.saveSurgery(surgery);

        return new SuccessResult(ui.message("Successfully added surgery note."));
    }


    public SimpleObject getSurgeryNote(UiUtils UI,
                                       @RequestParam("surgery") Surgery surgery){


        if (surgery == null){
            throw new IllegalArgumentException("Surgery does not exist");
        }

        ObsService obsService = Context.getObsService();

        Obs procedureObs = surgery.getSurgeryObsGroup();

        Obs procedureNoteObs = null;

        if (procedureObs != null){
            for (Obs groupMember : procedureObs.getGroupMembers()) {
                if (groupMember.getConcept().getConceptId() == 160714) {
                    procedureNoteObs = groupMember;
                    break;
                }
            }
        }

        if (procedureNoteObs != null){
            PastProcedure procedureNote = PastProcedure.getPastProcedure(procedureNoteObs);
            return SimpleObject.fromObject(procedureNote, UI, "name", "date", "comment");
        }

        return null;

    }



    /**
     * Helper Method
     * @param patient
     * @param conceptId
     * @return
     */

    public Obs createXTheaterDrugsParentObs(Patient patient, int conceptId){

        String workflowPos;

        if (conceptId == 200003){
            workflowPos = "in";
        }else {
            workflowPos = "post";
        }


        log.info("Doesn't have " + workflowPos + "-theater drugs parent.");

        Obs xTheaterDrugsObsGroup = new Obs();
        xTheaterDrugsObsGroup.setConcept(Context.getConceptService().getConcept(conceptId));
        xTheaterDrugsObsGroup.setPerson(patient);
        xTheaterDrugsObsGroup.setObsDatetime(new Date());
        xTheaterDrugsObsGroup.setLocation(new Location(1));

        return  xTheaterDrugsObsGroup;

    }
}
