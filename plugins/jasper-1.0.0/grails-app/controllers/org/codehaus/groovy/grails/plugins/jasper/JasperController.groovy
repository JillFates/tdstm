/* Copyright 2006-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.codehaus.groovy.grails.plugins.jasper

import org.apache.commons.io.FilenameUtils
import org.codehaus.groovy.grails.plugins.jasper.demo.ExamplePersonForReport

/*
 * @author mfpereira 2007
 */
class JasperController {
  JasperService jasperService

  def index = {
    // The default location for jasper reports is "%PROJECT_HOME%/web-app/reports/", but it can be changed in Config.groovy
    // The sample reports for this plugin follow the default and are installed in the app along with the the plugin
    String jasperReportDir = FilenameUtils.normalizeNoEndSeparator(grailsApplication.config.jasper.dir.reports ?: "reports")

    if (params.SUBREPORT_DIR == null) {
      params.SUBREPORT_DIR = jasperReportDir
    }

    if (!params.REPORT_LOCALE) {
      params.REPORT_LOCALE = request.getLocale()
    } else {
      params.REPORT_LOCALE = jasperService.getLocaleFromString(params.REPORT_LOCALE)
    }

    Collection reportData = null
    def testModel = this.getProperties().containsKey('chainModel') ? chainModel : null

    if (testModel?.data) {
      log.info "Using chainModel.data"
      try {
        reportData = testModel.data
      } catch (Throwable e) {
        throw new Exception("Expected chainModel.data parameter to be a Collection, but it was ${chainModel.data.class.name}", e)
      }
    } else {
      testModel = this.getProperties().containsKey('model') ? model : null
      if (testModel?.data) {
        log.info "Using model.data"
        try {
          reportData = testModel.data
        } catch (Throwable e) {
          throw new Exception("Expected model.data parameter to be a Collection, but it was ${model.data.class.name}", e)
        }
      } else if (params?.data) {
        log.info "Using params.data"
        try {
          reportData = params.data
        } catch (Throwable e) {
          throw new Exception("Expected data parameter to be a Collection, but it was ${params.data.class.name}", e)
        }
      } else {
        log.info "No data supplied"
      }
    }


    def inline = Boolean.valueOf(params._inline)

    switch (params._format) {
      case "PDF":
        createBinaryFile(jasperReportDir, jasperService.PDF_FORMAT, reportData, params, "pdf", "application/pdf", inline)
        break
      case "HTML":
        render(text: jasperService.generateReport(jasperReportDir, jasperService.HTML_FORMAT, reportData, params), contentType: "text/html", encoding: "UTF-8")
        break
      case "XML":
        render(text: jasperService.generateReport(jasperReportDir, jasperService.XML_FORMAT, reportData, params), contentType: "text/xml")
        break
      case "CSV":
        response.setHeader("Content-disposition", "attachment; filename=\"" + params._name + ".csv\"");
        render(text: jasperService.generateReport(jasperReportDir, jasperService.CSV_FORMAT, reportData, params), contentType: "text/csv")
        break
      case "XLS":
        createBinaryFile(jasperReportDir, jasperService.XLS_FORMAT, reportData, params, "xls", "application/vnd.ms-excel", false)
        break
      case "RTF":
        createBinaryFile(jasperReportDir, jasperService.RTF_FORMAT, reportData, params, "rtf", "text/rtf", false)
        break
      case "TEXT":
        render(text: jasperService.generateReport(jasperReportDir, jasperService.TEXT_FORMAT, reportData, params), contentType: "text")
        break
      case "ODT":
        createBinaryFile(jasperReportDir, jasperService.ODT_FORMAT, reportData, params, "odt", "application/vnd.oasis.opendocument.text", false)
        break
      case "ODS":
        createBinaryFile(jasperReportDir, jasperService.ODS_FORMAT, reportData, params, "ods", "application/vnd.oasis.opendocument.spreadsheetl", false)
        break
      case "DOCX":
        createBinaryFile(jasperReportDir, jasperService.DOCX_FORMAT, reportData, params, "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", false)
        break
      case "XLSX":
        createBinaryFile(jasperReportDir, jasperService.XLSX_FORMAT, reportData, params, "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", false)
        break
      case "PPTX":
        createBinaryFile(jasperReportDir, jasperService.PPTX_FORMAT, reportData, params, "pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", false)
        break
      default:
        throw new Exception(message(code: "jasper.controller.invalidFormat", args: [params._format]))
        break
    }
  }


  def createBinaryFile = {jasperFile, format, reportData, params, ext, mime, inline ->
    def data = jasperService.generateReport(jasperFile, format, reportData, params).toByteArray()
    if (!inline) {
      response.setHeader("Content-disposition", "attachment; filename=\"" + params._name + "." + ext + "\"");
    }
    response.contentType = mime
    response.characterEncoding = "UTF-8"
    response.outputStream << data
  }

  def admin = {
    // There is nothing to administer, so just show the demo
    redirect(action: 'demo')
  }

  def demo = {
    // This "people" object in this data model is only for displaying the data in a table on the Demo page (demo.gsp) next to the
    // example that invokes the exampleWithData action, below.  It has nothing to do directly with the data that is
    // actually reported.
    List people = makeUpSomeDemoData()
    [people: people]
  }

  def exampleWithData = {
    // This "data" object in this data model is the data that drives this Jasper report (i.e. what appears in the
    // detail band)
    List reportDetails = makeUpSomeDemoData()

    chain(controller: 'jasper', action: 'index', model: [data: reportDetails], params: params)
  }

  protected List makeUpSomeDemoData() {
    java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")

    List people = [
            new ExamplePersonForReport(name: 'Amy', email: 'amy@example.com', birthday: simpleDateFormat.parse("1980-02-14")),
            new ExamplePersonForReport(name: 'Brad', email: 'brad@example.com', birthday: simpleDateFormat.parse("1984-05-21")),
            new ExamplePersonForReport(name: 'Charlie', email: 'charlie@example.com', birthday: simpleDateFormat.parse("1982-08-10")),
            new ExamplePersonForReport(name: 'Diane', email: 'diane@example.com', birthday: simpleDateFormat.parse("1979-04-13")),
            new ExamplePersonForReport(name: 'Edward', email: 'edward@example.com', birthday: simpleDateFormat.parse("1985-01-29"))]
    return people
  }

}

