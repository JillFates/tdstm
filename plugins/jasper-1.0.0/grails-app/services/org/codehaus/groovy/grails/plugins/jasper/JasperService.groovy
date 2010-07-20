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

import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import net.sf.jasperreports.engine.util.JRProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.io.Resource
import net.sf.jasperreports.engine.*
import net.sf.jasperreports.engine.export.*
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter
import net.sf.jasperreports.engine.export.oasis.JROdsExporter
import net.sf.jasperreports.engine.export.oasis.JROdtExporter
import java.lang.reflect.Field
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporterParameter

/*
 * @author mfpereira 2007 
 */
class JasperService implements ApplicationContextAware {
  final int PDF_FORMAT = 1;
  final int HTML_FORMAT = 2;
  final int XML_FORMAT = 3;
  final int CSV_FORMAT = 4;
  final int XLS_FORMAT = 5;
  final int RTF_FORMAT = 6;
  final int TEXT_FORMAT = 7;
  final int ODT_FORMAT = 8;
  final int ODS_FORMAT = 9;
  final int DOCX_FORMAT = 10;
  final int XLSX_FORMAT = 11;
  final int PPTX_FORMAT = 12;

  final boolean FORCE_TEMP_FOLDER = false;

  boolean transactional = true
  javax.sql.DataSource dataSource
  ApplicationContext applicationContext

  @Deprecated
  public ByteArrayOutputStream generateReport(String jasperReportDir, Integer format, Collection reportData, Map parameters) {
    JasperReportDef reportDef = new JasperReportDef(name: parameters._file, folder: jasperReportDir, reportData: reportData)
    return generateReport(format, reportDef, parameters)
  }

