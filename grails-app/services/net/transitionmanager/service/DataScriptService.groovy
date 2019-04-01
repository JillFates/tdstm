package net.transitionmanager.service

import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.etl.DataSetFacade
import com.tdsops.etl.TDSJSONDriver
import com.tdssrc.grails.GormUtil
import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.data.Field
import getl.exception.ExceptionGETL
import getl.json.JSONConnection
import getl.json.JSONDataset
import grails.gorm.transactions.Transactional
import net.transitionmanager.command.DataScriptNameValidationCommand
import net.transitionmanager.action.ApiAction
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.imports.DataScript
import net.transitionmanager.imports.DataScriptMode
import net.transitionmanager.imports.ImportBatch
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.action.Provider
import org.apache.commons.io.FilenameUtils
import org.apache.poi.poifs.filesystem.NotOLE2FileException
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.grails.web.json.JSONObject
import org.supercsv.exception.SuperCsvException

import java.text.DecimalFormat

class DataScriptService implements ServiceMethods{

	ProviderService providerService
	FileSystemService fileSystemService

	 static final Map EMPTY_SAMPLE_DATA_TABLE = [
			   config: [],
			   rows  : []
	 ].asImmutable()

    /**
     * Create or update a DataScript instance based on the JSON object received.
     *
     * @param jsonObject
     * @param dataScriptId
     * @return
     */
	@Transactional
    DataScript saveOrUpdateDataScript(JSONObject dataScriptJson, Long dataScriptId = null) {

        // Get the current project
        Project currentProject = securityService.userCurrentProject

        DataScript dataScript

        // Get the current person
        Person currentPerson = securityService.loadCurrentPerson()

        if (!dataScriptId) {
            // Create a new DataScript instance
            dataScript = new DataScript()
        } else {
            // Fetch the DataScript with the given id.
            dataScript = getDataScript(dataScriptId, currentProject)
        }

        // Validate that there's no other DataScript for this project and provider with the same name
        if (!validateUniqueName(dataScriptJson.name, dataScriptId, dataScriptJson.providerId, currentProject)) {
            throw new DomainUpdateException("Cannot update or create DataScript because the name is not unique for this project and provider.")
        }

        // Find the provider
        Provider providerInstance = providerService.getProvider(dataScriptJson.providerId, currentProject, true)

        // Copy the values received from the JSON Object over to the DataScript instance.
        dataScript.with {
            name = dataScriptJson.name
            description = dataScriptJson.description
            target = dataScriptJson.target
            mode = DataScriptMode.forLabel(dataScriptJson.mode)
            etlSourceCode = dataScriptJson.etlSourceCode?:dataScript.etlSourceCode
            provider = providerInstance
            // if it's an existing instance, update the lastModifiedBy field
            if (id) {
                lastModifiedBy = currentPerson
            } else { // if it's a new instance, set the createdBy and project fields.
                createdBy = currentPerson
                project = currentProject
            }
        }

        // Try to save or fail.
        if (!dataScript.save(failOnError: false)) {
            throw new DomainUpdateException("Error creating or updating DataScript ${GormUtil.allErrorsString(dataScript)}")
        }
        return dataScript
    }

    /**
     * Updates a DataScript instance with a scriptContent String instance
     * @param id - the ID of the DataScript to save the script to
     * @param scriptContent - the new ETL Script content to be saved in DataScript instance
     * @return the DataScript instance with the etlSourceCode already updated in database
     */
	@Transactional
    DataScript saveScript (Long id, String scriptContent) {
        Project project = securityService.userCurrentProject
        DataScript dataScript = GormUtil.findInProject(project, DataScript.class, id, true)
        dataScript.etlSourceCode = scriptContent
        dataScript.save()

        return dataScript
    }

    /**
     * Find a DataScript with the given ID, checking if it belongs
     * to the user's project.
     *
     * @param dataScriptId
     * @return
     */
	@Transactional
    DataScript getDataScript(Long id, Project project = null) {
        if (!project) {
            project = securityService.userCurrentProject
        }

        DataScript dataScript = GormUtil.findInProject(project, DataScript.class, id, true)

	     // Check if the Sample File is still available, if not delete the temporary data
	     if ( dataScript.sampleFilename ) {
		     if ( ! fileSystemService.temporaryFileExists(dataScript.sampleFilename) ) {
			     dataScript.sampleFilename = ''
			     dataScript.originalSampleFilename = ''
			     dataScript.save()
		     }
	     }

        return dataScript
    }

