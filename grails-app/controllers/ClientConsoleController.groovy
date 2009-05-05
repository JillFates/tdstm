class ClientConsoleController {

    def index = { 
    	redirect(action:list,params:params)
    }
    def list={
        def projectId=params.projectId
        def assetEntityList=AssetEntity.findAll("from AssetEntity ae where ae.project.id="+projectId)
        def applicationList=AssetEntity.executeQuery("select distinct ae.application from AssetEntity ae where ae.application is not null and ae.project.id="+projectId)
        def appOwnerList=AssetEntity.executeQuery("select distinct ae.appOwner from AssetEntity ae where ae.appOwner is not null and ae.project.id="+projectId)
        def appSmeList=AssetEntity.executeQuery("select distinct ae.appSme from AssetEntity ae where ae.appSme is not null and ae.project.id="+projectId)
        return [assetEntityList: assetEntityList,appOwnerList:appOwnerList,applicationList:applicationList,appSmeList:appSmeList,projectId:projectId]
    }

    def searchFilters={
           
        def assetEntityList
        def appValue=request.getParameter("applicationVal")
        def appOwnerValue=request.getParameter("appOwnerVal")
        def appSmeValue=request.getParameter("appSmeVal")
        def projectId=request.getParameter("projectId")
        def applicationList=AssetEntity.executeQuery("select distinct ae.application from AssetEntity ae where ae.project.id="+projectId)
        def appOwnerList=AssetEntity.executeQuery("select distinct ae.appOwner from AssetEntity ae where ae.project.id="+projectId)
        def appSmeList=AssetEntity.executeQuery("select distinct ae.appSme from AssetEntity ae where ae.project.id="+projectId)
            
        def sb=new StringBuffer("from AssetEntity ae where ae.project.id="+projectId)
        if(appValue!="All"){
            sb.append(" and ae.application='$appValue'")
        }
        if(appOwnerValue!="All"){
            sb.append(" and ae.appOwner='$appOwnerValue'")
        }
        if(appSmeValue!="All"){
            sb.append(" and ae.appSme='$appSmeValue'")
        }
        assetEntityList=AssetEntity.findAll(sb.toString())
        render(view:'list',model:[assetEntityList:assetEntityList,appOwnerList:appOwnerList,applicationList:applicationList,appSmeList:appSmeList,projectId:projectId,appOwnerValue:appOwnerValue,appValue:appValue,appSmeValue:appSmeValue])
    }
}
