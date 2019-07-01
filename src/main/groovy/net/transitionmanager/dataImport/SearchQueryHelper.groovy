package net.transitionmanager.dataImport

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.etl.DomainClassQueryHelper
import com.tdsops.etl.ETLDomain
import com.tdsops.etl.FindCondition
import com.tdsops.etl.QueryResult
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import groovy.util.logging.Slf4j
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.Room
import net.transitionmanager.exception.InvalidRequestException
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.model.Model
import net.transitionmanager.person.Person
import net.transitionmanager.person.PersonService
import net.transitionmanager.project.Project

@Slf4j
class SearchQueryHelper {

	static final Integer NOT_FOUND_BY_ID = -1
	static final String FIND_FOUND_MULTIPLE_REFERENCES_MSG = 'Multiple records found for find/elseFind criteria'
	static final String SEARCH_BY_ID_NOT_FOUND_MSG = 'Record not found searching by id'
	static final String ALTERNATE_LOOKUP_FOUND_MULTIPLE_MSG = 'Multiple records found with current value'
	static final String WHEN_NOT_FOUND_PROPER_USE_MSG = "whenNotFound create only applicable for reference properties"
	static final String PROPERTY_NAME_NOT_IN_FIELDS = "Field {propertyName} was not found in ETL dataset"
	static final String PROPERTY_NAME_NOT_IN_DOMAIN = "Invalid field {propertyName} in domain"
	static final String NO_FIND_QUERY_SPECIFIED_MSG = 'No find/findElse specified for property'

	@Lazy
	private static PersonService personService = { -> (PersonService) ApplicationContextHolder.getBean('personService', PersonService) }()

	/**
	 * Used to initialize the errors list that will be stuffed into the context object
	 */
	static private void initSearchQueryHelperErrors(Map context, Boolean reset=false) {
		if (reset) {
			context.searchQueryHelperErrors = []
		} else if (! context.containsKey('searchQueryHelperErrors')) {
			context.searchQueryHelperErrors = []
		}
	}

	/**
	 * Used to add an error to the error list that is stuffed into the context object
	 */
	static private void recordError(Map context, String errorMsg) {
		initSearchQueryHelperErrors(context)
		context.searchQueryHelperErrors << errorMsg
	}

