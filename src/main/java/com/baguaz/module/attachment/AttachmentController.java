/**
 * Copyright (c) 2015-2016, Yinchuan Dai 戴银川 (daiyinchuan@163.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baguaz.module.attachment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.baguaz.BgzKit;
import com.baguaz.common.BaseController;
import com.baguaz.interceptor.MemberInterceptor;
import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.JsonKit;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@ControllerBind(controllerKey = "/attachment")
@Before(MemberInterceptor.class)
public class AttachmentController extends BaseController {
	public void upload(){
		HttpServletRequest request=this.getRequest();
		//文件保存目录路径
		String savePath=BgzKit.getBgzProp("upload_path")+File.separator;
		//根目录URL，可以指定绝对路径，比如 http://www.yoursite.com/attached/
		String saveUrl=BgzKit.getBgzProp("UPLOAD_URL")+"/";
		//定义允许上传的文件扩展名
		HashMap<String,String> extMap=new HashMap<String,String>();
		extMap.put("image","gif,jpg,jpeg,png,bmp");
		extMap.put("flash","swf,flv");
		extMap.put("media","swf,flv,mp3,wav,wma,wmv,mid,avi,mpg,asf,rm,rmvb,mp4,ogg,webm");
		extMap.put("file","doc,docx,xls,xlsx,ppt,htm,html,txt,zip,rar,gz,bz2");
		//最大文件大小
		long maxSize=Long.parseLong(BgzKit.getBgzProp("upload_maxsize"));
		this.getResponse().setContentType("text/html; charset=UTF-8");
		if(!ServletFileUpload.isMultipartContent(request)){
			this.renderText(getError("请选择文件。"));
			return;
		}
		//检查目录
		File uploadDir=new File(savePath);
		if(!uploadDir.isDirectory()){
			this.renderText(getError("上传目录不存在。"));
			return;
		}
		//检查目录写权限
		if(!uploadDir.canWrite()){
			this.renderText(getError("上传目录没有写权限。"));
			return;
		}
		String dirName=request.getParameter("dir");
		if(dirName==null){
			dirName="image";
		}
		if(!extMap.containsKey(dirName)){
			this.renderText(getError("目录名不正确。"));
			return;
		}
		//创建文件夹
		savePath+=dirName+"/";
		saveUrl+=dirName+"/";
		File saveDirFile=new File(savePath);
		if(!saveDirFile.exists()){
			saveDirFile.mkdirs();
		}
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
		String ymd=sdf.format(new Date());
		savePath+=ymd+"/";
		saveUrl+=ymd+"/";
		File dirFile=new File(savePath);
		if(!dirFile.exists()){
			dirFile.mkdirs();
		}
		FileItemFactory factory=new DiskFileItemFactory();
		ServletFileUpload upload=new ServletFileUpload(factory);
		upload.setHeaderEncoding("UTF-8");
		List<FileItem> items=null;
		try{
			items=upload.parseRequest(request);
		}catch(FileUploadException e1){
			e1.printStackTrace();
		}
		Iterator<FileItem> itr=items.iterator();
		while(itr.hasNext()){
			FileItem item=(FileItem)itr.next();
			String fileName=item.getName();
			long fileSize=item.getSize();
			if(!item.isFormField()){
				//检查文件大小
				if(fileSize>maxSize){
					this.renderText(getError("上传文件大小超过"+maxSize/1024000+"限制。"));
					return;
				}
				//检查扩展名
				String fileExt=fileName.substring(fileName.lastIndexOf(".")+1)
						.toLowerCase();
				if(!Arrays.<String>asList(extMap.get(dirName).split(","))
						.contains(fileExt)){
					this.renderText(getError("上传文件扩展名是不允许的扩展名。\n只允许"
							+extMap.get(dirName)+"格式。"));
					return;
				}
				SimpleDateFormat df=new SimpleDateFormat("yyyyMMddHHmmss");
				String newFileName=df.format(new Date())+"_"
						+new Random().nextInt(1000)+"."+fileExt;
				try{
					File uploadedFile=new File(savePath,newFileName);
					item.write(uploadedFile);
				}catch(Exception e){
					this.renderText(getError("上传文件失败。"));
					return;
				}
				this.renderText(saveUrl+newFileName);
			}
		}
	}
	
	private String getError(String message){
		Map<String,Object> obj=new HashMap<String,Object>();
		obj.put("error",1);
		obj.put("message",message);
		return JsonKit.toJson(obj);
	}
}
