package com.jamesward.forcegradleplugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class ForcePlugin implements Plugin<Project> {

    public static String srcPath = "src/main/salesforce"

    void apply(Project project) {
        project.task('forceMetadataFetch', type: ForceMetadataFetchTask)
        project.task('forceMetadataDeploy', type: ForceMetadataDeployTask)
        project.extensions.create("force", ForcePluginExtension)
    }

}