package com.lyk.finger.util;

import com.microsoft.schemas.office.office.CTLock;
import com.microsoft.schemas.vml.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.stream.Stream;

/**
 * @author: lyk
 * @description: Word 添加水印工具类
 **/
public class WatermarkUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(WatermarkUtil.class);

    /** word字体 */
    private static final String FONT_NAME = "宋体";
    /** 字体大小 */
    private static final String FONT_SIZE = "0.2pt";
    /** 字体颜色 */
    private static final String FONT_COLOR = "#d0d0d0";

    /** 一个字平均长度，单位pt，用于：计算文本占用的长度（文本总个数*单字长度）*/
    private static final Integer WIDTH_PER_WORD = 10;
    /** 与顶部的间距 */
    private static Integer STYLE_TOP = 0;
    /** 文本旋转角度 */
    private static final String STYLE_ROTATION = "30";


    /**
     * @param inPutPath
     * @param putPutPath
     * @param fingerText
     * @author: lyk
     * @description: 添加水印入口方法
     * @date: 2024/1/25 23:42
     **/
    public static void waterMarkDocXDocument(String inPutPath, String putPutPath, String fingerText) {

        long beginTime = System.currentTimeMillis();

        try (
                OutputStream out = new FileOutputStream(putPutPath);
                InputStream in = new FileInputStream(inPutPath);
                OPCPackage srcPackage = OPCPackage.open(in);
                XWPFDocument doc = new XWPFDocument(srcPackage)
        ) {

            // 把整页都打上水印
            for (int lineIndex = -5; lineIndex < 20; lineIndex++) {
                STYLE_TOP = 100 * lineIndex;
                waterMarkDocXDocument(doc, fingerText);
            }

            // 输出新文档
            doc.write(out);

            LOGGER.info("添加水印成功!,一共耗时" + (System.currentTimeMillis() - beginTime) + "毫秒");

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidFormatException e) {
            throw new RuntimeException(e);
        }
    }



    /**
     * 为文档添加水印
     * @param doc        需要被处理的docx文档对象
     * @param fingerText 需要添加的水印文字
     */
    public static void waterMarkDocXDocument(XWPFDocument doc, String fingerText) {
        // 水印文字之间使用8个空格分隔
        fingerText = fingerText + repeatString(" ", 8);
        // 一行水印重复水印文字次数
        fingerText = repeatString(fingerText, 10);
        // 如果之前已经创建过 DEFAULT 的Header，将会复用
        XWPFHeader header = doc.createHeader(HeaderFooterType.DEFAULT);
        int size = header.getParagraphs().size();
        if (size == 0) {
            header.createParagraph();
        }
        CTP ctp = header.getParagraphArray(0).getCTP();
        byte[] rsidr = doc.getDocument().getBody().getPArray(0).getRsidR();
        byte[] rsidrDefault = doc.getDocument().getBody().getPArray(0).getRsidRDefault();
        ctp.setRsidP(rsidr);
        ctp.setRsidRDefault(rsidrDefault);
        CTPPr ppr = ctp.addNewPPr();
        ppr.addNewPStyle().setVal("Header");
        // 开始加水印
        CTR ctr = ctp.addNewR();
        CTRPr ctrpr = ctr.addNewRPr();
        ctrpr.addNewNoProof();
        CTGroup group = CTGroup.Factory.newInstance();
        CTShapetype shapeType = group.addNewShapetype();
        CTTextPath shapeTypeTextPath = shapeType.addNewTextpath();
        shapeTypeTextPath.setOn(STTrueFalse.T);
        shapeTypeTextPath.setFitshape(STTrueFalse.T);
        CTLock lock = shapeType.addNewLock();
        lock.setExt(STExt.VIEW);
        CTShape shape = group.addNewShape();
        shape.setId("PowerPlusWaterMarkObject");
        shape.setSpid("_x0000_s102");
        shape.setType("#_x0000_t136");
        // 设置形状样式（旋转，位置，相对路径等参数）
        shape.setStyle(getShapeStyle(fingerText));
        shape.setFillcolor(FONT_COLOR);
        // 字体设置为实心
        shape.setStroked(STTrueFalse.FALSE);
        // 绘制文本的路径
        CTTextPath shapeTextPath = shape.addNewTextpath();
        // 设置文本字体与大小
        shapeTextPath.setStyle("font-family:" + FONT_NAME + ";font-size:" + FONT_SIZE);
        shapeTextPath.setString(fingerText);
        CTPicture pict = ctr.addNewPict();
        pict.set(group);
    }

    /**
     * 构建Shape的样式参数
     *
     * @param fingerText
     * @return
     */
    private static String getShapeStyle(String fingerText) {
        StringBuilder sb = new StringBuilder();
        // 文本path绘制的定位方式
        sb.append("position: ").append("absolute");
        // 计算文本占用的长度（文本总个数*单字长度）
        sb.append(";width: ").append(fingerText.length() * WIDTH_PER_WORD).append("pt");
        // 字体高度
        sb.append(";height: ").append("20pt");
        sb.append(";z-index: ").append("-251654144");
        sb.append(";mso-wrap-edited: ").append("f");
        // 设置水印的间隔，这是一个大坑，不能用top,必须要margin-top。
        sb.append(";margin-top: ").append(STYLE_TOP);
        sb.append(";mso-position-horizontal-relative: ").append("page");
        sb.append(";mso-position-vertical-relative: ").append("page");
        sb.append(";mso-position-vertical: ").append("left");
        sb.append(";mso-position-horizontal: ").append("center");
        sb.append(";rotation: ").append(STYLE_ROTATION);
        return sb.toString();
    }

    /**
     * 将指定的字符串重复repeats次.
     */
    private static String repeatString(String pattern, int repeats) {
        StringBuilder buffer = new StringBuilder(pattern.length() * repeats);
        Stream.generate(() -> pattern).limit(repeats).forEach(buffer::append);
        return new String(buffer);
    }
}
