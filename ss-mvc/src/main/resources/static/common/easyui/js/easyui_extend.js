//============================  原型定义  ============================
Array.prototype.contains = function(val){
    for (var i = 0; i < this.length; i++){
        if (this[i] == val){
            return true;
        }
    }
    return false;
};

//============================  方法定义  ============================

/**
 * 判断对象是否是JSON
 * @param obj
 * @returns {boolean}
 */
var isJson = function(obj){
    var isjson = typeof(obj) == "object" && Object.prototype.toString.call(obj).toLowerCase() == "[object object]" && !obj.length;
    return isjson;
}

//textbox有数据修改时也要显示清空按钮
function _changeTextboxShowClear(newValue, oldValue) {
    var icon = $(this).textbox('getIcon',0);
    if(!newValue || newValue == null || newValue == ""){
        icon.css('visibility','hidden');
    } else {
        icon.css('visibility','visible');
    }
    if(newValue == $(this).textbox("getText")){
        $(this).textbox("initValue", oldValue);
        $(this).textbox("setText", newValue);
    }
}
//判断textbox中icons对象数组中是否存在iconCls
function _containsIconCls(iconsArr, iconCls){
    for (var i = 0; i < iconsArr.length; i++) {
        if (iconsArr[i]["iconCls"] == iconCls) {
            return true;
        }
    }
    return false;
}

//表格日期格式化器
function dateFormatter(value) {
    var date = new Date(value);
    var y = date.getFullYear();
    var m = date.getMonth() + 1;
    var d = date.getDate();
    return y + '-' + m + '-' + d;
}

/**
 * 表格时间格式化
 * @param value
 * @returns {string}
 */
function datetimeFormatter (value) {
    var date = new Date(value);
    var year = date.getFullYear().toString();
    var month = (date.getMonth() + 1);
    var day = date.getDate().toString();
    var hour = date.getHours().toString();
    var minutes = date.getMinutes().toString();
    var seconds = date.getSeconds().toString();
    if (month < 10) {
        month = "0" + month;
    }
    if (day < 10) {
        day = "0" + day;
    }
    if (hour < 10) {
        hour = "0" + hour;
    }
    if (minutes < 10) {
        minutes = "0" + minutes;
    }
    if (seconds < 10) {
        seconds = "0" + seconds;
    }
    return year + "-" + month + "-" + day + " " + hour + ":" + minutes + ":" + seconds;
}

/**
 * 绑定实体的metadata信息，用于提供者转换
 * @param gridId    datagrid Id
 * @param isClearQueryParams    是否清空datagrid中原有的queryParams, 默认为false,不清空
 * @param isTreegrid 是否树表，默认false
 * @returns {queryParams|{provider}|*|string|{}}
 */
function bindMetadata(gridId, isClearQueryParams, isTreegrid){
    var opts = isTreegrid == null ? $("#"+gridId).datagrid("options") : $("#"+gridId).treegrid("options");
    //赋默认值
    isClearQueryParams = isClearQueryParams || false;
    var params = isClearQueryParams ? {} : opts.queryParams || {};
    //获取最后一行的列(可能是多表头)
    var lastColumns = opts.columns[opts.columns.length-1];
    params["metadata"] = {};
    //提供者的默认排序索引
    var index = 10;
    for(var column in lastColumns){
        var _provider = lastColumns[column]["_provider"];
        var _data = lastColumns[column]["_data"];
        //优先解析直接数据的_data属性
        if(_data != null){
            var field = lastColumns[column]["field"];
            var fieldMetadata = {};
            fieldMetadata["provider"] = "simpleDataProvider";
            fieldMetadata["data"] = _data;
            params["metadata"][field] = JSON.stringify(fieldMetadata);
            continue;
        }
        //没有_data属性，则解析_table,_valueField和_textField等其它属性
        var _table = lastColumns[column]["_table"];
        //如果有_table属性，则按simpleValueProvider处理
        if(_table != null){
            _provider = "simpleValueProvider";
        }
        if(_provider != null){
            //设值
            var field = lastColumns[column]["field"];
            var fieldMetadata = {};
            fieldMetadata["provider"] = _provider;
            fieldMetadata["table"] = _table;
            fieldMetadata["valueField"] = lastColumns[column]["_valueField"];
            fieldMetadata["textField"] = lastColumns[column]["_textField"];
            fieldMetadata["queryParams"] = lastColumns[column]["_queryParams"];
            fieldMetadata["index"] = lastColumns[column]["_index"] == null ? index : lastColumns[column]["_index"];
            fieldMetadata["field"] = field;
            //设置通用批量提供者参数
            fieldMetadata["_escapeFileds"] = lastColumns[column]["_escapeFileds"];
            fieldMetadata["_relationTablePkField"] = lastColumns[column]["_relationTablePkField"];
            fieldMetadata["_relationTable"] = lastColumns[column]["_relationTable"];
            fieldMetadata["_fkField"] = lastColumns[column]["_fkField"];
            params["metadata"][field] = JSON.stringify(fieldMetadata);
            index+=10;
        }
    }
    return params;
}

