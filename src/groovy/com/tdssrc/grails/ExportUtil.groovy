package com.tdssrc.grails

import org.apache.poi.hssf.usermodel.HSSFWorkbook
// import org.codehaus.groovy.grails.commons.ApplicationHolder
import com.tdsops.common.grails.ApplicationContextHolder

class ExportUtil {

	/**
	 * Used to load a spreadsheet template file
	 * @param templateFilename - the relative path+filename of the spreadsheet template to open
	 * @return the spreadsheet workbook
	 */
	static HSSFWorkbook loadSpreadsheetTemplate(String templateFilename) {
		
		// File file =  ApplicationHolder.application.parentContext.getResource( templateFilename ).getFile()
		File file =  ApplicationContextHolder.getApplicationContext().getResource( templateFilename ).getFile()
		if (! file.exists()) {
			throw new RuntimeException("Unable to load template file $templateFilename")
		}
		HSSFWorkbook book = new HSSFWorkbook(new FileInputStream( file ))

		return book
	}

	/**
	 * Used to send the appropriate content-type header to the response 
	 * @param response - the servlet response object
	 * @param filename - the name that the file should be saved as when downloaded
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
	 */
	def static workBookInstance(String fileName, String templateFilename, Object response) {
		// TODO : JPM 3/2016 : ExportUtil.workBookInstance() method should be swapped out for loadSpreadsheetTemplate and setExcelContentType
		HSSFWorkbook book =  loadSpreadsheetTemplate(templateFilename)
		setExcelContentType(response, fileName)
		return book
	}
	
}
