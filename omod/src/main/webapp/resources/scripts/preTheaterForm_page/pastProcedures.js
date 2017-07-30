
(function (pastProcedures, $, undefined) {

    var surgeryUuid;
    var patient;
    var pastProceduresTable;


    pastProcedures.init = function(surgeryId, patientId){

        surgeryUuid = surgeryId;
        patient = patientId;

        pastProceduresTable = document.getElementById('past-procedures-table-body');

        pastProcedures.get();
    };



    pastProcedures.get = function () {
        emr.getFragmentActionWithCallback("operationtheater", "pretheaterData", "getPastProcedures", { patient: patient}
            , function (data) {

                if (data.length > 0){
                    jq('#past-procedures-table').show();
                    jq('#past-procedures-table-body').empty();

                    for (var i = 0; i < data.length; i++){

                        var pastProcedureEntry = document.createElement('tr');
                        var pastProcedureName = document.createElement('td');
                        var pastProcedureDate = document.createElement('td');
                        var pastProcedureComment = document.createElement('td');

                        pastProcedureName.innerHTML= data[i].name;
                        pastProcedureDate.innerHTML= data[i].date;
                        pastProcedureComment.innerHTML = data[i].comment;


                        pastProcedureEntry.appendChild(pastProcedureName);
                        pastProcedureEntry.appendChild(pastProcedureDate);
                        pastProcedureEntry.appendChild(pastProcedureComment);
                        pastProceduresTable.appendChild(pastProcedureEntry);


                    }


                } else {
                    jq('#past-procedures-table').hide();
                    var emptyTable = "<p>There are no recorded past procedures.</p>";
                    jq('#past-procedures').append(jq(emptyTable));
                }

            }, function (err) {
                emr.handleError(err);
                console.log(err);
            }
        );
    };


    pastProcedures.add = function(){


        var ppName = jq('#pastProcedureName').val();
        var ppDate = jq('#pastProcedureDate-field').val();
        var ppComment = jq('#pastProcedureComment').val();

        emr.getFragmentActionWithCallback("operationtheater", "pretheaterData", "addPastProcedureRecord",
            { patient: patient,
              pastProcedureName: ppName,
                pastProcedureDate: ppDate,
                pastProcedureComment: ppComment
            }, function (data) {
                emr.successMessage(data.message);
                pastProcedures.get();
            }, function (err) {
                emr.handleError(err);
            });

    };


}(window.pastProcedures = window.pastProcedures || {}, jQuery));
