package net.transitionmanager.service

import com.tdssrc.grails.GormUtil
import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.data.Field
import groovy.json.JsonSlurper
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.DataScriptMode
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import org.apache.commons.io.FilenameUtils
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.codehaus.groovy.grails.web.json.JSONObject

class DataScriptService implements ServiceMethods{

    ProviderService providerService
    SecurityService securityService

    /**
     * Create or update a DataScript instance based on the JSON object received.
     *
     * @param jsonObject
     * @param dataScriptId
     * @return
     */
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
            etlSourceCode = dataScriptJson.etlSourceCode
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
        if (!dataScript.save()) {
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
    DataScript saveScript (Long id, String scriptContent) {
        Project project = securityService.userCurrentProject
        DataScript dataScript = GormUtil.findInProject(project, DataScript.class, id, true)
        dataScript.etlSourceCode = scriptContent
        dataScript.save(failOnError: true)

        return dataScript
    }

    /**
     * Find a DataScript with the given ID, checking if it belongs
     * to the user's project.
     *
     * @param dataScriptId
     * @return
     */
    DataScript getDataScript(Long id, Project project = null) {
        if (!project) {
            project = securityService.userCurrentProject
        }

        DataScript dataScript = GormUtil.findInProject(project, DataScript.class, id, true)

        return dataScript
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
    void deleteDataScript(Long dataScriptId) {
        // Fetch the DataScript, validating it belogns to the user's project.
        DataScript dataScript = getDataScript(dataScriptId)
        // Delete the DataScript if found.
        if (dataScript) {
            dataScript.delete()
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


    Map parseDataFromFile (String fileName) {
	    try{
		    String extension = FilenameUtils.getExtension(fileName)?.toUpperCase()

		    switch (extension) {
			    case 'JSON':
				    return parseDataFromJSON(fileName)

			    case 'CSV':
				    return parseDataFromCSV(fileName)

			    case ['XLS', 'XLSX'] :
				    return parseDataFromXLS(fileName)

			    default :
				      throw new RuntimeException("Format ($extension) not supported")
		    }

	    } catch (ex) {
		    log.error(ex.message, ex)
		    throw ex
	    }
    }

	Map parseDataFromJSON (String jsonFile) throws RuntimeException {
		def inputFile = new File(FileSystemService.temporaryDirectory, jsonFile)
		def jsonObject = new JsonSlurper().parseText(inputFile.text)

		def jsonMap = [
				  status: 'success',
				  data  : jsonObject
		]

		return jsonMap
	}

    Map parseDataFromCSV (String csvFile) throws RuntimeException {
        CSVConnection con = new CSVConnection(extension: 'csv', codePage: 'utf-8', config: "csv", path: FileSystemService.temporaryDirectory)
        def csv = new CSVDataset(connection: con, fileName: csvFile, header: true)

        List<Field> fields = csv.connection.driver.fields(csv)

       List<Map<String, String>> config = fields.collect {
            def type = (it.type == Field.Type.STRING) ? 'text' : it.type?.toString().toLowerCase()
            [
                    property: it.name,
                    type    : type
            ]
        }

        def jsonMap = [
                status: 'success',
                data  : [
                        config: config,
                        rows  : csv.rows()
                ]
        ]

        return jsonMap
    }

	Map parseDataFromXLS (String xlsFile, int sheetNumber=0, int headerRowIndex=0) throws RuntimeException {


		Workbook workbook = WorkbookFactory.create(new File(FileSystemService.temporaryDirectory, xlsFile))

		Sheet sheet = workbook.getSheetAt(sheetNumber)

		Row header = sheet.getRow(headerRowIndex)

		Iterator<Cell> headerCells = header.cellIterator()


		List<Map<String, String>> config = []

		while (headerCells.hasNext()) {
			Cell cell = headerCells.next()
			def type = 'text'
			config << [
					  property: cell.toString(),
					  type    : type
			]
		}


		List<Map> data = []
		int lastRowNum = sheet.getLastRowNum()

		for( int r = headerRowIndex + 1; r <= lastRowNum; r++ ) {
			Row row = sheet.getRow(r)

			Map object = [:]
			data << object
			Iterator<Row>cellIterator = row.cellIterator()

			int c = 0
			while (cellIterator.hasNext()) {
				String key = config[ c ].property
				Cell cell = cellIterator.next()
				object[ key ] = cell.toString()
				c ++
			}
		}
		log.info("data: $data")


		def jsonMap = [
				  status: 'success',
				  data  : [
							 config: config,
							 rows  : data
				  ]
		]

		return jsonMap
	}
}
