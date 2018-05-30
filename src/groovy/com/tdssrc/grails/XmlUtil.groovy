package com.tdssrc.grails

import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder
import net.transitionmanager.service.InvalidParamException

@Slf4j(value='logger')
class XmlUtil {

	/**
	 * Convert a map to xml string
	 * @param map
	 * @return
	 */
	static String convertMapToXmlString(Map<String, ?> map) {
		return convertMapToXmlString('root', map)
	}

	/**
	 * Convert a map to xml string with custom root element
	 * @param map
	 * @return
	 */
	static String convertMapToXmlString(String rootElement, Map<String, ?> map) {
		try {
			new StringWriter().with { sw ->
				new MarkupBuilder(sw).with {
					"${rootElement}"() {
						map.collect { k, v ->
							"$k" { v instanceof Map ? v.collect(owner) : mkp.yield(v) }
						}
					}
				}
				return sw.toString()
			}
		} catch (Exception e) {
			logger.error(e.message)
			throw new InvalidParamException("Invalid XML : ${e.message}")
		}
	}

}
