package com.hp.dbsync.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringUtils;

public class ShellUtil {
	public static void main(String[] args)  {
		String r = runShell("C:/1new/test.bat");
		System.out.println(r);
	}

	static public String runShell(String... sh) {
		System.out.println("run command:"+StringUtils.join(sh," "));
		System.out.println("set Executable:"+sh[0]);
		new File(sh[0]).setExecutable(true);
		
		String result=null;
		try {
			ProcessBuilder pb = new ProcessBuilder(sh);
			// Map<String, String> env = pb.environment();
			// env.put("VAR1", "myValue");
			// env.remove("OTHERVAR");
			// pb.directory(new File("myDir"));
	        
			Process ps = pb.start();
//			Process ps=Runtime.getRuntime().exec(sh);
			ps.waitFor();

			BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			result = sb.toString();
//			System.out.println(result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}
}
