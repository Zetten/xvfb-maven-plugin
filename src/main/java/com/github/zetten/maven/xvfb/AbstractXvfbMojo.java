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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Common fields for handling Xvfb instances and the xvfb-maven-plugin goals.
 */
public abstract class AbstractXvfbMojo extends AbstractMojo {

	/**
	 * The Maven plugin context map key storing the Xvfb Process object.
	 */
	protected static final String XVFB_PROCESS_KEY = "xvfb.process";

	/**
	 * The Maven plugin context map key storing the path to the lockfile for the Xvfb port.
	 */
	protected static final String XVFB_LOCKFILE_KEY = "xvfb.lockfile";

	/**
	 * A reference to the containing Maven project. Not user-editable.
	 */
	@Parameter(defaultValue = "${project}", readonly = true)
	protected MavenProject mavenProject;

	/**
	 * Path to the Xvfb binary. By default the first available Xvfb in $PATH will be used.
	 */
	@Parameter(defaultValue = "Xvfb", required = true)
	protected String xvfbBinary;

	/**
	 * An optional parameter to fix the X display port used by Xvfb, e.g. ":20".
	 */
	@Parameter
	protected String display;

	/**
	 * The base value for X display port lookups.
	 */
	@Parameter(defaultValue = "6000", required = true, property = "xvfb.ports.base")
	protected Integer xDisplayPortBase;

	/**
	 * The first display variable to test, e.g. 20 for "DISPLAY=:20".
	 */
	@Parameter(defaultValue = "20", required = true, property = "xvfb.ports.first")
	protected Integer xDisplayDefaultNumber;

	/**
	 * The upper bound to limit searching for a free X display port.
	 */
	@Parameter(defaultValue = "20", required = true, property = "xvfb.ports.max")
	protected Integer maxDisplaysToSearch;

	/**
	 * <p>
	 * True if xvfb:run should keep retrying the port detection until a valid X display port is found.
	 * </p>
	 * <p>
	 * This provides some rudimentary thread-safety against the race condition caused by the window between finding an
	 * unused port and launching Xvfb against that port.
	 * </p>
	 */
	@Parameter(defaultValue = "true", required = true)
	protected Boolean doRetry;

	/**
	 * <p>
	 * True if the DISPLAY environment variable should be set.
	 * </p>
	 * <p>
	 * <strong>Warning:</strong> This is almost certainly not OS-portable and may render builds unstable. Use at your
	 * own risk!
	 * </p>
	 */
	@Parameter(defaultValue = "false")
	protected Boolean setDisplayEnvVar;

	/**
	 * <p>
	 * True if the Maven property defined in {@link #displayMavenProp} should be set to the value of the X display
	 * variable.
	 * </p>
	 * <p>
	 * This property may be used in subsequent lifecycle phases, for example to inject the DISPLAY environment variable
	 * to a Surefire execution or Ant task.
	 * </p>
	 */
	@Parameter(defaultValue = "true")
	protected Boolean setDisplayMavenProp;

	/**
	 * The Maven property to be set if #setDisplayMavenProp is true.
	 */
	@Parameter(defaultValue = "xvfb.display")
	protected String displayMavenProp;

	/**
	 * The directory to contain the memory-mapped framebuffer files as described by the '-fbdir' option to Xfvb
	 *
	 * @see <a href="http://www.x.org/archive/X11R7.6/doc/man/man1/Xvfb.1.xhtml">
	 * http://www.x.org/archive/X11R7.6/doc/man/man1/Xvfb.1.xhtml</a>
	 */
	@Parameter(defaultValue = "${project.build.directory}/Xvfb", required = false)
	protected String fbdir;

	/**
	 * Shut down the given Xvfb process.
	 *
	 * @param process The process to be destroyed.
	 */
	protected void destroyXvfb(Process process) {
		getLog().debug("Shutting down Xvfb from previous run...");
		process.destroy();
		try {
			int exitValue = process.waitFor();
			getLog().info("Xvfb shut down with exit code " + exitValue + ".");
		} catch (InterruptedException e) {
		}
	}

}