/**
 * 为表单绑定表格的metadata，保持原有的meta信息
 * 返回绑定好的对象
 * @param gridId
 * @param formId
 * @returns {*}
 */
function bindGridMeta2FormData(gridId, formId, json, containsNull){
    var formData = bindGridMeta2Form(gridId, formId, containsNull);
    if(!json || json == null || json === "" || !isJson(json)) {
        return formData;
    }
    return $.extend({}, formData, json);
}

/**
 * 为表单绑定表格的metadata，保持原有的meta信息
 * 返回绑定好的对象
 * @param gridId
 * @param formId
 * @returns {*}
 */
function bindGridMeta2Form(gridId, formId, containsNull){
    var param = bindMetadata(gridId, true);
    if(!formId || formId == null || formId === "") return param;
    var formData = $("#"+formId).serializeObject(containsNull);
    return $.extend({}, param, formData);
}

/**
 * 为表单绑定树型表格的metadata，保持原有的meta信息
 * 返回绑定好的对象
 * @param gridId
 * @param formId
 * @returns {*}
 */
function bindTreegridMeta2Form(gridId, formId, containsNull){
    var param = bindMetadata(gridId, true, true);
    if(!formId || formId == null || formId === "") return param;
    var formData = $("#"+formId).serializeObject(containsNull);
    return $.extend({}, param, formData);
}

/**
 * 为一个JSON对象绑定树型表格的metadata，保持原有的meta信息
 * 返回绑定好的对象
 * @param gridId
 * @param formId
 * @returns {*}
 */
function bindTreegridMeta2Data(gridId, json){
    var param = bindMetadata(gridId, true, true);
    if(!json || json == null || json === "" || !isJson(json)) return param;
    return $.extend({}, param, json);
}

/**
 * 为一个JSON对象绑定表格的metadata，保持原有的meta信息
 * 返回绑定好的对象
 * @param gridId
 * @param formId
 * @returns {*}
 */
function bindGridMeta2Data(gridId, json){
    var param = bindMetadata(gridId, true);
    if(!json || json == null || json === "" || !isJson(json)) return param;
    return $.extend({}, param, json);
}

