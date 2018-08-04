package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * Tag Colors
 */
@CompileStatic
enum Color {
	Grey('tag-grey'),					//tag-color9: #e6e6e6 grey
	Red('tag-red'),						//tag-color1: #f2d9e1 salmon
	Orange('tag-orange'),				//tag-color8: #f2e8d9 orange
	Yellow('tag-yellow'),				//tag-color7: #eaf2d9 yellow
	Green('tag-green'),					//tag-color6: #d9f2d9 green
	Cyan('tag-cyan'),					//tag-color5: #d9f2ec teal
	Blue('tag-blue'),					//tag-color4: #d9e8f2 blue
	Purple('tag-purple'),				//tag-color3: #dad9f2 purple
	Pink('tag-pink'),					//tag-color2: #efd9f2 magenta
	White('tag-white')					//tag-color0: #ffffff white

	final String css

	Color(String cssValue){
		css = cssValue
	}

	String toString() { name() }

	static Color valueOfParam(String param) {
		values().find { it.name() == param }
	}
}
