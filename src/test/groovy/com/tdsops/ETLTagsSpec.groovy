package com.tdsops

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
import com.tdsops.etl.dataset.ETLDataset
import net.transitionmanager.project.Project
import spock.lang.See

class ETLTagsSpec extends ETLBaseSpec {

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
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				application id,tag
				12345678,$tagName
			""".stripIndent())

			tagValidator.addTag(tagName, 100l)

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
				getFileSystemServiceTestBean().deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11583')
	void 'test can add a list of tags to an asset if not already associated'() {

		given:
			List<String> tagNames = ['FUBAR', 'SNAFU']

			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				application id,tag1,tag2
				12345678,${tagNames[0]},${tagNames[1]}
			""".stripIndent())

			tagValidator.addTag(tagNames[0], 100l)
			tagValidator.addTag(tagNames[1], 101l)

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
				getFileSystemServiceTestBean().deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11583')
	void 'test can remove a single tag from an asset if associated'() {

		given:
			String tagName = 'Code Blue'

		and:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				application id,tag
				12345678,$tagName
			""".stripIndent())

			tagValidator.addTag(tagName, 100l)

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
				getFileSystemServiceTestBean().deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11583')
	void 'test can removes a list of tags from an asset if associated'() {

		given:
			List<String> tagNames = ['FUBAR', 'SNAFU']

			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				application id,tag1,tag2
				12345678,${tagNames[0]},${tagNames[1]}
			""".stripIndent())

			tagValidator.addTag(tagNames[0], 100l)
			tagValidator.addTag(tagNames[1], 101l)

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
				getFileSystemServiceTestBean().deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11583')
	void 'test can replace one tag with another tag on an asset if associated using SOURCE'() {

		given:
			String currentTag = 'Code Blue'
			String newTag = 'Code Green'

			tagValidator.addTag(currentTag, 105l)
			tagValidator.addTag(newTag, 106l)

		and:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				application id,current tag,new tag
				12345678,$currentTag,$newTag
			""".stripIndent())


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

		cleanup:
			if (fileName) {
				getFileSystemServiceTestBean().deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11583')
	void 'test can replace one tag with another tag on an asset if associated using simple Map definition'() {

		given:
			String currentTag = 'Code Blue'
			String newTag = 'Code Green'

			tagValidator.addTag(currentTag, 105l)
			tagValidator.addTag(newTag, 106l)

		and:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				application id,current tag,new tag
				12345678,$currentTag,$newTag
			""".stripIndent())

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
				getFileSystemServiceTestBean().deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11583')
	void 'test can replace one tag with another tag on an asset if associated using local variables'() {

		given:
			String currentTag = 'Code Blue'
			String newTag = 'Code Green'

			tagValidator.addTag(currentTag, 105l)
			tagValidator.addTag(newTag, 106l)

		and:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				application id,current tag,new tag
				12345678,$currentTag,$newTag
			""".stripIndent())


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
					extract 'current tag' set currentTag
					extract 'new tag' set newTag
					tagReplace "\${currentTag}":newTag
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
				getFileSystemServiceTestBean().deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11583')
	void 'test can replaces a list of tag defined by the map key with map value on an asset if associated'() {

		given:
			List<String> tagNames = ['c', 'd', 'g', 'h']

			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				application id,current tag1,new tag 1,current tag2,new tag tag2
				12345678,${tagNames[0]},${tagNames[1]},${tagNames[2]},${tagNames[3]}
			""".stripIndent())

			tagValidator.addTag(tagNames[0], 100l)
			tagValidator.addTag(tagNames[1], 101l)
			tagValidator.addTag(tagNames[2], 102l)
			tagValidator.addTag(tagNames[3], 103l)

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
				getFileSystemServiceTestBean().deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11583')
	void 'test can throw an ETLProcessorException if a tag used in tag commands does not exists in database'() {
		given:
		    String fileName
		    ETLDataset dataSet
			String tagA = 'A'
			String tagB = 'B'
			String tagC = 'C'
			String tagD = 'D'

			tagValidator.addTag(tagA, 100l)
			tagValidator.addTag(tagB, 101l)
			tagValidator.addTag(tagC, 102l)
			tagValidator.addTag(tagD, 103l)

		and:
            (fileName, dataSet) = buildCSVDataSet("""
				application id,current tag1,new tag 1,current tag2,new tag tag2
				12345678,${tagA},${tagB},${tagC},${tagD}
			""".stripIndent())

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

            (fileName, dataSet) = buildCSVDataSet("""
				application id,current tag1,new tag 1,current tag2,new tag tag2
				12345678,${tagA},${tagB},${tagC},${tagD}
			""".stripIndent())

		    etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator,
				tagValidator
			)

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

            (fileName, dataSet) = buildCSVDataSet("""
				application id,current tag1,new tag 1,current tag2,new tag tag2
				12345678,${tagA},${tagB},${tagC},${tagD}
			""".stripIndent())

            etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator,
				tagValidator
			)

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

            (fileName, dataSet) = buildCSVDataSet("""
				application id,current tag1,new tag 1,current tag2,new tag tag2
				12345678,${tagA},${tagB},${tagC},${tagD}
			""".stripIndent())

            etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator,
				tagValidator
			)

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

            (fileName, dataSet) = buildCSVDataSet("""
				application id,current tag1,new tag 1,current tag2,new tag tag2
				12345678,${tagA},${tagB},${tagC},${tagD}
			""".stripIndent())

            etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator,
				tagValidator
			)

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

            (fileName, dataSet) = buildCSVDataSet("""
				application id,current tag1,new tag 1,current tag2,new tag tag2
				12345678,${tagA},${tagB},${tagC},${tagD}
			""".stripIndent())

            etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator,
				tagValidator
			)

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
				getFileSystemServiceTestBean().deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11583')
	void 'test can not create tags results in ETLProcessorResult if any command of tags was used in an ETL script'() {

		given:
			String tagName = 'Code Blue'

		and:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				application id,tag
				12345678,$tagName
			""".stripIndent())

			tagValidator.addTag(tagName, 100l)

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
						tags == null
					}
				}
			}

		cleanup:
			if (fileName) {
				getFileSystemServiceTestBean().deleteTemporaryFile(fileName)
			}
	}

	@See('TM-16944')
	void 'test can add a single tag without using extract command'() {

		given:
			String tagName = 'Code Blue'

		and:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				application id,tag
				12345678,$tagName
			""".stripIndent())

			tagValidator.addTag(tagName, 100l)

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
						fields.size() == 0
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

	/**
	 * Creates a {@code ETLTagValidator} instance used in test cases.
	 *
	 * @return an instance of {@code ETLTagValidator}
	 */
	protected ETLTagValidator createTagValidator() {
		ETLTagValidator tagValidator = new ETLTagValidator('GDPR', 5l)
		tagValidator.addTag('HIPPA', 6l)
		tagValidator.addTag('PCI', 7l)
		tagValidator.addTag('SOX', 8l)
		return tagValidator
	}
}
