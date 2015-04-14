package com.tdssrc.grails

import org.apache.poi.hssf.usermodel.HSSFWorkbook

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

		response.setContentType( "application/vnd.ms-excel" )
		def filename = fileName.replace(".xls",'')
		response.setHeader( "Content-Disposition", "attachment; filename = ${filename}" )
		response.setHeader( "Content-Disposition", "attachment; filename=\""+filename+".xls\"" )
		def book = new HSSFWorkbook(new FileInputStream( file ))

		return book
	}
	
}
