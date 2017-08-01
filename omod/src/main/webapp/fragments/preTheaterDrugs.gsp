<%
    config.require("includeForm")
%>

<h3>Pre-theater Prescriptions</h3>

<% if (config.includeForm) { %>
<p>Note the preparatory prescriptions to be taken before the procedure.</p>
<% } %>

<div <% if (config.includeForm) { %> style="padding: 0px 10px" <% } %> id="preTheaterPrescriptions">
    <table id="preTheaterPrescriptions-table" style="margin: 10px 0px">
        <thead>
        <tr>
            <th>
                Drug
            </th>
            <th>
                Dosage
            </th>
            <th>
                Notes
            </th>
        </tr>
        </thead>
        <tbody id="preTheaterPrescriptions-table-body">

        </tbody>

    </table>
</div>

<% if (config.includeForm) { %>


<div style="margin-top: 10px">
    <form id="drugPrescriptionsForm">
        <table style="border: 1px solid black;
        background-color: #F2F2F2;
        min-width: 300px;
        max-width: 500px;
        margin: 0 0 10px 0;
        padding: 100px 0px;">
            <tr style="border: none; background-color: #F2F2F2">
                <td style="border: none; min-width: 60px;">
                    Name
                </td>
                <td style="border: none; min-width: 100px">
                    ${ui.includeFragment("uicommons", "field/text", [
                            label        : '',
                            formFieldName: "preTheaterDrug",
                            id           : "preTheaterDrug",
                            maxLength    : 50,
                            initialValue : ''
                    ])}
                </td>
            </tr>
            <tr style="border: none; background-color: #F2F2F2">
                <td style="border: none; min-width: 60px;">
                    Quantity
                </td>
                <td style="border: none; min-width: 100px;">
                    ${ ui.includeFragment('uicommons', 'field/text', [
                            label: '',
                            formFieldName: "preTheaterDrugQuantity",
                            id           : "preTheaterDrugQuantity",
                            maxLength    : 50,
                            initialValue : ''
                    ]) }
                </td>
            </tr>
            <tr style="border: none; background-color: #F2F2F2">
                <td style="border: none; min-width: 60px;">
                    Time & Notes
                </td>
                <td style="border: none; min-width: 100px">
                    <textarea rows="3" id="preTheaterDrugNotes" placeholder="Time before surgery, additional notes etc."></textarea>
                </td>
            </tr>
        </table>
    </form>
    <button style="margin: 10px 0px" id="addPreTheaterDrug">Add</button>
</div>

<% } %>