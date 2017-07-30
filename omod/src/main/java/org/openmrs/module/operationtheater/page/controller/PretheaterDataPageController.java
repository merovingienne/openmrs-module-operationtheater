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
package org.openmrs.module.operationtheater.page.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.event.ApplicationEventService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.module.operationtheater.OTMetadata;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public class PretheaterDataPageController {

	protected final Log log = LogFactory.getLog(getClass());

	public Object controller(PageModel model,
	                         UiUtils ui,
	                         @RequestParam(value = "patient") Patient patient,
	                         @RequestParam(value = "surgeryId") Surgery surgery,
	                         @RequestParam(value = "returnUrl", required = false) String returnUrl,
	                         @SpringBean OperationTheaterService otService,
	                         @SpringBean("providerService") ProviderService providerService,
	                         @SpringBean("patientService") PatientService patientService,
	                         @InjectBeans PatientDomainWrapper patientDomainWrapper,
	                         @SpringBean("adtService") AdtService adtService,
	                         @SpringBean("applicationEventService") ApplicationEventService applicationEventService,
	                         UiSessionContext sessionContext) {

		if (patient.getVoided() || patient.getPersonVoided()) {
			return new Redirect("coreapps", "patientdashboard/deletedPatient", "patientId=" + patient.getId());
		}


		model.addAttribute("surgery", surgery);

		model.addAttribute("returnUrl", returnUrl);

		patientDomainWrapper.setPatient(patient);
		model.addAttribute("patient", patientDomainWrapper);

		List<Procedure> procedureList = otService.getAllProcedures(false);
		model.addAttribute("procedureList", procedureList);

		List<Concept> drugList = Context.getConceptService().getConceptsByClass(
				Context.getConceptService().getConceptClass(3));
		model.addAttribute("drugList", drugList);

		AppContextModel contextModel = sessionContext.generateAppContextModel();
		contextModel.put("patientId", patient.getId());

		Allergies allergies = patientService.getAllergies(patient);

		model.addAttribute( "allergies", allergies);

		return null;
	}
}
