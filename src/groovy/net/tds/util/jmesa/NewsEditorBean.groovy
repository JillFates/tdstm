package net.tds.util.jmesa

import groovy.transform.CompileStatic

@CompileStatic
class NewsEditorBean implements Serializable {
	private static final long serialVersionUID = 1

	long id
	String displayOption
	String commentType
	String comment
	String resolution
	String createdBy
	String resolvedBy
	Date createdAt
	Date resolvedAt
}
