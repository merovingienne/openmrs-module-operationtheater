package org.openmrs.module.operationtheater.page.controller;

import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.event.ApplicationEventService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public class PostTheaterDataPageController {




    public Object controller(PageModel model,
                             UiUtils ui,
                             @RequestParam(value = "patient") Patient patient,
                             @RequestParam(value = "surgeryId", required = false) Surgery surgery,
                             @RequestParam(value = "returnUrl", required = false) String returnUrl,
                             @SpringBean OperationTheaterService otService,
                             @InjectBeans PatientDomainWrapper patientDomainWrapper,
                             @SpringBean("adtService") AdtService adtService,
                             @SpringBean("applicationEventService") ApplicationEventService applicationEventService,
                             UiSessionContext sessionContext) {

        model.addAttribute("surgery", surgery);

        model.addAttribute("returnUrl", returnUrl);

        patientDomainWrapper.setPatient(patient);
        model.addAttribute("patient", patientDomainWrapper);

        List<Concept> drugList = Context.getConceptService().getConceptsByClass(
                Context.getConceptService().getConceptClass(3));
        model.addAttribute("drugList", drugList);

        AppContextModel contextModel = sessionContext.generateAppContextModel();
        contextModel.put("patientId", patient.getId());



        return null;
    }
}
