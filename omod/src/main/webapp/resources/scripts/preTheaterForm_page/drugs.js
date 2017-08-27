
(function (preTheaterDrugs, $, undefined) {

    var surgery;
    var patient;
    var drugsTable;
    var drugConceptMap;


    preTheaterDrugs.init = function(surgeryId, patientId, drugMap){

        surgery = surgeryId;
        patient = patientId;
        drugConceptMap = drugMap;

        drugsTable = document.getElementById('preTheaterPrescriptions-table-body');

        preTheaterDrugs.get();

        jq('#addPreTheaterDrug').click(function () {
            preTheaterDrugs.add();
        });

    };


    preTheaterDrugs.get = function () {
        emr.getFragmentActionWithCallback("operationtheater", "pretheaterData", "getPreTheaterDrugs", { surgery: surgery}
            , function (data) {

                if (data.length > 0) {
                    jq('#preTheaterPrescriptions-table').show();
                    jq('#preTheaterPrescriptions-table-body').empty();

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
                        drugsTable.appendChild(drugEntry);
                    }

                }else{
                    jq('#preTheaterPrescriptions-table').hide();
                    var emptyTable = "<p>There are no drugs recorded to be administered.</p>";
                    jq('#preTheaterPrescriptions').append(jq(emptyTable));

                }


            }, function (err) {
                emr.handleError(err);
                console.log(err);
            }
        );

    };


    preTheaterDrugs.add = function(){

        var drug = drugConceptMap[jq('#preTheaterDrug-field').val()];
        var quantity = jq('#preTheaterDrugQuantity').val();
        var notes = jq('#preTheaterDrugNotes').val();
        var error = jq('#preTheaterDrugErrorMsg');
        error.hide();

        if (jq('#preTheaterDrug-field').val() == 0){
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

        emr.getFragmentActionWithCallback("operationtheater", "pretheaterData", "addPretheaterDrugPrescription",
            { patient: patient,
                surgery: surgery,
                drugConceptId: drug,
                drugQuantity: quantity,
                prescriptionNotes: notes
            }, function (data) {
                emr.successMessage(data.message);

                preTheaterDrugs.get();

            }, function (err) {
                emr.handleError(err);
            });

    };


}(window.preTheaterDrugs = window.preTheaterDrugs || {}, jQuery));
