# xvfb-maven-plugin

A Maven plugin to manage an Xvfb server. This may be used for headless testing
of GUI applications.

## Typical usage

Enable the `xvfb:run` and `xvfb:stop` executions in your module's plugin
configuration.

These goals bind by default to the `pre-integration-test` and
`post-integration-test` lifecycle phases. When starting up a free X display
port is searched for and Xvfb launched against it, and the process is stopped
after integration tests have run.

The DISPLAY variable corresponding to the temporary Xvfb instance may be passed
to integration tests in two ways:

1. The default is that the `xvfb:run` goal sets a Maven property `xvfb.display`
which can be used in any other plugin executions (e.g. via the
[environmentVariables][1] option of `maven-failsafe-plugin`).
2. An experimental (and unstable) form of setting the `$DISPLAY` environment
variable directly within the executing JVM is also available, but is not
recommended.

For example, to run full-UI tycho-surefire-test executions in their own Xvfb
instances, use configuration like the below in your parent pom.xml. Since the
Tycho test plugin binds to the `integration-test` phase, the Xvfb plugin will
wrap their execution and set the `xvfb.display` plugin value.

```
<pluginManagement>
    <plugins>
        <plugin>
            <groupId>com.github.zetten</groupId>
            <artifactId>xvfb-maven-plugin</artifactId>
            <version>${xvfb-maven-plugin.version}</version>
            <executions>
                <execution>
                    <id>wrap-tests</id>
                    <goals>
                        <goal>run</goal>
                        <goal>stop</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        <plugin>
            <groupId>org.eclipse.tycho</groupId>
            <artifactId>tycho-surefire-plugin</artifactId>
            <version>${tycho.version}</version>
            <configuration>
                <useUIHarness>true</useUIHarness>
                <environmentVariables>
                    <DISPLAY>${xvfb.display}</DISPLAY>
                </environmentVariables>
            </configuration>
        </plugin>
    </plugins>
</pluginManagement>
```

## Credits

The approach was inspired by the [Jenkins Xvfb plugin][2], the Selenium Maven
Plugin's [Xvfb goal][3] and [process-exec-maven-plugin][4]. 


[1]: http://maven.apache.org/surefire/maven-failsafe-plugin/integration-test-mojo.html#environmentVariables
[2]: https://github.com/jenkinsci/xvfb-plugin
[3]: http://mojo.codehaus.org/selenium-maven-plugin/examples/headless-with-xvfb.html
[4]: https://github.com/bazaarvoice/maven-process-plugin