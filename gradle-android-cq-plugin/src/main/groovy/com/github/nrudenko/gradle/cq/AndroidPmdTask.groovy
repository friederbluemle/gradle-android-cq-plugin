package com.github.nrudenko.gradle.cq

import org.gradle.api.file.FileCollection
import org.gradle.api.internal.project.IsolatedAntBuilder
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
/**
 * See parameters at http://pmd.sourceforge.net/pmd-4.2.6/ant-task.html
 */
class AndroidPmdTask extends BaseStatisticTask {

    public static final String pmdTaskClassname = 'net.sourceforge.pmd.ant.PMDTask'
    public static final String rulesetPath = "pmd/pmd-ruleset.xml"

    @InputFile
    @Optional
    File rulesetFile

    FileCollection pmdClasspath = project.configurations.codequality
    Boolean ignoreFailures = true
    Boolean showViolations = false

    @Override
    String getXslFilePath() {
        return "pmd/pmd-html.xsl"
    }

    @Override
    String getOutputPath() {
        return "$project.buildDir/reports/pmd/pmd-result.xml"
    }

    def AndroidPmdTask() {
        description = 'Runs PMD against Android sourcesets.'
        group = 'Code Quality'
    }

    @TaskAction
    def runPmd() {

        prepareTaskFiles()
        rulesetFile = getFileFromConfigCache(rulesetPath)

        def antBuilder = services.get(IsolatedAntBuilder)
        antBuilder.withClasspath(pmdClasspath).execute {
            ant.taskdef(name: 'pmd', classname: pmdTaskClassname)
            ant.pmd(shortFilenames: 'true',
                    failonruleviolation: !ignoreFailures,
                    rulesetfiles: rulesetFile.toURI().toString()) {
                formatter(type: 'xml', toFile: outputFile, toConsole: showViolations)
                fileset(dir: gradleProject.projectDir.getPath()) {
                    applyToFileSet({file -> include(name: gradleProject.relativePath(file))})
                }
            }
            makeHtml(ant)
        }
    }
}