	/**
	 * Validate if the name for the script is valid.
	 * @param command
	 * @return
	 */
	boolean validateUniqueName(DataScriptNameValidationCommand command, Project project = null) {
		if (!project) {
			project = securityService.userCurrentProject
		}
		// Make sure that name is unique
		int count = DataScript.where {
			project == project
			name == command.name
			provider.id == command.providerId
			if (command.dataScriptId) {
				id != command.dataScriptId
			}
		}.count()
		if (count > 0) {
				return false
		}
		return true
	}


    /**
     * Check if a given DataScript name is unique across project and provider.
     *
     * This method will check that there's no other DataScript for this project
     * and provider with the same name or, if there is, that they have the same id.
     *
     * @param params
     * @param dataScriptName
     * @return
     */
    boolean validateUniqueName(String dataScriptName, Long dataScriptId, Long providerId, Project project = null) {
        boolean isUnique = true
        if (!project) {
            project = securityService.userCurrentProject
        }

        // If the name is null don't validate, throw an exception.
        if (!dataScriptName) {
            throw new InvalidParamException("The DataScript name cannot be null.")
        }

        DataScript dataScript = DataScript.where {
            name == dataScriptName
            project == project
            provider.id == providerId
        }.find()

        if (dataScript) {
            // If the ids don't match or params has no id, then it's a duplicate.
            if (dataScriptId != dataScript.id) {
                isUnique = false
            }
        }

        return isUnique
    }

    /**
     * Return a list of DataScripts for the current project and provider (optional).
     * @param providerId
     * @return
     */
    List<DataScript> getDataScripts(Long providerId = null) {
        return DataScript.where {
            project == securityService.userCurrentProject
            if (providerId) {
                provider.id == providerId
            }
        }.list()
    }

    /**
     * Delete the given DataScript making sure it belongs to user's current project.
     *
     * @param dataScriptId
     */
	@Transactional
    void deleteDataScript(Long dataScriptId) {
        // Fetch the DataScript, validating it belogns to the user's project.
        DataScript foundDataScript = getDataScript(dataScriptId)
        // Delete the DataScript if found.
        if (foundDataScript) {
			// check api action references
			int countApiActions = ApiAction.where { defaultDataScript == foundDataScript }.count()
			if (countApiActions > 0) {
				throw new InvalidParamException("The ETL Script is being referenced by one or more Api Actions.")
			}

			// check import batch references
			int countImportBatches = ImportBatch.where { dataScript == foundDataScript }.count()
			if (countImportBatches > 0) {
				throw new InvalidParamException("The ETL Script is being referenced by one or more Import Batches.")
			}

			// if no references to foundDataScript then delete
			foundDataScript.delete()
        }
    }

    /**
     * Find a DataScript with the given id, project and provider.
     *
     * @param id
     * @param project
     * @param provider
     * @param throwException
     * @return
     */
    DataScript findByProjectAndProvider(Long id, Project project, Provider provider, boolean throwException = false) {
        DataScript dataScript = DataScript.where {
            id == id
            project == project
            provider == provider
        }.find()

        if (! dataScript && throwException) {
            throw new EmptyResultException("No DataScript exists with the ID $id for the Project $project and Provider $provider.")
        }
        return dataScript
    }

	/**
	 * Find a DataScript with the given name, project and provider
	 *
	 * @param name - datascript name
	 * @param project - datascript project
	 * @param provider - datascript provider
	 * @param throwException - whether to throw an exception if datascript is not found
	 * @return
	 */
	DataScript findByProjectAndProvider(String name, Project project, Provider provider, boolean throwException = false) {
		DataScript dataScript = DataScript.where {
			name == name
			project == project
			provider == provider
		}.find()

		if (! dataScript && throwException) {
			throw new EmptyResultException("No DataScript exists with name $name for the Project $project and Provider $provider.")
		}
		return dataScript
	}

    /**
     * Find a given DataScript with the given ID and Project.
     * @param dataScriptId
     * @param project
     * @return
     */
    DataScript findDataScriptByProject(Long dataScriptId, Project project = null) {
        if (!project) {
            project = securityService.userCurrentProject
        }
        return GormUtil.findInProject(project, DataScript, dataScriptId, true)
    }

