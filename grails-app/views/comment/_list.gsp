<%@page import="net.transitionmanager.security.Permission"%>
<div draggable id="listCommentsTasksPopup" class="ui-dialog ui-widget ui-widget-content ui-corner-all ui-front" style="width: 700px" tabindex="-1" >
    <div class="ui-list-comments ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix">
        <span id="ui-id-5" class="ui-dialog-title">Show Comments{{commentsData[0]?(': ' + commentsData[0].assetType + '/'+ commentsData[0].assetName):''}}</span>
        <button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close" role="button" aria-disabled="false" title="close" ng-click="close()">
        <span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span>
        <span class="ui-button-text">close</span>
        </button>
    </div>
    <div id="commentsListDialog" title="Show Assets" class="comment-dialog-content">
        <loading-indicator></loading-indicator>
        <br/>
        <div class="list">
            <table id="listCommentsTable">
                <thead>
                    <tr>
                        <th nowrap>Action</th>
                        <th nowrap>Comment</th>
                        <th nowrap>Status</th>
                        <th nowrap>Category</th>
                    </tr>
                </thead>
                <tbody id="listCommentsTbodyId">
                    <tr class="comments-table-row" ng-repeat="comment in commentsData" id="commentTr_{{comment.commentInstance.id}}" ng-style="rowColor(comment)">
                        <td>
                        <tds:hasPermission permission="${Permission.CommentEdit}">
                            <a id="link_{{comment.commentInstance.id}}" ng-click="edit(comment.commentInstance.id, comment.commentInstance.commentType)"><asset:image src="icons/comment_edit.png" border="0" /></a>
                        </tds:hasPermission>
                        </td>
                        <td id="comment_{{comment.commentInstance.id}}" ng-click="view(comment.commentInstance.id, comment.commentInstance.commentType)">{{truncate(comment.commentInstance.comment)}}</td>
                        <td id="type_{{comment.commentInstance.id}}" ng-click="view(comment.commentInstance.id, comment.commentInstance.commentType)">{{(comment.commentInstance.isResolved == '1'?'Archived':'')}}</td>
                        <td id="category_{{comment.commentInstance.id}}" ng-click="view(comment.commentInstance.id, comment.commentInstance.commentType)">{{truncate(comment.commentInstance.category)}}</td>
                    </tr>
                </tbody>
            </table>
        </div>
        <div class="buttons">
            <tds:hasPermission permission="${Permission.CommentCreate}">
                <a ng-click="createComment()" class="comment-create-button">
                <asset:image src="icons/comment_add.png" border="0px" style="margin-bottom: -4px;" /> &nbsp;&nbsp;Add Comment
                </a>
            </tds:hasPermission>
        </div>
    </div>
</div>