//树的转换加载
function convertTree(rows){
    function exists(rows, parentId){
        for(var i=0; i<rows.length; i++){
            if (rows[i].id == parentId) return true;
        }
        return false;
    }

    var nodes = [];
    // 得到顶层节点
    for(var i=0; i<rows.length; i++){
        var row = rows[i];
        if (!exists(rows, row.parentId)){
            nodes.push({
                id:row["id"],
                text:row["text"],
                iconCls:row["iconCls"],
                state:row["state"],
                checked: row["checked"],
                attributes:row["attributes"]
            });
        }
    }

    var toDo = [];
    for(var i=0; i<nodes.length; i++){
        toDo.push(nodes[i]);
    }
    while(toDo.length){
        var node = toDo.shift();    // 父节点
        // 得到子节点
        for(var i=0; i<rows.length; i++){
            var row = rows[i];
            if (row.parentId == node.id){
                var child = {
                    id:row["id"],
                    text:row["text"],
                    iconCls:row["iconCls"],
                    state:row["state"],
                    checked: row["checked"],
                    attributes:row["attributes"]
                };
                if (node.children){
                    node.children.push(child);
                } else {
                    node.children = [child];
                }
                toDo.push(child);
            }
        }
    }
    return nodes;
}
//树加载过滤器
var treeLoadFilter = function(data,parent){
    var idField = $(this).tree("options")["_idField"];
    var textField = $(this).tree("options")["_textField"];
    var parentIdField = $(this).tree("options")["_parentIdField"];
    var jsonData = $.isArray(data) ? data : data.rows;
    //如果没数据或者已经有children属性，则不进行转换，主要用于拖动有子节点的场景
    //这里不能返回jsonData，jsonData可能为null导致报错，只能返回data
    if(null == jsonData || (jsonData.length <=0 || jsonData[0].hasOwnProperty("children"))){
        return data;
    }
    if(idField && idField != null && idField != ""){
        modifyJsonKey(jsonData,idField,"id");
    }
    if(textField && textField != null && textField != ""){
        modifyJsonKey(jsonData,textField,"text");
    }
    if(parentIdField && parentIdField != null && parentIdField != ""){
        modifyJsonKey(jsonData,parentIdField,"parentId");
    }
    return convertTree(jsonData);
}

//树表加载过滤器
var treegridLoadFilter = function(data){
    var parentIdField = $(this).treegrid("options")["_parentIdField"];
    var idField = $(this).treegrid("options")["idField"];
    if(parentIdField && parentIdField != null && parentIdField != ""){
        if(data.rows){
            modifyJsonKey(data.rows,parentIdField,"_parentId");
            introspect(data.rows, idField);
        }else{
            modifyJsonKey(data,parentIdField,"_parentId");
            introspect(data, idField);
        }
    }
    return data;
}
//内省，将没有id对应的parentId删除掉
function introspect(rows, idField){
    for(var r in rows){
        var row = rows[r];
        if(undefined == row["_parentId"] || row["_parentId"] == null || row["_parentId"] === ""){
            continue;
        }
        if(!findParentIdFromId(rows, row["_parentId"], idField)){
            delete row["_parentId"];
        }
    }
}
//判断当前行的parentId是否有对应的id
function findParentIdFromId(rows, parentId, idField){
    for(var r in rows){
        var row = rows[r];
//                parentId找到对应的id
        if(row[idField] && row[idField] == parentId){
            return true;
        }
    }
    return false;
}

//修改json对象或数组中的key
function modifyJsonKey(json,oldkey,newkey){
    if(oldkey == newkey){
        return;
    }
    if(json instanceof Array){
        for(var i in json){
            var obj = json[i];
            modifyJsonKey(json[i],oldkey,newkey);
        }
    }else{
        //没有指定的字段名则不进行key转换，这里主要用于节点拖动时，数据已经转换成标准的id,text和parentId了
        if(!json.hasOwnProperty(oldkey)){
            return;
        }
        var val = json[oldkey];
        // delete json[oldkey];
        json[newkey]=val;
    }
}

//修改表格边框
function changeBorder(gridId, cls){
    $('#'+gridId).datagrid('getPanel').removeClass('lines-both lines-no lines-right lines-bottom').addClass(cls);
}

