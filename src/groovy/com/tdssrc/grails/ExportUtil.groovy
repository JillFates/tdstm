package com.tdssrc.grails

import jxl.Workbook
import jxl.WorkbookSettings

import org.codehaus.groovy.grails.commons.ApplicationHolder

/**
 *To reuse the code while exporting excel file to get the workbook instance.
 *
 *@param 'fileName' as name of file in which date will be exported,
 *       'filePath' as path of template file in which data need to be export
 *        response
 *@return workbook-instance
 *TODO : This method should be used in assetEntity export as well
 */

class ExportUtil {

	def public static workBookInstance(fileName, filePath, response){
		
		File file =  ApplicationHolder.application.parentContext.getResource( filePath ).getFile()
		WorkbookSettings wbSetting = new WorkbookSettings()
		wbSetting.setUseTemporaryFileDuringWrite(true)
		def workbook = Workbook.getWorkbook( file, wbSetting )
		
		response.setContentType( "application/vnd.ms-excel" )
		def filename = fileName.replace(".xls",'')
		response.setHeader( "Content-Disposition", "attachment; filename = ${filename}" )
		response.setHeader( "Content-Disposition", "attachment; filename=\""+filename+".xls\"" )
		def book = Workbook.createWorkbook( response.getOutputStream(), workbook )
		
		return book
	}
	
}
