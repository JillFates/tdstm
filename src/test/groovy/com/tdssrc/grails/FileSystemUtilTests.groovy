package com.tdssrc.grails

import spock.lang.Specification
import spock.lang.Unroll

class FileSystemUtilTests extends Specification{

	@Unroll
	void "Test isValidExtension for extension #ext given the extensions #exts responds #result"() {
		expect: "The function responds as expected"
			FileSystemUtil.isValidExtension(ext, exts) == result
		where: "The following data table is given."
			ext     |       exts                ||  result
			null    |    null                   ||  false
			null    |   []                      ||  false
			"json"  |   []                      ||  false
			"json"  |   ["json", "xls"]         ||  true
			"JSON"  |   ["json", "xls"]         ||  true
			"JSON"  |   ["txt", "xls"]          ||  false
			".JSON" |   ["json", "xls"]         ||  true
	}

	@Unroll
	void "Test formatExtension for extension #ext responds #result"() {
		expect: "The function responds as expected"
			FileSystemUtil.formatExtension(ext) == result
		where: "The following data table is given."
			ext     ||  result
			null    ||  null
			"json"  ||  "json"
			".json" ||  "json"
			"JSON"  ||  "json"
			".JSON" ||  "json"
	}

	@Unroll
	void "Test getFileExtension for filename #filename responds #result"() {
		expect: "The function responds as expected"
			FileSystemUtil.getFileExtension(filename) == result
		where: "The following data table is given."
			filename            ||  result
			null                ||  null
			""                  ||  null
			"filename"          ||  null
			"filename.json"     ||  "json"
			"filename.JSON"     ||  "json"
			"file.name.json"    ||  "json"
	}
}
