/**
 * 
 */
package com.jfinal.ext.kit;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

/**
 * 类路径工具
 * @author daiyc
 *
 */
public class CpKit{
	/**
	 * 抬头带/
	 * @param classPath
	 * @return
	 */
	public static String toAbsPath(String classPath){
		return CpKit.class.getResource(classPath).getPath();
	}
	public static Reader toReader(String classPath) throws FileNotFoundException{
		return new FileReader(toAbsPath2(classPath));
	}
	/**
	 * 抬头不带/
	 * @param classPath
	 * @return
	 */
	public static String toAbsPath2(String classPath){
		return Thread.currentThread().getContextClassLoader().getResource(classPath).getPath();
	}
}