	/**
	 * Used in an attempt to lookup a domain record using the metadata that is provided by the
	 * ETL process. This method will leverage caching of the domain entities to expedite retrieval
	 * for entities that are frequently cross-referenced (e.g. Clusters to Servers) when the context
	 * contains a cache reference.
	 *
	 * An errors that occur during this process will be recorded into the context.searchQueryHelperErrors.
	 *
	 * @param fieldName - the domain fieldname
	 * @param fieldsInfo - the Map with the ETL meta data for all of the fields for the row
	 * @param context - the context map that the process uses to cart crap around
	 * @param entityInstance - optional used when looking up a reference field
	 * @return will return various results based on searches
	 * 		entity 	: if found
	 *		null	: if not found by alternate or query
	 *		-1 		: an error occurred, which is recorded in the fieldsInfo appropriately
	 *
	 * The process logic should flow as documented here:
	 *
	 * 		If find.results contains a single id
	 *		Then get with id
	 *			If found then done
	 *			Else error
	 *		Else if field.value is an ID (number)
	 *			Then get with id
	 *				If found then done
	 *				Else error
	 *		Else if find.query specified then requery
	 *			If found one (1) then done
	 *			Else if found more than one (1) then error
	 *		Try searching by alternate key value in field.value
	 *			if found one (1) then done else error
	 *
	 * The structure looks like the following:
	 *	{
	 * 		fields": {
	 * 			"asset": {
	 *				// Search by Alternate Key Example
	 * 				"value": "xraysrv01",
	 *				// Search by primary ID Example
	 * 				"value": 114052,
	 * 				"originalValue": "114052",   // THIS MAY BE GOING AWAY JPM 7/2018
	 *              "previousValue": "23432" // This is set during the POSTING process for existing records being updated
	 * 				"error": false,
	 *				"errors": [ "Lookup by ID was not found"],
	 * 				"warn": false,
	 * 				"find": {
	 * 					"query": [
	 *						[ domain: 'Device', kv: [ assetName: 'xraysrv01', assetType: 'Server' ] ]
	 *						[ domain: 'Device', kv: [ assetName: 'xraysrv01'] ]
	 *					],
	 *					"matchOn": 2,
	 *					"results": [12312,123123,123123123]
	 * 				}
	 * 			},
	 *
	 * @test Integration
	 */
	static Object findEntityByMetaData(String fieldName, Map fieldsInfo, Map context=null, Object entityInstance=null) {
		// This will be populated with the entity object or error message appropriately
		Object entity

		if (context == null) {
			context = [:]
		}

		// Initialize the errors placeholder that this or any of the support functions might set an error
		initSearchQueryHelperErrors(context, true)

		// This will be used to check/set cache for previously searched items
		String md5

		// This is a QA Easter Egg to test the error handling
		if (fieldsInfo[fieldName]?.value == '!~! Go ahead, Make my day !~!') {
			throw new InvalidRequestException('Do you feel lucky, punk?')
		}
		boolean foundInCache=false
		boolean errorPreviouslyRecorded = false
		boolean fieldIsId = fieldName == 'id'
		boolean fieldIsInFieldsInfo = fieldsInfo.containsKey(fieldName)

		// Flags that a search by ID failed which will result in an error so that duplicates are not created
		boolean searchedById = false

		Class domainClassOfProperty
		if (entityInstance) {
			domainClassOfProperty = entityInstance.getClass()
		} else {
			// The context.domainClass seems to vary (bug) between the actual domain class and the ETLDomain
			domainClassOfProperty = (context.domainClass in ETLDomain) ? context.domainClass.getClazz() : context.domainClass
		}

		Class domainClass
		(domainClass, entity) = classOfDomainProperty(fieldName, fieldsInfo, domainClassOfProperty)
		String domainShortName = domainClass ? GormUtil.domainShortName(domainClass) : null

		switch (domainClass) {
			case AssetDependency:
				// Like asset fields, first check fetch entity by id.
				// It enables to looks up AssetDependency by ids.
				entity = fetchEntityById(domainClass, fieldName, fieldsInfo, context)
				if (!entity) {
					entity = fetchAssetDependencyByAssets(fieldsInfo, context)
				}

				break

			case Person:
				// When the Person is a reference in another domain then we can pass it into the fetchPerson logic
				Person existingPerson = entityInstance ? entityInstance[fieldName] : null
				String searchValue = getValueOrInitialize(fieldName, fieldsInfo)
				if (searchValue){
					String errorMsg
					(entity, errorMsg) = fetchPerson(existingPerson,  searchValue, fieldName, fieldsInfo, context)
					if (errorMsg) {
						// If no entity was found then we want to capture the error message to save in the cache
						entity = errorMsg
						// addErrorToFieldsInfoOrRecord(fieldName, fieldsInfo, context, errorMsg)
						recordError(context, errorMsg)
					}
				}
				break

			default:
				//
				// Now going to try up to 5+ different ways to find the domain entity
				//

				// 1. See if this property based on ID is in the cache already
				if (context.cache && fieldsInfo.containsKey(fieldName)) {
					md5 = generateMd5OfFieldsInfoField(domainShortName, fieldName, fieldsInfo)
					// log.debug 'fetchEntityByFieldMetaData() has cache key {}', md5
					entity = context.cache.get(md5)
					if (entity) {
						log.debug 'fetchEntityByFieldMetaData() resolved by method 1 (cache ID {}, {})', md5, entity
						foundInCache=true
						break
					}
				}

				// 2. Attempt to find the domain by the ID in the property field.value (Number or String)
				entity = fetchEntityById(domainClass, fieldName, fieldsInfo, context)
				if (entity) {
					if (entity == NOT_FOUND_BY_ID) {
						// Didn't find but we did have an ID
						searchedById = true
					} else {
						log.debug 'fetchEntityByFieldMetaData() resolved by method 2 (ID)'
						break
					}
				}

				// 3. Attempt to find domain with the single result (find.results[0])
				if ( hasSingleFindResult(fieldName, fieldsInfo) ) {
					searchedById = true
					entity = fetchEntityByFindResults(fieldName, fieldsInfo, context)
					if (entity) {
						log.debug 'fetchEntityByFieldMetaData() resolved by method 3 (find results)'
						break
					}
				}

				// Fail out if the field had a previously set/resolved ID
				if (searchedById) {
					// This is when we give up because there were attempts by previously specified or resolved ID but
					// now attempting to retrieve has failed indicating that the entity was deleted. As such we do NOT
					// what a create a new record.
					log.info 'fetchEntityByFieldMetaData() failed to resolve by ELT ID reference - domain {}, field {}',
						domainClass.getName(), fieldName
					entity = SEARCH_BY_ID_NOT_FOUND_MSG
					break
				}

				// 4. Attept to find domain by re-applying the find/elseFind queries
				if ( hasFindQuery(fieldName, fieldsInfo)) {
					List entities = performQueryAndUpdateFindElement(fieldName, fieldsInfo, context)
					int qtyFound = entities?.size() ?: 0
					if (qtyFound == 1) {
						entity = entities[0]
						log.debug 'fetchEntityByFieldMetaData() resolved by method 4 (requery), found 1 '
						break
					} else if (qtyFound > 1 ) {
						log.debug 'fetchEntityByFieldMetaData() resolved by method 4 (requery), found {}', qtyFound
						entity = FIND_FOUND_MULTIPLE_REFERENCES_MSG
						errorPreviouslyRecorded = true
						break
					}
				}

				// 5. Attempt to find domain by alternate key (which is the least precise)
				// If the value was a String and try looking up the entity by it's alternate key (e.g. assetName or name)
				def searchValue = getValueOrInitialize(fieldName, fieldsInfo)
				Map findResult = fetchEntityByAlternateKey(domainClass, searchValue, fieldName, fieldsInfo, context)
				// entities = findDomainByAlternateProperty(fieldName, fieldsInfo, context)
				if (findResult.error) {
					// addErrorToFieldsInfoOrRecord(fieldName, fieldsInfo, context, findResult.error)
					entity = findResult.error
					recordError(context, findResult.error)
					break
				} else {
					int qtyFound = findResult.entities?.size() ?: 0
					if (qtyFound == 1) {
						log.debug 'fetchEntityByFieldMetaData() resolved by method 5 (alternate key), found 1 by {}', fieldsInfo[fieldName].value
						entity = findResult.entities[0]
						break
					} else if (qtyFound > 1 ) {
						log.debug 'fetchEntityByFieldMetaData() resolved by method 5 (alternate key), found {} by []', qtyFound, fieldsInfo[fieldName].value
						entity = ALTERNATE_LOOKUP_FOUND_MULTIPLE_MSG
						break
					}
				}
		} // switch (domainClass)

		// Cache the entity or error message for the lookup (unless it was found in cache above)
		if (! foundInCache && md5) {
			log.debug ('fetchEntityByFieldMetaData() added to cache: key {}, class {}, fieldName {}, entity {}', md5, domainShortName, fieldName, entity)
			context.cache.put(md5, entity)
		}

		// Deal with setting the error message if the entity wasn't found
		if ( (entity instanceof CharSequence) ) {
			if (! errorPreviouslyRecorded) {
				// addErrorToFieldsInfoOrRecord(fieldName, fieldsInfo, context, entity)
				recordError(context, entity)
			}
			entity = -1
		}

		return entity
	}

