/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.operationtheater;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.operationtheater.api.OperationTheaterService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;


/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
public class OperationTheaterModuleActivator implements ModuleActivator, DaemonTokenAware {

    public static DaemonToken DAEMON_TOKEN;

    protected Log log = LogFactory.getLog(getClass());

    private IdentifierSourceService idService;

    /**
     * @see ModuleActivator#willRefreshContext()
     */
    public void willRefreshContext() {
        log.info("Refreshing Operation Theater Module");
    }

    /**
     * @see ModuleActivator#contextRefreshed()
     */
    public void contextRefreshed() {
        log.info("Operation Theater Module refreshed");
    }

    /**
     * @see ModuleActivator#willStart()
     */
    public void willStart() {
        log.info("Starting Operation Theater Module");
    }

    /**
     * @should create emergency procedure and patient placeholder
     * @see ModuleActivator#started()
     */
    public void started() {
        log.info("Operation Theater Module started");

        OperationTheaterService otService = Context.getService(OperationTheaterService.class);
        PatientService patientService = Context.getPatientService();
        LocationService locationService = Context.getLocationService();
        ConceptService conceptService = Context.getConceptService();
        if (idService == null) {
            idService = Context.getService(IdentifierSourceService.class);
        }

        setUpEmergencyPlaceholders(otService, patientService, locationService);
        setupInitialConcepts(conceptService);
    }

    /**
     * @see ModuleActivator#willStop()
     */
    public void willStop() {
        log.info("Stopping Operation Theater Module");
    }

    /**
     * @see ModuleActivator#stopped()
     */
    public void stopped() {
        log.info("Operation Theater Module stopped");
    }

    @Override
    public void setDaemonToken(DaemonToken token) {
        this.DAEMON_TOKEN = token;
    }

    private void setUpEmergencyPlaceholders(OperationTheaterService otService,
                                            PatientService patientService,
                                            LocationService locationService) {

        //placeholder procedure
        if (otService.getProcedureByUuid(OTMetadata.PLACEHOLDER_PROCEDURE_UUID) == null) {
            Procedure procedure = getEmergencyProcedure();
            otService.saveProcedure(procedure);
        }

        //placeholder patient
        if (patientService.getPatientByUuid(OTMetadata.PLACEHOLDER_PATIENT_UUID) == null) {
            PatientIdentifierType patientIdentifierType = patientService
                    .getPatientIdentifierTypeByName(OTMetadata.OPENMRS_ID_NAME);
            Patient patient = getEmergencyPatient(patientIdentifierType, locationService);
            patientService.savePatient(patient);
        }

    }

