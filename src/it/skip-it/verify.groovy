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

def foundSkipStart = false
def foundSkipStop = false
def foundLaunch = false
def foundShutDown = false
new File(basedir, "build.log").eachLine { line ->
	if (!foundSkipStart) {
		foundSkipStart = (line =~ /.*Xvfb start is skipped\./).matches()
	}
	if (!foundSkipStop) {
		foundSkipStop = (line =~ /.*Xvfb stop is skipped\./).matches()
	}
	if (!foundLaunch) {
		foundLaunch = (line =~ /.*Attempting to launch Xvfb with arguments.*/).matches()
	}
	if (!foundShutDown) {
		foundShutDown = (line =~ /.*Xvfb shut down.*/).matches()
	}
}
assert foundSkipStart
assert foundSkipStop
assert !foundLaunch
assert !foundShutDown