	/**
	 * Used to determine what the actual class is of a particular domain property. In the case of AssetEntity the logic
	 * logic needs to determine which type is actually intended based on the ETLDomain property name (e.g. Device, Asset, etc)
	 * @param propertyName - the property to get the class type for
	 * @param fieldsInfo - the ETL info on the fields of the entity
	 * @param domainClass - the class of the domain that the property is for
	 * @return A list containing the Class of the property and String with a value if there was any errors
	 */
	static List classOfDomainProperty(String propertyName, Map fieldsInfo, Class domainClass ) {
		Class domainClassToCreate
		ETLDomain ed
		String errorMsg
		if (domainClass == null) {
			throw new RuntimeException("classOfDomainProperty() called with missing domainClass parameter")
		}

		if (! GormUtil.isDomainProperty(domainClass, propertyName)) {
			errorMsg = StringUtil.replacePlaceholders(PROPERTY_NAME_NOT_IN_DOMAIN, [propertyName:propertyName])
			log.debug 'classOfDomainProperty() {} for domain {}', errorMsg, domainClass.getName()
		} else {
			while ( true ) {
				if (propertyName == 'id') {
					domainClassToCreate = domainClass
					break
				}

				// if (! fieldsInfo.containsKey(propertyName)) {
				// 	errorMsg = StringUtil.replacePlaceholders(PROPERTY_NAME_NOT_IN_FIELDS, [propertyName:propertyName])
				// 	log.debug 'classOfDomainProperty() {}', errorMsg
				// 	break
				// }

				Boolean isIdentifierProperty = GormUtil.isDomainIdentifier(domainClass, propertyName)
				Boolean isReferenceProperty = GormUtil.isReferenceProperty(domainClass, propertyName)
				log.debug 'classOfDomainProperty() for property {}, isIdentifierProperty {}, isReferenceProperty {}', propertyName, isIdentifierProperty, isReferenceProperty

				// propertyName MUST be a reference or identifier for this function otherwise record an error
				if (! ( isIdentifierProperty || isReferenceProperty ) ) {
					errorMsg = WHEN_NOT_FOUND_PROPER_USE_MSG
					log.debug 'classOfDomainProperty() {}', errorMsg
					break
				}

				if (isIdentifierProperty) {
					log.debug 'classOfDomainProperty() is the identifier'
					domainClassToCreate = domainClass
					break
				}

				// Get the type for the property of domain class being processed by the batch
				domainClassToCreate = GormUtil.getDomainPropertyType(domainClass, propertyName)

				if (isReferenceProperty) {
					// We need to try and resolve what class to create. Most times it is just the class type of the property in the
					// parent domain. In the case of AssetEntity however the class could be AssetEntity, Application, Database, etc.
					// In order to know which the assumption is that there will be a find.query and that the first search is going to
					//be precisely what that DataScript developer intended to be created.

					String classShortName = GormUtil.domainShortName(domainClassToCreate)
					if (classShortName == 'AssetEntity') {
						// Try looking for the exact class type in the find.query
						List query = fieldsInfo[propertyName]?.find?.query
						if (query?.size() > 0) {
							ed = ETLDomain.lookup(query[0].domain)
							domainClassToCreate = ed.getClazz()
						} else {
							// Need to look into the create kv map for 'assetClass' to see if the DataScript developer specified it
							Map createInfo = fieldsInfo[propertyName]?.create ?: [:]
							if (createInfo.containsKey('assetClass')) {
								ed = ETLDomain.lookup(createInfo['assetClass'])
								domainClassToCreate = ed.getClazz()
							}
						}
					}
					break
				}

				break
			}
		}

		log.debug 'classOfDomainProperty() for property {} for class {} type is {}',
			propertyName,
			domainClass.getName(),
			( domainClassToCreate ? domainClassToCreate.getName() : 'Not a Reference' )

		return [ domainClassToCreate, errorMsg ]
	}

