<%
    ui.decorateWith('appui', 'standardEmrPage')

%>

<h1>Pre-theater Data Collection</h1>

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
    border: 1px solid rgba(128, 128, 128, 0.22)
    width: 95%;
}

fieldset {
    padding: 10px;
}


</style>


<!-- <hr style="border-bottom: 1px solid rgba(156, 156, 156, 0.48)"> -->

<fieldset class="section-container" id="patient_history">
    <legend style="padding: 5px 5px 0px 5px">Patient History</legend>
    <div class="section">
        <div class="section-item">
            <h3>Current Medications</h3>
            <p>Please enter the medications currently used by the patient.</p>
            ${ ui.includeFragment('operationtheater', 'field/textarea', [
                    placeholder        : "Current Medications",
                    formFieldName: "currentMedications",
                    id           : "currentMedications",
                    rows         : 3
            ]) }
            <br>
        </div>

        <div class="section-item">
            <h3>Past Surgeries / Procedures</h3>
            <p>Please enter any recent surgeries or procedures of the patient.</p>
            ${ ui.includeFragment('operationtheater', 'field/textarea', [
                    placeholder      : "Past Surgeries / Procedures",
                    formFieldName: "pastProcedures",
                    id           : "pastProcedures",
                    rows         : 3
            ]) }
            <br>
        </div>

        <div class="section-item">
            <h3>Allergies</h3>
            <p>List any allergies that may cause complications.</p>
            ${ ui.includeFragment('operationtheater', 'field/textarea', [
                    placeholder  : "Allergies",
                    formFieldName: "allergies",
                    id           : "allergies",
                    rows         : 3
            ]) }
            <br>
        </div>

    </div>
</fieldset>

<fieldset class="section-container" id="patient_history">
    <legend style="padding: 5px 5px 0px 5px">Fitness for Surgery</legend>
    <div class="section">
        <div class="section-item">
            <h3>Physical condition</h3>
            <p>Enter conditions that may be of concern for the procedure.</p>
            ${ ui.includeFragment('operationtheater', 'field/textarea', [
                    placeholder        : "Physical Condition",
                    formFieldName: "physicalCondition",
                    id           : "physicalCondition",
                    rows         : 3
            ]) }
        </div>
    </div>
</fieldset>

<fieldset class="section-container" id="patient_history">
    <legend style="padding: 5px 5px 0px 5px">Pre-procedure tasks</legend>
    <div class="section">
        <div class="section-item">
            <h3>Prescriptions</h3>
            <p>Note the preparatory prescriptions to be taken before the procedure.</p>
            <script type="text/javascript">
                var prescCount = 1;
                var fragment = "<td>\
                                <select style='margin:5px 0px' id='prescription-field' name='Prescription' >\
                                    <option value=''>&nbsp;</option>\
                                    <option value='1'  selected>Drug</option>\
                                    <option value='2'  >Anasthetic</option>\
                                    <option value='3'  >Analgesic</option>\
                                </select>\
                            </td>\
                            <td>\
                                <textarea rows='2'></textarea>\
                            </td>\
                            <td>\
                                <textarea rows='2'></textarea>\
                            </td>\
                            <td>\
                                <textarea rows='2'></textarea>\
                            </td>\
                            <td class='align-center'>\
                                <span>\
                                    <i class='deleteElement delete-item icon-remove'' title='Delete'></i>\
                                </span>\
                            </td>";

                jq().ready(function() {
                    var addFragButton = document.getElementById('addFrag');
                    var div = document.getElementById('prescriptions');

                    addFragButton.onclick = function(){
                        var newFrag = document.createElement('tr');
                        newFrag.setAttribute('id', 'prescription_' + ++prescCount);
                        newFrag.innerHTML=fragment;
                        document.getElementById('prescriptions').appendChild(newFrag);
                    };

                    jq(document).on('click', '.deleteElement', function (event) {
                        jq(this).closest('tr').remove();
                    });
                });


            </script>
            <div style="padding: 0px 10px">
                <table id="prescriptions" style="margin: 10px 0px">
                    <thead>
                    <tr>
                        <th>
                            Type
                        </th>
                        <th>
                            Identifier / Name
                        </th>
                        <th>
                            Dosage / Notes
                        </th>
                        <th>
                            Time (before surgery)
                        </th>
                        <th>

                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr id="prescription_1">
                        <td>
                            <select style='margin:5px 0px' id='prescription-field' name='Prescription' >\
                                <option value=''>&nbsp;</option>\
                                <option value='1'  selected>Drug</option>\
                                <option value='2'  >Anasthetic</option>\
                                <option value='3'  >Analgesic</option>\
                            </select>
                        </td>
                        <td>
                            <textarea rows="2"></textarea>
                        </td>
                        <td>
                            <textarea rows="2"></textarea>
                        </td>
                        <td>
                            <textarea rows="2"></textarea>
                        </td>
                        <td class="align-center">
                        </td>
                    </tr>
                    </tbody>

                </table>
            </div>

            <button style="margin: 10px 0px" id="addFrag">Add</button>

        </div>
    </div>
</fieldset>
