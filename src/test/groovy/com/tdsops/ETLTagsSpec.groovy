package com.tdsops

import com.tdsops.etl.DataSetFacade
import com.tdsops.etl.DebugConsole
import com.tdsops.etl.DomainResult
import com.tdsops.etl.ETLBaseSpec
import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLFieldsValidator
import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.ETLProcessorException
import com.tdsops.etl.ETLProcessorResult
import com.tdsops.etl.RowResult
import com.tdsops.etl.TagResults
import grails.test.mixin.Mock
import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.Database
import net.transitionmanager.common.CoreService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.project.Project
import spock.lang.See

@Mock([AssetEntity, Application, Database])
class ETLTagsSpec extends ETLBaseSpec {

	static doWithSpring = {
		coreService(CoreService) {
			grailsApplication = ref('grailsApplication')
		}
		fileSystemService(FileSystemService) {
			coreService = ref('coreService')
		}
	}

	Project GMDEMO
	Project TMDEMO
	DebugConsole debugConsole
	ETLFieldsValidator validator
	ETLTagValidator tagValidator

	def setup() {
		GMDEMO = Mock(Project)
		GMDEMO.getId() >> 125612l

		TMDEMO = Mock(Project)
		TMDEMO.getId() >> 125612l

		validator = createDomainClassFieldsValidator()
		tagValidator = createTagValidator()

		debugConsole = new DebugConsole(buffer: new StringBuilder())
	}

	@See('TM-11583')
	void 'test can add a single tag to an asset if not already associated'() {

		given:
			String tagName = 'Code Blue'

		and:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				application id,tag
				12345678,$tagName
			""".stripIndent())

			tagValidator.addTag(100l, tagName)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator,
				tagValidator
			)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Application
					extract 'application id' load 'Id'
					extract 'tag' set tag
					tagAdd tag 
				}
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
			assertWith(etlProcessor.finalResult(), ETLProcessorResult) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					assertWith(data[0], RowResult) {
						fields.size() == 1
						assertFieldResult(fields['id'], '12345678', '12345678')
						assertWith(tags, TagResults) {
							add == [tagName] as Set
							remove == [] as Set
							replace == [:]
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11583')
	void 'test can add a list of tags to an asset if not already associated'() {

		given:
			List<String> tagNames = ['FUBAR', 'SNAFU']

			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				application id,tag1,tag2
				12345678,${tagNames[0]},${tagNames[1]}
			""".stripIndent())

