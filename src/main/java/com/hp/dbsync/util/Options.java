/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.dbsync.util;

import org.apache.commons.cli.Option;

public class Options extends org.apache.commons.cli.Options {
	private static final long serialVersionUID = 2038786153863881963L;

	public Options addOption(String opt, String longOpt, boolean hasArg, boolean required, String description) {
		Option option = new Option(opt, longOpt, hasArg, description);
		option.setRequired(required);
		addOption(option);
		return this;
	}

}
