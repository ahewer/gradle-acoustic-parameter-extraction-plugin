package de.dfki.mary.coefficientextraction

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.MavenPlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.bundling.Zip

import static groovyx.gpars.GParsPool.runForkJoin
import static groovyx.gpars.GParsPool.withPool

import de.dfki.mary.coefficientextraction.export.*
import de.dfki.mary.coefficientextraction.process.*

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.xml.*

class CoefficientExtractionPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.plugins.apply JavaPlugin
        project.plugins.apply MavenPlugin

        project.sourceCompatibility = JavaVersion.VERSION_1_7


        project.ext {
            basename = project.name
        }

        project.status = project.version.endsWith('SNAPSHOT') ? 'integration' : 'release'

        project.afterEvaluate {

            project.task("configurationExtraction") {
                dependsOn "configuration"

                def wav_dir = project.configuration.hasProperty("wav_dir") ? project.configuration.wav_dir : null // DataFileFinder.getFilePath(config.data.wav_dir)
                def ema_dir = project.configuration.hasProperty("ema_dir") ? project.configuration.ema_dir : null // DataFileFinder.getFilePath(config.data.ema_dir)

                if(project.configuration.hasProperty("project_path") ) {

                    DataFileFinder.project_path = project.configuration.project_path

                }

                if (project.configuration.kind.equals("ema")) {
                    ext.input_file = "$ema_dir/${project.name}.ema"
                } else {
                    ext.input_file = "$wav_dir/${project.name}.wav"
                }
                
                ext.nb_proc = project.configuration.hasProperty("nb_proc") ? project.configuration.nb_proc : 1
                ext.user_configuration = project.configuration.hasProperty("user_configuration") ? project.configuration.user_configuration : null
            }


            def kinds = [
            "ema":          new EMAProcess(),
            "straight":     new STRAIGHTProcess(),
            "straightdnn" : new STRAIGHTDNNProcess(),
            "spline":       new SplineProcess(),
            "world":        new WorldProcess(),
            "straightema":  new STRAIGHTEMAProcess(),
            "straightemadnn":  new STRAIGHTEMADNNProcess(),
            "weight":       new WEIGHTProcess()
            ];

            if (project.configurationExtraction.user_configuration != null)
            {
                kinds[project.configurationExtraction.user_configuration.settings.extraction.kind].addTasks(project)
            }
        }
    }
}
