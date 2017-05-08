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

package com.baguaz.module.tools;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.C;
import com.baguaz.Ret;
import com.baguaz.common.BaseController;
import com.jfinal.ext.route.ControllerBind;

/**
 * 工具
 * 
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */ 
@ControllerBind(controllerKey = "/tools")
public class ToolsController extends BaseController {
	private static final Logger log=LoggerFactory
			.getLogger(ToolsController.class);
	
	private String pwdChars="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%&-_~+=";
	
	public void randPwd(){
		int pwdRange=this.getParaToInt("pwdRange");
		
		int pwdLenMin=0,pwdLenMax=0;
		switch(pwdRange){
		case 1:
			pwdLenMin=6;
			pwdLenMax=16;
			break;
		case 2:
			pwdLenMin=18;
			pwdLenMax=24;
			break;
		case 9:
			pwdLenMin=this.getParaToInt("pwdLenMin");
			pwdLenMax=this.getParaToInt("pwdLenMax");
		}
		
		//密码长度18~24随机长度
		int pwdLen=RandomUtils.nextInt(pwdLenMin, pwdLenMax+1);
		log.debug("pwdLen="+pwdLen);
		
		StringBuilder randPwd=new StringBuilder();
		int pwdEnd=pwdChars.length();
		for(int i=0;i<pwdLen;i++){
			int iIdx=RandomUtils.nextInt(0,pwdEnd);
			randPwd.append(pwdChars.charAt(iIdx));
		}
		
		String randPwdStr=randPwd.toString();
		
		Ret ret=Ret.create();
		ret.put("randPwd",randPwdStr);
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
}
