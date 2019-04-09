<div class="body task-report-container">
    <g:if test="${flash.message}">
        <div class="message">
            ${flash.message}
        </div>
    </g:if>
    <table class="planning-application-table">
        <thead>
        <tr>
            <th>Task #</th>
            <th>Task Description</th>
            <th>Related to</th>
            <th>Predecessor Task(s)</th>
            <th>Responsible Resource</th>
            <th>Instructions Link</th>
            <th>Team</th>
            <th>Status</th>
            <th>Date Planned</th>
            <th>Date Required</th>
            <th>Comments</th>
            <th>Duration</th>
            <th>Duration Scale</th>
            <th>Estimated Start</th>
            <th>Estimated Finish</th>
            <th>Actual Start</th>
            <th>Actual Finish</th>
            <th>WorkFlow Step</th>
            <th>Category</th>
            <th>Due Date</th>
            <th>Created on</th>
            <th>Created by</th>
            <th>Event</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${taskList}" var="task" status="i">
            <tr class="${i%2==0 ? 'even' : 'odd' }">
                <td>
                    ${task.taskNumber}
                </td>
                <td>
                    ${task.comment}
                </td>
                <td>
                    ${task.assetEntity?.assetName}
                </td>
                <td>
                    <g:each in="${task.taskDependencies}" var="dep">
                        <g:if test="${viewUnpublished || dep.predecessor.isPublished}">
                            ${dep.predecessor == null ? '' : dep.predecessor.taskNumber + ' ' + dep.predecessor.comment.toString()}
                            <br />
                        </g:if>
                    </g:each></td>
                <td>
                    ${task.assignedTo}
                </td>
                <td>
                    <tds:textAsLink text="${task.instructionsLink}" target="_blank"/>
                </td>
                <td>
                    ${task.role}
                </td>
                <td>
                    ${task.status}
                </td>
                <td>NA</td>
                <td>NA</td>
                <td><g:each in="${task.notes}" var="note">
                    ${note.note}
                </g:each></td>
                <td>
                    ${task.duration}
                </td>
                <td>
                    ${task.durationScale}
                </td>
                <td><tds:convertDateTime date="${task.estStart}" /></td>
                <td><tds:convertDateTime date="${task.estFinish }" /></td>
                <td><tds:convertDateTime date="${task.actStart}"  /></td>
                <td><tds:convertDateTime date="${task.dateResolved }"/></td>
                <td>NA</td>
                <td>
                    ${task.category}
                </td>
                <td><tds:convertDate date="${task.dueDate }"/></td>
                <td>
                    <tds:convertDateTime date="${task.dateCreated }"/>
                </td>
                <td>${task.createdBy}</td>
                <td>
                    ${task.moveEvent }
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
</div>
