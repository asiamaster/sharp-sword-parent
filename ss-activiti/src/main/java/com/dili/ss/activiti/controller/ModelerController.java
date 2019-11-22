package com.dili.ss.activiti.controller;

import com.dili.ss.activiti.service.ActivitiService;
import com.dili.ss.activiti.util.ActivitiUtils;
import com.dili.ss.domain.BaseOutput;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * 流程模型控制器
 * @author wm
 * @date 2019-3-6
 * @since 1.0
 */
@Controller
@RequestMapping("/modeler")
public class ModelerController implements ModelDataJsonConstants {

    private final Logger log = LoggerFactory.getLogger(ModelerController.class);

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ActivitiService activitiService;

    /**
     * 创建模型页面
     * 需要传参closeUrl, 指定关闭时的页面跳转URL
     * @param name
     * @param key
     * @param description
     * @param category
     * @param request
     */
    @RequestMapping(value = "/create.html", method = {RequestMethod.POST, RequestMethod.GET})
    public String createModel(@RequestParam("name") String name,
                              @RequestParam(value="key", required=false) String key,
                              @RequestParam(value="description", required=false) String description,
                              @RequestParam(value="category", required=false) String category,
                              HttpServletRequest request, RedirectAttributes attr) throws IOException {
        Model model = activitiService.createModel(key, name, description, category);
        attr.addAttribute("modelId", model.getId());
        attr.addAttribute("closeUrl", request.getParameter("closeUrl"));
        return "redirect:"+request.getContextPath()+"/modeler.html";
//        response.sendRedirect(request.getContextPath() + "/modeler.html");
    }
    /**
     * 保存流程模型设计图
     * 该地址由static/editor-app/configuration/url-config.js文件配置
     * @param modelId
     * @param name
     * @param description
     * @param json_xml
     * @param svg_xml
     */
    @RequestMapping(value = "/{modelId}/save.action", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    public void saveModel(@PathVariable String modelId, String name, String description, String json_xml, String svg_xml) {
       activitiService.saveModel(modelId, name, description, json_xml, svg_xml);
    }

    /**
     * 上传bpmn.xml文件，生成model并部署
     * @param request
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    @ResponseBody
    @RequestMapping("/uploadBpmnXml.action")
    public BaseOutput uploadBpmnXml(MultipartHttpServletRequest request) throws IOException, XMLStreamException {
        for (MultipartFile file : request.getFileMap().values()) {
            String filename = file.getOriginalFilename();
            if (filename.indexOf(".bpmn20") > 0) {
                filename = filename.substring(0, filename.indexOf(".bpmn20"));
            } else {
                filename = filename.substring(0, filename.indexOf(".xml"));
            }
            BpmnModel bpmnModel = ActivitiUtils.toBpmnModel(file.getInputStream());
            String errorMsg = ActivitiUtils.validateBpmnModel(bpmnModel);
            if(errorMsg != null){
                return BaseOutput.failure(errorMsg);
            }
            //创建部署
            Deployment deployment = repositoryService.createDeployment().name(filename).addInputStream(filename + ".bpmn20.xml", file.getInputStream()).deploy();
            //创建Model
            activitiService.createModel(bpmnModel, filename, deployment.getId());
            //参考代码
//      InputStream inputStream = file.getInputStream();
//      //创建转换对象
//      BpmnXMLConverter converter = new BpmnXMLConverter();
//      XMLInputFactory factory = XMLInputFactory.newInstance();
//      XMLStreamReader reader = factory.createXMLStreamReader(inputStream);//createXmlStreamReader
//      //将xml文件转换成BpmnModel
//      BpmnModel bpmnModel = converter.convertToBpmnModel(reader);
//      String msg = validateBpmnModel(bpmnModel);
//      if (msg != null) {
//        return BaseOutput.failure(msg);
//      }
//      repositoryService.createDeployment().name(filename).addBpmnModel(filename, bpmnModel).deploy();
            // 自动创建流程图
//      new BpmnAutoLayout(bpmnModel).execute();
        }
        return BaseOutput.success();
    }

    /**
     * 导出模型xml文件
     * @param modelId 模型id
     * @param response HttpServletResponse
     * @throws IOException
     */
    @RequestMapping(value = "/export.action", method = {RequestMethod.GET})
    public void exportModel(@RequestParam String modelId, HttpServletResponse response) throws IOException{
        activitiService.exportModel(modelId, response);
    }

    /**
     * 显示部署流程图片(无节点高亮)
     * @param modelId
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/img.action", method = {RequestMethod.GET})
    public void showImageByModelId(@RequestParam String modelId, HttpServletResponse response) throws Exception{
        activitiService.showImageByModelId(modelId, response);
    }

}
