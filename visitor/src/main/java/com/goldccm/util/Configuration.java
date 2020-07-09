package com.goldccm.util;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import sun.misc.BASE64Encoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Configuration {

    //请求地址
    public static String REQUEST_URL = "http://111.200.45.122:8100/ctid/api/v1/request";
//    public static String REQUEST_URL = "http://MA1.chnctid.cn:8100/ctid/api/v1/request";


    //认证地址
    public static String AUTH_URL = "http://111.200.45.122:8200/ctid/api/v1/authentication";
//    public static String AUTH_URL = "http://MA1.chnctid.cn:8200/ctid/api/v1/authentication";

   // public static String picture = GetImageStrFromPath("C:\\Users\\Administrator\\Desktop\\biddata\\zp.jpg");//   ./config/123.jpg
    //public static String s = replaceBlank(picture);

    private static final double accuracy = 0.95d;

    public static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    public static String GetImageStrFromPath(String imgPath,long fileSize) {
        InputStream in = null;
        byte[] data = null;
        // 读取图片字节数组
        try {
//            in = new FileInputStream(imgPath);
            in =FilesUtils.getStreamByUrl(imgPath);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();


        System.out.println("原始数据大小=======" + data.length);

        if (data.length < fileSize * 1024) {
            return encoder.encode(data);
        }

        try {
            while (data.length > fileSize * 1024) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
                /**
                 * Thumbnails.of(InputStream... inputStreams) 从流读入源;
                 * .scale(double scale) 按比例缩放，0~1缩小，1原比例，>1放大;
                 * .scale(doublescaleWidth, double scaleHeight) 长宽各自设置比例，会拉伸;
                 * .outputQuality(double quality) 质量0.0<=quality<=1.0;
                 * .toOutputStream(OutputStream os) 无返回，写入outputStream里;
                 *
                 */
                Thumbnails.of(inputStream).scale(accuracy)
                        .outputQuality(accuracy).toOutputStream(outputStream);
                data = outputStream.toByteArray();
            }
        } catch (Exception e) {
           // logger.error("图片压缩失败=======", e);
        }
        System.out.println("压缩后数据大小=======" + data.length);
        return encoder.encode(data);

    }
//
//    public static void main(String[] args) {
//        System.out.println(s);
//    }
}
