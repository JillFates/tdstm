package net.transitionmanager.util

import grails.compiler.GrailsCompileStatic
import grails.plugin.json.view.JsonViewTemplateEngine
import grails.plugin.json.view.template.JsonViewTemplate
import org.springframework.beans.factory.annotation.Autowired

/**
 * A service to render JSON using the Grails JSON views.
 */
@GrailsCompileStatic
class JsonViewRenderService {
	static final String ETL = '/etl/etlResult'

	@Autowired
	JsonViewTemplateEngine jsonViewTemplateEngine

	/**
	 * Renders a view to a string
	 *
	 * @param view The string path of the view relative to the grails-app/views folder starting with a slash.
	 * @param model The model data to render into the template. Note the object must match the type used in the view.
	 *
	 * @return
	 */
	String render(String view, Object model) {
		JsonViewTemplate template = (JsonViewTemplate) jsonViewTemplateEngine.resolveTemplate(view)

		Writable writable = template.make([result: model])
		StringWriter stringWriter = new StringWriter()
		writable.writeTo(stringWriter)
		stringWriter.flush()
		String rendered = stringWriter.toString()
		stringWriter.close()

		return rendered

	}

	/**
	 * Renders a view to an output stream
	 * @param view The string path of the view relative to the grails-app/views folder starting with a slash.
	 * @param model The model data to render into the template. Note the object must match the type used in the view.
	 * @param outputStream The output stream to render the view to.
	 */
	void render(String view, Object model, OutputStream outputStream) {
		JsonViewTemplate template = (JsonViewTemplate) jsonViewTemplateEngine.resolveTemplate(view)

		Writable writable = template.make([result: model])
		Writer writer = outputStream.newWriter()
		writable.writeTo(writer)
		writer.flush()
		writer.close()
		outputStream.close()
	}
}
