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

package com.jfinal.ext.plugin.cron4j;

import com.jfinal.plugin.IPlugin;

import it.sauronsoftware.cron4j.Scheduler;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class Cron4jPlugin implements IPlugin {
	private Scheduler scheduler = new Scheduler();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jfinal.plugin.IPlugin#start()
	 */
	@Override
	public boolean start() {
		scheduler.start();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jfinal.plugin.IPlugin#stop()
	 */
	@Override
	public boolean stop() {
		scheduler.stop();
		return true;
	}
	
	public void addTask(String cronExpress, Runnable task) {
		scheduler.schedule(cronExpress, task);
	}
}
