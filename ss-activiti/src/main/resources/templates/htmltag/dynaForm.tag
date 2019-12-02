<form id="form"  method="post" fit="true">
    <input name="id" id="id" type="hidden">
    <input name="processDefinitionId" id="processDefinitionId" type="hidden">
    <input name="taskId" id="taskId" type="hidden" >
    <input name="formKey" id="formKey" type="hidden" >
    <input name="redirectUrl" id="redirectUrl" type="hidden" >

    <table style="padding:10px;" width="360px">
        <% for (actControl in actControls){ %>
        <% if(actControl.readable == false){ %>
        <input type="hidden" name="${actControl.name}" id="${actControl.controlId}"/>
        <% continue;}%>
        <tr>
            <td style="padding:5px;">
                <% if(actControl.type == 'text'){%>
                <input class="easyui-textbox" name="${actControl.name}" id="${actControl.controlId}" style="${actControl.style!"width:100%"};" <% if(actControl.required == true){ %> required="true" <%}%> <% if(actControl.writable == false){ %> readonly="true" <%}%>
                       data-options="labelAlign:'right',labelWidth:90,label:'${actControl.label}' <%if(actControl.minLength != null && actControl.maxLength != null){%>, validType:'length[${actControl.minLength},${actControl.maxLength}]'<%}%>" />
                <%}else if(actControl.type == 'combobox'){%>
                <input name="${actControl.name}" id="${actControl.controlId}" style="<%if(isEmpty(actControl.style)){%>width:100%;<%}else{%>${actControl.style}<%}%>" editable="false" panelWidth="auto" panelHeight="auto" <% if(actControl.required == true){ %> required="true" <%}%> <% if(actControl.writable == false){ %> readonly="true" <%}%>
                       data-options="labelAlign:'right',labelWidth:90,label:'${actControl.label}'"/>
                <%
                    var metaObj;
                    if(actControl.meta != null && strutil.trim(actControl.meta) != ""){
                        metaObj = @com.alibaba.fastjson.JSONObject.parseObject(actControl.meta);
                    }
                %>
                <#comboProvider _id="${actControl.controlId}" _provider='${metaObj.provider!"actDataDictionaryValueProvider"}' _queryParams="${actControl.meta}" />

                <%}else if(actControl.type == 'number'){%>
                <input class="easyui-numberbox" name="${actControl.name}" id="${actControl.controlId}" style="${actControl.style!"width:100%"};" <% if(actControl.required == true){ %> required="true" <%}%> <% if(actControl.writable == false){ %> readonly="true" <%}%>
                       data-options="labelAlign:'right',labelWidth:90,label:'${actControl.label}' <%if(actControl.minLength != null && actControl.maxLength != null){%>, validType:'length[${actControl.minLength},${actControl.maxLength}]'<%}%>" />
                <%}else if(actControl.type == 'datetime'){%>
                <input class="easyui-datetimebox" name="${actControl.name}" id="${actControl.controlId}" style="${actControl.style!"width:100%"};" <% if(actControl.required == true){ %> required="true" <%}%> <% if(actControl.writable == false){ %> readonly="true" <%}%> editable="false"
                       data-options="labelAlign:'right',labelWidth:90,label:'${actControl.label}'" />
                <%}else if(actControl.type == 'date'){%>
                <input class="easyui-datebox" name="${actControl.name}" id="${actControl.controlId}" style="${actControl.style!"width:100%"};" <% if(actControl.required == true){ %> required="true" <%}%> <% if(actControl.writable == false){ %> readonly="true" <%}%> editable="false"
                       data-options="labelAlign:'right',labelWidth:90,label:'${actControl.label}'" />
                <%}%>
            </td>
        </tr>
        <%}%>
        <tr>
            <td align="center">
                <a href="#" class="easyui-linkbutton" iconCls="icon-ok" id="submitBtn"
                   onclick="submit()">提交</a>
            </td>
        </tr>
    </table>
</form>