	/**
	 * Used by the fetchEntityByFieldMetaData method to find an AssetDependency by the asset and dependent assets specified
	 * in the fieldsInfo appropriately. If either assets can not be located then a null is returned.
	 *
	 * @return the Dependency if found
	 * @test None
	 */
	private static AssetDependency fetchAssetDependencyByAssets(Map fieldsInfo, Map context ) {
		Object primary
		AssetEntity supporting
		AssetDependency dependency

		log.debug 'fetchAssetDependencyByAssets() was called'

		primary = findEntityByMetaData('asset', fieldsInfo, context)
		if (primary in AssetEntity) {
			log.debug 'fetchAssetDependencyByAssets() primary asset was found'
			supporting = findEntityByMetaData('dependent', fieldsInfo, context)
			if (supporting in AssetEntity) {
				log.debug 'fetchAssetDependencyByAssets() supporting asset was found'
				dependency = AssetDependency.where {
					asset.id == primary.id
					dependent.id == supporting.id
				}.find()
				if (dependency) {
					log.debug 'fetchAssetDependencyByAssets() asset dependency was found'
				}
			}
		}

		return dependency
	}

	/**
	 * Used by fetchEntityByFieldMetaData to get the entity by the field.value containing an ID as a Number or
	 * String. If the value is a String containing a number it could actually be the name of the entity so if that
	 * is so then it will not flag the NOT_FOUND_BY_ID if not found.
	 *
	 * @return One of three values:
	 * 		entity : The entity instance if ID was specified and found
	 *		null : ID was not specified
	 *		NOT_FOUND_BY_ID : if ID specified but not found
	 * @test Integration
	 */
	private static Object fetchEntityById(Class domainClass, String fieldName, Map fieldsInfo, Map context) {
		Object searchValue = fieldsInfo[fieldName]?.value ?: null
		log.debug 'fetchEntityById() {}.{} with {}', domainClass.getName(), fieldName, searchValue
		Object entity
		Boolean searchedById = false
		if (fieldsInfo.containsKey(fieldName)) {
			Boolean valueIsString = (searchValue instanceof CharSequence)
			Long id = NumberUtil.toPositiveLong(searchValue)
			// log.debug 'fetchEntityById() isaNumber={}, isaString={}, idValue={}', isaNumber, isaString, idValue
			if (id) {
				searchedById = true
				entity = GormUtil.findInProject(context.project, domainClass, id)
			}
			log.debug 'fetchEntityById() domainClass={}, fieldName={}, id={}, entity={}', domainClass.getName(), fieldName, id, entity

			if (searchedById && ! entity) {
				entity = (valueIsString ? null : NOT_FOUND_BY_ID)
			}
		}
		return entity
	}

	/**
	 * Used to determine if the fieldsInfo for a property has a single result.
	 * Called by fetchEntityByFieldMetaData.
	 * @param fieldsInfo - the Map with the ETL meta data for all of the fields for the row
	 * @param context - the context map that the process uses to cart crap around
	 * @return true if there is a single result otherwise false
	 * @test Integration
	 */
	private static Boolean hasSingleFindResult(String fieldName, Map fieldsInfo) {
		Boolean hasSingleResult = null
		if (fieldsInfo.containsKey(fieldName)) {
			hasSingleResult = fieldsInfo[fieldName].find?.results?.size() == 1
			log.debug 'hasSingleFindResult() for field {} has single result? {}', fieldName, hasSingleResult
		}
		return hasSingleResult
	}

	/**
	 * Used to determine if the fieldsInfo for a property has a find/elseFind query specified
	 * @param fieldsInfo - the Map with the ETL meta data for all of the fields for the row
	 * @param context - the context map that the process uses to cart crap around
	 * @return true if there is one or more queries defined
	 * @test Integration
	 */
	private static Boolean hasFindQuery(String fieldName, Map fieldsInfo) {
		Boolean hasFindQuery=null
		if (fieldsInfo.containsKey(fieldName)) {
			hasFindQuery = fieldsInfo[fieldName].find?.query?.size() > 0
			log.debug 'hasFindQuery() for field {} has find query? {}', fieldName, hasFindQuery
		}
		return hasFindQuery
	}

