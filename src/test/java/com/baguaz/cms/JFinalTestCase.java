/**
 * 
 */
package com.baguaz.cms;

import java.util.ArrayList;

import org.junit.Test;

import com.baguaz.AppConfig;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.jfinal.ext.test.ControllerTestCase;

/**
 * mock方式测试jfinal web请求
 * 
 * @author daiyc
 *
 */
public class JFinalTestCase extends ControllerTestCase<AppConfig>{
	/**
	 * 添加栏目，模仿界面操作所得的请求参数，向后台请求
	 * 
	 * 参照此可进行模拟界面的功能测试，不依赖web环境和界面来进行批量的单元测试，保证后台功能正常
	 */
	@Test
	public void testCatAdd(){
		ArrayList<String> paras=Lists
				.newArrayList(	"info[parentid]=0",
								"info[catname]=1",
								"info[catdir]=1",
								"setting[ishtml]=1",
								"info[type]=0",
								"info[modelid]=1",
								"setting[content_ishtml]=0",
								"setting[create_to_html_root]=0",
								"setting[category_ruleid]=1",
								"setting[show_ruleid]=16",
								"setting[template_list]=default",
								"setting[workflowid]=0",
								"setting[meta_keywords]=",
								"setting[show_template]=",
								"info[ismenu]=1",
								"info[url]=",
								"setting[list_template]=",
								"info[description]=",
								"setting[meta_description]=",
								"setting[category_template]=",
								"setting[meta_title]=",
								"info[image]=",
								"dosubmit=");
		String url="/admin/category/add?"+Joiner.on("&").join(paras);
		this.use(url).post("").invoke();
	}
}
