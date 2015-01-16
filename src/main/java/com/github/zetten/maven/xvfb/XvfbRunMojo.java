package com.github.zetten.maven.xvfb;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * <p>
 * Start a new Xvfb server. Optionally export the DISPLAY variable for the new server for use in other phases.
 * </p>
 * <p>
 * By default, the property "xvfb.display" will be set, and may be used in other plugin executions (occurring after this
 * xvfb:run) in the standard manner.
 * </p>
 * <p>
 * Note that if the "display" parameter is explicitly set this mojo may be considered thread-safe. The automatic X
 * display port determination could lead to a race condition with parallel executions.
 * </p>
 */
@Mojo(name = "run", threadSafe = true)
public class XvfbRunMojo extends AbstractXvfbMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (display != null) {
			// If a fixed display variable is configured, use it
			if (isDisplayActive(display)) {
				throw new MojoExecutionException("Cannot start Xvfb: X display already in use.");
			}

			try {
				startXvfb(display);
				return;
			} catch (IOException e) {
				throw new MojoExecutionException("Could not launch Xvfb.", e);
			}
		} else {
			// Otherwise, search for an available display, retrying until one is found in the requested range
			int n = xDisplayDefaultNumber;
			while (n <= xDisplayDefaultNumber + maxDisplaysToSearch) {
				String d = ":" + n;
				getLog().debug("Testing DISPLAY=" + d);

				if (!isDisplayActive(d)) {
					try {
						startXvfb(d);
						return;
					} catch (IOException e) {
						if (doRetry) {
							// swallow the exception with a log message
							getLog().info("Failed to start Xvfb on display " + d + ", retrying.");
							getLog().debug(e);
						} else {
							throw new MojoExecutionException("Could not find a usable display and doRetry is false.");
						}
					}
				} else {
					n++;
				}
			}

			throw new MojoExecutionException("Could not find a usable display in the given bounds.");
		}
	}

	/**
	 * Launch Xvfb on the given X display.
	 * 
	 * @param d
	 *            The X display to use, e.g. ":20".
	 * @return The Process representing the Xvfb server.
	 * @throws IOException
	 *             If Xvfb fails to start.
	 */
	@SuppressWarnings("unchecked")
	private void startXvfb(String d) throws IOException {
		List<String> command = Lists.newArrayList(xvfbBinary);
		command.add(d);

		if (!Strings.isNullOrEmpty(fbdir)) {
			File dir = new File(fbdir);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			command.add("-fbdir");
			command.add(fbdir);
		}

		final ProcessBuilder pb = new ProcessBuilder();
		pb.command(command);

		Process process = null;

		getLog().info("Attempting to launch Xvfb with arguments: " + command);
		process = pb.start();
		getLog().info("Xvfb launched.");

		if (process != null) {
			getPluginContext().put(XVFB_PROCESS_KEY, process);
			if (setDisplayMavenProp) {
				setPropDisplay(d);
			}
			if (setDisplayEnvVar) {
				setEnvDisplay(d);
			}
		}
	}

	/**
	 * Set the Maven property defined by 'displayMavenProp' to the given value. This property should then be usable by
	 * subsequent goals and plugins in the current reactor.
	 */
	private void setPropDisplay(String d) {
		getLog().info("Setting Maven property '" + displayMavenProp + "' to: " + d);
		mavenProject.getProperties().put(displayMavenProp, d);
	}

	/**
	 * <p>
	 * This is a <em>very</em> dirty hack for setting the DISPLAY environment variable within the running JVM. In
	 * theory, subsequent methods or launched Processes will see the correct DISPLAY value.
	 * </p>
	 * <p>
	 * See: {@link http://stackoverflow.com/a/19040660/137403}
	 * </p>
	 */
	@SuppressWarnings("all")
	private void setEnvDisplay(String d) {
		getLog().info("Setting DISPLAY environment variable to: " + d);
		try {
			Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
			Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
			theEnvironmentField.setAccessible(true);

			Class<?> variableClass = Class.forName("java.lang.ProcessEnvironment$Variable");
			Method convertToVariable = variableClass.getMethod("valueOf", String.class);
			convertToVariable.setAccessible(true);

			Class<?> valueClass = Class.forName("java.lang.ProcessEnvironment$Value");
			Method convertToValue = valueClass.getMethod("valueOf", String.class);
			convertToValue.setAccessible(true);

			Object sampleVariable = convertToVariable.invoke(null, "");
			Object sampleValue = convertToValue.invoke(null, "");
			Map<Object, Object> env = (Map<Object, Object>) theEnvironmentField.get(null);
			for (Map.Entry<String, String> e : System.getenv().entrySet()) {
				Object var, val;
				if (e.getKey().equals("DISPLAY")) {
					var = (Object) convertToVariable.invoke(null, "DISPLAY");
					val = (Object) convertToValue.invoke(null, d);
				} else {
					var = (Object) convertToVariable.invoke(null, e.getKey());
					val = (Object) convertToValue.invoke(null, e.getValue());
				}
				env.put(var, val);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check if the given display identifier is active or not.
	 */
	private boolean isDisplayActive(String d) {
		try {
			Integer port = decodeDisplayPort(d);
			Socket socket = new Socket("localhost", port);
			socket.close();
			return true;
		} catch (ConnectException e) {
			return false;
		} catch (UnknownHostException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * <p>
	 * Convert the DISPLAY variable into a port number.
	 * </p>
	 * <p>
	 * Typically X displays map to TCP ports like :1 -> 6001
	 * </p>
	 */
	private Integer decodeDisplayPort(String d) {
		Matcher m = Pattern.compile("[^:]*:(\\d*)(?:\\.(\\d*))?").matcher(d);

		if (!m.matches()) {
			throw new IllegalArgumentException("Requested DISPLAY variable is not in the correct format (e.g. ':20')");
		}

		Integer i = Integer.parseInt(m.group(1));

		return xDisplayPortBase + i;
	}

}
