package com.hp.dbsync.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SFTPUtil {
	private static final Logger logger = LoggerFactory.getLogger(SFTPUtil.class);
	// TODO move to database
	// private static final String dadFileFolder = "Svu0049.sgp.hp.com/elt/adjustedlot/";
	// private static final String dadSftpUser = "elt";
	// private static final String dadSftpPwd = "!qaz2wsx3edc";
	private static FileSystemManager fsm = null;

	static {
		try {
			fsm = VFS.getManager();
		} catch (FileSystemException e) {
			e.printStackTrace();
		}
	}

	public static FileObject getFile(String path) throws FileSystemException {
		return fsm.resolveFile(path);
	}

	public static void delete(String path) throws FileSystemException {
		FileObject fo = fsm.resolveFile(path);
		fo.delete();
	}

	public static boolean isDirectory(String path) throws FileSystemException {
		FileObject fo = fsm.resolveFile(path);
		return fo.getType().equals(FileType.FOLDER);
	}

	public static InputStream getInputStream(String path) throws FileSystemException {
		FileObject fo = fsm.resolveFile(path);
		return fo.getContent().getInputStream();
	}

	public static OutputStream getOutputStream(String path) throws FileSystemException {
		FileObject fo = fsm.resolveFile(path);
		return fo.getContent().getOutputStream();
	}

	public static boolean isFile(String path) throws FileSystemException {
		FileObject fo = fsm.resolveFile(path);
		return fo.getType().equals(FileType.FILE);
	}

	/**
	 * create directory recursively ignoring last file name
	 * 
	 * @param path
	 * @return succrss: true
	 */
	public static boolean mkdirs(String path) {
		String realPath = "";
		path = path.replaceAll("\\\\", "/");
		// end with "/"
		if (path.endsWith("/")) {
			realPath = path;
		} else {
			int fileNamePoint = path.lastIndexOf("/");
			// real path
			if (fileNamePoint >= 0) {
				realPath = path.substring(0, fileNamePoint);
			}
			String fileName = path.substring(fileNamePoint + 1);
			// if there is no "." in filename, the whole string is path
			if (fileName.indexOf(".") < 0) {
				realPath = path;
			}
		}
		try {
			FileObject fo = fsm.resolveFile(realPath);
			fo.createFolder();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean copyFile(String sourceFilePath, String targetFilePath, boolean overWrite) throws IOException {
		if (StringUtils.isBlank(sourceFilePath) || StringUtils.isBlank(targetFilePath)) {
			throw new IOException("sourceFilePath or targetFilePath is blank");
		}
		FileObject from = fsm.resolveFile(sourceFilePath);
		FileObject to = fsm.resolveFile(targetFilePath);
		if (to.exists()) {
			if (to.getType() == FileType.FILE) {
				if (overWrite && !to.delete()) {
					throw new IOException("targetFilePath[" + targetFilePath + "]is protected, cannot be overwritten");
				} else if (!overWrite) {
					throw new IOException("targetFilePath[" + targetFilePath + "] is already existing");
				}
			}
		}
		to.copyFrom(from, Selectors.SELECT_ALL);
		return true;
	}

	public static boolean moveFile(String srcFile, String targetFile, boolean overWrite) throws IOException {
		if (srcFile.equals(targetFile)) {
			return true;
		}
		FileObject src = fsm.resolveFile(srcFile);
		// File (or directory) to be moved
		if (StringUtils.isNotBlank(srcFile) && !src.exists()) {
			throw new IOException("srcFile[" + srcFile + "] is not existing");
		}
		// Destination directory
		FileObject to = fsm.resolveFile(targetFile);
		if (to.exists()) {
			if (to.getType() == FileType.FILE) {
				if (overWrite && !to.delete()) {
					throw new IOException("targetFile[" + targetFile + "]is protected, cannot be overwritten");
				} else if (!overWrite) {
					throw new IOException("targetFile[" + targetFile + "] is already existing");
				}
			}
		}
		src.moveTo(to);
		return true;
	}

	public static void createEmptyFile(String path, boolean overWrite) throws IOException {
		createFile(path, null, overWrite);
	}

	public static void createFile(String path, String content, boolean overWrite) throws IOException {
		if (StringUtils.isBlank(path)) {
			throw new IOException("path is blank");
		}
		// Destination directory
		FileObject to = fsm.resolveFile(path);
		if (to.exists()) {
			if (overWrite) {
				if (!to.delete())
					throw new IOException("file[" + path + "]is protected, cannot be overwritten");
			} else {
				throw new IOException("file[" + path + "] is already existing");
			}
		}
		if (content != null) {
			OutputStream os = to.getContent().getOutputStream();
			os.write(content.getBytes());
			os.close();
		}
		to.createFile();
	}

	public static void main(String[] args) throws IOException {
		// FTPClient ftp = open();
		// // upload(ftp,"C:\\Users\\xule\\Desktop\\1new\\","sharing.pptx");
		// download(ftp,"C:\\Users\\xule\\Desktop\\1new\\","sharing.pptx");
		// close(ftp);

		// copyFile("e:/crpt.txt","e:/crpt",true);
		// moveFile("M:/u-dsm/store/store/enterprise/a9845232-bea2-4d9e-92be-d16cfcdcea01/ad5b00475fd0408aaed2eb195bf90913.docx","M:/u-dms/a9845232-bea2-4d9e-92be-d16cfcdcea01/ad5b00475fd0408aaed2eb195bf90913.docx",
		// true);
		// copyFile("ftp://zzg:zzg@192.168.1.100/test/teeee/fff","smb://user:user@192.168.1.100/共享/1.txt",true);
		// copyFile("smb://user:user@192.168.1.100/共享/1.txt","file://e:/ffff",true);
		// moveFile("ftp://zzg:zzg@192.168.1.100/test1/teeee2/fff1", "ftp://unis:unis@192.168.1.100/test/teeee/fff", true);
		// copyFile("file://e:/crpt.txt","file://e:/crpt",true);

		String url = "sftp://elt:!qaz2wsx3edc@Svu0049.sgp.hp.com/";
		// copyFile(url+"ftp/et05lnhd.224","C:/1new/et05lnhd.224",true);
		// copyFile("C:/1new/elt_tasks.xlsx",url+"ftp/elt_tasks.xlsx",true);
		// copyFile(url + "ftp/elt_tasks.xlsx", "C:/1new/elt_tasks.xlsx", true);
		createEmptyFile(url + "ftp/empt.txt", true);
		// createFile(url + "ftp/empt.txt","ddddd55ddddddd",true);
	}
}
