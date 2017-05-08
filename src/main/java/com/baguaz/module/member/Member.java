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

package com.baguaz.module.member;

import com.baguaz.common.BaseModel;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class Member extends BaseModel<Member> {
	private static final long serialVersionUID = 3554066662942528953L;
	public static final Member dao=new Member();
	/**
	 * @param decryptToken
	 * @return
	 */
	public Member findByToken(String decryptToken) {
		return dao.getOneM("*", "token=?", "", "", decryptToken);
	}
	
}