$(function() {
    //自定义日期解析
    $.fn.datebox.defaults.parser = function(s){
        if (!s) return new Date();
        var ss = (s.split('-'));
        var y = parseInt(ss[0],10);
        var m = parseInt(ss[1],10);
        var d = parseInt(ss[2],10);
        if (!isNaN(y) && !isNaN(m) && !isNaN(d)){
            return new Date(y,m-1,d);
        } else {
            return new Date();
        }
    }
    //自定义日期格式化
    $.fn.datebox.defaults.formatter = function(date){
        var y = date.getFullYear();
        var m = date.getMonth()+1;
        var d = date.getDate();
        return y+'-'+(m<10?('0'+m):m)+'-'+(d<10?('0'+d):d);
    }
    //自定义日期时间解析
    $.fn.datetimebox.defaults.parser = function(s){
        if (!s) return new Date();
        var y = s.substring(0,4);
        var m =s.substring(5,7);
        var d = s.substring(8,10);
        var h = s.substring(11,13);
        var min = s.substring(14,16);
        var sec = s.substring(17,19);
        if (!isNaN(y) && !isNaN(m) && !isNaN(d) && !isNaN(h) && !isNaN(min) && !isNaN(sec)){
            return new Date(y,m-1,d,h,min,sec);
        } else {
            return new Date();
        }
    }
//自定义日期时间格式化
    $.fn.datetimebox.defaults.formatter = function(date){
        var y = date.getFullYear();
        var m = date.getMonth()+1;
        var d = date.getDate();
        var h = date.getHours();
        var min = date.getMinutes();
        var sec = date.getSeconds();
        return y+'-'+(m<10?('0'+m):m)+'-'+(d<10?('0'+d):d)+' '+(h<10?('0'+h):h)+':'+(min<10?('0'+min):min)+':'+(sec<10?('0'+sec):sec);
    }

    //文本框添加清空按钮
    $.extend($.fn.textbox.methods, {
        addClearBtn: function (jq, iconCls) {
            return jq.each(function () {
                var t = jq;
                var opts = t.textbox('options');
                //使用深复制，解决有多个textbox时，调用一次addClearBtn可能或为其它文本框添加cleaBtn的问题
                opts.icons = opts.icons ? opts.icons.slice() : [];
                if (!_containsIconCls(opts.icons, iconCls)) {
                    opts.icons.unshift({
                        iconCls: iconCls,
                        handler: function (e) {
                            //针对textbox with button清除不掉，这里强制清除
                            $(e.data.target).textbox('initValue', "");
                            $(e.data.target).textbox('clear').textbox('textbox').focus();
                            $(this).css('visibility', 'hidden');
                        }
                    });
                }
                t.textbox();
                if (!t.textbox('getText') || t.textbox('getText') == "") {
                    t.textbox('getIcon', 0).css('visibility', 'hidden');
                }
                t.textbox('textbox').bind('keyup', function () {
                    var icon = t.textbox('getIcon', 0);
                    if ($(this).val()) {
                        icon.css('visibility', 'visible');
                    } else {
                        icon.css('visibility', 'hidden');
                    }
                });
            });
        }
    });

    /**
     * 根据字段按百分比重置列宽
     * 示例:
     * grid.datagrid('resizeColumn', [{
 *							field : 'id',
 *							width : '35%'
 *						}, {
 *							field : 'name',
 *							width : '35%'
 *						}]);
     */
    $.extend($.fn.datagrid.methods, {
        resizeColumn : function(jq, param) {
            return jq.each(function() {
                var dg = $(this);
                var fn = function(item) {
                    var col = dg.datagrid('getColumnOption', item.field);
                    col.width = item.width;
                    if (typeof(col.width) == 'string') {
                        var width = parseInt(col.width.replace('%', ''));
                        col.boxWidth = col.boxWidth * width / 100;
                    } else {
                        col.boxWidth = col.width;
                    }
                    dg.datagrid('fixColumnSize', param.field);
                };
                if (param instanceof Array) {
                    $(param).each(function(index, item) {
                        fn.call(this, item);
                    });
                } else {
                    fn.call(this, param);
                }
            })
        }
    });
});


/*******************************************************************************
 * 表单光标定位
 *
 * @param formId
 * @param focusInputName
 */
