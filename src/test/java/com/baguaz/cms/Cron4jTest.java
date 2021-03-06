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

package com.baguaz.cms;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.sauronsoftware.cron4j.Scheduler;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class Cron4jTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws InterruptedException {
		Scheduler scheduler = new Scheduler();
		scheduler.schedule("*/1 * * * *",new Runnable(){
			@Override
			public void run() {
				System.out.println(new Date());
			}
		});
		scheduler.start();
		
		Thread.sleep(500000); // pause the main thread for 5 minutes
		
		scheduler.stop();
	}

}