	/**
	 * Called by fetchEntityByFieldMetaData
	 * Used to query for domain entities using the meta-data generated by the find/elseFind commands in the
	 * ELT DataScript. After performing the queries it will update the find section of the fieldsInfo with the
	 * the results. It will return a list of the entities found.
	 *
	 * @param fieldsInfo - the Map with the ETL meta data for all of the fields for the row
	 * @param context - the context map that the process uses to cart crap around
	 * @return list of entities found
	 */
	private static List<Object> performQueryAndUpdateFindElement(String propertyName, Map fieldsInfo, Map context) {
		List<Object> entities = []

		initSearchQueryHelperErrors(context)

		// If the lookup is for a reference field then it is mandatory in the script to account for this via
		if ( ! fieldsInfo[propertyName].find?.query || fieldsInfo[propertyName].find.query.size() == 0 ) {
			// addErrorToFieldsInfoOrRecord(propertyName, fieldsInfo, context, NO_FIND_QUERY_SPECIFIED_MSG)
			recordError(context, NO_FIND_QUERY_SPECIFIED_MSG)
		} else {
			// log.debug 'performQueryAndUpdateFindElement() for property {}: Searching with query={}', propertyName, fieldsInfo[propertyName].find?.query
			int recordsFound = 0
			int foundMatchOn = -1

			// Iterate over the list of Queries until something is found
			//  and update the find section appropriately.
			for (query in fieldsInfo[propertyName].find.query) {
				foundMatchOn++

				// Use the ETL find logic to try searching for the domain entities
				ETLDomain whereDomain = ETLDomain.lookup(query.domain)

				// Support the new find query language using criterias
				if(query.getClass().isAssignableFrom(QueryResult) || query.containsKey('criteria')){
					List<FindCondition> conditions = FindCondition.buildCriteria(query.criteria)
					entities = DomainClassQueryHelper.where(whereDomain, context.project, conditions, false)
				} else {
					entities = DomainClassQueryHelper.where(whereDomain, context.project, query.kv, false)
				}

				recordsFound = entities.size()
				if (recordsFound > 0) {
					break
				}
			}

			// Update the field section of the fieldsInfo with the results of the this series of queries
			fieldsInfo[propertyName].find.with() {
				matchOn = (recordsFound > 0 ? foundMatchOn : 0)
				fieldsInfo[propertyName].find.size = recordsFound
				fieldsInfo[propertyName].find.results = entities*.id
			}

			log.debug 'performQueryAndUpdateFindElement() for property={}, find={}', propertyName, fieldsInfo[propertyName].find
			// Record error on the field if more than one entity was found
			if (recordsFound > 1) {
				// addErrorToFieldsInfoOrRecord(propertyName, fieldsInfo, context, FIND_FOUND_MULTIPLE_REFERENCES_MSG)
				recordError(context, FIND_FOUND_MULTIPLE_REFERENCES_MSG)
			}
		}

		return entities
	}

	/**
	 * Called by fetchEntityByFieldMetaData.
	 * Used to fetch a single domain entity based on the results of the find/elseFind commands having
	 * found a single entity. The hasSingleFindResult method must be called first to determine if this
	 * method should be called.
	 *
	 * @param propertyName - the property that has find results to lookup the object
	 * @param fieldsInfo - the Map with the ETL meta data for all of the fields for the row
	 * @param context - the context map that the process uses to cart crap around
	 * @return One of two values:
	 * 		entity : the entity instance referenced in find results if found
	 *		null : the find result reference was not found, must of been deleted
	 */
	private static Object fetchEntityByFindResults(String propertyName, Map fieldsInfo, Map context) {
		Object entity=null
		if (hasSingleFindResult(propertyName, fieldsInfo)) {
			def find = fieldsInfo[propertyName].find ?: null
			Long entityId = find.results[0]
			String domainName = find.query[0].domain
			// Get the class of the domain specified in find of the ETL script
			Class domainClass = ETLDomain.lookup(domainName)?.getClazz()

			if (domainClass) {
				// Now get the entity by the id in the results
				entity = GormUtil.findInProject(context.project, domainClass, entityId, false)
			} else {
				// This really should never happen but just in case
				throw new RuntimeException("ETL find/elseFind references invalid domain '${domainName}'")
			}
		}
		return entity
	}

