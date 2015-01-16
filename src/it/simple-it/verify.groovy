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