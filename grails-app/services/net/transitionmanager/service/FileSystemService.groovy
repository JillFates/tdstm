package net.transitionmanager.service

import com.tdsops.etl.TDSExcelDriver
import com.tdssrc.grails.FileSystemUtil
import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.data.Dataset
import getl.data.Field
import getl.excel.ExcelConnection
import getl.excel.ExcelDataset
import getl.utils.FileUtils
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.command.FileCommand
import net.transitionmanager.command.UploadFileCommand
import net.transitionmanager.command.UploadTextCommand
import net.transitionmanager.service.InvalidRequestException
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.StringUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.InitializingBean

import javax.management.RuntimeErrorException
/**
 * FileSystemService provides a number of methods to use to interact with the application server file system.
 */
@Transactional(readOnly = true)
@Slf4j
class FileSystemService  implements InitializingBean {

	/*
     * These are the accepted file extensions when uploading ETL files
     */
	public static final List<String> ALLOWED_FILE_EXTENSIONS_FOR_ETL_UPLOADS = ['csv', 'json', 'xls', 'xlsx', 'xml']

    // The maximum number of tries to get a unique filename so that the getUniqueFilename doens't get into infinite loop
    static final int maxUniqueTries=100

    // The directory that temporary files will be created
    static String temporaryDirectory

    CoreService coreService
    SecurityService securityService

    void afterPropertiesSet() throws Exception {
        // Load the temporary directory name and make sure that it has the
        String appTempDirectory = coreService.getAppTempDirectory()

        if (! appTempDirectory.endsWith(File.separator)) {
            temporaryDirectory = appTempDirectory + File.separator
        } else {
            temporaryDirectory = appTempDirectory
        }
    }

	/**
	 * Initialize a CSV file
	 * @param os   OutputStream to the File
	 * @param dataSet String of comma separated values
	 */
	 private void initCSV(OutputStream os, String dataSet) {
		 os << dataSet
		 os.close()
	 }

	/**
	 * Initialize an Excel file using the passed data
	 * This is mostly used in MockETLController for testing
	 * @param ext  Extension of the file to create
	 * @param os   OutputStream to the File
	 * @param dataSet String of comma separated values
	 */
	private void initExcel(String ext, OutputStream os, String dataSet) {
		List<String> csv = dataSet.split('\n')

		Workbook workbook
		if (ext == 'XLSX') {
			workbook = new XSSFWorkbook()
		} else if (ext == 'XLS') {
			workbook = new HSSFWorkbook()
		}

		if (!workbook) {
			throw new InvalidRequestException('Uploaded file does not appear to be in the Excel format')
		}

		Sheet sheet = workbook.createSheet("Data Sheet")
		int rowNum = 0
		for (String line : csv) {
			Row row = sheet.createRow(rowNum++)
			int colNum = 0
			List<String> cellValues = line.split(',')
			for (String value : cellValues) {
				Cell cell = row.createCell(colNum++)
				cell.setCellValue(value)
			}
		}

		workbook.write(os)
		workbook.close()
		os.close()
	}

	/**
	 * Initialize a file with the dataSet String passed
	 * @param filename   File name
	 * @param os         Output Stream
	 * @param dataSet    String of comma separated values
	 */
	void initFile(String filename, OutputStream os, String dataSet){
		String ext = FilenameUtils.getExtension(filename)?.toUpperCase()

		switch (ext) {
			case 'CSV' :
				initCSV(os, dataSet)
				break

			case ['XLS', 'XLSX'] :
				initExcel(ext, os, dataSet)
				break
		}
	}

	/**
	 * Build a CVSDataset from the given fileName
	 * @param fileName
	 * @return
	 */
	static CSVDataset buildCVSDataset(String fileName) {
		CSVConnection con = new CSVConnection(config: "csv", path: FileUtils.PathFromFile(fileName))
		return new CSVDataset(connection: con, fileName: FileUtils.FileName(fileName), header: true)
	}

	/**
	 * Build a ExcelDataset from the given fileName
	 * @param fileName
	 * @return
	 */
	static ExcelDataset buildExcelDataset(String fileName){
		// LETS EXTRACT THE HEADER FROM THE SPREADSHEET
		Workbook workbook = WorkbookFactory.create( new File(fileName) )
		// Getting the Sheet at index zero
		Sheet sheet = workbook.getSheetAt(0)
		// Getting the first row for the header
		Row row = sheet.getRow(0)

		ExcelConnection con = new ExcelConnection(path: FileUtils.PathFromFile(fileName), fileName: FileUtils.FileName(fileName), driver: TDSExcelDriver)
		ExcelDataset dataset = new ExcelDataset(connection: con, header: true)

		// Iterate over the Header Row to build the Header fields
		Iterator<Cell> cellIterator = row.cellIterator()
		while ( cellIterator.hasNext() ) {
			Cell cell = cellIterator.next()
			String value = cell.toString()
			dataset.field << new Field(name: value, type: Field.Type.STRING)
		}

		// TODO: Need to fix column name lookup - see ticket TM-9584
        // This will need to be moved to a function that will evetunally get context of the sheet name and
        // the row that the labels reside on and compute dynamically instead of this statically built list.
		// currently adding Monkey-patching to support getting the fields since the original behaviour is not supported
//		dataset.connection.driver.metaClass.fields = { Dataset ds ->
//			return ds.field
//		}

		return dataset
	}

