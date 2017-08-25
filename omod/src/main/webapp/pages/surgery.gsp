<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeJavascript("operationtheater", "surgery_page/surgery.js")
    ui.includeJavascript("operationtheater", "surgery_page/surgicalTeam.js")
    ui.includeJavascript("operationtheater", "surgery_page/workflow.js")
    ui.includeJavascript("operationtheater", "preTheaterForm_page/pastProcedures.js")
    ui.includeJavascript("operationtheater", "preTheaterForm_page/drugs.js")
    ui.includeJavascript("operationtheater", "postTheaterForm_page/drugs.js")
    ui.includeJavascript("operationtheater", "postTheaterForm_page/surgeryNote.js")

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
    ];
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
    border-color: #eeeeee;
}

fieldset {
    min-width: 95%;
    padding: 10px;
    border-color: #eeeeee;
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


        <% if (surgery.procedure.name) { %>
        // Past procedures
        pastProcedures.init(${surgery.id}, ${patient.id});

        // pre-theater drugs
        preTheaterDrugs.init(${surgery.id}, ${patient.id}, null);

        // in and post theater drugs
        postTheaterDrugs.init(${surgery.id}, ${patient.id}, null);

        // surgery notes
        surgeryNote.init(${surgery.id}, ${patient.id});

        <% } %>
    });
</script>




<!- Main content (left hand side) -!>


<div style="padding: 20px; overflow: hidden">

    <h2>
        ${ui.message("operationtheater.surgery.page.title")}
    </h2>


    <div style="
    float: left;
    width: 70%;
    ">
        <form class="simple-form-ui" id="surgeryForm" autocomplete="off">
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

            <br><br>

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

        </form>

        <br>


        <div id="past-procedures" ${surgery.procedure.name ?: 'style="display:none"'}>
            <h3>Past Procedures</h3>

            <table id="past-procedures-table">
                <thead>
                <tr>
                    <th style="width: 20%">${ui.message("Procedure")}</th>
                    <th style="width: 20%">${ui.message("Date")}</th>
                    <th style="width: 20%">${ui.message("Comment")}</th>
                </tr>
                </thead>
                <tbody id="past-procedures-table-body">

                </tbody>
            </table>
        </div>

        <br>

        <div id="allergies"  ${surgery.procedure.name ?: 'style="display:none"'}>

            <h3>Allergies</h3>

            <% if (allergies.size() > 0) { %>

            <table id="allergies-table">
                <thead>
                <th>Allergen</th>
                <th>Reactions</th>
                </thead>
                <% allergies.each { allergy -> %>
                <tr>
                    <td>
                        ${ allergy.getAllergen().toString() }
                    </td>
                    <td>
                        <% if (allergy.getReactions().size() > 1) { %>
                        <% for (int i=0; i < allergy.getReactions().size()-1; i++) { %>
                        ${ allergy.getReactions()[i] },
                        <% } %>
                        ${ allergy.getReactions()[allergy.getReactions().size()-1]}
                        <% } else { %>
                        <%   allergy.getReactions().each { reaction -> %>

                        ${ reaction.toString()}
                        <% } %>
                        <% } %>
                    </td>
                </tr>
                <% } %>

                <tbody>

                </tbody>
            </table>

            <%  } else { %>
            <p>There are no allergies.</p>
            <% } %>

        </div>



        <% if (surgery.procedure.name) { %>
        <br>
        ${ ui.includeFragment("operationtheater", "preTheaterDrugs", [
                includeForm: false
        ])}
        <br>
        ${ ui.includeFragment('operationtheater', 'inTheaterDrugs',[
                includeForm: false,
        ])}
        <br>
        ${ ui.includeFragment('operationtheater', 'postTheaterDrugs',[
                includeForm: false,
        ])}

        <br>
        <div class="section-item">
            <h3>Surgery notes</h3>
            <div id="surgery-note-div">
                <table id="surgery-note-table" style="margin: 10px 0px">
                    <tbody id="surgery-note-table-body">

                    </tbody>

                </table>
            </div>
        </div>



        <% } %>



    </div>


    <% if (surgery.procedure.name) { %>

    <!- Right hand column -!>

    <div style="float: right;
    width: 30%;">

        <fieldset id="workflowFieldset" ${surgery.procedure.name ?: 'style="display:none"'}>

            <legend>Data</legend>
            <div class="action-section">
                <h3>General Actions</h3>

                <ul class="float-left">


                    <li class="float-left">
                        <a href="/openmrs/operationtheater/patientsSurgeries.page?patientId=${patient.id}" id="operationtheater.patientsSurgeriesPatientDashboardLink" class="float-left">
                            <i class="icon-folder-open float-left"></i>
                            Open Surgeries
                        </a>


                    </li>


                    <li>
                        <a href="/openmrs/operationtheater/pretheaterData.page?patient=${patient.id}&surgeryId=${surgery.id}">
                            <i class="icon-plus float-left"></i>
                            Add pre-theater data
                        </a>
                    </li>


                    <li>
                        <a href="/openmrs/operationtheater/postTheaterData.page?patient=${patient.id}&surgeryId=${surgery.id}">
                            <i class="icon-plus float-left"></i>
                            Add post-theater data
                        </a>
                    </li>

                </ul>

                <ul style="-webkit-margin-start: 30px">


                    <li style="list-style-type: circle">
                        <a id="startSurgeryButton">
                            ${ui.message("operationtheater.surgery.page.button.beginSurgery")}
                        </a>
                    </li>


                    <li style="list-style-type: disc">
                        <a id="finishSurgeryButton">${ui.message("operationtheater.surgery.page.button.finishSurgery")}</a>
                    </li>

                </ul>

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






            </div>
        </fieldset>

    </div>

    <% } %>

</div>
