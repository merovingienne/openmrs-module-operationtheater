
(function (surgeryNote, $, undefined) {

    var surgery;
    var patient;
    var surgeryNoteTable;


    surgeryNote.init = function(surgeryId, patientId){

        surgery = surgeryId;
        patient = patientId;

        surgeryNoteTable = jq('#surgery-note-table-body');
        jq('#surgery-note-table').hide();

        surgeryNote.get();

        jq('#saveSurgeryNote').click(function () {
            surgeryNote.add();
        });



    };


    surgeryNote.get = function () {
        emr.getFragmentActionWithCallback("operationtheater", "postTheaterData", "getSurgeryNote", { surgery: surgery}
            , function (data) {

                if (data.message != null){

                    surgeryNoteTable.empty();

                        var surgeryNoteEntry = "<tr style='border: none; background-color: #F2F2F2'>" +
                            "<td style='border: none; min-width: 60px;'><b>Date</b></td>" +
                            "<td style='border: none; min-width: 100px'>" +
                            "<p id='surgery-date'>" + data.date + "</p>"+
                            "</td>" +
                            "</tr>" +
                            "<tr style='border: none; background-color: #F2F2F2'>" +
                            "<td style='border: none; min-width: 60px;'>" +
                            "<b>Comment</b>" +
                            "</td>" +
                            "<td style='border: none; min-width: 100px'>" +
                            "<p id='saved-surgery-note'>" + data.comment + "</p>" +
                            "</td>" +
                            "</tr>";

                        surgeryNoteTable.append($(surgeryNoteEntry));

                    jq('#surgery-note-table').show();
                }

            }, function (err) {
                emr.handleError(err);
                console.log(err);
            }
        );
    };


    surgeryNote.add = function(){
        var enteredSurgeryNote = jq('#surgery-note').val();

        if (enteredSurgeryNote.length === 0){
            console.log("Empty surgery note.");
            return;
        }

        emr.getFragmentActionWithCallback("operationtheater", "postTheaterData", "addOrUpdateSurgeryNote",
            { patient: patient,
                surgery: surgery,
                surgeryNote: enteredSurgeryNote
            }, function (data) {
                emr.successMessage(data.message);
                surgeryNote.get();
            }, function (err) {
                emr.handleError(err);
            });

    };


}(window.surgeryNote = window.surgeryNote || {}, jQuery));
