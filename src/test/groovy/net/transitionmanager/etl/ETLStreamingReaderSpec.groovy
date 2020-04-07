package net.transitionmanager.etl

import com.tdsops.etl.ETLBaseSpec

class ETLStreamingReaderSpec extends ETLBaseSpec {

    void 'test can deserialize a JSON from InputStream content without fields and without tags'() {
        given:
            InputStream inputStream = new ByteArrayInputStream("""
             [
                {
                  "comments": [],
                  "duplicate": false,
                  "errorCount": 0,
                  "errors": [],
                  "fields": null,
                  "op": "Insert",
                  "rowNum": 1,
                  "warn": false,
                  "tags": null
                }
             ]"""
                    .stripIndent()
                    .trim()
                    .getBytes('UTF-8')
            )

            ETLStreamingReader reader = new ETLStreamingReader(inputStream)

            List<Map<String, ?>> rows = []

        when:
            reader.eachRow { Map<String, ?> row ->
                rows.add(row)
            }

        then:
            rows.size() == 1
            with(rows[0], Map) {
                comments == []
                duplicate == false
                errorCount == 0
                errors == []
                fields == null
                op == 'Insert'
                rowNum == 1
                warn == false
                tags == null
            }

    }

    void 'test can deserialize a JSON from InputStream content and without tags'() {
        given:
            InputStream inputStream = new ByteArrayInputStream("""
             [
                {
                  "comments": [],
                  "duplicate": false,
                  "errorCount": 0,
                  "errors": [],
                  "fields": null,
                  "op": "Insert",
                  "rowNum": 1,
                  "warn": false,
                  "tags": {
                      "add": [
                        "GDPR",
                        "PCI"
                      ],
                      "remove": [
                        "HIPPA"
                      ],
                      "replace": {
                        "PCI": "SOX"
                      }
                  }
                }
             ]"""
                    .stripIndent()
                    .trim()
                    .getBytes('UTF-8')
            )

            ETLStreamingReader reader = new ETLStreamingReader(inputStream)

            List<Map<String, ?>> rows = []

        when:
            reader.eachRow { Map<String, ?> row ->
                rows.add(row)
            }

        then:
            rows.size() == 1
            with(rows[0], Map) {
                comments == []
                duplicate == false
                errorCount == 0
                errors == []
                fields == null
                op == 'Insert'
                rowNum == 1
                warn == false
                with(tags, Map) {
                    add == ["GDPR", "PCI"]
                    remove == ['HIPPA']
                    replace == [PCI: 'SOX']
                }
            }

    }

    void 'test can deserialize a JSON from InputStream content with fields and find using multiple criterias'() {
        given:
            InputStream inputStream = new ByteArrayInputStream("""
                [
                  {
                    "comments": [],
                    "duplicate": false,
                    "errorCount": 0,
                    "errors": [],
                    "fields": {
                      "asset": {
                        "create": null,
                        "errors": [],
                        "fieldOrder": 0,
                        "find": {
                          "matchOn": null,
                          "query": [],
                          "results": [],
                          "size": 0
                        },
                        "init": null,
                        "originalValue": "Application 3",
                        "update": null,
                        "value": "Application 3",
                        "warn": false
                      },
                      "id": {
                        "create": null,
                        "errors": [],
                        "fieldOrder": 2,
                        "find": {
                          "matchOn": null,
                          "query": [
                            {
                              "domain": "Dependency",
                              "criteria": [
                                {
                                  "propertyName": "asset",
                                  "operator": "eq",
                                  "value": "Application 3"
                                },
                                {
                                  "propertyName": "dependent",
                                  "operator": "eq",
                                  "value": "Server 3"
                                },
                                {
                                  "propertyName": "type",
                                  "operator": "eq",
                                  "value": "Runs On"
                                }
                              ]
                            }
                          ],
                          "results": [],
                          "size": 0
                        },
                        "init": null,
                        "originalValue": null,
                        "update": null,
                        "value": null,
                        "warn": false
                      }
                    },
                    "op": "Insert",
                    "rowNum": 1,
                    "warn": false,
                    "tags": {
                      "add": [
                        "GDPR",
                        "PCI"
                      ],
                      "remove": [
                        "HIPPA"
                      ],
                      "replace": {
                        "PCI": "SOX"
                      }
                    }
                  }
                ]
            """
                    .stripIndent()
                    .trim()
                    .getBytes('UTF-8')
            )

            ETLStreamingReader reader = new ETLStreamingReader(inputStream)

            List<Map<String, ?>> rows = []

        when:
            reader.eachRow { Map<String, ?> row ->
                rows.add(row)
            }

        then:
            rows.size() == 1
            assertWith(rows[0], Map) {
                comments == []
                duplicate == false
                errorCount == 0
                errors == []
                assertWith(fields, Map) {

                    assertWith(asset, Map) {
                        create == null
                        fieldOrder == 0
                        with(find, Map) {
                            matchOn == null
                            query == []
                            results == []
                            size == 0
                        }
                        init == null
                        originalValue == 'Application 3'
                        update == null
                        warn == false
                        value == 'Application 3'
                        errors == []
                    }
                    assertWith(id, Map) {
                        create == null
                        errors == []
                        fieldOrder == 2
                        init == null
                        originalValue == null
                        update == null
                        value == null
                        warn == false
                        assertWith(find, Map) {
                            matchOn == null
                            query.size() == 1
                            assertWith(query[0], Map) {
                                domain == 'Dependency'
                                criteria.size() == 3
                                assertWith(criteria[0], Map) {
                                    propertyName == 'asset'
                                    operator == 'eq'
                                    value: 'Application 3'
                                }
                                assertWith(criteria[1], Map) {
                                    propertyName == 'dependent'
                                    operator == 'eq'
                                    value == 'Server 3'
                                }
                                assertWith(criteria[2], Map) {
                                    propertyName == 'type'
                                    operator == 'eq'
                                    value: 'Runs On'
                                }
                            }
                            results == []
                            size == 0
                        }
                    }
                }
                op == 'Insert'
                rowNum == 1
                warn == false
                assertWith(tags, Map) {
                    add == ["GDPR", "PCI"]
                    remove == ['HIPPA']
                    replace == [PCI: 'SOX']
                }
            }

    }

}