function formFocus(formId, focusInputId) {
    window.setTimeout(function() {
        //所有form元素都继承textbox，所以直接获取textbox对象来获取焦点
        $("#"+ focusInputId).textbox('textbox').focus();
    }, 0);
}

function formFocusTextArea(formId, focusInputNameId) {
    window.setTimeout(function() {
        $("#" + formId + " #" + focusInputName).focus();
    }, 0);
}

function formFocusTextArea(formId, focusInputName) {
    window.setTimeout(function() {
        $("#" + formId + " textarea[name=" + focusInputName + "]").focus();
    }, 0);
}

/*******************************************************************************
 * 绑定指定表单元素类型的回车事件
 *
 * @param formId
 *            表单
 * @param element
 *            元素类型 (input/select/radio/...)
 * @param subFunction
 *            回车后要执行的js函数
 * @param eventName
 *            键盘事件 (keyup/keydown/keypress...)
 */
function bindEnter(formId, element, subFunction, eventName) {
    $("#" + formId + " " + element).bind(eventName, function(event) {
        //回车
        if (event.keyCode == '13') {
            return subFunction(event);
        }
        //下拉
        // if (event.keyCode == '40') {
        //     $("#deviceName").combobox('showPanel');
        // }
    });
}

/*******************************************************************************
 * 绑定指定表单元素类型的ESC事件
 *
 * @param formId
 *            表单
 * @param element
 *            元素类型 (input/select/radio/...)
 * @param subFunction
 *            回车后要执行的js函数
 * @param eventName
 *            键盘事件 (keyup/keydown/keypress...)
 */
function bindEsc(formId, element, subFunction, eventName) {
    $("#" + formId + " " + element).bind(eventName, function(event) {
        //ESC
        if (event.keyCode == '27') {
            return subFunction(event);
        }
    });
}

/*******************************************************************************
 * 表单回车事件绑定 表单光标定位
 *
 * @param formId
 * @param focusInputId
 * @param subFunction 回车要执行的函数
 * @param subFunction Esc要执行的函数
 *
 */
function bindFormEvent(formId, focusInputId, subFunction, escFunction) {
    if(subFunction && subFunction != null) {
        bindEnter(formId, 'input', subFunction, 'keyup');
        bindEnter(formId, 'select', subFunction, 'keyup');
    }
    if(escFunction && escFunction != null){
        bindEsc(formId, 'input', escFunction, 'keyup');
        bindEsc(formId, 'select', escFunction, 'keyup');
    }
    formFocus(formId, focusInputId);
}

/**
 * 单独实现一个combobox的keyHandler的回车(enter)事件函数，
 * 用于解决如果输入的值不是下拉中的值，按回车键会被清空问题
 * @param target
 */
function _doComboboxEnter(target){
    var t = $(target);
    var opts = t.combobox('options');
    var panel = t.combobox('panel');
    var item = panel.children('div.combobox-item-hover');
    if (item.length){
        item.removeClass('combobox-item-hover');
        var row = opts.finder.getRow(target, item);
        var value = row[opts.valueField];
        if (opts.multiple){
            if (item.hasClass('combobox-item-selected')){
                t.combobox('unselect', value);
            } else {
                t.combobox('select', value);
            }
        } else {
            t.combobox('select', value);
        }
    }
    if (!opts.multiple){
        t.combobox('hidePanel');
    }
}

//combobox的onLoadSuccess事件，加载成功后默认选中第一项或第二项(第一项是请选择且value为null)
function onComboLoadSuccessSelectOne() {
    var data = $(this).combobox("getData");
    var opts = $(this).combobox("options");
    var valueField = opts.valueField;
    if(data == null || data.length<=0) return;
    if(data[0][valueField] == null || data[0][valueField] === ""){
        if(data == null || data.length<=1) return;
        $(this).combobox("select", data[1][valueField]);
    }else{
        $(this).combobox("select", data[0][valueField]);
    }
}

/**
 * 判断面板是否折叠
 * @param panelId
 * @returns {boolean}
 */
