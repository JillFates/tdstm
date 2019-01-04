package net.transitionmanager.search

import com.tdsops.tm.enums.domain.Color
import com.tdssrc.grails.JsonUtil
import org.grails.web.json.JSONObject

trait TagSearchMethods {

	/**
	 * Convert a string containing the tags for a domain into an actual list.
	 * @param jsonTagField
	 * @return
	 */
	List<Map> handleTags(String jsonTagField) {
		List tags = JsonUtil.parseJsonList(jsonTagField)
		tags.each { Map tag ->
			tag.css = Color.valueOfParam(tag.color).css
		}
		return tags
	}
}
