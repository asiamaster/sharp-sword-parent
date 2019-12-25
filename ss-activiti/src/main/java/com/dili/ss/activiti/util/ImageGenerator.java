package com.dili.ss.activiti.util;

import com.dili.ss.activiti.component.CustomProcessDiagramGenerator;
import com.dili.ss.activiti.consts.ActivitiConstants;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.image.exception.ActivitiImageException;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * activiti图片生成器
 * 解决中文显示问题
 */
public class ImageGenerator {

    public static CustomProcessDiagramGenerator diagramGenerator;

    /**
     * 生成高亮的流程图
     * @param bpmnModel
     * @param imageType
     * @param highLightedActivities
     * @param highLightedFlows
     * @param activityFontName
     * @param labelFontName
     * @param annotationFontName
     * @param customClassLoader
     * @param scaleFactor
     * @param colors
     * @return
     */
    public static InputStream generateDiagram(BpmnModel bpmnModel, String imageType, List<String> highLightedActivities, List<String> highLightedFlows, String activityFontName, String labelFontName, String annotationFontName, ClassLoader customClassLoader, double scaleFactor, Color[] colors){
        return diagramGenerator.generateDiagram(bpmnModel, imageType, highLightedActivities, highLightedFlows, activityFontName, labelFontName, annotationFontName, customClassLoader, scaleFactor, colors);
    }

    /**
     * 生成没有高亮的流程图
     * @param bpmnModel
     * @param activityFontName
     * @param labelFontName
     * @param annotationFontName
     * @param customClassLoader
     * @return
     * @throws IOException
     */
    public static byte[] generateDiagram(BpmnModel bpmnModel, String activityFontName, String labelFontName, String annotationFontName, ClassLoader customClassLoader) throws IOException {
        InputStream is = diagramGenerator.generateDiagram(bpmnModel, "png",
                null, null, ActivitiConstants.FONT_NAME,ActivitiConstants.FONT_NAME,ActivitiConstants.FONT_NAME,
                customClassLoader,1.0, new Color[]{Color.BLACK, Color.BLACK});
        return IOUtils.toByteArray(is);
    }

    public static BufferedImage createImage(BpmnModel bpmnModel) {
        return diagramGenerator.generatePngImage(bpmnModel, 1.0D);
    }

    public static BufferedImage createImage(BpmnModel bpmnModel, double scaleFactor) {
        return diagramGenerator.generatePngImage(bpmnModel, scaleFactor);
    }

    public static byte[] createByteArrayForImage(BufferedImage image, String imageType) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, imageType, out);
        } catch (IOException e) {
            throw new ActivitiImageException("Error while generating byte array for process image", e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch(IOException ignore) {
                // Exception is silently ignored
            }
        }
        return out.toByteArray();
    }
}
