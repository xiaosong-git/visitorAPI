package com.goldccm.util;

import java.io.IOException;



/**
 * @program: visitor
 * @description: 压缩字符串
 * @author: cwf
 * @create: 2019-09-30 11:13
 **/
public class ZipUtils {
    public static String caesar(String s, int offset) throws Exception {
        String cipher = "";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            // 是小写字母
            if (c >= 'a' && c <= 'z') {
                if (offset > 0) {
                    // 这里不光根据 offset 进行加密，还添加了该元素的下标进行加密。
                    c += (offset + i) % 26;
                } else {
                    // 这里不光根据 offset 进行加密，还添加了该元素的下标进行加密。
                    c += (offset - i) % 26;
                }
                if (c < 'a')
                    c += 26; // 向左超界
                if (c > 'z')
                    c -= 26; // 向右超界
            }
            // 是大写字母
            else if (c >= 'A' && c <= 'Z') {
                if (offset > 0) {
                    // 这里不光根据 offset 进行加密，还添加了该元素的下标进行加密。
                    c += (offset + i) % 26;
                } else {
                    // 这里不光根据 offset 进行加密，还添加了该元素的下标进行加密。
                    c += (offset - i) % 26;
                }
                if (c < 'A')
                    c += 26;
                if (c > 'Z')
                    c -= 26;
            }
            cipher += c;
        }
        return cipher;
    }

    // 测试方法
    public static void main(String[] args) throws IOException {

        //测试字符串

        String cipher = null;
        String text = null;
        try {
            cipher = caesar("abc&2&1&1&1569811676678|W0M0MzQ3NzIzNjA5NDI1OTIwXVvlj7bpnJZdWzEyQTEzQ0ZFMDA0MDc0RTBERDVBMzJFRjkxMDM0QzEzQTc0QUU0NEVBQTk4NUU4Q11b6ZmI57u05Y+RXVsxODE1MDc5Nzc0OF1baGx4el1bMjAxOS0wOS0yMCAxMDo1MF1bMjAxOS0wOS0yMCAxMjo1MF0=", 2);
            text = caesar(cipher, -2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("原文：Hello\r\n加密后：" + cipher + "\r\n解密后：" + text);

    }


}
