/* Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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
 */
package grails.plugin.databasemigration.dbdoc

import java.text.DateFormat

import liquibase.change.Change
import liquibase.changelog.ChangeSet
import liquibase.database.Database
import liquibase.util.LiquibaseUtil
import liquibase.util.StringUtils

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
abstract class HTMLWriter {

	protected Database database
	private Map files
	private String subdir

	protected HTMLWriter(Map files, String subdir, Database database) {
		this.files = files
		this.subdir = subdir
		this.database = database
	}

	protected abstract void writeCustomHTML(StringBuilder content, object, List<Change> changes)

	void writeHTML(object, List<Change> ranChanges, List<Change> changesToRun, String changeLog) {
		StringBuilder content = new StringBuilder()

		content.append("<html>")
		writeHeader(object, content)
		content.append('<body bgcolor="white" onload="windowTitle()">')

		content.append("<h2>").append(createTitle(object)).append("</h2>\n")

		writeBody content, object, ranChanges, changesToRun

		writeFooter content, changeLog

		content.append("</body>")
		content.append("</html>")
		files["$subdir/${object.toString().toLowerCase()}".toString()] = content.toString()
	}

	protected void writeFooter(StringBuilder content, String changeLog) {
		content.append("<hr>Generated: ")
		content.append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date()))
		content.append("<br/>Against: ")
		content.append(database)
		content.append("<br/>Change Log: ")
		content.append(changeLog)
		content.append("<br/><br/>Generated By: ")
		content.append("<a href='http://www.liquibase.org' target='_blank'>LiquiBase ")
		content.append(LiquibaseUtil.getBuildVersion()).append("</a>")
	}

	protected void writeBody(StringBuilder content, object, List<Change> ranChanges, List<Change> changesToRun) {
		writeCustomHTML content, object, ranChanges
		writeChanges 'Pending Changes', content, changesToRun
		writeChanges 'Past Changes', content, ranChanges
	}

	protected void writeTable(String title, List<List<String>> cells, StringBuilder content) {
		content.append('<p>')
		int colspan = 0
		if (cells) {
			colspan = cells[0].size()
		}
		else {
			colspan = 0
		}
		content.append("""<table border="1" width="100%" cellpadding="3" cellspacing="0" summary="">\n""")
				.append("""<tr bgcolor="#ccccff" class="TableHeadingColor">\n""").append("<td colspan=")
				.append(colspan).append("""><font size="+2">\n""").append("<b>").append(title)
				.append("</b></font></td>\n").append("</tr>\n")

		for (List<String> row : cells) {
			content.append("""<tr bgcolor="white" class="TableRowColor">\n""")
			for (String cell : row) {
				writeTD(content, cell)
			}
			content.append("</tr>\n")
		}
		content.append("</table>\n")
	}

	protected void writeTD(StringBuilder content, String filePath) {
		content.append('<td valign="top">\n')
		content.append(filePath)
		content.append("</td>\n")
	}

	protected void writeHeader(object, StringBuilder content) {
		String title = createTitle(object)
		content.append("<head>\n").append("<title>").append(title).append("</title>\n")
				.append('<link rel="stylesheet" type="text/css" href="dbdoc_stylesheet_css" title="Style"/>\n')
				.append('<script type="text/javascript">\n')
				.append("function windowTitle() {\n")
				.append("    parent.document.title='").append(title).append("';").append("\n}\n")
				.append("</script>\n")
				.append("</head>\n")
	}

	protected abstract String createTitle(object)

	protected void writeChanges(String title, StringBuilder content, List<Change> changes, boolean relative = true) {
		content.append('<p><table border="1" width="100%" cellpadding="3" cellspacing="0" summary="">\n')
		content.append('<tr bgcolor="#ccccff" class="TableHeadingColor">\n')
		content.append('<td colspan="4"><font size="+2">\n')
		content.append("<b>")
		content.append(title)
		content.append('</b></font></td>\n')
		content.append('</tr>\n')

		String pathStart = relative ? '../' : ''

		ChangeSet lastChangeSet
		if (!changes) {
			content.append('<tr><td>None Found</td></tr>')
		}
		else {
			for (Change change : changes) {
				if (!change.changeSet.equals(lastChangeSet)) {
					lastChangeSet = change.changeSet
					content.append('<tr bgcolor="#EEEEFF" class="TableSubHeadingColor">\n')

					String hrefName = change.changeSet.filePath.toLowerCase().endsWith('.xml') ? change.changeSet.filePath[0..-5] : change.changeSet.filePath
					writeTD(content, "<a href='${pathStart}changelogs/$hrefName'>"
							+ change.changeSet.filePath + "</a>")
					writeTD(content, change.changeSet.id)
					writeTD(content, "<a href='${pathStart}authors/${change.changeSet.author.toLowerCase()}'>"
							+ change.changeSet.author.toLowerCase() + "</a>")

					ChangeSet.RunStatus runStatus = database.getRunStatus(change.changeSet)
					if (runStatus.equals(ChangeSet.RunStatus.NOT_RAN)) {
						String anchor = change.changeSet.toString(false).replaceAll("\\W", "_")
						writeTD(content, "NOT YET RAN [<a href='${pathStart}pending/sql#$anchor'>SQL</a>]")
					}
					else if (runStatus.equals(ChangeSet.RunStatus.INVALID_MD5SUM)) {
						writeTD(content, "INVALID MD5SUM")
					}
					else if (runStatus.equals(ChangeSet.RunStatus.ALREADY_RAN)) {
						writeTD(content, "Executed " +
							DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(
								database.getRanDate(change.changeSet)))
					}
					else if (runStatus.equals(ChangeSet.RunStatus.RUN_AGAIN)) {
						writeTD(content, "Executed, WILL RUN AGAIN")
					}
					else {
						throw new RuntimeException("Unknown run status: " + runStatus)
					}

					content.append("</tr>")

					if (StringUtils.trimToNull(change.changeSet.comments)) {
						content.append("<tr><td bgcolor='#eeeeff' class='TableSubHeadingColor' colspan='4'>")
								.append(change.changeSet.comments).append("</td></tr>")
					}

				}

				content.append('<tr bgcolor="white" class="TableRowColor">\n')
				content.append("<td colspan='4'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
				       .append(change.confirmationMessage).append("</td></tr>")
			}
		}

		content.append("</table>")
		content.append("&nbsp;</p>")
	}
}
