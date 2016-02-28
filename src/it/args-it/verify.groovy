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

def foundArgs = false
new File(basedir, "build.log").eachLine { line ->
	if (!foundArgs) {
		foundArgs = (line =~ /.*xvfbArgs = \[-screen, 0, 1800x1024x24\]/).matches()
	}
}
assert foundArgs

def foundLaunchArgs = false
new File(basedir, "build.log").eachLine { line ->
	if (!foundLaunchArgs) {
		foundLaunchArgs = (line =~ /.*Attempting to launch Xvfb.*-screen, 0, 1800x1024x24.*/).matches()
	}
}
assert foundLaunchArgs