	/**
	 * Called by fetchEntityByFieldMetaData
	 * Used by the createReferenceDomain to locate other reference domain objects (e.g. manufacturer or model) that will be set
	 * on the entity being created.
	 * @param entity - the Entity that is being created
	 * @param refDomainPropName - the property name of the entity for which the reference is going to be searched
	 * @param fieldsInfo - the information map of all of the parent record properties
	 * @param referenceFieldName - the field name in the parent record for which the reference domain is being searched (e.g. roomSource of AssetEntity)
	 * @param context - the map that contains the holy grail of the Import Batch processing
	 * @return A map containing
	 *		entities: List of reference domain entities that were found
	 * 		error: A String with any error encountered
	 */
	private static Map fetchEntityByAlternateKey(Class domainClass, String searchValue, String referenceFieldName, Map fieldsInfo, Map context) {
		Map result = [entities: [], error: '']
		log.debug 'fetchEntityByAlternateKey() domainClass {}, searchValue {}', domainClass.getName(), searchValue

		if (searchValue?.size() > 0) {
			// Class refDomainClass = GormUtil.getDomainPropertyType(domainClass, refDomainPropName)
			String refDomainName = GormUtil.domainShortName(domainClass)

			// Make sure that the domain has an alternateLookup defined on the class
			// if (! GormUtil.getAlternateKeyPropertyName(refDomainClass)) {
			// 	result.error = "Reference ${refDomainPropName} of domain ${refDomainName} does not support alternate key lookups")
			// 	return result
			// }

			Map extraCriteria = [:]
			// TODO : JPM 6/2018 : This requires that we have access to the parent instance so we can snag manufacturer or other related fields
			/*
			if (refDomainName == 'Model') {
				// The first query of Model will be by Name + Mfg & assetType (if they are specified)
				if (entity.manufacturer) {
					extraCriteria.put('manufacturer', entity.manufacturer)
				} else {
					result.error = 'Manufacturer is required in order to find model by alternate key reference'
					return result
				}

				// TODO : JPM 6/2018 : why did I add this as additional criteria? May need to add back
				// if (entity.assetType) {
				// 	extraCriteria.put('assetType', entity.assetType)
				// }
			}
			*/

			List extraAlternate = []
			Manufacturer mfg

			// TODO : JPM 6/2018 : Searching rooms/racks requires knowing the target field that we're looking up the resource. Therefore
			// we need to pass the parentPropertyName into this logic...
			switch (refDomainName) {
				case 'Room':
					// Get the Location field
					if (referenceFieldName == 'roomSource'){
						extraCriteria.put('source',  1)
					}
					List parts = searchValue.split('/')
					if (parts.size() != 2) {
						result.error = 'Room must be formatted as Location/Name'
					} else {
						searchValue = parts[1].trim()
						extraCriteria.put( 'location', parts[0].trim() )
					}
					break

				case 'Rack':
					// Resolve the Room first
					boolean isSource = referenceFieldName == 'rackSource'
					extraCriteria.put('source', (isSource ? 1 : 0))
					String roomFieldName = isSource ? 'roomSource' : 'roomTarget'
					if (fieldsInfo.containsKey(roomFieldName)) {
						Room room = findEntityByMetaData(roomFieldName, fieldsInfo, context)
						if (! room) {
							result.error = 'Unable to find Room'
						} else {
							extraCriteria.put('room.id', room.id)
						}
					} else {
						result.error = 'Room must be included to set rack'
					}
					break

				case 'Model':
					// Need to get the Manufacturer ID
					// TODO : 6/2018 : properly get the mfg id
					if (fieldsInfo.containsKey('manufacturer')) {
						mfg = findEntityByMetaData('manufacturer', fieldsInfo, context)
						if (! mfg) {
							result.error = 'Unable to resolve manufacturer'
						} else {
							extraCriteria.put('manufacturer.id', mfg.id)
						}
					} else {
						result.error = 'Manufacturer must be included to set model'
					}
					break
			}

			if (! result.error) {
				List entities = GormUtil.findDomainByAlternateKey(domainClass, searchValue, context.project, extraCriteria)

				// For Manufacturer and Model this will try searching via their Aliases tables
				if ( !entities && refDomainName in ['Manufacturer', 'Model']) {
					Object entity
					if ( refDomainName == 'Model' ) {
						entity = Model.lookupFirstAlias(searchValue, mfg)
					} else if ( refDomainName == 'Manufacturer' ) {
						entity = Manufacturer.lookupFirstAlias(searchValue)
					}
					if (entity) {
						entities = [entity]
					} else {
						entities = []
					}
				}

				int numFound = entities.size()

				log.debug 'fetchEntityByAlternateKey() RESULTS: domainClass={}, searchValue={}, extraCriteria={}, found={}',
					domainClass.getName(), searchValue, extraCriteria, numFound

				if (numFound > 0) {
					result.entities = entities
				}
			}
		}

		return result
	}

