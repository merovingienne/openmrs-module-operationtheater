
(function (postTheaterDrugs, $, undefined) {

    var surgery;
    var patient;
    var inTheaterDrugsTable, postTheaterDrugsTable;
    var drugConceptMap;


    postTheaterDrugs.init = function(surgeryId, patientId, drugMap){

        surgery = surgeryId;
        patient = patientId;
        drugConceptMap = drugMap;

        console.log("post theater init.");
        console.log(patientId);

        inTheaterDrugsTable = document.getElementById('inTheaterPrescriptions-table-body');
        postTheaterDrugsTable = document.getElementById('postTheaterPrescriptions-table-body');

        postTheaterDrugs.get(2);
        postTheaterDrugs.get(3);

        jq('#inTheaterErrorMsg');
        jq('#postTheaterErrorMsg');



        jq('#addInTheaterDrug').click(function (e) {
            postTheaterDrugs.add(2);

        });


        jq('#addPostTheaterDrug').click(function () {
            postTheaterDrugs.add(3);
        });

    };


    postTheaterDrugs.get = function (workflowPosition) {

        var table;

        switch (workflowPosition) {
            case 2:
                table = "inTheater";
                break;
            case 3:
                table = "postTheater";
                break;
            default:
                console.log('invalid workflow position');
                return;
        }

        emr.getFragmentActionWithCallback("operationtheater", "postTheaterData", "getTheaterDrugs",
            { surgery: surgery,
                workflowPos : workflowPosition
            }
            , function (data) {

                if (data.length > 0) {
                    jq('#' + table +'Prescriptions-table').show();
                    jq('#' + table +'Prescriptions-table-body').empty();

                    for (var i = 0; i < data.length; i++){

                        var drugEntry = document.createElement('tr');
                        var drugName = document.createElement('td');
                        var drugQuantity = document.createElement('td');
                        var drugNotes = document.createElement('td');

                        drugName.innerHTML= data[i].name;
                        drugQuantity.innerHTML= data[i].quantity;
                        drugNotes.innerHTML = data[i].notes;


                        drugEntry.appendChild(drugName);
                        drugEntry.appendChild(drugQuantity);
                        drugEntry.appendChild(drugNotes);
                        document.getElementById(table + 'Prescriptions-table-body').appendChild(drugEntry);
                    }

                }else{
                    jq('#' + table +'Prescriptions-table').hide();
                    var emptyTable = "<p>There are no records.</p>";
                    jq('#' + table +'Prescriptions').append(jq(emptyTable));

                }


            }, function (err) {
                emr.handleError(err);
                console.log(err);
            }
        );

    };


    postTheaterDrugs.add = function(workflowPosition){

        var table;

        switch (workflowPosition) {
            case 2:
                table = "inTheater";
                break;
            case 3:
                table = "postTheater";
                break;
            default:
                console.log('invalid workflow position');
                return;
        }

        var drug = drugConceptMap[jq('#' + table + 'DrugName').val()];
        var quantity = jq('#' + table + 'DrugQuantity').val();
        var notes = jq('#' + table + 'DrugNotes').val();
        var error = jq('#' + table + 'DrugErrorMsg');
        error.hide();

        if (jq('#' + table + 'DrugName').val() == 0){
            error.text("Please enter a drug.");
            error.show();
            return;
        }
        else if (quantity.length == 0){
            error.text("Please enter a quantity.");
            error.show();
            return;
        }
        else if (notes == "") {
            error.text("Please enter notes (unit of quantity etc.)");
            error.show();
            return;
        }
        
        console.log("Request data");
        console.log('drug: ', drug);
        console.log('quantity: ', quantity);
        console.log('notes: ', notes);

        emr.getFragmentActionWithCallback("operationtheater", "postTheaterData", 'addTheaterDrugPrescription',
            {
                patient: patient,
                surgery: surgery,
                workflowPos: workflowPosition,
                drugConceptId: drug,
                drugQuantity: quantity,
                prescriptionNotes: notes
            }, function (data) {
                emr.successMessage(data.message);

                postTheaterDrugs.get(workflowPosition);

            }, function (err) {
                emr.handleError(err);
            });

    };


}(window.postTheaterDrugs = window.postTheaterDrugs || {}, jQuery));
