<%
    config.require("includeForm")
%>

<h3>In-theater Prescriptions</h3>
<% if (config.includeForm) { %>
<p>Note the prescriptions administered during the procedure.</p>
<% } %>

<div <% if (config.includeForm) { %> style="padding: 0px 10px" <% } %> id="inTheaterPrescriptions">
    <table id="inTheaterPrescriptions-table" style="margin: 10px 0px">
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
        <tbody id="inTheaterPrescriptions-table-body">

        </tbody>

    </table>
</div>

<% if (config.includeForm) { %>

<div style="margin-top: 10px">
    <form id="inTheaterDrugPrescriptionsForm">
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
                    <input type="text" id="inTheaterDrugName" name="inTheaterDrugName" required autocomplete="off">
                </td>
            </tr>
            <tr style="border: none; background-color: #F2F2F2">
                <td style="border: none; min-width: 60px;">
                    Quantity
                </td>
                <td style="border: none; min-width: 100px;">
                    <input type="number" id="inTheaterDrugQuantity" name="inTheaterDrugQuantity" required>
                </td>
            </tr>
            <tr style="border: none; background-color: #F2F2F2">
                <td style="border: none; min-width: 60px;">
                    Time & Notes
                </td>
                <td style="border: none; min-width: 100px">
                    <textarea rows="3" id="inTheaterDrugNotes" placeholder="Unit, additional notes etc." required></textarea>
                </td>
            </tr>
        </table>
        <button type="button" style="margin: 10px 0px" id="addInTheaterDrug">Add</button>
        <p id="inTheaterErrorMsg"></p>
    </form>

</div>

<% } %>