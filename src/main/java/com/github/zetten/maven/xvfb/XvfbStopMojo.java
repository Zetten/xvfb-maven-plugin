package com.github.zetten.maven.xvfb;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Stop a running Xvfb server which was launched via xvfb:run. This retrieves the process to kill from the Maven plugin
 * context.
 */
@Mojo(name = "stop", threadSafe = true)
public class XvfbStopMojo extends AbstractXvfbMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Process process = (Process) getPluginContext().get(XVFB_PROCESS_KEY);

		if (process == null) {
			throw new MojoExecutionException("Cannot stop Xvfb: Could not determine X display to stop.");
		}

		getLog().info("Shutting down Xvfb from previous run...");
		process.destroy();
		try {
			int exitValue = process.waitFor();
			getLog().info("Xvfb shut down with exit code " + exitValue + ".");
		} catch (InterruptedException e) {
		}
	}

}
