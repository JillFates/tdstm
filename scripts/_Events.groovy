// Deal with post plugin installation issues

eventPluginInstalled = { pluginName -> 
	String jmesa = 'jmesa-2.0.4-SNAPSHOT-0.1'
	if (pluginName == 'jmesa-2.0.4-SNAPSHOT-0.1') {
		// Lets move the readme.txt from the lib directory as it causes an issue with running tests
		File fh = new File("./plugins/$jmesa/lib/readme.txt")
		if (fh.exists()) {
			if (fh.delete()) {
				println "*** Deleted $jmesa/lib/readme.txt - caused errors during unit-tests (see TM-4689)"
			}
		}
	}
}
