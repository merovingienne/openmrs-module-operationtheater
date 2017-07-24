<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeJavascript("operationtheater", "surgery_page/surgery.js")
    ui.includeJavascript("operationtheater", "surgery_page/surgicalTeam.js")
    ui.includeJavascript("operationtheater", "surgery_page/workflow.js")

    ui.includeJavascript("uicommons", "emr.js")
    ui.includeJavascript("uicommons", "typeahead.js");
    ui.includeJavascript("uicommons", "datatables/jquery.dataTables.min.js")
    ui.includeJavascript("uicommons", "moment.min.js")
    ui.includeJavascript("operationtheater", "bower_components/validation/jquery.validate.js")
    ui.includeJavascript("operationtheater", "patientSearchWidget.js")

    ui.includeCss("referenceapplication", "referenceapplication.css")
    ui.includeCss("coreapps", "findpatient/findPatient.css")
    ui.includeCss("uicommons", "datatables/dataTables_jui.css")
%>


<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.format(patient.patient.familyName) }, ${ ui.format(patient.patient.givenName) }",
            link: '${ui.pageLink("coreapps", "clinicianfacing/patient", [patientId: patient.id])}'},
        { label: "${ui.message("operationtheater.patientsSurgeries.breadcrumbLabel")}",
            link: '${ui.pageLink("operationtheater", "patientsSurgeries", [patientId: patient.patient.id])}'},
        { label: "${surgery.procedure.name?surgery.procedure.name: ui.message("operationtheater.patientsSurgeries.button.new")}"}
    ]
    var patient = { id: ${ patient.id } };
</script>

<%=ui.includeFragment("appui", "messages", [codes: [
        "coreapps.delete",
        "general.save",
        "operationtheater.procedure.notFound",
        "operationtheater.provider.notFound",
        "uicommons.dataTable.emptyTable",
].flatten()
])%>

${ui.includeFragment("coreapps", "patientHeader", [patient: patient.patient, activeVisit: activeVisit])}


<!-- TODO create css with compass -->
<style type='text/css'>
input.error {
    border: 2px solid;
    border-color: #ff6666;
}

.icon-custom{

    display: inline-block;
    width: 1.25em;
    text-align: center;
}

.icon-custom:before{
    content: "\f07c";
}



form fieldset {
    min-width: 95%;
    padding: 10px;
}

</style>


<script type="text/javascript">

    jq().ready(function () {

        var procedureMap = {};
        <% procedureList.each{ procedure ->%>
        procedureMap['${procedure.name}'] = '${procedure.uuid}';
        <% } %>
        var options = { source: Object.keys(procedureMap) };
        jq('#surgeryProcedure-field').typeahead(options);
        var newSurgery = ${surgery.procedure.name == null};
        var patientId = ${patient.id}

            //procedure
            surgery.initProcedureButton("${surgery.uuid}", procedureMap, newSurgery, patientId);

        //validation
        //surgery.setUpValidation(options, providerOptions);

        //surgical team
        var providerMap = {};
        <% providerList.each{ provider ->%>
        providerMap['${provider.name}'] = '${provider.uuid}';
        <% } %>
        var providerOptions = { source: Object.keys(providerMap) };

        surgicalTeam.init("${surgery.uuid}", providerMap, providerOptions);
        surgicalTeam.get();

        //workflow
        workflow.init("${surgery.uuid}");


        var widgetConfig = {
            initialPatients: [],
            minSearchCharacters: 1,
            selectCallback: function (patientUuid) {
                console.log(patientUuid);
                var actionURL = emr.fragmentActionLink("operationtheater", "surgery", "replaceEmergencyPlaceholderPatient",
                    {"patient": patientUuid,
                        "surgery": "${surgery.uuid}"});

                jq.getJSON(actionURL, null)
                    .success(function (data) {
                        emr.successMessage(data.message);
                        jq('#replaceEmergencyPlaceholderPatient').hide();
                    })
                    .error(function (xhr, status, err) {
                        emr.handleError(xhr);
                    })
            },
            messages: {
                info: '${ ui.message("coreapps.search.info") }',
                first: '${ ui.message("coreapps.search.first") }',
                previous: '${ ui.message("coreapps.search.previous") }',
                next: '${ ui.message("coreapps.search.next") }',
                last: '${ ui.message("coreapps.search.last") }',
                noMatchesFound: '${ ui.message("coreapps.search.noMatchesFound") }',
                noData: '${ ui.message("coreapps.search.noData") }',
                recent: '${ ui.message("coreapps.search.label.recent") }',
                searchError: '${ ui.message("coreapps.search.error") }',
                identifierColHeader: '${ ui.message("coreapps.search.identifier") }',
                nameColHeader: '${ ui.message("coreapps.search.name") }',
                genderColHeader: '${ ui.message("coreapps.gender") }',
                ageColHeader: '${ ui.message("coreapps.age") }',
                birthdateColHeader: '${ ui.message("coreapps.birthdate") }'
            }
        };

        new PatientSearchWidget(widgetConfig);
    });
