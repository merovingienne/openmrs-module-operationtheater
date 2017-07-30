package org.openmrs.module.operationtheater;

import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PastProcedure {


    private String name;
    private String date;
    private String comment;

    private SimpleDateFormat outputFormatter = new SimpleDateFormat("yyyy-MM-dd");


    private PastProcedure(Obs pastProcedure){
        Set<Obs> pastProcedureObs = pastProcedure.getGroupMembers();

        for (Obs ob: pastProcedureObs){
            switch (ob.getConcept().getConceptId()){
                case (1651):
                    setName(ob.getValueAsString(Locale.ENGLISH));
                    break;
                case (160715):
                    setDate(outputFormatter.format(ob.getValueDate()));
                    break;
                case (160716):
                    setComment(ob.getValueText());
            }
        }

    }

    public static List<PastProcedure> getAllPastProcedures(Patient patient){

        ObsService obsService = Context.getObsService();
        ConceptService conceptService = Context.getConceptService();


        List<Obs> pastProcedureGroupObs = obsService.getObservationsByPersonAndConcept(patient, conceptService.getConcept(160714));

        List<PastProcedure> pastProcedures = new ArrayList<PastProcedure>();

        for (Obs pastProcedure : pastProcedureGroupObs){
            pastProcedures.add(new PastProcedure(pastProcedure));
        }


        return pastProcedures;


    }


    public static PastProcedure getPastProcedure(Obs obs){

        return new PastProcedure(obs);
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
