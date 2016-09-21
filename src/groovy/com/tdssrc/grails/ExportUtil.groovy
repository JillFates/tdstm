package com.tdssrc.grails

import com.tdsops.common.grails.ApplicationContextHolder
import org.apache.commons.io.FilenameUtils
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class ExportUtil {

	/**
	 * Used to load a spreadsheet template file
	 * @param templateFilename - the relative path+filename of the spreadsheet template to open
	 * @return the spreadsheet workbook
	 */
	static Workbook loadSpreadsheetTemplate(String templateFilename) {
		File file =  ApplicationContextHolder.getApplicationContext().getResource( templateFilename ).getFile()
		if (! file.exists()) {
			throw new RuntimeException("Unable to load template file $templateFilename")
		}
		return WorkbookFactory.create(file)
	}

	/**
	 * Set the mimetype of the file, and the Header disposition
	 * @param response
	 * @param filename
	 */
	static void setContentType(Object response, String filename) {
		def mimetypes = ApplicationContextHolder.config.grails.mime.types

		String ext = FilenameUtils.getExtension(filename)
		String mime = mimetypes[ext]
		response.setContentType(mime)
		response.setHeader( "Content-Disposition", "attachment; filename=\"${filename}\"" )
	}

	static String getWorkbookExtension(Workbook wb){
		return (wb instanceof XSSFWorkbook)? "xlsx" : "xls"
	}
	/**
	 * Used to send the appropriate content-type header to the response
	 * @param response - the servlet response object
	 * @param filename - the name that the file should be saved as when downloaded
	 * @deprecated use setExcelContentType(response, filename, mimetypes)
	 */
	static void setExcelContentType(Object response, String filename) {

		response.setContentType( "application/vnd.ms-excel" )

		// TODO : JPM 3/2016 : We shouldn't be mucking with the Excel file extension since might have different types going forward
		// Strip off the file extention if it exists as it will be added below
		filename = filename.replace(".xls",'')

		response.setHeader( "Content-Disposition", "attachment; filename=\"${filename}.xls\"" )
	}

	/**
	 * Legacy method used to load a spreadsheet and send the ContentType all in one function
	 * @param fileName - the name of the spreadsheet as it will appear to the user when generated
	 * @param templateFilename - the relative path+filename of the spreadsheet template to open
	 * @return the spreadsheet workbook
	 * @deprecated
	 */
	def static workBookInstance(String fileName, String templateFilename, Object response) {
		// TODO : JPM 3/2016 : ExportUtil.workBookInstance() method should be swapped out for loadSpreadsheetTemplate and setExcelContentType
		Workbook book =  loadSpreadsheetTemplate(templateFilename)
		setExcelContentType(response, fileName)
		return book
	}
}