    /**
     * Given a DataScript ID, this method returns a map containing the details
     * of the references of this DataScript in other domains.
     *
     * @param dataScriptId
     * @return
     */
    Map checkDataScriptReferences(Long dataScriptId) {
        DataScript dataScript = findDataScriptByProject(dataScriptId)
        List<Map> references = DataScript.domainReferences
        Map<String, Long> foundReferences = [:]
        for (reference in references) {
            if (reference.delete == 'restrict') {
                String hql = """
                    SELECT COUNT(*) FROM ${reference.domain.getSimpleName()}
                    WHERE ${reference.property} = :dataScript
                """
                Long refs = reference.domain.executeQuery(hql, [dataScript: dataScript])[0]
                if (refs > 0) {
                    foundReferences[reference.domainLabel] = refs
                }
            }
        }

        return [canDelete: !foundReferences, references: foundReferences]
    }

	/**
	* Parse a file that represent a MAP of data
	* Current Formats supported:
	*   JSON, CSV, EXCEL
	* @param fileName - the filename to use to retrieve data from filesystem
	* @param maxRows - the maximum number of rows to retrieve
	 * 	(currently only supported by Excel)
	* @return
	*/
	Map parseDataFromFile (Long id, String originalFileName, String fileName, String rootNode, Long maxRows) throws EmptyResultException {
		String message
		try {

			if ( id ) {
				saveSampleFile(id, originalFileName, fileName)
			}

			String extension = FilenameUtils.getExtension(fileName)?.toUpperCase()
			switch (extension) {
				case 'JSON':
					return parseDataFromJSON(fileName, maxRows, rootNode)

				case 'CSV':
					return parseDataFromCSV(fileName, 0, maxRows)

				case ['XLS', 'XLSX'] :
					return parseDataFromXLS(fileName, 0, 0, maxRows)

				default :
					message = "File format ($extension) is not supported"
			}

		} catch (ex) {
			log.info(ex.message)

			switch(ex) {
				case FileNotFoundException :
					message = "The source data was not found"
					break

				case NotOLE2FileException:
				case InvalidParamException:
				case SuperCsvException:
					message = "Unable to parse the source data"
					break
				case getl.exception.ExceptionGETL:
					message = ex.message
					break
				default:
					log.error ExceptionUtil.stackTraceToString(ex)
					message = ex.message
			}
		}

		if (message) {
			throw new InvalidParamException(message)
		}
	}

	/**
	 * Used to retrieve the contents of a temporary file
	 * @param filename - the name of the temporary file
	 * @return the File handle of the file
	 */
	private File tempFile(String filename) throws FileNotFoundException {
		File inputFile = fileSystemService.openTempFile(filename)
		if (! inputFile.exists()) {
			throw new FileNotFoundException(inputFile.toString())
		}

		return inputFile
	}

	/**
	 * Check that the File with the JSON data is ok and return the map
	 * @param jsonFile json file name
	 * @param maxRows max number of results requested
	 * @param rootNode json root node used to build map fields
	 * @return Map with the description of JSON data in the File
	 * @throws RuntimeException
	 */
	Map parseDataFromJSON(String jsonFile, Long maxRows, String rootNode) throws RuntimeException {

		JSONConnection jsonCon = new JSONConnection(
			config: "json",
			path: fileSystemService.temporaryDirectory, driver: TDSJSONDriver)

		JSONDataset dataSet = new JSONDataset(
			connection: jsonCon,
			rootNode: rootNode ?: '.',
			fileName: jsonFile)

		DataSetFacade dataSetFacade = new DataSetFacade(dataSet)

		// If there are no fields it means that none was found in the rootNode location
		if (dataSetFacade.fields().isEmpty()) {
			throw new ExceptionGETL(
					  'No data found, please check the uploaded data and the documentation for JSON data sources'
			)
		}

		return [
			config: dataSetFacade.fields().collect {
				[
					property: it.name,
					type    : (it.type == Field.Type.STRING) ? 'text' : it.type?.toString().toLowerCase()
				]
			},
			rows  : dataSetFacade.rows().take(maxRows.intValue())
		]
	}

