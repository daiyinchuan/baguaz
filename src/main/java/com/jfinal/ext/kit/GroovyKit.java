/**
 * 
 */
package com.jfinal.ext.kit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 * @author daiyc
 *
 */
public class GroovyKit{
	private static final ScriptEngineManager sem = new ScriptEngineManager();
	private static final ScriptEngine engine = sem.getEngineByName("groovy");//javascript
    
	public static Object runScriptFromFile(String classPath){
	    Reader reader=null;
	    try{
	    	reader=CpKit.toReader(classPath);
			return engine.eval(reader);
		}catch(ScriptException e){
			throw new RuntimeException(e);
		}catch(FileNotFoundException e){
			throw new RuntimeException(e);
		}finally{
			if(reader!=null){
				try{
					reader.close();
				}catch(IOException e){
					throw new RuntimeException(e);
				}
			}
		}
	}
	public static Object runScriptFromStr(String scriptStr){
	    try{
			return engine.eval(scriptStr);
		}catch(ScriptException e){
			throw new RuntimeException(e);
		}
	}
	public static Object runScriptFromStr(String scriptStr,Map<String,Object> params){
		try{
			Bindings sb=new SimpleBindings();
			sb.putAll(params);
			return engine.eval(scriptStr,sb);
		}catch(ScriptException e){
			throw new RuntimeException(e);
		}
	}
	
	public static String j2gStr(String scriptStr){
		return "\""+scriptStr+"\"";
	}
}
