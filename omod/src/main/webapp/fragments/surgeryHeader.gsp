<%
    config.require("title")
    config.require('id')
    config.require('returnUrl')
%>


<div class="surgery-header"
     id="${config.id}"
     style="background-color: #F9F9F9;
     margin: -9px -9px 10px -9px;
     padding: 10px;
     display: block;
     min-height: 60px"   >


    <div class="surgery-header-title" style="float: left; width: 79%; padding: 1px; margin: 1px">
        <h1>${config.title}</h1>
    </div>

    <div style="float: right; width: 19%; padding: 1px; margin: 1px">
        <a href="${config.returnUrl}"><button style="float: right; color: white; background: none;background-color: blue">Back</button></a>
    </div>

</div>