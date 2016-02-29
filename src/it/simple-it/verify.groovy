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

def foundDisplay = false
new File(basedir, "target/out-environment").eachLine { line ->
	if (!foundDisplay) {
		foundDisplay = (line =~ /^DISPLAY=:99$/).matches()
	}
}
assert foundDisplay

def foundFbdir = false
new File(basedir, "target/out-fbdir").eachLine { line ->
	if (!foundFbdir) {
		foundFbdir = (line =~ /^Xvfb_screen0$/).matches()
	}
}
assert foundFbdir

def foundDestroyTimeout = false
new File(basedir, "build.log").eachLine { line ->
	if (!foundDestroyTimeout) {
		foundDestroyTimeout = (line =~ /.*destroyTimeout = 1313/).matches()
	}
}
assert foundDestroyTimeout
