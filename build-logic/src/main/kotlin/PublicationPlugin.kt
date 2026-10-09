import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.gradle.plugins.signing.SigningExtension

abstract class PublicationPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit =
        with(target) {
            pluginManager.apply("com.vanniktech.maven.publish")

            val definedVersion: String = providers.gradleProperty("VERSION_NAME").get()

            project.version =
                if (hasProperty("kotlinDev")) {
                    "${definedVersion.removeSuffix("-SNAPSHOT")}-kotlin-dev-SNAPSHOT"
                } else {
                    definedVersion
                }

            extensions.configure<MavenPublishBaseExtension> {
                publishToMavenCentral()
                coordinates(
                    groupId = localGradleProperty("POM_GROUP_ID").get(),
                    artifactId = localGradleProperty("POM_ARTIFACT_ID").get(),
                    version = version.toString(),
                )

                // Pass `-PuseGpgCmd` to sign artifacts using local gpg agent
                val useGpgCmd = localGradleProperty("useGpgCmd").orNull

                val signingKeyId = localGradleProperty("signingInMemoryKeyId").orNull?.takeIf { it.isNotEmpty() }
                val signingKey = localGradleProperty("signingInMemoryKey").orNull?.takeIf { it.isNotEmpty() }
                val signingPassword = localGradleProperty("signingInMemoryKeyPassword").orNull?.takeIf { it.isNotEmpty() }
                val signingConfigPresent = signingKeyId != null && signingKey != null && signingPassword != null

                // Avoid setting empty strings as signing keys. This avoids breaking the build when PR is opened from a fork.
                // Also, due to https://github.com/gradle/gradle/issues/18477 signing tasks try to prematurely access the signatory.
                // This also improves error messages if something's misconfigured
                if (signingConfigPresent || useGpgCmd != null) {
                    signAllPublications()

                    configure<SigningExtension> {
                        isRequired = signingConfigPresent // for more reproducible builds, fail if SNAPSHOT builds are misconfigured too

                        if (useGpgCmd != null && useGpgCmd != "false") {
                            useGpgCmd()
                        }
                    }
                } else {
                    logger.info(
                        listOfNotNull(
                            "signingKeyId".takeIf { signingKeyId.isNullOrBlank() },
                            "signingKey".takeIf { signingKey.isNullOrBlank() },
                            "signingPassword".takeIf { signingPassword.isNullOrBlank() },
                        ).joinToString(prefix = "Signing info not complete. Field(s) should not be empty: "),
                    )
                }

                configureBasedOnAppliedPlugins()

                pom {
                    name.set(localGradleProperty("POM_NAME"))
                    description.set(providers.gradleProperty("POM_DESCRIPTION"))
                    url.set(providers.gradleProperty("POM_URL"))
                    licenses {
                        license {
                            name.set(providers.gradleProperty("POM_LICENSE_NAME"))
                            url.set(providers.gradleProperty("POM_LICENSE_URL"))
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set(providers.gradleProperty("POM_DEVELOPER_ID"))
                            name.set(providers.gradleProperty("POM_DEVELOPER_NAME"))
                        }
                    }
                    scm {
                        url.set(providers.gradleProperty("POM_SCM_URL"))
                        connection.set(providers.gradleProperty("POM_SCM_CONNECTION"))
                        developerConnection.set(providers.gradleProperty("POM_SCM_DEV_CONNECTION"))
                    }
                }
            }
        }

    // TODO: remove this once https://github.com/gradle/gradle/issues/23572 is fixed
    @Suppress("ForbiddenComment")
    fun Project.localGradleProperty(name: String): Provider<String> =
        provider {
            if (hasProperty(name)) property(name)?.toString() else null
        }
}
