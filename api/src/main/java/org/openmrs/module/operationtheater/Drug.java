package org.openmrs.module.operationtheater;


import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Drug {



    private String name, quantity, notes;


    public Drug(String name, String quantity, String notes){
        setName(name);
        setQuantity(quantity);
        setNotes(notes);
    }


    public static List<Drug> getAllDrugs(Surgery surgery, int workflowPos){

        ObsService obsService = Context.getObsService();
        ConceptService conceptService = Context.getConceptService();


        Obs procedureInfo = surgery.getSurgeryObsGroup();

        if (procedureInfo != null) {


            int conceptId = -1;

            switch (workflowPos) {
                case 1:
                    conceptId = 200002;
                    break;
                case 2:
                    conceptId = 200003;
                    break;
                case 3:
                    conceptId = 200004;
            }

            Set<Obs> drugSet = Collections.emptySet();


            List<Drug> drugs = new ArrayList<Drug>();

            for (Obs groupMember : procedureInfo.getGroupMembers()) {
                if (groupMember.getConcept().getConceptId() == conceptId) {
                    drugSet = groupMember.getGroupMembers();
                    break;
                }
            }


            for (Obs drug : drugSet) {
                drugs.add(new Drug(drug.getConcept().getName().toString(),
                        drug.getValueNumeric().toString(),
                        drug.getComment()));
            }


            return drugs;

        }

        return null;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }



}