    private Patient getEmergencyPatient(PatientIdentifierType patientIdentifierType, LocationService locationService) {
        Patient patient = new Patient();
        patient.setUuid(OTMetadata.PLACEHOLDER_PATIENT_UUID);

        PersonName pName = new PersonName();
        String gender = "M";
        boolean male = gender.equals("M");
        pName.setGivenName("EMERGENCY");
        pName.setFamilyName("PLACEHOLDER PATIENT");
        patient.addName(pName);

        patient.setBirthdate(Date.from(LocalDate.of(1970,1,1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        patient.setBirthdateEstimated(false);
        patient.setGender(gender);

        PatientIdentifier pa1 = new PatientIdentifier();
        pa1.setIdentifier(idService.generateIdentifier(patientIdentifierType, "EmergencyData"));
        pa1.setIdentifierType(patientIdentifierType);
        pa1.setDateCreated(new Date());
        pa1.setLocation(locationService.getLocation(1));
        patient.addIdentifier(pa1);

        return patient;
    }

    private Procedure getEmergencyProcedure() {
        Procedure procedure = new Procedure();
        procedure.setUuid(OTMetadata.PLACEHOLDER_PROCEDURE_UUID);
        procedure.setName("EMERGENCY Placeholder");
        procedure.setDescription("This procedure is used as placeholder for emergencies");
        procedure.setOtPreparationDuration(10);
        procedure.setInterventionDuration(50);
        procedure.setInpatientStay(1);
        return procedure;
    }


//	setup concepts that are required to save data
//	Using concepts from CIEL

    private void setupInitialConcepts(ConceptService conceptService){

        /**
         *  Concept definitions for
         *  Past procedure data collection
         *  From CIEL
         *  See https://www.openconceptlab.org/orgs/CIEL/sources/CIEL/concepts/160714/details/
         */

        {
            {
                Integer id = 1651;
                String uuid = "302e8046-b689-4324-920e-24d296eb821f";
                String name = "Procedure performed";
                Concept c = conceptService.getConcept(id);
                if ( c == null ) {
                    log.warn("Creating " + name);
                    c = new Concept();
                    c.setConceptId(id);
                    c.setUuid(uuid);
                    c.setConceptClass(conceptService.getConceptClassByName("Question"));
                    c.setDatatype(conceptService.getConceptDatatypeByName("N/A"));
                    c.setFullySpecifiedName(new ConceptName(name, Locale.ENGLISH));
                    conceptService.saveConcept(c);
                }
            }
        }


        {
            {
                Integer id = 160715;
                String uuid = "f1432a33-e7b3-48e3-951f-c5996b48c849";
                String name = "Procedure Date/Time";
                Concept c = conceptService.getConcept(id);
                if ( c == null ) {
                    log.warn("Creating " + name);
                    c = new Concept();
                    c.setConceptId(id);
                    c.setUuid(uuid);
                    c.setConceptClass(conceptService.getConceptClassByName("Question"));
                    c.setDatatype(conceptService.getConceptDatatypeByName("Date"));
                    c.setFullySpecifiedName(new ConceptName(name, Locale.ENGLISH));
                    conceptService.saveConcept(c);
                }
            }
        }


        {
            {
                Integer id = 160716;
                String uuid = "5259f178-ae0b-48fc-bda2-48bbf25cc387";
                String name = "Procedure Comment";
                Concept c = conceptService.getConcept(id);
                if ( c == null ) {
                    log.warn("Creating " + name);
                    c = new Concept();
                    c.setConceptId(id);
                    c.setUuid(uuid);
                    c.setConceptClass(conceptService.getConceptClassByName("Question"));
                    c.setDatatype(conceptService.getConceptDatatypeByName("Text"));
                    c.setFullySpecifiedName(new ConceptName(name, Locale.ENGLISH));
                    conceptService.saveConcept(c);
                }
            }
        }






        {
            /**
             * Procedure History
             * Grouping Concept Collecting past procedure data.
             * From CIEL
             * See https://www.openconceptlab.org/orgs/CIEL/sources/CIEL/concepts/160714/details/
             */

            Integer id = 160714;
            String uuid = "99b1ece8-53dc-424e-ac25-7168fadd3262";
            String name = "Past Procedure History";
            String fullySpecifiedName = "Procedure History";
            Concept c = conceptService.getConcept(id);

            if ( c == null ) {
                log.warn("Creating Concept " + name);
                c = new Concept();
                c.setConceptId(id);
                c.setUuid(uuid);
                c.setConceptClass(conceptService.getConceptClassByName("ConvSet"));
                c.setDatatype(conceptService.getConceptDatatypeByName("N/A"));
                c.setFullySpecifiedName(new ConceptName(fullySpecifiedName, Locale.ENGLISH));
                c.setSet(true);
                c.getConceptSets().clear();
                c.addSetMember(conceptService.getConcept(1651));
                c.addSetMember(conceptService.getConcept(160715));
                c.addSetMember(conceptService.getConcept(160716));
                conceptService.saveConcept(c);
            }
        }


        /**
         *  708b2463-dc5b-4124-8656-51fad151e12b
         *  8a6fa154-eff5-4c64-858f-0775efb18ecd
         *  5304beae-1ae6-4ceb-b2ac-c0c02ac18c6a
         *  b6563116-73aa-459b-b4f8-48a908c859ca
         *  75d7b79f-f74a-4dd8-ba43-52bf34129b30
         *  663453fe-a9d5-46db-b0fd-c324a49a79ce
         *  b9738d2e-6a29-4b58-8260-aa20b2be3df3
         *  9ccdfaaf-1c1e-407b-bbd7-ebc6c5a9ddeb
         */



        {
            {
                Integer id = 200004;
                String uuid = "708b2463-dc5b-4124-8656-51fad151e12b";
                String name = "Post-theater prescriptions";
                String fullySpecifiedName = "Post-theater prescriptions";
                Concept c = conceptService.getConcept(id);
                if ( c == null ) {
                    log.warn("Creating " + name);
                    c = new Concept();
                    c.setConceptId(id);
                    c.setUuid(uuid);
                    c.setConceptClass(conceptService.getConceptClassByName("ConvSet"));
                    c.setDatatype(conceptService.getConceptDatatypeByName("N/A"));
                    c.setFullySpecifiedName(new ConceptName(fullySpecifiedName, Locale.ENGLISH));
                    c.setSet(true);
                    conceptService.saveConcept(c);
                }
            }
        }


        {
            {
                Integer id = 200003;
                String uuid = "8a6fa154-eff5-4c64-858f-0775efb18ecd";
                String name = "In-theater prescriptions";
                String fullySpecifiedName = "In-theater prescriptions";
                Concept c = conceptService.getConcept(id);
                if ( c == null ) {
                    log.warn("Creating " + name);
                    c = new Concept();
                    c.setConceptId(id);
                    c.setUuid(uuid);
                    c.setConceptClass(conceptService.getConceptClassByName("ConvSet"));
                    c.setDatatype(conceptService.getConceptDatatypeByName("N/A"));
                    c.setFullySpecifiedName(new ConceptName(fullySpecifiedName, Locale.ENGLISH));
                    c.setSet(true);
                    conceptService.saveConcept(c);
                }
            }
        }


        {
            {
                Integer id = 200002;
                String uuid = "5304beae-1ae6-4ceb-b2ac-c0c02ac18c6a";
                String name = "Pre-theater prescriptions";
                String fullySpecifiedName = "Pre-theater prescriptions";
                Concept c = conceptService.getConcept(id);
                if ( c == null ) {
                    log.warn("Creating " + name);
                    c = new Concept();
                    c.setConceptId(id);
                    c.setUuid(uuid);
                    c.setConceptClass(conceptService.getConceptClassByName("ConvSet"));
                    c.setDatatype(conceptService.getConceptDatatypeByName("N/A"));
                    c.setFullySpecifiedName(new ConceptName(fullySpecifiedName, Locale.ENGLISH));
                    c.setSet(true);
                    conceptService.saveConcept(c);
                }
            }
        }


        {
            {
                Integer id = 200001;
                String uuid = "75d7b79f-f74a-4dd8-ba43-52bf34129b30";
                String name = "Procedure information";
                String fullySpecifiedName = "Procedure information";
                Concept c = conceptService.getConcept(id);
                if ( c == null ) {
                    log.warn("Creating " + name);
                    c = new Concept();
                    c.setConceptId(id);
                    c.setUuid(uuid);
                    c.setConceptClass(conceptService.getConceptClassByName("ConvSet"));
                    c.setDatatype(conceptService.getConceptDatatypeByName("N/A"));
                    c.setFullySpecifiedName(new ConceptName(fullySpecifiedName, Locale.ENGLISH));
                    c.setSet(true);
                    c.getConceptSets().clear();
                    c.addSetMember(conceptService.getConcept(200004));
                    c.addSetMember(conceptService.getConcept(200003));
                    c.addSetMember(conceptService.getConcept(200002));
                    conceptService.saveConcept(c);
                }
            }
        }






    }


}
