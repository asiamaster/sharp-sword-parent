package com.dili.ss.activiti.service.impl;

import com.dili.ss.activiti.component.CustomBpmnJsonConverter;
import com.dili.ss.activiti.consts.ActivitiConstants;
import com.dili.ss.activiti.dto.ActModelVO;
import com.dili.ss.activiti.service.ActivitiService;
import com.dili.ss.activiti.util.ImageGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Activiti通用服务
 * @author asiamaster
 * @date 2019-2-27 10:02:54
 * @since 1.0
 */
@Service
@ConditionalOnExpression("'${activiti.enable}'=='true'")
public class ActivitiServiceImpl implements ActivitiService {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private SpringProcessEngineConfiguration springProcessEngineConfiguration;

    private final Logger log = LoggerFactory.getLogger(ActivitiServiceImpl.class);
    //高亮线
    private final Color LINE_COLOR = new Color(30, 160, 30);
    //高亮活动
    private final Color ACTIVITY_COLOR = new Color(200, 30, 30);

    /**
     * 创建设计器模型(重载)
     * @param key 必填
     * @param name  必填
     * @param description 选填
     * @param category 选填
     * @return
     * @throws UnsupportedEncodingException
     */
    @Override
    @Transactional
    public Model createModel(String key, String name, String description, String category) throws UnsupportedEncodingException {
        ActModelVO actModelVO = new ActModelVO();
        actModelVO.setKey(key);
        actModelVO.setName(name);
        actModelVO.setDescription(description);
        actModelVO.setCategory(category);
        return createModel(actModelVO);
    }
    /**
     * 创建设计器模型
     * @param actModelVO
     * @return
     * @throws UnsupportedEncodingException
     */
    @Override
    @Transactional
    public Model createModel(ActModelVO actModelVO) throws UnsupportedEncodingException {
        //初始化一个空模型
        Model model = repositoryService.newModel();
        model.setKey(StringUtils.defaultString(actModelVO.getKey()));
        model.setName(actModelVO.getName());
        model.setCategory(actModelVO.getCategory());
        model.setVersion(Integer.parseInt(String.valueOf(repositoryService.createModelQuery().modelKey(model.getKey()).count()+1)));
        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(ModelDataJsonConstants.MODEL_NAME, model.getName());
        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, model.getVersion());
        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, actModelVO.getDescription());
        model.setMetaInfo(modelNode.toString());
        repositoryService.saveModel(model);
        //完善ModelEditorSource
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace","http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.set("stencilset", stencilSetNode);
        repositoryService.addModelEditorSource(model.getId(),editorNode.toString().getBytes("utf-8"));
        return model;
    }

    /**
     * 创建Model
     * 用于上传xml，并部署后，再创建模型
     * @param bpmnModel
     * @param filename
     * @param deploymentId
     * @throws IOException
     */
    @Override
    public void createModel(BpmnModel bpmnModel, String filename, String deploymentId) throws IOException, XMLStreamException {
        Model model = repositoryService.newModel();
        model.setDeploymentId(deploymentId);
        model.setVersion(Integer.parseInt(String.valueOf(repositoryService.createModelQuery().modelKey(bpmnModel.getMainProcess().getId()).count()+1)));
        model.setName(filename);
        model.setKey(bpmnModel.getMainProcess().getId());
        ObjectNode modelObjectNode = objectMapper.createObjectNode();
        modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, filename);
        modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, model.getVersion());
        modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, bpmnModel.getMainProcess().getDocumentation());
        model.setMetaInfo(modelObjectNode.toString());
        ObjectNode objectNode = new BpmnJsonConverter().convertToJson(bpmnModel);
        //保存模型
        repositoryService.saveModel(model);
        repositoryService.addModelEditorSource(model.getId(), objectNode.toString().getBytes("utf-8"));
    }

    /**
     * 设计器保存模型
     * @param modelId
     * @param name
     * @param description
     * @param json_xml
     * @param svg_xml
     */
    @Override
    public void saveModel(String modelId, String name, String description, String json_xml, String svg_xml){
        try {
            Model model = repositoryService.getModel(modelId);
            ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
            modelJson.put(MODEL_NAME, name);
            modelJson.put(MODEL_DESCRIPTION, description);
            model.setMetaInfo(modelJson.toString());
            model.setName(name);
//      通过RepositoryService的saveModel方法将模型的元数据存入数据库的ACT_RE_MODEL表
            repositoryService.saveModel(model);
//      通过RepositoryService的addModelEditorSource方法将模型JSON数据UTF8字符串存入数据库的ACT_GE_BYTEARRAY表
            repositoryService.addModelEditorSource(model.getId(), json_xml.getBytes("utf-8"));

            //生成图片(此方式中文会有乱码)
//            InputStream svgStream = new ByteArrayInputStream(svg_xml.getBytes("utf-8"));
//            TranscoderInput input = new TranscoderInput(svgStream);
//            PNGTranscoder transcoder = new PNGTranscoder();
//            // Setup output
//            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//            TranscoderOutput output = new TranscoderOutput(outStream);
//            // Do the transformation
//            transcoder.transcode(input, output);
//            final byte[] result = outStream.toByteArray();
//            outStream.close();
            //下面的方式不会有中文乱码问题
            ObjectNode modelNode = (ObjectNode) new ObjectMapper()
                    .readTree(json_xml.getBytes("utf-8"));
            BpmnModel bpmnModel = new CustomBpmnJsonConverter().convertToBpmnModel(modelNode);

//            final byte[] result = ImageGenerator.createByteArrayForImage(ImageGenerator.createImage(bpmnModel), "png");
            final byte[] result = ImageGenerator.generateDiagram(bpmnModel, ActivitiConstants.FONT_NAME, ActivitiConstants.FONT_NAME, ActivitiConstants.FONT_NAME, springProcessEngineConfiguration.getClassLoader());
//      通过Apache™ Batik SVG Toolkit将模型的SVG图像数据转换成PNG格式，通过RepositoryService的addModelEditorSourceExtra方法将PNG图像存入数据库的ACT_GE_BYTEARRAY表
            repositoryService.addModelEditorSourceExtra(model.getId(), result);
        } catch (Exception e) {
            log.error("Error saving model", e);
            throw new ActivitiException("Error saving model", e);
        }
    }

    /**
     * 根据modelId部署流程
     * @param modelId
     * @throws Exception
     * @return Deployment
     */
    @Override
    public Deployment deployByModelId(String modelId) throws ActivitiException, IOException {
        //获取模型
        Model modelData = repositoryService.getModel(modelId);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
        if (bytes == null) {
            throw new ActivitiException("模型数据为空，请先设计流程并成功保存，再进行发布");
        }
        JsonNode modelNode = new ObjectMapper().readTree(bytes);
        //解决activiti中由模板转换的流程图连线名称缺失问题
        BpmnModel bpmnModel = new CustomBpmnJsonConverter().convertToBpmnModel(modelNode);
        if(bpmnModel.getProcesses().size() == 0){
            throw new ActivitiException("数据模型不符要求，请至少设计一条主线流程");
        }
        //发布流程
        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = null;
        try {
            deployment = repositoryService.createDeployment()
                    .name(modelData.getName())
                    .enableDuplicateFiltering()
                    .addBpmnModel(processName, bpmnModel)
                    .deploy();
        } catch (org.activiti.bpmn.exceptions.XMLException e) {
            e.printStackTrace();
            throw new ActivitiException("流程节点的id不能以数字开头");
        }
        modelData.setDeploymentId(deployment.getId());
        repositoryService.saveModel(modelData);
        return deployment;
    }

    /**
     * 根据modelId部署流程和表单
     * @param modelId
     * @throws Exception
     * @return Deployment
     */
    @Override
    public Deployment deployFormByModelId(String modelId) throws ActivitiException, IOException {
        //获取模型
        Model modelData = repositoryService.getModel(modelId);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
        if (bytes == null) {
            throw new ActivitiException("模型数据为空，请先设计流程并成功保存，再进行发布");
        }
        JsonNode modelNode = new ObjectMapper().readTree(bytes);
        //解决activiti中由模板转换的流程图连线名称缺失问题
        BpmnModel bpmnModel = new CustomBpmnJsonConverter().convertToBpmnModel(modelNode);
        if(bpmnModel.getProcesses().size() == 0){
            throw new ActivitiException("数据模型不符要求，请至少设计一条主线流程");
        }
        //发布流程
        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = null;
        try {
            deployment = repositoryService.createDeployment()
                    .name(modelData.getName())
                    .enableDuplicateFiltering()
                    .addBpmnModel(processName, bpmnModel)
                    .addString("start.form", "<input type=\"text\" id=\"name\" name=\"name\"></input>")
                    .deploy();
        } catch (org.activiti.bpmn.exceptions.XMLException e) {
            e.printStackTrace();
            throw new ActivitiException("流程节点的id不能以数字开头");
        }
        modelData.setDeploymentId(deployment.getId());
        repositoryService.saveModel(modelData);
        return deployment;
    }

    /**
     * 根据模型id查询
     * @param modelId 模型id
     * @return
     */
    @Override
    public Model getModelById(String modelId){
        return repositoryService.getModel(modelId);
    }

    /**
     * 删除模型
     * @param modelId 模型id
     */
    @Override
    @Transactional
    public void deleteModel(String modelId){
        repositoryService.deleteModel(modelId);
    }

    /**
     * 查询模型列表
     * @param pageable
     * @return
     */
    @Override
    public Page<Model> getAllModels(Pageable pageable){
        ModelQuery modelQuery = repositoryService.createModelQuery().latestVersion().orderByLastUpdateTime().desc();
        return new PageImpl<>(modelQuery.listPage(pageable.getPageNumber()*pageable.getPageSize(), pageable.getPageSize()), pageable, modelQuery.count());
    }

    /**
     * 删除流程部署
     * @param deploymentId
     */
    @Override
    @Transactional
    public void deleteDeployment(String deploymentId){
        repositoryService.deleteDeployment(deploymentId);
    }

    /**
     * 导出模型xml文件
     * @param modelId 模型id
     * @param response HttpServletResponse
     * @throws IOException
     */
    @Override
    public void exportModel(String modelId, HttpServletResponse response) throws IOException{
        Model modelData = repositoryService.getModel(modelId);
        BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
        ObjectNode editorNode = (ObjectNode) new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
        BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
        BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
        byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);
        ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);
        IOUtils.copy(in, response.getOutputStream());
        String filename = bpmnModel.getMainProcess().getId() + ".bpmn20.xml";
        response.setHeader("Content-Disposition", "attachment; filename=" + java.net.URLEncoder.encode(filename, "UTF-8"));
        response.flushBuffer();
    }

    /**
     * 根据实例id显示图片
     * @param processInstanceId
     * @param response
     * @throws Exception
     */
    @Override
    public void showImageByProcessInstanceId(String processInstanceId, HttpServletResponse response) throws Exception{
        showImageByBpmnModel(repositoryService.getBpmnModel(processInstanceId), response);
    }

    /**
     * 根据部署id显示图片
     * @param deploymentId
     * @param response
     * @throws Exception
     */
    @Override
    public void showImageByDeploymentId(String deploymentId, HttpServletResponse response) throws Exception{
        // 第一种方案因为是实时根据BpmnModel生成图片，不会有乱码问题，但是效率较低
//        Model model = repositoryService.createModelQuery().deploymentId(deploymentId).singleResult();
//        if(model == null){
//            return;
//        }
//        showImageByModelId(model.getId(), response);
        // 下面的代码可能会因`org.activiti.image.impl.DefaultProcessDiagramGenerator`类的问题，产生乱码
        // 目前已经在ModelController.saveModel保存模型时解决了中文乱码问题
        // 更好面的代码由于是直接读取资源流，所以效率应该较高
        // 获取图片资源名称
        List<String> list = repositoryService.getDeploymentResourceNames(deploymentId);
        // 定义图片资源的名称
        String resourceName = "";
        if (list != null && list.size() > 0) {
            for (String name : list) {
                if (name.indexOf(".png") >= 0 || name.indexOf(".jpg") >= 0) {
                    resourceName = name;
                    break;
                }
            }
        }
        InputStream inputStream = repositoryService.getResourceAsStream(deploymentId,resourceName);
        int b = -1;
        OutputStream outputStream = response.getOutputStream();
        while ((b=inputStream.read())!=-1){
            outputStream.write(b);
        }
        inputStream.close();
        outputStream.close();
    }

    /**
     * 根据BpmnModel生成图片
     * @param bpmnModel
     * @param response
     * @throws Exception
     */
    @Override
    public void showImageByBpmnModel(BpmnModel bpmnModel, HttpServletResponse response) throws Exception{
        InputStream is = ImageGenerator.generateDiagram(bpmnModel, "png",
                null, null, ActivitiConstants.FONT_NAME,ActivitiConstants.FONT_NAME,ActivitiConstants.FONT_NAME,
                springProcessEngineConfiguration.getClassLoader(),1.0, new Color[]{LINE_COLOR, ACTIVITY_COLOR});
        OutputStream outputStream = response.getOutputStream();
        int b = -1 ;
        while ((b=is.read())!=-1){
            outputStream.write(b);
        }
        is.close();
        outputStream.close();
    }

    /**
     * 根据model显示图片
     * @param modelId
     * @param response
     * @throws Exception
     */
    @Override
    public void showImageByModelId(String modelId, HttpServletResponse response) throws Exception{
        Model modelData = repositoryService.getModel(modelId);
        ObjectNode modelNode = (ObjectNode) new ObjectMapper()
                .readTree(repositoryService.getModelEditorSource(modelData.getId()));
        BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        showImageByBpmnModel(bpmnModel, response);
    }

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
    @Override
    public void processTracking(String processDefinitionId, String processInstanceId, OutputStream out) throws Exception {
        if(StringUtils.isBlank(processDefinitionId)){
            processDefinitionId = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult().getProcessDefinitionId();
        }
        // 当前活动节点、活动线
        List<String> activeActivityIds = new ArrayList<String>();
        List<String> highLightedFlows = new ArrayList<String>();

        /**
         * 获得当前活动的节点
         */
        if (this.isFinished(processInstanceId)) {// 如果流程已经结束，则得到结束节点

            List<HistoricActivityInstance> historicActivityInstances = historyService
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(processInstanceId).activityType("endEvent")
                    .list();
            for(HistoricActivityInstance historicActivityInstance : historicActivityInstances){
                activeActivityIds.add(historicActivityInstance.getActivityId());
            }
        } else {// 如果流程没有结束，则取当前活动节点
            // 根据流程实例ID获得当前处于活动状态的ActivityId合集
            activeActivityIds = runtimeService
                    .getActiveActivityIds(processInstanceId);
        }

        /**
         * 获得活动的线
         */
        // 获得历史活动记录实体（通过启动时间正序排序，不然有的线可以绘制不出来）
        List<HistoricActivityInstance> historicActivityInstances = historyService
                .createHistoricActivityInstanceQuery().processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc().list();
        // 计算活动线
        highLightedFlows = this.getHighLightedFlows((ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                                .getDeployedProcessDefinition(processDefinitionId), historicActivityInstances);
        /**
         * 绘制图形(结束也可以正确绘制)
         */
//        if (null != activeActivityIds) {
            InputStream imageStream = null;
            try {
                // 根据流程定义ID获得BpmnModel
                BpmnModel bpmnModel = repositoryService
                        .getBpmnModel(processDefinitionId);
                // 输出资源内容到相应对象
//                imageStream = springProcessEngineConfiguration.getProcessDiagramGenerator().generateDiagram(bpmnModel, "png",
//                        activeActivityIds, highLightedFlows, "宋体","宋体","宋体",
//                        springProcessEngineConfiguration.getClassLoader(),
//                                1.0);

                imageStream = ImageGenerator.generateDiagram(bpmnModel, "png",
                        activeActivityIds, highLightedFlows, ActivitiConstants.FONT_NAME,ActivitiConstants.FONT_NAME,ActivitiConstants.FONT_NAME,
                        //Color数据第一个是线的颜色，第二个是活动的颜色
                        springProcessEngineConfiguration.getClassLoader(),1.0, new Color[]{LINE_COLOR, ACTIVITY_COLOR});
                IOUtils.copy(imageStream, out);
            } finally {
                IOUtils.closeQuietly(imageStream);
            }
//        }
    }

    /**
     * 流程是否已经结束
     *
     * @param processInstanceId 流程实例ID
     * @return
     */
    @Override
    public boolean isFinished(String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery().finished().processInstanceId(processInstanceId).count() > 0;
    }

    /**
     * 判断流程是否结束，查询正在执行的执行对象表
     *
     * @param processInstanceId 流程实例ID
     * @return
     */
    @Override
    public boolean isFinished2(String processInstanceId) {
        ProcessInstance pi = runtimeService//
                .createProcessInstanceQuery()//创建流程实例查询对象
                .processInstanceId(processInstanceId)
                .singleResult();
        //pi == null说明流程实例结束了
        return pi == null ? true : false;
    }

    /**
     * 获得高亮线
     *
     * @param processDefinitionEntity
     *            流程定义实体
     * @param historicActivityInstances
     *            历史活动实体
     * @return 线ID集合
     */
    private List<String> getHighLightedFlows(
            ProcessDefinitionEntity processDefinitionEntity,
            List<HistoricActivityInstance> historicActivityInstances) {
        List<String> highFlows = new ArrayList<>();// 用以保存高亮的线flowId
        List<String> highActivitiImpl = new ArrayList<>();
        for(HistoricActivityInstance historicActivityInstance : historicActivityInstances){
            highActivitiImpl.add(historicActivityInstance.getActivityId());
        }
        for(HistoricActivityInstance historicActivityInstance : historicActivityInstances){
            ActivityImpl activityImpl = processDefinitionEntity.findActivity(historicActivityInstance.getActivityId());
            List<PvmTransition> pvmTransitions = activityImpl.getOutgoingTransitions();
            // 对所有的线进行遍历
            for (PvmTransition pvmTransition : pvmTransitions) {
                // 如果取出的线的目标节点存在id相同的节点，保存该线的id，进行高亮显示
                ActivityImpl pvmActivityImpl = (ActivityImpl) pvmTransition.getDestination();
                if (highActivitiImpl.contains(pvmActivityImpl.getId())) {
                    highFlows.add(pvmTransition.getId());
                }
            }
        }
        return highFlows;
    }

}
