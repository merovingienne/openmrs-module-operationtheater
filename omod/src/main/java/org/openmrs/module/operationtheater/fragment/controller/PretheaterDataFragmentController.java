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
     * @see omod/src/main/webapp/resources/scripts/preTheaterForm_page/pastProcedures.js
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
    

}