  /**
   * Generate a report based on a single jasper file.
   * @param format , target format
   * @param reportDef , jasper report object
   * return ByteArrayOutStream with the generated Report
   */
  public ByteArrayOutputStream generateReport(Integer format, JasperReportDef reportDef, Map parameters) {
    ByteArrayOutputStream byteArray = new ByteArrayOutputStream()
    JRExporter exporter = generateExporter(format, parameters)
    JasperPrint jasperPrint = generatePrinter(reportDef, parameters)

    exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint)
    exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, byteArray)
    exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING, "UTF-8")
    exporter.exportReport()

    return byteArray
  }

  /**
   * Generate a single report based on a list of jasper files.
   * @param format , target format
   * @param reports , a List with report objects
   * @param parameters , additional parameters
   * return ByteArrayOutStream with the generated Report
   */
  public ByteArrayOutputStream generateReport(Integer format, List<JasperReportDef> reports, Map parameters) {
    ByteArrayOutputStream byteArray = new ByteArrayOutputStream()
    JRExporter exporter = generateExporter(format, parameters)


    def printers = []
    for (report in reports) {
      printers << generatePrinter(report, parameters)
    }

    exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, printers)
    exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, byteArray)
    exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING, "UTF-8")
    exporter.exportReport()

    return byteArray
  }

  /**
   * Forces the Jasper Reports  temp folder to be "~/.grails/.jasper" and ensures that such a folder exists.
   * The user (however the app server is logged in) is much more likely to have read/write/delete rights here than the
   * default location that Jasper Reports uses.
   */
  protected def forceTempFolder() {
    /* TODO This is currently disabled, because it doesn't work. Jasper Reports seems to always use the current
    * folder (.) no matter what.  (I'll be filing a bug report against Jasper Reports itself shortly - Craig Jones 16-Aug-2008)
    */
    if (FORCE_TEMP_FOLDER) {
      // Look up the home folder explicitly (don't trust that tilde notation will work).
      String userHomeDir = System.getProperty('user.home')
      File tempFolder = new File(userHomeDir, "/.grails/.jasper")

      // This is the current official means for setting the temp folder for jasper reports to use when compiling
      // reports on the fly, but it doesn't work
      JRProperties.setProperty(JRProperties.COMPILER_TEMP_DIR, tempFolder.getAbsolutePath())

      // This is a deprecated means for setting the temp folder that supposedly still works (still in the Jasper
      // Reports source code trunk as of 14-Aug-2008, and, in fact, takes precedence over the official method);
      // however, it doesn't work either.
      System.setProperty("jasper.reports.compile.temp", tempFolder.getAbsolutePath())

      if (!tempFolder.exists()) {
        def ant = new AntBuilder()
        ant.mkdir(dir: tempFolder.getAbsolutePath())
        if (!tempFolder.exists()) {
          throw new Exception("Unable to create temp folder: ${tempFolder.getPath()}")
        }
      }
      log.info "Using temp folder: " + tempFolder.getPath() + " (" + tempFolder.getAbsolutePath() + ")"
    }
  }

  private JRExporter generateExporter(Integer format, Map parameters) {
    JRExporter exporter
    Field[] fields
    Boolean useDefaultParameters = parameters.useDefaultParameters.equals("false") ? false : true

    switch (format) {
      case PDF_FORMAT:
        exporter = new JRPdfExporter()
        fields = JRPdfExporterParameter.getFields()
        break
      case HTML_FORMAT:
        exporter = new JRHtmlExporter()
        fields = JRHtmlExporterParameter.getFields()
        if (useDefaultParameters) {
          exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE)
        }
        break
      case XML_FORMAT:
        exporter = new JRXmlExporter()
        fields = JRXmlExporterParameter.getFields()
        break
      case CSV_FORMAT:
        exporter = new JRCsvExporter()
        fields = JRCsvExporterParameter.getFields()
        break
      case XLS_FORMAT:
        exporter = new JRXlsExporter()
        fields = JRXlsExporterParameter.getFields()
        if (useDefaultParameters) {
          exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.TRUE);
          exporter.setParameter(JRXlsExporterParameter.IS_AUTO_DETECT_CELL_TYPE, Boolean.TRUE);
          exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
          exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
        }
        break
      case RTF_FORMAT:
        exporter = new JRRtfExporter()
        break
      case TEXT_FORMAT:
        exporter = new JRTextExporter()
        fields = JRTextExporterParameter.getFields()
        if (useDefaultParameters) {
          exporter.setParameter(JRTextExporterParameter.PAGE_WIDTH, 80)
          exporter.setParameter(JRTextExporterParameter.PAGE_HEIGHT, 60)
        }
        break
      case ODT_FORMAT:
        exporter = new JROdtExporter()
        break
      case ODS_FORMAT:
        exporter = new JROdsExporter()
        break
      case DOCX_FORMAT:
        exporter = new JRDocxExporter()
        fields = JRDocxExporterParameter.getFields()
        break
      case XLSX_FORMAT:
        exporter = new JRXlsxExporter()
        break
      case PPTX_FORMAT:
        exporter = new JRPptxExporter()
        break
      default:
        throw new Exception("${format} is a unknown Report Format")
    }

    if (fields) {
      applyParameters(fields, exporter, parameters)
    }

    return exporter
  }

  /**
   * Generate a JasperPrint object for a gives report.
   * @param reportDefinition , the report
   * @param parameters , additional parameters
   * @return JasperPrint , jasperreport printer
   */
  private JasperPrint generatePrinter(JasperReportDef reportDefinition, Map parameters) {
    Resource reportSpecResource = reportDefinition.fetchReportSpec()
    def reportData = reportDefinition.getReportData()
    JasperPrint jasperPrint

    if (reportData != null) {
      JRBeanCollectionDataSource jrBeanCollectionDataSource = new JRBeanCollectionDataSource(reportData);
      if (reportSpecResource.filename.endsWith('.jasper')) {
        jasperPrint = JasperFillManager.fillReport(reportSpecResource.inputStream, parameters, jrBeanCollectionDataSource)
      } else {
        forceTempFolder()
        jasperPrint = JasperFillManager.fillReport(JasperCompileManager.compileReport(reportSpecResource.inputStream), parameters, jrBeanCollectionDataSource)
      }
    } else {
      java.sql.Connection conn = dataSource.getConnection()
      try {
        if (reportSpecResource.filename.endsWith('.jasper')) {
          jasperPrint = JasperFillManager.fillReport(reportSpecResource.inputStream, parameters, conn)
        } else {
          forceTempFolder()
          jasperPrint = JasperFillManager.fillReport(JasperCompileManager.compileReport(reportSpecResource.inputStream), parameters, conn)
        }
      }
      finally {
        conn.close()
      }
    }

    return jasperPrint
  }

  /**
   * Apply additional parameters to the exporter. If the user submits a parameter that is not available for
   * the file format this parameter is ignored.
   * @param fields , available fields for the choosen file format
   * @param exporter , the exporter object
   * @param parameter , the parameters to apply
   */
  private void applyParameters(Field[] fields, JRExporter exporter, Map<String, Object> parameters) {
    def fieldNames = fields.collect {it.getName()}

    parameters.each { p ->
      if (fieldNames.contains(p.getKey())) {
        exporter.setParameter(fields.find {it.name = p.getKey()}.getType()[p.getKey()], p.getValue());
        log.info "Applied parameter ${p.getKey()} with value ${p.getValue()}"
      }
    }
  }

  /**
   * Convert a String to a Locale.
   * @param localeString , a string
   * @returns Locale
   */
  public static Locale getLocaleFromString(String localeString) {
    if (localeString == null) {
      return null;
    }
    localeString = localeString.trim();

    // Extract language
    int languageIndex = localeString.indexOf('_');
    String language = null;
    if (languageIndex == -1) {  // No further "_" so is "{language}" only
      return new Locale(localeString, "");
    } else {
      language = localeString.substring(0, languageIndex);
    }

    // Extract country
    int countryIndex = localeString.indexOf('_', languageIndex + 1);
    String country = null;
    if (countryIndex == -1) {     // No further "_" so is "{language}_{country}"
      country = localeString.substring(languageIndex + 1);
      return new Locale(language, country);
    } else {   // Assume all remaining is the variant so is "{language}_{country}_{variant}"  
      country = localeString.substring(languageIndex + 1, countryIndex);
      String variant = localeString.substring(countryIndex + 1);
      return new Locale(language, country, variant);
    }
  }

}
