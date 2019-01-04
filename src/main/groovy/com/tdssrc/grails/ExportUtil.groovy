package com.tdssrc.grails

import com.tdsops.common.grails.ApplicationContextHolder
import groovy.transform.CompileStatic
import org.apache.commons.io.FilenameUtils
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.core.io.Resource
import org.springframework.util.StreamUtils

import javax.servlet.http.HttpServletResponse

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION

@CompileStatic
class ExportUtil {

	/**
	 * Used to load a spreadsheet template file
	 * @param templateFilename - the filename of the spreadsheet template to open
	 * @return the spreadsheet workbook
	 */
	static Workbook loadSpreadsheetTemplate(String templateFilename) {
		Resource resource = getResource(templateFilename)
		if (!resource.exists()) {
			throw new RuntimeException("Unable to load template file $templateFilename")
		}
		return WorkbookFactory.create(resource.inputStream)
	}

	/**
	 * Gets a resource from the templates directory.
	 * @param templateFilename  path to the template file
	 */
	static Resource getResource(String templateFilename) {
		ApplicationContextHolder.getApplicationContext().getResource(templateFilename)
	}

	/**
	 * Set the mimetype of the file, and the Header disposition
	 */
	static void setContentType(HttpServletResponse response, String filename) {
		Map<String, String> mimetypes = (Map) ApplicationContextHolder.grailsApplication.config['grails']['mime']['types']
		response.contentType = mimetypes[FilenameUtils.getExtension(filename)]
		response.setHeader(CONTENT_DISPOSITION, 'attachment; filename="' + filename + '"')
	}

	static void sendWorkbook(Workbook workbook, HttpServletResponse response, String filename) {
		setContentType response, filename
		workbook.write(response.outputStream)
	}

	static String getWorkbookExtension(Workbook wb){
		return (wb instanceof XSSFWorkbook)? "xlsx" : "xls"
	}

	static void writeToFile(Workbook workbook, File file) {
		OutputStream out = new FileOutputStream(file)
		workbook.write out
		out.close()
	}

	static void writeToResponse(File file, HttpServletResponse response) {
		StreamUtils.copy new FileInputStream(file), response.outputStream
	}
}
