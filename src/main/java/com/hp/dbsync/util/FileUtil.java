package com.hp.dbsync.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileUtil {
	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

	/**
	 * @param bs
	 * @param os
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void saveFile(InputStream bs, OutputStream os) throws FileNotFoundException, IOException {
		byte[] b = new byte[1000];
		int l;
		while ((l = bs.read(b)) != -1) {
			os.write(b, 0, l);
		}
	}

	/**
	 * @param bs
	 * @param fileName
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void saveFile(InputStream bs, String fileName) throws FileNotFoundException, IOException {
		FileOutputStream fos = new FileOutputStream(fileName);
		saveFile(bs, fos);
		fos.close();
	}

	public static PrintWriter getPrintWriter(String filename, String charset) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), charset)));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pw;
	}

	public static BufferedReader getFileBr(String filename, String charset) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), charset));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return br;
	}

	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void processFileContent(String filename, String charset, LineReader lr) {
		BufferedReader br = getFileBr(filename, charset);
		String line;
		int lineNum=0;
		try {
			while ((line = br.readLine()) != null) {
				try {
					lr.processLine(line,++lineNum);
				} catch (BreakException e) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(br!=null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public interface LineReader{
		void processLine(String line,int lineNum);
	}

	static public class BreakException extends RuntimeException{
		private static final long serialVersionUID = -5706506693580281929L;
	}


	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String getFileContent(String filename, String charset) {
		StringBuilder content = new StringBuilder();
		BufferedReader br = getFileBr(filename, charset);
		String line;
		try {
			while ((line = br.readLine()) != null) {
				content.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(br!=null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return content.toString();
	}

	/**
	 * get file list in a directory under classpath
	 * 
	 * @param classpath
	 *            : relative path under classpath, note not start with '/' or
	 *            '\'
	 * @return
	 */
	public static File[] getFilesInClasspathUnderFolderOf(String folderName) {
		String p=getDirectoryByClasspath(folderName);
		return new File(p).listFiles();
	}
	
	public static File getFileInClasspath(String folderName,String fileName){
		String p=getDirectoryByClasspath(folderName);
		return new File(p,fileName);
	}

	public static String getDirectoryByClasspath(String folderName){
		URL resource = FileUtil.class.getClassLoader().getResource(folderName);
		String p=URLDecoder.decode(resource==null?"":resource.getPath());
		if(resource==null|| !new File(p).isDirectory()){
			URL[] urls = ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs();
			for (int i = 0; i < urls.length; i++) {
				File d = new File(URLDecoder.decode(urls[i].getFile())+folderName);
				if(d.isDirectory()){
					p=URLDecoder.decode(d.getPath());
					logger.info("getDirectoryByClasspath("+folderName+"):"+p);
					return p;
				}
			}
			logger.info("getDirectoryByClasspath("+folderName+"): null");
			return null;
		}
		logger.info("getDirectoryByClasspath("+folderName+"): "+p);
		return p;
	}
	
}
