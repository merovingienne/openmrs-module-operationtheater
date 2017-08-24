<%
    ui.decorateWith('appui', 'standardEmrPage')
    ui.includeJavascript("operationtheater", "preTheaterForm_page/drugs.js")
    ui.includeJavascript("operationtheater", "preTheaterForm_page/pastProcedures.js")
    ui.includeJavascript("uicommons", "typeahead.js");


    def returnUrl = "/openmrs/operationtheater/surgery.page?surgeryId=${surgery.id}&patientId=${patient.id}"
%>

<style type="text/css">


.column {
    float: left;
    width: 50%
}

.simple-form-ui input {
    min-width: 80%
}

form fieldset {
    min-width: 90%
}

textarea {
    overflow-y: hidden; /* fixes scrollbar flash - kudos to @brettjonesdev */
    padding-top: 1.1em; /* fixes text jump on Enter keypress */
}


.section {
    background: #F2F2F2;
    box-shadow: 3px 3px 3px 1px rgba(0, 0, 0, 0.2);
    padding: 10px 5px 10px 15px;
    line-height: 1.5em; /*add this for vertical spacing between elements*/
    margin: 0px 15px 15px 5px;
}

.section-container input[type="checkbox"] {
    margin: 0px 5px; /*changed values to vertical, horizontal*/
    top:5px; /*added to offset the checkbox position to line up*/
}

.section-container label { /*new definition to override labels inside section-containers*/
    margin: 0px;
}

.section-container {
    border: 1px solid rgba(128, 128, 128, 0.22);
    width: 95%;
}

fieldset {
    padding: 10px;
}


</style>


<script type="text/javascript">
    jq().ready(function(){

        var procedureMap = {};
        <% procedureList.each{ procedure ->%>
        procedureMap['${procedure.name}'] = '${procedure.uuid}';
        <% } %>
        var procedureOptions = { source: Object.keys(procedureMap) };
        jq('#pastProcedureName').typeahead(procedureOptions);


        var drugMap = {};
        <% drugList.each{ drug ->%>
        drugMap['${drug.getName()}'] = '${drug.getId()}';
        <% } %>
        var drugOptions = { source: Object.keys(drugMap) };
        jq('#preTheaterDrug-field').typeahead(drugOptions);



        preTheaterDrugs.init(${surgery.id}, ${patient.id}, drugMap);
        pastProcedures.init(${surgery.id}, ${patient.id});



    });
</script>


<!-- <hr style="border-bottom: 1px solid rgba(156, 156, 156, 0.48)"> -->


${ ui.includeFragment('operationtheater', 'surgeryHeader', [
        id: 'surgery-header',
        title: "Pre-theater Data Collection",
        returnUrl: returnUrl
])}

<fieldset class="section-container" id="patient_history">
    <legend style="padding: 5px 5px 0px 5px">Patient History</legend>
    <div class="section">
        <div class="section-item">
            <h3>Past Surgeries / Procedures</h3>
            <p>Please enter any recent surgeries or procedures of the patient.</p>
            <br>
            <div id="surgical-team-list">
                <table id="past-procedures-table"
                       empty-value-message='${ui.message("uicommons.dataTable.emptyTable")}'>
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
            <div>
                <form id="pastProcedureForm">
                    <table style="border: 1px solid black;
                    background-color: #F2F2F2;
                    min-width: 300px;
                    max-width: 500px;
                    margin: 0 0 10px 0;
                    padding: 100px 0px;">
                        <tr style="border: none; background-color: #F2F2F2">
                            <td style="border: none; min-width: 60px;">
                                Procedure name
                            </td>
                            <td style="border: none; min-width: 100px">
                                <input type="text" id="pastProcedureName">
                            </td>
                        </tr>
                        <tr style="border: none; background-color: #F2F2F2">
                            <td style="border: none; min-width: 60px;">
                                Procedure date
                            </td>
                            <td style="border: none; min-width: 100px;">
                                ${ ui.includeFragment('uicommons', 'field/datetimepicker', [
                                        formFieldName: "pastProcedureDate",
                                        id           : "pastProcedureDate",
                                        label: "",
                                        useTime: false
                                ]) }
                            </td>
                        </tr>
                        <tr style="border: none; background-color: #F2F2F2">
                            <td style="border: none; min-width: 60px;">
                                Procedure comment
                            </td>
                            <td style="border: none; min-width: 100px">
                                <textarea rows="3" id="pastProcedureComment" placeholder="Comments, diagnosis etc."></textarea>
                            </td>
                        </tr>
                    </table>
                </form>
                <a class="button"
                   id="addPastProcedureButton"
                   onclick=surgeryNote.add()>${ui.message("general.add")}</a>
            </div>
            <br>
        </div>

        <div class="section-item">
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

            <a href="/openmrs/allergyui/allergy.page?patientId=${patient.id}"
               class="button"
               style="margin-top: 10px"
               target="_blank">
                Add
            </a>
        </div>

    </div>
</fieldset>


<fieldset class="section-container" id="patient_history">
    <legend style="padding: 5px 5px 0px 5px">Pre-procedure tasks</legend>
    <div class="section">
        <div class="section-item">

            ${ ui.includeFragment('operationtheater', 'preTheaterDrugs',[
                    includeForm: true
            ])}


        </div>
    </div>
</fieldset>
