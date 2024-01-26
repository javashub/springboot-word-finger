package com.lyk.finger;

import com.lyk.finger.util.WatermarkUtil;

/**
 * @author lyk
 * @version 1.0
 * @date 2024/1/25 23:16
 * @description
 */
public class Main {

    public static void main(String[] args) {

        /**
         * 你路径下没文件你就自己添加了~
         */
        final String inPath = "src/main/java/com/lyk/finger/doc/aaaa.docx";
        final String outPath = "src/main/java/com/lyk/finger/doc/out.docx";

        // 添加水印
        WatermarkUtil.waterMarkDocXDocument(inPath, outPath, "落魄程序员在线炒粉");

    }

}
