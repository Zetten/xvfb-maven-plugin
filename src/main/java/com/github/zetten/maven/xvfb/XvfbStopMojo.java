package com.github.zetten.maven.xvfb;

/*
 * #%L
 * Xvfb Maven Plugin
 * %%
 * Copyright (C) 2015 Peter van Zetten
 * %%
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
 * #L%
 */

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Stop a running Xvfb server which was launched via xvfb:run. This retrieves the process to kill from the Maven plugin
 * context.
 */
@Mojo(name = "stop", threadSafe = true, defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class XvfbStopMojo extends AbstractXvfbMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (getPluginContext().containsKey(XVFB_PROCESS_KEY)) {
			Process process = (Process) getPluginContext().get(XVFB_PROCESS_KEY);

			if (process == null) {
				throw new MojoExecutionException("Cannot stop Xvfb: Could not determine X display to stop.");
			}

			destroyXvfb(process);
			getPluginContext().remove(XVFB_PROCESS_KEY);
		}
		if (getPluginContext().containsKey(XVFB_LOCKFILE_KEY)) {
			File lockFile = (File) getPluginContext().get(XVFB_LOCKFILE_KEY);
			if (lockFile != null) {
				lockFile.delete();
				getPluginContext().remove(XVFB_LOCKFILE_KEY);
			}
		}
	}

}
