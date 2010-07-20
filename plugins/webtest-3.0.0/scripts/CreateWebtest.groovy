/*
* Copyright 2004-2005 the original author or authors.
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
*/

/**
 * Gant script that generates a new web test from a domain class
 *
 * @author Graeme Rocher
 * @author Dierk Koenig
 *
 * @since 0.4
 */

includeTargets << grailsScript("_GrailsInit")
includeTargets << grailsScript("_PluginDependencies")
includeTargets << grailsScript("_GrailsCreateArtifacts")

pluginHome = getPluginDirForName("webtest").path

target('default': "Creates a skeleton of a Canoo WebTest (functional test) for a given domain name") {
    depends(checkVersion, parseArguments)

    promptForName(type: "Canoo WebTest")
    def name = argsMap["params"][0]
    def superClass = argsMap["superClass"] ?: "grails.util.WebTest"
    createArtifact(name: name, suffix: "WebTests", type: "WebTests", path: "test/webtest", superClass: superClass)    
}
