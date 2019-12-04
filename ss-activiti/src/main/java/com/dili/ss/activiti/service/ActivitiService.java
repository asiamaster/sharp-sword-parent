package com.dili.ss.activiti.service;

import com.dili.ss.activiti.dto.ActModelVO;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * 工作流中台服务
 */
public interface ActivitiService extends ModelDataJsonConstants {
    /**
     * 创建设计器模型(重载)
     * @param key 必填
     * @param name  必填
     * @param description 选填
     * @param category 选填
     * @return
     * @throws UnsupportedEncodingException
     */
    @Transactional
    Model createModel(String key, String name, String description, String category) throws UnsupportedEncodingException;

    /**
     * 创建设计器模型
     * @param actModelVO
     * @return
     * @throws UnsupportedEncodingException
     */
    @Transactional
    Model createModel(ActModelVO actModelVO) throws UnsupportedEncodingException;

    /**
     * 创建Model
     * 用于上传xml，并部署后，再创建模型
     * @param bpmnModel
     * @param filename
     * @param deploymentId
     * @throws IOException
     */
    void createModel(BpmnModel bpmnModel, String filename, String deploymentId) throws IOException, XMLStreamException;

    /**
     * 设计器保存模型
     * @param modelId
     * @param name
     * @param description
     * @param json_xml
     * @param svg_xml
     */
    void saveModel(String modelId, String name, String description, String json_xml, String svg_xml);

    /**
     * 根据modelId部署流程
     * @param modelId
     * @throws Exception
     * @return Deployment
     */
    Deployment deployByModelId(String modelId) throws ActivitiException, IOException;

    /**
     * 根据modelId部署流程和表单
     * @param modelId
     * @throws Exception
     * @return Deployment
     */
    Deployment deployFormByModelId(String modelId) throws ActivitiException, IOException;

    /**
     * 根据模型id查询
     * @param modelId 模型id
     * @return
     */
    Model getModelById(String modelId);

    /**
     * 删除模型
     * @param modelId 模型id
     */
    @Transactional
    void deleteModel(String modelId);

    /**
     * 查询模型列表
     * @param pageable
     * @return
     */
    Page<Model> getAllModels(Pageable pageable);

    /**
     * 删除流程部署
     * @param deploymentId
     */
    @Transactional
    void deleteDeployment(String deploymentId);

    /**
     * 导出模型xml文件
     * @param modelId 模型id
     * @param response HttpServletResponse
     * @throws IOException
     */
    void exportModel(String modelId, HttpServletResponse response) throws IOException;

    /**
     * 根据实例id显示图片
     * @param processInstanceId
     * @param response
     * @throws Exception
     */
    void showImageByProcessInstanceId(String processInstanceId, HttpServletResponse response) throws Exception;

    /**
     * 根据部署id显示图片
     * @param deploymentId
     * @param response
     * @throws Exception
     */
    void showImageByDeploymentId(String deploymentId, HttpServletResponse response) throws Exception;

    /**
     * 根据BpmnModel生成图片
     * @param bpmnModel
     * @param response
     * @throws Exception
     */
    void showImageByBpmnModel(BpmnModel bpmnModel, HttpServletResponse response) throws Exception;

    /**
     * 根据model显示图片
     * @param modelId
     * @param response
     * @throws Exception
     */
    void showImageByModelId(String modelId, HttpServletResponse response) throws Exception;

    /**
     * 流程跟踪图片
     *
     * @param processDefinitionId
     *            流程定义ID
     * @param processInstanceId
     *            流程运行ID
     * @param out
     *            输出流
     * @throws Exception
     */
    void processTracking(String processDefinitionId, String processInstanceId, OutputStream out) throws Exception;

    /**
     * 流程是否已经结束
     *
     * @param processInstanceId 流程实例ID
     * @return
     */
    boolean isFinished(String processInstanceId);

    /**
     * 判断流程是否结束，查询正在执行的执行对象表
     *
     * @param processInstanceId 流程实例ID
     * @return
     */
    boolean isFinished2(String processInstanceId);

    /**
     * 根据流程定义key获取最新的formKey
     * @param procDefKey
     * @return
     */
    String getLatestFormKeyByProcDefKey(String procDefKey);
}