			tagValidator.addTags([
				[id: 101l, name: tagNames[0]],
				[id: 102l, name: tagNames[1]]
			])

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator,
				tagValidator
			)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Application
					extract 'application id' load 'Id'
					tagAdd SOURCE.tag1, SOURCE.tag2
				}
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
			assertWith(etlProcessor.finalResult(), ETLProcessorResult) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					assertWith(data[0], RowResult) {
						fields.size() == 1
						assertFieldResult(fields['id'], '12345678', '12345678')

						assertWith(tags, TagResults) {
							add == [tagNames[0], tagNames[1]] as Set
							remove == [] as Set
							replace == [:]
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11583')
	void 'test can remove a single tag from an asset if associated'() {

		given:
			String tagName = 'Code Blue'

		and:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				application id,tag
				12345678,$tagName
			""".stripIndent())

			tagValidator.addTag(100l, tagName)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator,
				tagValidator
			)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Application
					extract 'application id' load 'Id'
					extract 'tag' set tag
					tagRemove tag 
				}
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
			assertWith(etlProcessor.finalResult(), ETLProcessorResult) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					assertWith(data[0], RowResult) {
						fields.size() == 1
						assertFieldResult(fields['id'], '12345678', '12345678')
						assertWith(tags, TagResults) {
							add == [] as Set
							remove == [tagName] as Set
							replace == [:]
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11583')
	void 'test can removes a list of tags from an asset if associated'() {

		given:
			List<String> tagNames = ['FUBAR', 'SNAFU']

			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				application id,tag1,tag2
				12345678,${tagNames[0]},${tagNames[1]}
			""".stripIndent())

			tagValidator.addTags([
				[id: 101l, name: tagNames[0]],
				[id: 102l, name: tagNames[1]]
			])

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator,
				tagValidator
			)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Application
					extract 'application id' load 'Id'
					tagRemove SOURCE.tag1, SOURCE.tag2 
				}
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
			assertWith(etlProcessor.finalResult(), ETLProcessorResult) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					assertWith(data[0], RowResult) {
						fields.size() == 1
						assertFieldResult(fields['id'], '12345678', '12345678')

						assertWith(tags, TagResults) {
							add == [] as Set
							remove == [tagNames[0], tagNames[1]] as Set
							replace == [:]
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11583')
	void 'test can replace one tag with another tag on an asset if associated'() {

		given:
			String currentTag = 'Code Blue'
			String newTag = 'Code Green'

		and:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				application id,current tag,new tag
				12345678,$currentTag,$newTag
			""".stripIndent())

			tagValidator.addTag(105l, currentTag)
			tagValidator.addTag(106l, newTag)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator,
				tagValidator
			)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Application
					extract 'application id' load 'Id'
					tagReplace SOURCE.'current tag', SOURCE.'new tag'
				}
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
			assertWith(etlProcessor.finalResult(), ETLProcessorResult) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					assertWith(data[0], RowResult) {
						fields.size() == 1
						assertFieldResult(fields['id'], '12345678', '12345678')
						assertWith(tags, TagResults) {
							add == [] as Set
							remove == [] as Set
							replace == [(currentTag): newTag]
						}
					}
				}
			}


		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Application
					extract 'application id' load 'Id'
					tagReplace '${currentTag}':'${newTag}'
				}
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
			assertWith(etlProcessor.finalResult(), ETLProcessorResult) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					assertWith(data[0], RowResult) {
						fields.size() == 1
						assertFieldResult(fields['id'], '12345678', '12345678')
						assertWith(tags, TagResults) {
							add == [] as Set
							remove == [] as Set
							replace == [(currentTag): newTag]
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11583')
	void 'test can replaces a list of tag defined by the map key with map value on an asset if associated'() {

		given:
			List<String> tagNames = ['c', 'd', 'g', 'h']

			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				application id,current tag1,new tag 1,current tag2,new tag tag2
				12345678,${tagNames[0]},${tagNames[1]},${tagNames[2]},${tagNames[3]}
			""".stripIndent())

			tagValidator.addTags([
				[id: 101l, name: tagNames[0]],
				[id: 102l, name: tagNames[1]],
				[id: 103l, name: tagNames[2]],
				[id: 104l, name: tagNames[3]]
			])

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator,
				tagValidator
			)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Application
					extract 'application id' load 'Id'
					tagReplace '${tagNames[0]}': '${tagNames[1]}', '${tagNames[2]}': '${tagNames[3]}'
				}
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
			assertWith(etlProcessor.finalResult(), ETLProcessorResult) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					assertWith(data[0], RowResult) {
						fields.size() == 1
						assertFieldResult(fields['id'], '12345678', '12345678')

						assertWith(tags, TagResults) {
							add == [] as Set
							remove == [] as Set
							replace == [
								(tagNames[0]): tagNames[1],
								(tagNames[2]): tagNames[3]
							]
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11583')
	void 'test can throw an ETLProcessorException if a tag used in tag commands does not exists in database'() {
		given:
			String tagA = 'A'
			String tagB = 'B'
			String tagC = 'C'
			String tagD = 'D'

			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				application id,current tag1,new tag 1,current tag2,new tag tag2
				12345678,${tagA},${tagB},${tagC},${tagD}
			""".stripIndent())

			tagValidator.addTags([
				[id: 101l, name: tagA],
				[id: 102l, name: tagB],
				[id: 103l, name: tagC],
				[id: 104l, name: tagD]
			])

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator,
				tagValidator
			)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Application
					extract 'application id' load 'Id'
					tagAdd 'Code Blue' 
				}
			""".stripIndent())

		then: 'It throws an Exception because find command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message.startsWith(ETLProcessorException.unknownTag('Code Blue').message)
			}

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Application
					extract 'application id' load 'Id'
					tagRemove 'Code Blue' 
				}
			""".stripIndent())

		then: 'It throws an Exception because find command is incorrect'
			e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message.startsWith(ETLProcessorException.unknownTag('Code Blue').message)
			}

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Application
					extract 'application id' load 'Id'
					 tagReplace 'Code Blue', 'b'
				}
			""".stripIndent())

		then: 'It throws an Exception because find command is incorrect'
			e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message.startsWith(ETLProcessorException.unknownTag('Code Blue').message)
			}

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Application
					extract 'application id' load 'Id'
					 tagReplace '${tagB}', 'Code Blue'
				}
			""".stripIndent())

		then: 'It throws an Exception because find command is incorrect'
			e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message.startsWith(ETLProcessorException.unknownTag('Code Blue').message)
			}

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Application
					extract 'application id' load 'Id'
					 tagReplace 'Code Blue': 'b'
				}
			""".stripIndent())

		then: 'It throws an Exception because find command is incorrect'
			e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message.startsWith(ETLProcessorException.unknownTag('Code Blue').message)
			}

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Application
					extract 'application id' load 'Id'
					 tagReplace '${tagB}':'Code Blue'
				}
			""".stripIndent())

		then: 'It throws an Exception because find command is incorrect'
			e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message.startsWith(ETLProcessorException.unknownTag('Code Blue').message)
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}


	/**
	 * Creates a {@code ETLTagValidator} instance used in test cases.
	 * @return an instance of {@code ETLTagValidator}
	 */
	protected ETLTagValidator createTagValidator() {
		ETLTagValidator tagValidator = new ETLTagValidator(5l, 'GDPR', 'General Data Protection Regulation Compliance')

		tagValidator.addTag(6l, 'HIPPA', 'Health Insurance Portability and Accountability Act Compliance')
		tagValidator.addTag(7l, 'PCI', 'Payment Card Industry Data Security Standard Compliance')
		tagValidator.addTag(8l, 'SOX', 'Sarbanes–Oxley Act Compliance')
		tagValidator.addTag(9l, 'Pablo2019Tag1', '')


		return tagValidator
	}
}
