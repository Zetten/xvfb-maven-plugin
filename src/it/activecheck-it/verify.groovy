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

def foundWaiting = false
def foundActiveCheckFailed = false
def foundRetries = 0
def usedDisplay = ""
new File(basedir, "build.log").eachLine { line ->
	def displayMatcher = line =~ /.*Launching Xvfb on (.*)/
	if (displayMatcher.matches()) {
		usedDisplay = displayMatcher[0][1]
	}
	if (!foundWaiting) {
		foundWaiting = (line =~ /\[INFO\] Waiting for Xvfb to become active\.\.\./).matches()
	}
	if ((line =~ /\[DEBUG\] Active check failed, retries remaining:.*/).matches()) {
		foundRetries++
	}
	if (!foundActiveCheckFailed) {
		foundActiveCheckFailed = (line =~ /.*Active check failed for display: $usedDisplay/).matches()
	}
}
assert foundWaiting
assert foundActiveCheckFailed
assert foundRetries == 5