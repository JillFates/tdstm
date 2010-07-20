eventAllTestsStart = {
    if (getBinding().variables.containsKey("functionalTests")) {
        functionalTests << "functional"
    }
}

eventTestSuiteStart = { String type ->
      if (type == "functional") {
          testingBaseURL = argsMap["baseUrl"] ?: "http://localhost:$serverPort$serverContextPath/"
          System.setProperty("grails.functional.test.baseURL", testingBaseURL)
    }
}