	/**
	 * Used to fetch a reference entity that is a field within an existing entity instance. For Model and Room and other types it will
	 * perform more extensive searches with additional fields and aliases (mfg/model).
	 *
	 * @param entity - the entity instance that the field reference will resides in
	 * @param fieldName - the field name that is the reference to fetch
	 * @param fieldsValueMap - a map consistenting of the values that each of the fields will be set to
	 * @param context - the map that contains the holy grail of the Import Batch processing
	 * @return a map containing the following:
	 *		entities: List of reference domain entities that were found
	 * 		error: A String set if an error was encountered
	 */
	private static List fetchReferenceOfEntityField(Object entity, String fieldName, Map fieldsValueMap, Map context, String parentFieldName = '') {
		List<Object> entities = []
		Object searchValue = fieldsValueMap[fieldName]
		Manufacturer mfg
		String errorMsg

		log.debug 'fetchReferenceOfEntityField() Fetching {}.{} with value {}', entity.getClass().getName(), fieldName, searchValue

		if (! fieldsValueMap.containsKey(fieldName)) {
			errorMsg = "Field $fieldName must be defined"
		}

		if (! errorMsg && searchValue) {
			Class refDomainClass = GormUtil.getDomainPropertyType(entity, fieldName)
			String refDomainName = GormUtil.domainShortName(refDomainClass)

			// This map will get populated with extra criteria that will be used for the alternate key lookup appropriately
			Map extraCriteria = [:]

			switch (refDomainName) {
				case 'Model':
					// If the entity doesn't have the Manufacturer set yet or the Manufacturer.name doesn't match then
					// this will recursively call this method to get the room object
					if ( ! entity.manufacturer || entity.manufacturer.name != fieldsValueMap['manufacturer'] ) {
						List<Object> mfgEntities
						String mfgError
						(mfgEntities, mfgError) = fetchReferenceOfEntityField(entity, 'manufacturer', fieldsValueMap, context)
						if ( mfgError ) {
							errorMsg = mfgError
						} else {
							switch ( mfgEntities.size() ) {
								case 1:
									// Perfect - the Room was found so we can add it to the criteria
									mfg = mfgEntities[0]
									break
								case 0:
									errorMsg = "Manufacturer '${fieldsValueMap.manufacturer}' was not found"
									break
								default:
									errorMsg = "Found multiple Manufacturers with name '${fieldsValueMap.manufacturer}'"
							}
						}
					} else {
						mfg = entity.manufacturer
					}

					if (!errorMsg) {
						if (mfg) {
							extraCriteria.put('manufacturer', mfg)

							// Setting the assetType but then there could be a mismatch so this should be thought through...
							// TODO : JPM 9/2018 : Need to determine if the assetType should be a criteria
							// if (entity.assetType) {
							// 	extraCriteria.put('assetType', entity.assetType)
							// }

						} else {
							errorMsg = 'Manufacturer must be specified in order to set Model'
						}
					}
					break

				case 'Room':
					// Add criteria to indicate source or target Room
					boolean isSource = parentFieldName.endsWith('Source')
					String locationName = 'location' + isSource ? 'Source' : 'Target'
					extraCriteria.put('source', (isSource ? 1 : 0))

					if ( fieldsValueMap[locationName] ) {
						extraCriteria.put('location', fieldsValueMap[locationName])
					} else if (searchValue.contains('/')){
						String[] values = searchValue.split('/')
						extraCriteria.put('location', values[0].trim())
						searchValue = values[1].trim()
					} else {
						errorMsg = 'Room location was not defined'
					}
					break

				case 'Rack':
					boolean isSource = (fieldName == 'rackSource')

					// Add criteria to indicate source or target Room
					extraCriteria.put('source', (isSource ? 1 : 0))

					// Determine the room object that is necessary for finding the rack
					String roomFieldName = ( isSource ? 'roomSource' : 'roomTarget' )
					if ( ! fieldsValueMap[roomFieldName] ) {
						errorMsg = 'Room is required for Rack lookup'
					} else {
						// If the entity doesn't have the room set yet or the room name doesn't match then
						// this will recursively call this method to get the room object


						if ( ! entity[roomFieldName] || entity[roomFieldName].roomName != fieldsValueMap[roomFieldName] ) {
							List<Object> roomEntities
							String roomErrorMsg
							(roomEntities, roomErrorMsg) = fetchReferenceOfEntityField(entity, roomFieldName, fieldsValueMap, context, fieldName)
							if ( roomErrorMsg) {
								errorMsg = roomErrorMsg
							} else {
								switch (roomEntities) {
									case 1:
										// Perfect - the Room was found so we can add it to the criteria
										extraCriteria.put('room', roomEntities[0])
										break
									case 0:
										errorMsg = "Specified Room '${fieldsValueMap[roomFieldName]}' was not found"
										break
									default:
										errorMsg = "Found multiple Rooms with name '${fieldsValueMap[roomFieldName]}'"
								}
							}
						}
					}
					break

				case 'Person':
					// Person is a special case which we will create the person if not found unless the name is ambiguous.
					Map resultMap = personService.findOrCreatePerson(searchValue, context.project, context.staffList)
					if (resultMap) {
						if (resultMap.isAmbiguous) {
							errorMsg = 'Multiple references found for value'
						} else if (resultMap.person) {
							return [ [resultMap.person], null]
						}
					}
					break
			}

			if (! errorMsg) {

				// First try to find the Domain by its ID
				if(NumberUtil.isaNumber(searchValue)){
					entities = [refDomainClass.get(NumberUtil.toLong(searchValue))]
				} else {

					// Make sure that the domain has an alternateLookup defined on the class
					if (! GormUtil.getAlternateKeyPropertyName(refDomainClass)) {
						errorMsg = "Reference ${fieldName} of domain ${refDomainName} does not support alternate key lookups"
						return [entities, errorMsg]
					}
					entities = GormUtil.findDomainByAlternateKey(refDomainClass, (String)searchValue.value, (Project)context.project, extraCriteria)
				}

				// If not found and the domain has an alias (mfg/model) then try looking up that way
				if ( ! entities ) {
					log.debug 'fetchReferenceOfEntityField() Searching by alias'
					switch (refDomainName) {
						case 'Model':
							if (mfg) {
								entities = [ Model.lookupFirstAlias(searchValue, mfg) ]
							}
							break

						case 'Manufacturer':
							entities = [ Manufacturer.lookupFirstAlias(searchValue) ]
							break
					}
				}

				log.debug 'fetchReferenceOfEntityField() {}.{} == {}, extraCriteria: {}, found: {}',
					refDomainName, fieldName, searchValue, extraCriteria, entities.size()
			}
		}

		return [entities, errorMsg]
	}

	/**
	 * Used fetch the person based on a string search value
	 *
	 * The logic will first check to see if the name matches that of the person currently assigned to the
	 * field if pre-existing then if not it will then use the PersonService to lookup the person by their name.
	 *
	 * When errors encountered or multiple references found then an error will be recorded into the fieldsInfo appropriately.
	 *
	 * Note this this method is dependent on the staffList being populated in the context object for performances reasons.
	 * It also assumes that the value is not the person ID and that there is a searchValue (not null).
	 *
	 * @param existingPerson - the existing person for update operations where field previously set otherwise null
	 * @param searchValue - the name or email address of the person to fetch
	 * @param fieldName - the field name of the Person object
	 * @param fieldsInfo - the Map of the fields
	 * @param context - the context containing the goodies for the import batch process
	 * @return a list containing:
	 *     1) the Person if found/created
	 *     2) an error message if an error encountered or multiple references were found
	 */
	private static List fetchPerson(Person existingPerson, String searchValue, String fieldName, Map fieldsInfo, Map context) {
		Person person
		String errorMsg
		Boolean isEmail = searchValue.contains('@')

		// If the pre-existing person check if searchValue matches the person
		if (existingPerson) {
			if ( (isEmail && existingPerson.email.equalsIgnoreCase(searchValue) ) ||
				 (! isEmail && existingPerson.toString().equalsIgnoreCase(searchValue))
			) {
				person = existingPerson
			}
		}

		if (!person) {
			if (isEmail) {
				person = context.staffList.find { it.email.equalsIgnoreCase(searchValue) }
				if (!person) {
					errorMsg = 'Unable to find person by email address'
				}
			} else {
				try {
					Map resultMap = personService.findOrCreatePerson(searchValue, context.project, context.staffList)
					if (resultMap) {
						if (resultMap.isAmbiguous) {
							errorMsg = 'Multiple references found for value'
						} else if (resultMap.person) {
							person = resultMap.person
						}
					}
					if (!resultMap || ! resultMap.person && ! resultMap.isAmbiguous) {
						errorMsg = 'Unable to locate person'
					}
				} catch (e) {
					errorMsg = e.message
				}
			}
		}

		return [person, errorMsg]
	}

