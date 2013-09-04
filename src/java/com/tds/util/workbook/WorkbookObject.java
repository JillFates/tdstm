package com.tds.util.workbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.WritableWorkbook;

import org.codehaus.groovy.grails.commons.ApplicationHolder;

public class WorkbookObject {
 
  /**
   * This java class is used to get workbook related objects.
   * @param response : HttpServletResponse response
   * @param fileNameToExport : String fileNameToExport
   * @param filePath : Path files
   * @return
   * @throws IOException
   * @throws FileNotFoundException
   * @throws BiffException
   */
  public static	WritableWorkbook  getWritableWorkbook(HttpServletResponse response, String fileNameToExport, String filePath ) throws IOException, FileNotFoundException, BiffException{
		response.setContentType( "application/vnd.ms-excel" );
		fileNameToExport = fileNameToExport.replace(".xls", "");
		File file =  ApplicationHolder.getApplication().getParentContext().getResource( "/templates/TaskReport.xls" ).getFile();
		WorkbookSettings wbSetting = new WorkbookSettings();
		wbSetting.setUseTemporaryFileDuringWrite(true);
		Workbook workbook = Workbook.getWorkbook( file, wbSetting );
		//set MIME TYPE as Excel
		response.setContentType( "application/vnd.ms-excel" );
		response.setHeader( "Content-Disposition", "attachment; filename =\""+fileNameToExport+"");
		response.setHeader( "Content-Disposition", "attachment; filename=\""+fileNameToExport+".xls\"" );
		
		WritableWorkbook book = Workbook.createWorkbook( response.getOutputStream(), workbook );
		
		return book;
	} 
}
