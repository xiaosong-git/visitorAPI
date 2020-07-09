package com.goldccm.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

public class FilesUtils {
	/**
	 * 生成Byte流 TODO
	 * 
	 * @history
	 * @knownBugs
	 * @param
	 * @return
	 * @exception
	 */
	public static byte[] getBytesFromFile(File file) {
		byte[] ret = null;
		try {
			if (file == null) {
				// log.error("helper:the file is null!");
				return null;
			}
			FileInputStream in = new FileInputStream(file);
			ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
			byte[] b = new byte[4096];
			int n;
			while ((n = in.read(b)) != -1) {
				out.write(b, 0, n);
			}
			in.close();
			out.close();
			ret = out.toByteArray();
		} catch (IOException e) {
			// log.error("helper:get bytes from file process error!");
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * 把流生成图片 TODO
	 * 
	 * @history
	 * @knownBugs
	 * @param
	 * @return
	 * @exception
	 */
	public static File getFileFromBytes(byte[] files, String outputFile,String fileName) {
		File ret = null;
		
		BufferedOutputStream stream = null;
		try {
		if (StringUtils.isBlank(fileName)) {
			ret = new File(outputFile);
		}else {
			ret = new File(outputFile+fileName);
		}
			
			
			File fileParent = ret.getParentFile();  
			if(!fileParent.exists()){  
			    fileParent.mkdirs();  
			}  
			ret.createNewFile(); 
			
			FileOutputStream fstream = new FileOutputStream(ret);
			
			stream = new BufferedOutputStream(fstream);
			
			stream.write(files);
			
			
		} catch (Exception e) {
		
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					// log.error("helper:get file from byte process error!");
					e.printStackTrace();
				}
			}
		}
		return ret;
	}

	/***
	 * 根据路径获取
	 * 
	 * @param path
	 * @return
	 */
	public static byte[] getPhoto(String path) {
		byte[] data = null;
		FileImageInputStream input = null;
		try {
			input = new FileImageInputStream(new File(path));
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int numBytesRead = 0;
			while ((numBytesRead = input.read(buf)) != -1) {
				output.write(buf, 0, numBytesRead);
			}
			data = output.toByteArray();
			output.close();
			input.close();
		} catch (FileNotFoundException ex1) {
			ex1.printStackTrace();
		} catch (IOException ex1) {
			ex1.printStackTrace();
		}
		return data;
	}

	/**
	 * 获取网络地址图片
	 * @param strUrl
	 * @return
	 */
	public static byte[] getImageFromNetByUrl(String strUrl) {
	    try {
	        URL url = new URL(strUrl);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("GET");
	        conn.setConnectTimeout(5 * 1000);
	        InputStream inStream = conn.getInputStream();// 通过输入流获取图片数据
	        byte[] btImg = readInputStream(inStream);// 得到图片的二进制数据
	        return btImg;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	/**
	 * 获取网络地址图片
	 * @param strUrl
	 * @return
	 */
	public static InputStream getStreamByUrl(String strUrl) {
		try {
			URL url = new URL(strUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5 * 1000);
			InputStream inStream = conn.getInputStream();// 通过输入流获取图片数据// 得到图片的二进制数据
			return inStream;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	   /**
	 * 从输入流中获取数据
	 *
	 * @param inStream
	 *            输入流
	 * @return
	 * @throws Exception
	 */
	public static byte[] readInputStream(InputStream inStream) throws Exception {
	    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	    byte[] buffer = new byte[10240];
	    int len = 0;
	    while ((len = inStream.read(buffer)) != -1) {
	        outStream.write(buffer, 0, len);
	    }
	    inStream.close();
	    return outStream.toByteArray();
	}

	/**
	 * 文件下发
	 * update by cwf  2019/8/26 11:34
	 */
	public static void sendFile(String path, String filename, HttpServletResponse response) throws Exception {
		//否则直接使用response.setHeader("content-disposition", "attachment;filename=" + filename);即可
		response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(path); //获取文件的流
			int len = 0;
			//缓存作用
			byte buf[] = new byte[1024];
			//输出流
			out = response.getOutputStream();
			while ((len = in.read(buf)) > 0) {
				//向客户端输出，实际是把数据存放在response中，然后web服务器再去response中读取
				out.write(buf, 0, len);
			}
		}catch (Exception e){
			e.printStackTrace();
			return;
		}finally {
			if(in!=null){
				in.close();
			}
			if (out != null) {
				out.close();
				out.flush();
			}
		}
	}

	/**
	 * 将图片压缩
	 * @param srcImgData
	 * @param maxSize  10240L （10KB）
	 * @return
	 * @throws Exception
	 */

	public static byte[] compressUnderSize(byte[] srcImgData, long maxSize)
			throws Exception {
		double scale = 0.9;
		byte[] imgData = Arrays.copyOf(srcImgData, srcImgData.length);

		if (imgData.length > maxSize) {
			do {
				try {
					imgData = compress(imgData, scale);

				} catch (IOException e) {
					throw new IllegalStateException("压缩图片过程中出错，请及时联系管理员！", e);
				}

			} while (imgData.length > maxSize);
		}

		return imgData;
	}

	public static byte[] compress(byte[] srcImgData, double scale)
			throws IOException {
		BufferedImage bi = ImageIO.read(new ByteArrayInputStream(srcImgData));
		int width = (int) (bi.getWidth() * scale); // 源图宽度
		int height = (int) (bi.getHeight() * scale); // 源图高度

		Image image = bi.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		BufferedImage tag = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);

		Graphics g = tag.getGraphics();
		g.setColor(Color.RED);
		g.drawImage(image, 0, 0, null); // 绘制处理后的图
		g.dispose();

		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		ImageIO.write(tag, "JPEG", bOut);

		return bOut.toByteArray();
	}

}
