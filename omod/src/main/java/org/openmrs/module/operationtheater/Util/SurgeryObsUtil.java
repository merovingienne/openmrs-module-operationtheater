package org.openmrs.module.operationtheater.Util;


import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.operationtheater.Surgery;

import java.util.Date;

public class SurgeryObsUtil {

    /**
     * Helper method to get the obs group for recording procedure information.
     * @param surgery
     * @param patient
     * @return
     */
    public static Obs getSurgeryObs(Surgery surgery, Patient patient){
        Obs surgeryInfoObs = surgery.getSurgeryObsGroup();

        if (surgeryInfoObs == null) {
            System.out.println("INFO: Surgery info obs group null.");
            surgeryInfoObs = new Obs();
            surgeryInfoObs.setPerson(patient);
            surgeryInfoObs.setConcept(Context.getConceptService().getConcept(200001));
            surgeryInfoObs.setObsDatetime(new Date());
            surgeryInfoObs.setLocation(new Location(1));
        } else {
            System.out.println("INFO: Surgery info obs group exists.");
        }


        return surgeryInfoObs;
    }
}
