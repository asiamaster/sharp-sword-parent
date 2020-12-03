package com.dili.ss.activiti.component;

import org.activiti.bpmn.converter.CallActivityXMLConverter;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CallActivity;

import javax.xml.stream.XMLStreamReader;

/**
 * 调用子流程自定义转换
 * 将主流程的变量传递到子流程
 * @author: WM
 * @time: 2020/12/3 10:04
 */
public class CustomCallActivityXMLConverter extends CallActivityXMLConverter {
    /**
     * 重写转换方法，手动赋值
     */
    @Override
    protected BaseElement convertXMLToElement(XMLStreamReader xtr, BpmnModel model) throws Exception {
        CallActivity callActivity = new CallActivity();
        BpmnXMLUtil.addXMLLocation(callActivity, xtr);
        callActivity.setCalledElement(xtr.getAttributeValue(null, ATTRIBUTE_CALL_ACTIVITY_CALLEDELEMENT));
        callActivity.setInheritVariables(Boolean.TRUE);
        parseChildElements(getXMLElementName(), callActivity, childParserMap, model, xtr);
        return callActivity;
    }
}
