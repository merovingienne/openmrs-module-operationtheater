<%
    ui.decorateWith('appui', 'standardEmrPage')
    ui.includeJavascript("operationtheater", "postTheaterForm_page/drugs.js")
    ui.includeJavascript("operationtheater", "postTheaterForm_page/surgeryNote.js")
    ui.includeJavascript("uicommons", "typeahead.js");
    ui.includeJavascript("operationtheater", "bower_components/validation/jquery.validate.js")

    def returnUrl = "/openmrs/operationtheater/surgery.page?surgeryId=${surgery.id}&patientId=${patient.id}"

%>

<script type="text/javascript">
    jq().ready(function(){

        var drugMap = {};
        <% drugList.each{ drug ->%>
        drugMap['${drug.getName()}'] = '${drug.getId()}';
        <% } %>
        var drugOptions = { source: Object.keys(drugMap) };
        jq('#inTheaterDrugName').typeahead(drugOptions);
        jq('#postTheaterDrugName').typeahead(drugOptions);

        postTheaterDrugs.init(${surgery.id}, ${patient.id}, drugMap);
        surgeryNote.init(${surgery.id}, ${patient.id});
    });
</script>

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

${ ui.includeFragment('operationtheater', 'surgeryHeader', [
        id: 'surgery-header',
        title: "Post-theater Data Collection",
        returnUrl: returnUrl
])}

<fieldset class="section-container" id="in-theater-data">
    <legend style="padding: 5px 5px 0px 5px">In-theater data</legend>
    <div class="section">
        <div class="section-item">

            ${ ui.includeFragment('operationtheater', 'inTheaterDrugs', [
                    includeForm: true
            ])}

        </div>
    </div>
</fieldset>


<fieldset class="section-container" id="post-theater-data">
    <legend style="padding: 5px 5px 0px 5px">Post-theater data</legend>
    <div class="section">

        <div class="section-item">
            <h3>Surgery notes</h3>
            <p>The outcome of the surgery, comments and complications. This will save this surgery as a past procedure for future reference.</p>
            <div id="surgery-note-div">
                <table id="surgery-note-table" style="margin: 10px 0px">
                    <tbody id="surgery-note-table-body">

                    </tbody>

                </table>
            </div>
            <form id="surgery-notes-form">
                <textarea placeholder="Surgery notes" id="surgery-note" style="margin: 10px 0px"></textarea>
                <p id="inTheaterErrorMsg"></p>
                <button id="saveSurgeryNote" type="button">Save</button>
            </form>

        </div>

        <br>

        <div class="section-item">

            ${ ui.includeFragment('operationtheater', 'postTheaterDrugs', [
                    includeForm: true
            ])}

        </div>
    </div>
</fieldset>