function isCollapse(panelId){
    return $("#"+panelId)[0].clientWidth <= 0;
}
/**
 * 清除表单值，主要针对easyui form的clear方法无法清除textbox with button的问题
 * @param formId 表单id，必填
 */
function clearEasyuiForm(formId){
    $(':input','#'+formId)
        .not(':button, :submit, :reset')
        .val('')
        .removeAttr('checked')
        .removeAttr('selected');
}
//数据表格的页脚视图
var footerView = $.extend({}, $.fn.datagrid.defaults.view, {
    renderFooter: function(target, container, frozen){
        var opts = $.data(target, 'datagrid').options;
        var rows = $.data(target, 'datagrid').footer || [];
        var fields = $(target).datagrid('getColumnFields', frozen);
        var table = ['<table class="datagrid-ftable" cellspacing="0" cellpadding="0" border="0"><tbody>'];

        for(var i=0; i<rows.length; i++){
            var styleValue = opts.rowStyler ? opts.rowStyler.call(target, i, rows[i]) : '';
            var style = styleValue ? 'style="' + styleValue + '"' : '';
            table.push('<tr class="datagrid-row" datagrid-row-index="' + i + '"' + style + '>');
            table.push(this.renderRow.call(this, target, fields, frozen, i, rows[i]));
            table.push('</tr>');
        }

        table.push('</tbody></table>');
        $(container).html(table.join(''));
    }
});
//格式化数字，去掉最后的0
function cutZeroFormatter(value) {
    if(value == null || value == ""){
        return null;
    }
    //如果有多个小数点，返回0
    if(value.lastIndexOf(".") != value.indexOf(".")){
        return 0;
    }
    //去掉开头的0
    if(!value.startWith("0.")){
        while (value.startWith("0")){
            value = value.substring(1, value.length);
        }
    }
    //如果有小数点，去掉末尾的0
    if(value.lastIndexOf(".") != -1){
        for(var i = value.length; i > 0; i--){
            if(value.lastIndexOf("0") == i-1){
                value = value.substring(0, i-1);
            }else{
                break;
            }
        }
    }
    //获取精度，easyui默认值为0
    var precision = $(this).numberbox("options").precision;
    var indexOfDot = value.indexOf(".");
    //根据精度，截断后面的位数
    if( value.lastIndexOf(".") != -1 &&  value.length - indexOfDot - 1 > precision){
        value = value.substring(0, indexOfDot+precision+1);
    }
    //如果value是以.结尾，则去掉最后的.
    if(value.lastIndexOf(".") != -1 && value.lastIndexOf(".") == value.length-1){
        value = value.substring(0, value.length-1);
    }
    return value;
}

//日期格式正则校验
$.extend($.fn.validatebox.defaults.rules, {
    datetimeFmt: {
        validator: function(value, param){
            var reDateTime = /^(?:19|20)[0-9][0-9]-(?:(?:0[1-9])|(?:1[0-2]))-(?:(?:[0-2][1-9])|(?:[1-3][0-1])) (?:(?:[0-2][0-3])|(?:[0-1][0-9])):[0-5][0-9]:[0-5][0-9]$/;
            return reDateTime.test(value);
        },
        message: '非法日期格式， 示例:2019-01-30 19:59:05'
    }
})

$(function () {
    //日期和时间控件添加清空
    var d_buttons = $.extend([], $.fn.datebox.defaults.buttons);
    d_buttons.splice(1, 0, {
        text: '清空',
        handler: function (target) {
            $(target).datebox('setValue', '');
        }
    });
    $('.easyui-datebox').datebox({
        buttons: d_buttons,
        editable: false
    });

    var dt_buttons = $.extend([], $.fn.datetimebox.defaults.buttons);
    dt_buttons.splice(2, 0, {
        text: '清空',
        handler: function (target) {
            $(target).datetimebox('setValue', '');
        }
    });
    $('.easyui-datetimebox').datetimebox({
        buttons: dt_buttons
    });
});