	/**
	 * Build a getl.data.Dataset using the file name
	 * @param fileName it can be a type CSV, XLSX or XLS
	 * @return
	 */
	static Dataset buildDataset(String fileName) {
		String ext = FileUtils.FileExtension(fileName)?.toUpperCase()

		Dataset dataset
		if (ext == 'CSV'){
			dataset = buildCVSDataset(fileName)

		} else if ( ['XLSX', 'XLS'].contains(ext) ) {
			dataset = buildExcelDataset(fileName)

		}

		return dataset

	}


	/**
     * Used to create a temporary file where the name will consist of a prefix + random + extension
     * @param prefix
     * @param extension
     * @return Calling the method returns both the name of the file as well as the FileOutputStream
     *      String filename
     *      OutputStream output stream
     */
    List createTemporaryFile(String prefix='', String extension='tmp') {
        String filename = getUniqueFilename(temporaryDirectory, prefix, extension)
        OutputStream os = new File(temporaryDirectory + filename).newOutputStream()
        log.info 'Created temporary file {}{}', temporaryDirectory, filename
        return [filename, os]
    }

    /**
     * Used to get a temporary filename where the name will consist of fully qualified path + prefix + random + extension
     * @param prefix
     * @param extension
     * @return the full qualified path and filename
     */
    String getTemporaryFilename(String prefix='', String extension='tmp') {
        String filename = getUniqueFilename(temporaryDirectory, prefix, extension)
        return temporaryDirectory + filename
    }

    /**
     * Used to get the full path to a file in the application temporary directory
     * @param filename
     * @return the fully qualified path and filename of the temporary filename passed or null if not found
     */
    String getTemporaryFullFilename(String filename) {
        temporaryFileExists(filename) ? temporaryDirectory + filename : null
    }

    /**
     * Used to open a previously created temporary file
     * @param filename
     * @return an InputStream to read the file contents if it exists otherwise NULL
     */
    InputStream openTemporaryFile(String filename) {
        InputStream is
        validateFilename(filename)
        if (temporaryFileExists(filename)) {
            is = new File(temporaryDirectory + filename).newInputStream()
        }
        return is
    }

    /**
     * Used to determine if a file exists in the temporary directory
     * @param filename
     * @return true if the file exists otherwise false
     */
    boolean temporaryFileExists(String filename) {
        return new File(temporaryDirectory + filename).exists()
    }

    /**
     * Used to get unique filename in the temporary directory
     * @param prefix
     * @param extension
     * @return
     */
    String getUniqueFilename(String directory, String prefix='', String extension='tmp') {
        String filename
        int tries = maxUniqueTries
        prefix = StringUtils.defaultIfEmpty(prefix, '')
        while(true) {
            filename = prefix + RandomStringUtils.randomAlphanumeric(32) + '.' + extension
            if (! temporaryFileExists(filename) ) {
                break
            }
            if (! --tries) {
                log.error 'Failed to generate a unique filename in {} directory', temporaryDirectory
                throw new RuntimeErrorException('getUniqueFilename unable to determine unique filename')
            }
        }
        return filename
    }

    /**
     * Used to delete a temporary file
     * @param filename - the name of the file without any path information
     * @return a flag that the file was actually deleted
     */
    boolean deleteTemporaryFile(String filename) {
        validateFilename(filename)
        boolean success = false
        File file = new File(temporaryDirectory + filename)
        if (file.exists()) {
            success = file.delete()
        }

        log.info 'Deletion of temporary file {}{} {}', temporaryDirectory, filename, (success ? 'succeeded' : 'failed')

        return success
    }

    /**
     * Used by the service to determine that the filename is legitimate in that it does not include any directory
     * references. If it does it will log a security violation and throw an exception.
     * @param filename
     * @throws InvalidRequestException
     */
    private void validateFilename(String filename) {
        if (filename.contains(File.separator)) {
            securityService.reportViolation("attempted to access file with path separator ($filename)")
            throw new InvalidRequestException('Filename contains path separator')
        }
    }

    /**
     * Take a FileCommand instance and transfer the corresponding file
     * to the file system by delegating the task in one of the overloaded
     * implementations of writeFileFromCommand.
     *
     * @param fileCommand
     * @return the filename for the temporary file that was created.
     */
    String transferFileToFileSystem(FileCommand fileCommand) {
        String temporaryFileName = null
        if (fileCommand && fileCommand.validate()) {
            temporaryFileName = writeFileFromCommand(fileCommand)
        }
        return temporaryFileName
    }

    /**
     * Write a file to the temporary directory using the extension and the content
     * given in the command object.
     * @param uploadTextCommand
     * @return
     */
    private String writeFileFromCommand(UploadTextCommand uploadTextCommand) {
        String extension = FileSystemUtil.formatExtension(uploadTextCommand.extension)
        def (String filename, OutputStream os) = createTemporaryFile('', extension)
        os << uploadTextCommand.content
        os.close()
        return filename
    }

    /**
     * Copy an uploaded file to the temporary directory.
     *
     * @param uploadFileCommand
     * @return
     */
    private String writeFileFromCommand(UploadFileCommand uploadFileCommand) {
        String extension = FileSystemUtil.getFileExtension(uploadFileCommand.file.getOriginalFilename())
        OutputStream os
        String temporaryFileName
        try {
            (temporaryFileName, os) = createTemporaryFile('', extension)
            os.write(uploadFileCommand.file.getBytes())
            os.close()
        } catch (Exception e) {
            log.error(e.getMessage())
            deleteTemporaryFile(temporaryFileName)
            throw new InvalidParamException(e.getMessage())
        }

        return temporaryFileName
    }

	/**
	 * Return the allowed extensions for ETL Uploads.
	 * @return
	 */
	List<String> getAllowedExtensions() {
		return ALLOWED_FILE_EXTENSIONS_FOR_ETL_UPLOADS
	}
}
