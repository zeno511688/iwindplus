/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.img.ImgUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.util.domain.enums.FileTypeEnum;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;

/**
 * 拼接图片工具类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
public class ImageUtil extends ImgUtil {

    /**
     * 图片的间隙.
     */
    private static final int SIDE = 6;

    /**
     * 画板尺寸宽.
     */
    private static final int WIDTH = 112;

    /**
     * 生成群组头像.
     *
     * @param paths  图片链接
     * @param format 图片格式
     * @return byte[]
     */
    public static byte[] generate(List<String> paths, String format) {
        return generate(paths, WIDTH, WIDTH, SIDE, Optional.ofNullable(format).orElse(FileTypeEnum.PNG.getType()));
    }

    /**
     * 生成群组头像.
     *
     * @param paths  图片链接
     * @param width  画布宽
     * @param height 画布高
     * @param side   图片的间隙
     * @param format 图片格式
     * @return byte[]
     */
    public static byte[] generate(List<String> paths, int width, int height, int side, String format) {
        List<BufferedImage> bufferedImages = new ArrayList<>(10);
        int imageSize;
        if (paths.size() <= 1) {
            //若为一张图片
            imageSize = height - (2 * side);
        } else if (paths.size() > 1 && paths.size() < CommonConstant.NumberConstant.NUMBER_FIVE) {
            //若为2-4张图片
            imageSize = (height - (3 * side)) / 2;
        } else {
            //若>=5张图片
            imageSize = (height - (4 * side)) / 3;
        }
        int finalImageSize = imageSize;
        int finalImageSize1 = imageSize;
        paths.forEach(entity -> {
            BufferedImage resize = resize(entity, finalImageSize, finalImageSize1, true);
            if (resize != null) {
                bufferedImages.add(resize);
            }
        });
        BufferedImage outImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = outImage.createGraphics();
        try {
            // 设置背景色
            g2d.setBackground(new Color(0.906f, 0.906f, 0.906f, 0.5f));
            // 通过使用当前绘图表面的背景色进行填充来清除指定的矩形。
            g2d.clearRect(0, 0, width, height);
            // 开始拼凑 根据图片的数量判断该生成那种样式的组合头像目前为九种
            patchworkPic(width, side, bufferedImages, imageSize, g2d);
        } finally {
            g2d.dispose();
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            ImageIO.write(outImage, format, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
        return new byte[0];
    }

    private static void patchworkPic(int width, int side, List<BufferedImage> bufferedImages, int imageSize, Graphics2D g2d) {
        for (int i = 1; i <= bufferedImages.size(); i++) {
            Integer size = bufferedImages.size();
            switch (size) {
                case 1:
                    g2d.drawImage(bufferedImages.get(i - 1), side, side, null);
                    break;
                case 2:
                    switchCase(i == 1, g2d, bufferedImages, i, side, (width - imageSize) / 2, (width - imageSize) / 2, 2 * side, imageSize);
                    break;
                case 3:
                    switchCase(i == 1, g2d, bufferedImages, i, (width - imageSize) / 2, side, imageSize + (2 * side), (i - 1) * side,
                        (i - 2) * imageSize);
                    break;
                case 4:
                    switchCase(i <= 2, g2d, bufferedImages, i, i * side + (i - 1) * imageSize, side, imageSize + 2 * side, (i - 2) * side,
                        (i - 3) * imageSize);
                    break;
                case 5:
                    switchCase(i <= 2, g2d, bufferedImages, i, (width - 2 * imageSize - side) / 2 + (i - 1) * imageSize + (i - 1) * side,
                        (width - 2 * imageSize - side) / 2, ((width - 2 * imageSize - side) / 2) + imageSize + side, (i - 2) * side,
                        (i - 3) * imageSize);
                    break;
                case 6:
                    switchCase(i <= 3, g2d, bufferedImages, i, side * i + imageSize * (i - 1), (width - 2 * imageSize - side) / 2,
                        ((width - 2 * imageSize - side) / 2) + imageSize + side, ((i - 3) * side), ((i - 4) * imageSize));
                    break;
                case 7:
                    switchCase2(i, 1, g2d, bufferedImages, 2 * side + imageSize, side, 4, 2, imageSize, 7, 5);
                    break;
                case 8:
                    switchCase2(i, 2, g2d, bufferedImages, (width - 2 * imageSize - side) / 2 + (i - 1) * imageSize + (i - 1) * side, side, 5, 3,
                        imageSize, 8, 6);
                    break;
                case 9:
                    switchCase2(i, 3, g2d, bufferedImages, (i * side) + ((i - 1) * imageSize), side, 6, 4, imageSize, 9, 7);
                    break;
                default:
                    break;
            }
        }
    }

    private static void switchCase2(int i, int x, Graphics2D g2d, List<BufferedImage> bufferedImages, int side, int side1, int x1, int x2,
        int imageSize, int x3, int x4) {
        if (i <= x) {
            g2d.drawImage(bufferedImages.get(i - 1), side, side1, null);
        }
        if (i <= x1 && i > x) {
            g2d.drawImage(bufferedImages.get(i - 1), ((i - x) * side1) + ((i - x2) * imageSize), 2 * side1 + imageSize, null);
        }
        if (i <= x3 && i > x1) {
            g2d.drawImage(bufferedImages.get(i - 1), ((i - x1) * side1) + ((i - x4) * imageSize), 3 * side1 + 2 * imageSize, null);
        }
    }

    private static void switchCase(boolean i, Graphics2D g2d, List<BufferedImage> bufferedImages, int i1, int side, int width, int width1, int side1,
        int imageSize) {
        if (i) {
            g2d.drawImage(bufferedImages.get(i1 - 1), side, width, null);
        } else {
            g2d.drawImage(bufferedImages.get(i1 - 1), side1 + imageSize, width1, null);
        }
    }

    /**
     * 图片缩放.
     *
     * @param filePath 图片路径
     * @param height   高度
     * @param width    宽度
     * @param bb       比例不对时是否需要补白
     * @return BufferedImage
     */
    public static BufferedImage resize(String filePath, int height, int width, boolean bb) {
        try {
            // 缩放比例
            double ratio;
            BufferedImage bi;
            if (filePath.startsWith(CommonConstant.NetWorkConstant.HTTP_PREFIX)
                || filePath.startsWith(CommonConstant.NetWorkConstant.HTTPS_PREFIX)) {
                bi = ImageIO.read(new URI(filePath).toURL());
            } else {
                bi = ImageIO.read(new File(filePath));
            }
            Image iTemp = bi.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            // 计算比例
            if ((bi.getHeight() > height) || (bi.getWidth() > width)) {
                if (bi.getHeight() > bi.getWidth()) {
                    ratio = (double) height / bi.getHeight();
                } else {
                    ratio = (double) width / bi.getWidth();
                }
                AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(ratio, ratio), null);
                iTemp = op.filter(bi, null);
            }
            if (bb) {
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();
                g.setColor(Color.white);
                g.fillRect(0, 0, width, height);
                if (width == iTemp.getWidth(null)) {
                    g.drawImage(iTemp, 0, (height - iTemp.getHeight(null)) / 2, iTemp.getWidth(null), iTemp.getHeight(null), Color.white, null);
                } else {
                    g.drawImage(iTemp, (width - iTemp.getWidth(null)) / 2, 0, iTemp.getWidth(null), iTemp.getHeight(null), Color.white, null);
                }
                g.dispose();
                iTemp = image;
            }
            return (BufferedImage) iTemp;
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);
        } catch (URISyntaxException ex) {
            log.error(ExceptionConstant.URI_SYNTAX_EXCEPTION, ex);
        }
        return null;
    }
}