	/**
	 * Used to determine the proper domain class that a reference field is in a domain. When the class is AssetEntity then
	 * the logic needs to dig into the initial 'find' statement to know precisely which asset class should be be used.
	 * @param
	 */
	 /*
		static List determineClassOfReferenceField(String referenceFieldName, Map fieldsInfo, Map context) {
			Class domainClassToCreate
			String errMsg

			while(true) {
				// Determine the class to be created (AssetEntity can be tricking due to the inheritance)
				(domainClassToCreate, errMsg) = SearchQueryHelper.classOfDomainProperty(referenceFieldName, fieldsInfo, context.domainClass)
				if (errMsg) {
					break
				}
				if (domainClassToCreate in AssetEntity) {
					// For Asset classes due to inheritence we need to determine which class that the user intended by looking
					// at the initial find statement, which we will assume is the class that they really want. If they specified
					// Asset then that will be an issue since that is ambiguous and will error
					if (
						! referenceField.containsKey('find') ||
						! referenceField.find.containsKey('query') ||
						! referenceField.find.query.size() > 0)
					) {
						errMsg = 'A find command must preceed a whenNotFound create statement'
						break
					}

					String intendedDomainName = referenceField.find.query[0].domain
					log.debug "createReferenceEntityFromWhenNotFound() intended domain is {}", intendedDomainName
					if (intendedDomainName == 'Asset') {
						errMsg ="The <i>whenNotFound '$referenceFieldName' create</i> requires the find command to specify an absolute asset domain name"
						break
					}
					// Get the class of the domain specified in find of the ETL script
					domainClassToCreate = ETLDomain.lookup(intendedDomainName)?.getClazz()
				}

				if ( ! domainClassToCreate ) {
					errMsg = "Unable to determine class for property $referenceFieldName"
				}

				break
			}
			return [domainClassToCreate, errMsg]
		}
	*/

	/**
	 * Used to generate the MD5 value of the Map that is used to query for a domain of a particular
	 * fieldName. This will toString the Map of fieldName query names/values in order to create an unique key
	 * that can be used to cache the results afterward.
	 *
	 * The MD5 string will be composed like the following:
	 *		Dependency:asset
	 *		:value=123:
	 *		query=[[assetName:"xraysrv01", assetType:"VM"]]
	 *
	 * @param fieldName - the name of the field to fetch the Query element from the map
	 * @param fieldsInfo - the Map of all of the fields for the current row that came from the ETL process
	 * @return the MD5 32 character String of the query element
	 */
	static String generateMd5OfFieldsInfoField(String domainShortName, String fieldName, Map fieldsInfo) {
		log.debug 'generateMd5OfFieldsInfoField() domainShortName={}, fieldName={}', domainShortName, fieldName
		StringUtil.md5Hex(
			"${domainShortName}:${fieldName}" +
			":value=${fieldsInfo[fieldName].value}:query=" +
			( fieldsInfo[fieldName].find.containsKey('query') ? fieldsInfo[fieldName].find.query.toString() : 'NO-QUERY-SPECIFIED')
		)
	}

	/**
	 * Used to retrieve the value and initialize values from the fieldsInfo for a fieldName
	 * @param fieldName
	 * @param fieldsInfo
	 * @return List containing [value, initialValue]
	 */
	static List getValueAndInitialize(String fieldName, Map fieldsInfo) {
		Object value, init
		if (fieldsInfo.containsKey(fieldName)) {
			value = fieldsInfo[fieldName]['value']
			init = fieldsInfo[fieldName]['init']

			// Note the test of initValue and fieldName being a LazyMap. In testing it was discovered that accessing certain JSONObject node elements was
			// returning a LazyMap instead of a null value. Tried to reproduce in simple testcase but unsuccessful therefore had to add this
			// extra test.  See ticket TM-10981.
			value = (value instanceof groovy.json.internal.LazyMap) ? null : value
			init = (init instanceof groovy.json.internal.LazyMap) ? null : init
		}
		return [value, init]
	}

	/**
	 * Returns the initialize value or value from the fieldsInfo of a field
	 * @param fieldName
	 * @param fieldsInfo
	 * @return the initialize value if set otherwise the value property
	 */
	static Object getValueOrInitialize(String fieldName, Map fieldsInfo) {
		def (value, init) = getValueAndInitialize(fieldName, fieldsInfo)
		return (init != null ? init : value)
	}

}
