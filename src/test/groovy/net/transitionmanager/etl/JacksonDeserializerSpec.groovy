package net.transitionmanager.etl

import spock.lang.Specification

class JacksonDeserializerSpec extends Specification {


    void 'test can deserialize a JSON from InputStream content'() {
        given:
            InputStream inputStream = new ByteArrayInputStream("""
             [
                {
                  "comments": [],
                  "duplicate": false,
                  "errorCount": 0,
                  "errors": [],
                  "fields": {},
                  "op": "Insert",
                  "rowNum": 1,
                  "warn": false,
                  "tags": null
                },
             ]"""
                    .stripIndent()
                    .trim()
                    .getBytes('UTF-8')
            )

            JacksonDeserializer deserializer = new JacksonDeserializer(inputStream)

            List<Map<String, ?>> rows = []

        when:
            Closure closure = { Map<String, ?> row ->
                rows.add(row)
            }

            deserializer.eachRow(closure)

        then:
            rows.size() == 1

    }

    static String data = """[
        {
          "comments": [],
          "duplicate": false,
          "errorCount": 0,
          "errors": [],
          "fields": {},
          "op": "Insert",
          "rowNum": 1,
          "warn": false,
          "tags": null
        },
        {
          "comments": [],
          "duplicate": false,
          "errorCount": 0,
          "errors": [],
          "fields": {
            "assetName": {
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
              "originalValue": "Application 2",
              "update": null,
              "value": "Application 2",
              "warn": false
            },
            "description": {
              "create": null,
              "errors": [],
              "fieldOrder": 1,
              "find": {
                "matchOn": null,
                "query": [],
                "results": [],
                "size": 0
              },
              "init": null,
              "originalValue": "2019-12-23 12:41:04",
              "update": null,
              "value": "Created at: 2019-12-23 12:41:04",
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
                    "domain": "Application",
                    "criteria": [
                      {
                        "propertyName": "assetName",
                        "operator": "eq",
                        "value": "Application 2"
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
          "rowNum": 2,
          "warn": false,
          "tags": null
        },
        {
          "comments": [],
          "duplicate": false,
          "errorCount": 0,
          "errors": [],
          "fields": {
            "assetName": {
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
            "description": {
              "create": null,
              "errors": [],
              "fieldOrder": 1,
              "find": {
                "matchOn": null,
                "query": [],
                "results": [],
                "size": 0
              },
              "init": null,
              "originalValue": "2019-12-23 12:41:04",
              "update": null,
              "value": "Created at: 2019-12-23 12:41:04",
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
                    "domain": "Application",
                    "criteria": [
                      {
                        "propertyName": "assetName",
                        "operator": "eq",
                        "value": "Application 3"
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
          "rowNum": 3,
          "warn": false,
          "tags": null
        },
        {
          "comments": [],
          "duplicate": false,
          "errorCount": 0,
          "errors": [],
          "fields": {
            "assetName": {
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
              "originalValue": "Application 4",
              "update": null,
              "value": "Application 4",
              "warn": false
            },
            "description": {
              "create": null,
              "errors": [],
              "fieldOrder": 1,
              "find": {
                "matchOn": null,
                "query": [],
                "results": [],
                "size": 0
              },
              "init": null,
              "originalValue": "2019-12-23 12:41:04",
              "update": null,
              "value": "Created at: 2019-12-23 12:41:04",
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
                    "domain": "Application",
                    "criteria": [
                      {
                        "propertyName": "assetName",
                        "operator": "eq",
                        "value": "Application 4"
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
          "rowNum": 4,
          "warn": false,
          "tags": null
        },
        {
          "comments": [],
          "duplicate": false,
          "errorCount": 0,
          "errors": [],
          "fields": {
            "assetName": {
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
              "originalValue": "Application 5",
              "update": null,
              "value": "Application 5",
              "warn": false
            },
            "description": {
              "create": null,
              "errors": [],
              "fieldOrder": 1,
              "find": {
                "matchOn": null,
                "query": [],
                "results": [],
                "size": 0
              },
              "init": null,
              "originalValue": "2019-12-23 12:41:04",
              "update": null,
              "value": "Created at: 2019-12-23 12:41:04",
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
                    "domain": "Application",
                    "criteria": [
                      {
                        "propertyName": "assetName",
                        "operator": "eq",
                        "value": "Application 5"
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
          "rowNum": 5,
          "warn": false,
          "tags": null
        }
      ]"""
}
