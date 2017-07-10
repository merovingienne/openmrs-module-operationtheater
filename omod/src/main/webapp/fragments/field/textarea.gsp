<%
    config.require("placeholder")
    config.require("formFieldName")
    def rows = config.rows ?: 5;
%>

<div <% if (config.left) { %> class="left" <% } %> style="padding: 10px" >

    <textarea id="${ config.id }-field"
              placeholder="${ config.placeholder }"
              class="field-value <% if (config.classes) { config.classes.join(' ') } %>"
              rows="${ rows }" style="width: 100%" name="${ config.formFieldName }"
        <% if (config.maxlength) { %> maxlength="${ config.maxlength }" <% } %>
    >${ config.initialValue ?: "" }</textarea>
    ${ ui.includeFragment("uicommons", "fieldErrors", [ fieldName: config.formFieldName ]) }
</div>