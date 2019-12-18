<%@page import="net.transitionmanager.asset.AssetType; net.transitionmanager.asset.AssetType;"%>

<%-- TODO: Update this so that it will work for show and edit. --%>

<div class="legacy-modal-header">
    <%-- Header --%>
    <div class="modal-title-container">
        <button class="btn btn-icon close-button" onclick="closeModal()">
            <i class="fas fa-times"></i>
        </button>
        <div class="badge modal-badge">${assetEntity.assetClass.name().take(1).toUpperCase()}</div>
        <h4 class="modal-title">${assetEntity.assetName}</h4>
        <div class="modal-subtitle">${assetEntity.moveBundle}</div>
        <div class="badge modal-subbadge"><tds:showDependencyGroup groupId="${dependencyBundleNumber}" assetName="${assetEntity.assetName}"/></div>
    </div>

    <div class="modal-description">
        <div id="readMore">
            <p>${assetEntity.description} <a onclick="toggleReadMore()">Read Less</a></p>
        </div>
        <div id="readLess" class="readMore">
            <g:if test="${assetEntity.description?.length() > 80}">
                <div class="truncated-description">${assetEntity.description.substring(0,80)}...</div>
                <a onclick="toggleReadMore()"> Read More</a>
            </g:if>
            <g:else>
                <div class="truncated-description">${assetEntity.description}</div>
            </g:else>
        </div>
    </div>

    <%-- Navigation - Page scrolling --%>
    <g:if test="${mode == 'show'}">
        <ul class="nav">
            <li class="nav-item">
                <button type="button" class="btn btn-link nav-link active" id="tab1" onclick="navigate('tab1', 'details')">Summary</button>
            </li>
            <li class="nav-item">
                <button type="button" class="btn btn-link nav-link" id="tab2" onclick="navigate('tab2', 'supports')">Supports
                    <span class="badge">
                        <g:if test="${supportAssets?.size() > 99}">
                            99+
                        </g:if>
                        <g:else>
                            ${supportAssets?.size()}
                        </g:else>
                    </span>
                </button>
            </li>
            <li class="nav-item">
                <button type="button" class="btn btn-link nav-link" id="tab3" onclick="navigate('tab3', 'depends')">Depends On
                    <span class="badge">
                        <g:if test="${dependentAssets?.size() > 99}">
                            99+
                        </g:if>
                        <g:else>
                            ${dependentAssets?.size()}
                        </g:else>
                    </span>
                </button>
            </li>
            <li class="nav-item">
                <button type="button" class="btn btn-link nav-link" id="tab4" onclick="navigate('tab4', 'commentListId')">Tasks
                    <%-- TODO: UPDATE PLACEHOLDER / Add badge w/ number if possible --%>
                    <%-- <span class="badge">9</span> --%>
                </button>
            </li>
            <li class="nav-item">
                <button type="button" class="btn btn-link nav-link" id="tab5" onclick="navigate('tab5', 'commentListId')">Comments
                    <%-- TODO: UPDATE PLACEHOLDER / Add badge w/ number if possible --%>
                    <%-- <span class="badge">9</span> --%>
                </button>
            </li>
        </ul>
    </g:if>
    <g:else>
        <%-- Clarity Tabs --%>
        <ul class="nav">
            <li class="nav-item">
                <button type="button" class="btn btn-link nav-link active" id="tab1" onclick="navigate('tab1', 'details')">Details</button>
            </li>
            <li class="nav-item">
                <button type="button" class="btn btn-link nav-link" id="tab2" onclick="navigate('tab2', 'supports')">Supports/Depends</button>
            </li>
        </ul>
    </g:else>
</div>

<script>
	$(document).ready(function() { 
        <g:if test="${assetEntity.description?.length() > 0}">
            $("#modalBody").addClass("has-description");
            $("#modalDescription").addClass("modal-description-height");
        </g:if>
        <g:else>
            $("#modalBody").addClass("no-description");
        </g:else>

        $("#readMore").hide();
        $("#readLess").show();
    })

    var readMore = false;
    
    function closeModal() {
        var modalIds = ['#showEntityView', '#editEntityView'];
        modalIds.forEach(id => {
            if ($(id).length) {
                $(id).dialog('close');
            }
        });
    }

    function toggleReadMore() {
        readMore = !readMore;

        if (!readMore) {
            $("#readMore").hide();
            $("#readLess").show();
        } else {
            $("#readLess").hide();
            $("#readMore").show();
        }
    }

    function navigate(tabId, id) {
        // Use a list of tabIds so that we don't accidentally remove the active class from other tab lists.
        <g:if test="${mode == 'show'}">
            var tabList=['tab1', 'tab2', 'tab3', 'tab4', 'tab5'];
        </g:if>
        <g:else>
            var tabList=['tab1', 'tab2'];
        </g:else>

		tabList.forEach(t => {
            var tab = document.getElementById(t);
            if (tab.classList.contains('active')) {
			    tab.classList.remove('active');
            }
		});

		var tabEl = document.getElementById(tabId);
		tabEl.classList.add('active');

		var el = document.getElementById(id);
		el.scrollIntoView();
    }
</script>