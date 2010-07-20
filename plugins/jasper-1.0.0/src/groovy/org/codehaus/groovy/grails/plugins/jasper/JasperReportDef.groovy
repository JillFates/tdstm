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

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.springframework.core.io.Resource
import org.springframework.core.io.FileSystemResourceLoader.FileSystemContextResource
import org.apache.commons.io.FilenameUtils

/**
 * An abstract representation of a Jasper report.
 * <p>
 * It contains the location of the report design file (name and folder) and
 * data(reportData and/or parameters) needed to fill the report.
 */
public class JasperReportDef {

  /**
   * The name of the report file without extension.
   * <p>
   * The file can be in jrxml- or jasper-format.
   */
  private String name

  /**
   * The parent folder of the report file.
   * <p>
   * This can be an absolute path or an relative path based on class path.
   */
  private String folder

  /**
   * The data source used to fill the report.
   * <p>
   * This is a list of java beans.
   */
  private Collection reportData = []

  private getApplicationContext() {
    return ApplicationHolder.application.mainContext
  }

  public Resource fetchReportSpec() throws Exception {
    final String baseFilename = folder + File.separator + FilenameUtils.getPath(name) + FilenameUtils.getBaseName(name)
    Resource result = getApplicationContext().getResource(baseFilename + '.jasper')
    if (result.exists()) {
      return result
    }
    result = getApplicationContext().getResource(baseFilename + '.jrxml')
    if (result.exists()) {
      return result
    }

    result = new FileSystemContextResource(baseFilename + '.jasper')
    if (result.exists()) {
      return result
    }
    result = new FileSystemContextResource(baseFilename + '.jrxml')
    if (result.exists()) {
      return result
    }
    throw new Exception("No such report spec: ${baseFilename}.jasper or .jrxml")
  }

  public Collection getReportData() {
    return reportData
  }

  public String getName() {
    return name
  }
}