</script>

<h2>
    ${ui.message("operationtheater.surgery.page.title")}
</h2>

<form class="simple-form-ui" id="surgeryForm" autocomplete="off"

      xmlns:xlink="http://www.w3.org/1999/xlink"

>
    <div style="
    float: left;
    width: 70%;
    ">

        <fieldset id="replaceEmergencyPlaceholderPatient" ${emergencyPatient ? '' : 'style="display:none"'}>
            <legend>${ui.message("operationtheater.surgery.page.fieldset.replaceEmergencyPlaceholderPatient")}</legend>

            <input type="text" id="patient-search" placeholder="${ui.message("coreapps.findPatient.search.placeholder")}"
                   autocomplete="off"/>

            <div id="patient-search-results"></div>
        </fieldset>

        <fieldset>
            <legend>${ui.message("operationtheater.surgery.page.fieldset.procedure")}</legend>
            <!-- TODO maxlength should be managed centrally in the POJO-->
            ${ui.includeFragment("uicommons", "field/text", [
                    label        : ui.message("general.name"),
                    formFieldName: "surgeryProcedure",
                    id           : "surgeryProcedure",
                    maxLength    : 101,
                    initialValue : (surgery.procedure.name ?: '')
            ])}
            <a class="button"
               id="setProcedureButton">${surgery.procedure.name == null ? ui.message("operationtheater.surgery.page.button.createSurgery") : ui.message("general.save")}</a>

        </fieldset>

        <fieldset id="surgicalTeamFieldset" ${surgery.procedure.name ?: 'style="display:none"'}>
            <legend>${ui.message("operationtheater.surgery.page.fieldset.surgicalTeam")}</legend>

            <div id="surgical-team-list">
                <table id="surgical-team-table" empty-value-message='${ui.message("uicommons.dataTable.emptyTable")}'>
                    <thead>
                    <tr>
                        <th style="width: 80%">${ui.message("general.name")}</th>
                        <th style="width: 20%">${ui.message("general.action")}</th>
                    </tr>
                    </thead>
                    <tbody>

                    </tbody>
                </table>
            </div>

            <p>
                ${ui.includeFragment("uicommons", "field/text", [
                        label        : "Add Provider",
                        formFieldName: "addProviderTextfield",
                        id           : "addProviderTextfield",
                        maxLength    : 101,
                        initialValue : ""
                ])}
                <a class="button" id="addProviderButton">${ui.message("general.add")}</a>
            </p>

        </fieldset>



    </div>


    <div style="float: right;
    width: 30%;">

        <fieldset id="workflowFieldset" ${surgery.procedure.name ?: 'style="display:none"'}>

            <legend>Data</legend>
        <div class="action-section">
            <h3>General Actions</h3>

            <ul class="float-left">


                <li class="float-left">
                    <a href="/openmrs/operationtheater/patientsSurgeries.page?patientId=8&amp;returnUrl=%2Fopenmrs%2Fcoreapps%2Fclinicianfacing%2Fpatient.page%3FpatientId%3D8%26" id="operationtheater.patientsSurgeriesPatientDashboardLink" class="float-left">
                        <i class="icon-folder-open float-left"></i>
                        Open Surgeries
                    </a>


                </li>


                <li>
                    <a id="startSurgeryButton">
                            <svg class="icon-custom" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 35 35"><title>  play2</title><path d="M16 0c-8.8 0-16 7.2-16 16s7.2 16 16 16 16-7.2 16-16-7.2-16-16-16zM16 29c-7.2 0-13-5.8-13-13s5.8-13 13-13 13 5.8 13 13-5.8 13-13 13zM12 9l12 7-12 7z"/></svg>
                        ${ui.message("operationtheater.surgery.page.button.beginSurgery")}
                    </a>
                </li>


                <li>
                    <a id="finishSurgeryButton">${ui.message("operationtheater.surgery.page.button.finishSurgery")}</a>
                </li>



                <h3>Workflow</h3>

                <table id="timestamp-table">
                    <thead>
                    <tr>
                        <th>${ui.message("operationtheater.surgery.page.tableColumn.event")}</th>
                        <th>${ui.message("operationtheater.surgery.page.tableColumn.date")}</th>
                    </tr>
                    </thead>
                    <tbody>

                    </tbody>
                </table>


            </ul>

        </div>






            <p>

        </p>
        </fieldset>

    </div>
</form>