	/**
	 * Parse CSV file to get the
	 * @param csvFile
	 * @param pagination : page (starts on zero)
	 * @param maxRows : max number of results requested
	 * @return Map with the description of the CSV File Data
	 * @throws RuntimeException
	 */
	Map parseDataFromCSV (String csvFile, Long page, Long maxRows) throws RuntimeException {
		tempFile(csvFile)

		CSVConnection con = new CSVConnection(extension: 'csv', codePage: 'utf-8', config: "csv", path: fileSystemService.temporaryDirectory)
		def csv = new CSVDataset(connection: con, fileName: csvFile, header: true)

		List<Field> fields = csv.connection.driver.fields(csv)

		List<Map<String, String>> config = fields.collect {
			def type = (it.type == Field.Type.STRING) ? 'text' : it.type?.toString().toLowerCase()
			[
				property : it.name,
				type     : type
			]
		}

		List<Map> rows = csv.rows()
		Integer start = page * maxRows
		Integer end = Math.min(rows.size(), start + (Integer)maxRows)
		List<Map> filteredRows = []
		for (Integer i = start; i < end; i++) {
			filteredRows << rows[i]
		}

		return [
				  config: config,
				  rows  : filteredRows
		]
	}

	/**
	 * Parse Excel file to generate the JSON datafile
	 * @param xlsFile - the filename to use to retrieve data from filesystem
	 * @param sheetNumber - the workbook sheet number to pull data from
	 * @param headerRowIndex - the header row index
	 * @param maxRows - the maximum number of rows to retrieve
	 * @return Map with the description of the Excel File Data
	 * @throws RuntimeException
	 */
	Map parseDataFromXLS (String xlsFile, int sheetNumber=0, int headerRowIndex=0, Long maxRows) throws RuntimeException {

		DecimalFormat df = new DecimalFormat('#.#########')

		List<Map<String, String>> config = []
		Map<Integer, String> headerMap = [:]

		Workbook workbook = WorkbookFactory.create( tempFile(xlsFile) )
		Sheet sheet = workbook.getSheetAt(sheetNumber)
		Row header = sheet.getRow(headerRowIndex)

		if (header) {
			Iterator<Cell> headerCells = header.cellIterator()

			while (headerCells.hasNext()) {
				Cell cell = headerCells.next()
				String type = 'text'
				String value = cell.toString()

				headerMap[cell.columnIndex] = value

				config << [
						  property: value,
						  type    : type
				]
			}
		}

		List<Map> data = []
		int lastRowNum = sheet.getLastRowNum()

		for( int r = headerRowIndex + 1; (r <= lastRowNum && r <= maxRows); r++ ) {
			Row row = sheet.getRow(r)

			Map object = [:]
			data << object
			Iterator<Row>cellIterator = row.cellIterator()

			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next()
				String key = headerMap[cell.columnIndex]
				String value
				if (cell.cellType == Cell.CELL_TYPE_NUMERIC) {
					value = df.format(cell.numericCellValue)
				} else {
					value = cell.toString()
				}

				object[key] = value
			}
		}

		return [
				  config: config,
				  rows  : data
		]

	}

	/**
	 * Store the sampleFileName, if changed in the DataScript object
	 * @param id DataScript identifier
	 * @param tmpFileName filename to store
	 */
	@Transactional
	private void saveSampleFile(Long id, String originalFileName, String tmpFileName) {
		DataScript ds = DataScript.get(id)

		String extension = FilenameUtils.getExtension(tmpFileName)

		if(! originalFileName ) {
			originalFileName = tmpFileName.substring(0, tmpFileName.lastIndexOf('_')) + '.' + extension
		}

		if ( !originalFileName.equalsIgnoreCase(ds.originalSampleFilename) ) {
			if ( ds.sampleFilename ) { // delete old file associated
				fileSystemService.deleteTemporaryFile(ds.sampleFilename)
			}

			ds.originalSampleFilename = originalFileName
			ds.sampleFilename = tmpFileName
			ds.save()
		}
	}

	/**
	 * Clone any existing data scripts associated to sourceProject (if any),
	 * then associate those newly created data scripts to targetProject.
	 *
	 * @param sourceProject  The project from which the existing data scripts will be cloned.
	 * @param targetProject  The project to which the new data scripts will be associated.
	 */
	void cloneProjectDataScripts(Project sourceProject, Project targetProject) {
		List<DataScript> dataScripts = DataScript.where {
			project == sourceProject
		}.list()

		if (!dataScripts.isEmpty()) {
			dataScripts.each { DataScript sourceDataScript ->
				Provider targetProvider = providerService.getProvider(sourceDataScript.provider.name, targetProject, false)
				DataScript newDataScript = (DataScript)GormUtil.cloneDomainAndSave(sourceDataScript,
						[project: targetProject, provider: targetProvider], false, false);
				log.debug "Cloned data script ${newDataScript.name} for project ${targetProject.toString()} and provider ${targetProvider.name}"
			}
		}
	}
}
