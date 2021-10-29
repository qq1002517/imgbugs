
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.alibaba.fastjson.JSONObject;

public class HttpUtil {

	private final static String BOUNDARY = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");// 边界标识
	private final static String PREFIX = "--";// 必须存在
	private final static String LINE_END = "\r\n";

	/**
	 * POST Multipart Request
	 * 
	 * @Description:
	 * @param requestUrl  请求url
	 * @param requestText 请求参数(字符串键值对map)
	 * @param requestFile 请求上传的文件(File)
	 * @return
	 * @throws Exception
	 */
	public static String sendRequest(String requestUrl, Map<String, String> requestText, Map<String, File> requestFile)
			throws Exception {
		HttpURLConnection conn = null;
		InputStream input = null;
		OutputStream os = null;
		BufferedReader br = null;
		StringBuffer buffer = null;
		try {
			URL url = new URL(requestUrl);
			conn = (HttpURLConnection) url.openConnection();

			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setConnectTimeout(1000 * 10);
			conn.setReadTimeout(1000 * 10);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Connection", "keep-alive");
			conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
			conn.setRequestProperty("authority", "upload.qiniup.com");
			conn.setRequestProperty("accept", "application/json, text/plain, */*");
			conn.setRequestProperty("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");
			conn.setRequestProperty("origin", "https://store.youzan.com");
			conn.setRequestProperty("referer", "https://store.youzan.com/v2/goods/library/");
			conn.setRequestProperty("sec-ch-ua-platform", "macOS");
			conn.setRequestProperty("sec-fetch-dest", "empty");
			conn.setRequestProperty("sec-fetch-mode", "cors");
			conn.setRequestProperty("sec-fetch-site", "cross-site");

			conn.connect();

			// 往服务器端写内容 也就是发起http请求需要带的参数
			os = new DataOutputStream(conn.getOutputStream());
			// 请求参数部分
			writeParams(requestText, os);
			// 请求上传文件部分
			writeFile(requestFile, os);
			// 请求结束标志
			String endTarget = PREFIX + BOUNDARY + PREFIX + LINE_END;
			os.write(endTarget.getBytes());
			os.flush();

			// 读取服务器端返回的内容

			if (conn.getResponseCode() == 200) {
				input = conn.getInputStream();
			} else {
				input = conn.getErrorStream();
			}

			br = new BufferedReader(new InputStreamReader(input, "UTF-8"));
			buffer = new StringBuffer();
			String line = null;
			while ((line = br.readLine()) != null) {
				buffer.append(line);
			}
			// ......
			System.out.println(buffer.toString());
			JSONObject json = JSONObject.parseObject(buffer.toString());
			if(buffer.toString().indexOf("attachment_full_url")>-1) {
				return json.getJSONObject("data").getString("attachment_full_url");
			}

		} catch (Exception e) {
			// log.error(e.getMessage(), e);
			throw new Exception(e);
		} finally {
			try {
				if (conn != null) {
					conn.disconnect();
					conn = null;
				}

				if (os != null) {
					os.close();
					os = null;
				}

				if (br != null) {
					br.close();
					br = null;
				}
			} catch (IOException ex) {

				throw new Exception(ex);
			}
		}
		return buffer.toString();
	}

	/**
	 * 对post参数进行编码处理并写入数据流中
	 * 
	 * @throws Exception
	 * 
	 * @throws IOException
	 * 
	 */
	private static void writeParams(Map<String, String> requestText, OutputStream os) throws Exception {
		try {
			String msg = "请求参数部分:\n";
			if (requestText == null || requestText.isEmpty()) {
				msg += "空";
			} else {
				StringBuilder requestParams = new StringBuilder();
				Set<Map.Entry<String, String>> set = requestText.entrySet();
				Iterator<Entry<String, String>> it = set.iterator();
				while (it.hasNext()) {
					Entry<String, String> entry = it.next();
					requestParams.append(PREFIX).append(BOUNDARY).append(LINE_END);
					requestParams.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"")
							.append(LINE_END);
					requestParams.append("Content-Type: text/plain; charset=utf-8").append(LINE_END);
					requestParams.append("Content-Transfer-Encoding: 8bit").append(LINE_END);
					requestParams.append(LINE_END);// 参数头设置完以后需要两个换行，然后才是参数内容
					requestParams.append(entry.getValue());
					requestParams.append(LINE_END);
				}
				os.write(requestParams.toString().getBytes());
				os.flush();

				msg += requestParams.toString();
			}

			 System.out.println(msg);

		} catch (Exception e) {

			throw new Exception(e);
		}
	}

	/**
	 * 对post上传的文件进行编码处理并写入数据流中
	 * 
	 * @throws IOException
	 * 
	 */
	private static void writeFile(Map<String, File> requestFile, OutputStream os) throws Exception {
		InputStream is = null;
		try {
			String msg = "请求上传文件部分:\n";
			if (requestFile == null || requestFile.isEmpty()) {
				msg += "空";
			} else {
				StringBuilder requestParams = new StringBuilder();
				Set<Map.Entry<String, File>> set = requestFile.entrySet();
				Iterator<Entry<String, File>> it = set.iterator();
				while (it.hasNext()) {
					Entry<String, File> entry = it.next();
					requestParams.append(PREFIX).append(BOUNDARY).append(LINE_END);
					requestParams.append("Content-Disposition: form-data; name=\"").append(entry.getKey())
							.append("\"; filename=\"").append(entry.getValue().getName()).append("\"").append(LINE_END);
					requestParams.append("Content-Type:").append(getContentType(entry.getValue())).append(LINE_END);
					requestParams.append("Content-Transfer-Encoding: 8bit").append(LINE_END);
					requestParams.append(LINE_END);// 参数头设置完以后需要两个换行，然后才是参数内容

					os.write(requestParams.toString().getBytes());

					is = new FileInputStream(entry.getValue());

					byte[] buffer = new byte[1024 * 1024];
					int len = 0;
					while ((len = is.read(buffer)) != -1) {
						os.write(buffer, 0, len);
					}
					os.write(LINE_END.getBytes());
					os.flush();

					msg += requestParams.toString();
				}
			}
			 System.out.println(msg);

		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {

				throw new Exception(e);
			}
		}
	}

	/**
	 * ContentType
	 * 
	 * @Description:
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String getContentType(File file) throws Exception {
		String streamContentType = "application/octet-stream";
		String imageContentType = "";
		ImageInputStream image = null;
		try {
			image = ImageIO.createImageInputStream(file);
			if (image == null) {
				return streamContentType;
			}
			Iterator<ImageReader> it = ImageIO.getImageReaders(image);
			if (it.hasNext()) {
				imageContentType = "image/" + it.next().getFormatName();
				return imageContentType;
			}
		} catch (IOException e) {

			throw new Exception(e);
		} finally {
			try {
				if (image != null) {
					image.close();
				}
			} catch (IOException e) {

				throw new Exception(e);
			}

		}
		return streamContentType;
	}

	public static String uploadImageBugs(String token, String cid, String filePath) {
		String requestURL = "https://upload.qiniup.com/";
		String result ="";
		Map<String, String> requestText = new HashMap<String, String>();
		requestText.put("token",token);
		requestText.put("x:categoryId", cid);
		
		Map<String,File> requestFile = new HashMap<String,File>();
		requestFile.put("file", new   File(filePath));
		try {
			result = HttpUtil.sendRequest(requestURL, requestText, requestFile);
		} catch (Exception e) {
			result  =e.getMessage();
			e.printStackTrace();
		}
		return result;
	}

}