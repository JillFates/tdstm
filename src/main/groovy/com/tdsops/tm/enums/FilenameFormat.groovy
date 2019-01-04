package com.tdsops.tm.enums

import groovy.transform.CompileStatic

/**
 * Enumeration that contains all the possible file name format schemes in the app.
 * Used by FilenameUtil.
 * Created by ecantu on 12/12/2017.
 */
@CompileStatic
enum FilenameFormat {
		CLIENT_PROJECT_EVENT_DATE('Project_Client-Project_Code-Event_Name-yyyymmdd_hhmm'),
		CLIENT_PROJECT_BUNDLE_CHECKBOXCODES_DATE('Project_Client-Project_Code-Bundle(s)_CheckboxCodes-yyyymmdd_hhmm'),
		PROJECT_VIEW_DATE('Project_Code-View_Name-yyyymmdd_hhmm'),
		DEFAULT('Default')

		private String value

		FilenameFormat(String value) {
				this.value = value
		}

		@Override
		String toString() {
				return value
		}
}
