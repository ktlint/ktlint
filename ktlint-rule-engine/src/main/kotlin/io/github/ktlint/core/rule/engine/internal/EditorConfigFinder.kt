package io.github.ktlint.core.rule.engine.internal

import io.github.ktlint.core.logger.api.initKtLintKLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.ec4j.core.Resource
import org.ec4j.core.ResourcePropertiesService
import java.nio.charset.StandardCharsets
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.system.measureTimeMillis

private val LOGGER = KotlinLogging.logger {}.initKtLintKLogger()

internal class EditorConfigFinder(
    private val editorConfigLoaderEc4j: EditorConfigLoaderEc4j,
) {
    /**
     * Finds all relevant ".editorconfig" files for the given path.
     */
    fun findEditorConfigs(path: Path): List<Path> {
        READ_WRITE_LOCK.read {
            val cacheValue =
                IN_MEMORY_CACHE[path]
                    ?.also {
                        LOGGER.info { "Retrieving EditorConfig cache entry for path $path" }
                    }
            return cacheValue
                ?: READ_WRITE_LOCK.write {
                    cacheEditorConfigs(path)
                        .also { cacheValue ->
                            LOGGER.info { "Creating cache entry for path $path with value $cacheValue" }
                        }
                }
        }
    }

    private fun cacheEditorConfigs(path: Path): List<Path> {
        val result = mutableListOf<Path>()
        val normalizedPath = path.normalize().toAbsolutePath()
        if (path.isDirectory()) {
            result += findEditorConfigsInSubDirectories(normalizedPath)
        }
        result += findEditorConfigsInParentDirectories(normalizedPath)

        val editorConfigPaths =
            result
                .map {
                    // Resolve against original path as the drive letter seems to get lost on WindowsOs
                    path.resolve(it)
                }.toList()
        IN_MEMORY_CACHE[path] = editorConfigPaths

        return editorConfigPaths
    }

    private fun findEditorConfigsInSubDirectories(path: Path): List<Path> {
        val result = mutableListOf<Path>()
        var visitedDirectoryCount = 0

        measureTimeMillis {
            Files.walkFileTree(
                path,
                object : SimpleFileVisitor<Path>() {
                    override fun visitFile(
                        filePath: Path,
                        fileAttrs: BasicFileAttributes,
                    ): FileVisitResult {
                        if (filePath.name == ".editorconfig") {
                            LOGGER.trace { "- File: $filePath: add to list of accessed files" }
                            result.add(filePath)
                        }
                        return FileVisitResult.CONTINUE
                    }

                    override fun preVisitDirectory(
                        dirPath: Path,
                        dirAttr: BasicFileAttributes,
                    ): FileVisitResult {
                        visitedDirectoryCount++
                        return if (Files.isHidden(dirPath)) {
                            LOGGER.trace { "- Dir: $dirPath: Ignore" }
                            FileVisitResult.SKIP_SUBTREE
                        } else {
                            LOGGER.trace { "- Dir: $dirPath: Traverse" }
                            FileVisitResult.CONTINUE
                        }
                    }
                },
            )
        }.also { duration ->
            LOGGER.debug {
                "Scanning file system to find all '.editorconfig' files in directory '$path' scanned $visitedDirectoryCount directories " +
                    "in $duration ms"
            }
        }

        return result.toList()
    }

    private fun findEditorConfigsInParentDirectories(path: Path): List<Path> {
        // The logic to load parental ".editorconfig" files resides in the ec4j library. This library however uses a
        // cache provided by KtLint. As of this the list of parental ".editorconfig" files can be extracted from the
        // cache.
        createLoaderService().queryProperties(path.resource())
        return EDITOR_CONFIG_CACHE.getPaths()
    }

    private fun Path?.resource() = Resource.Resources.ofPath(this, StandardCharsets.UTF_8)

    private fun createLoaderService() =
        ResourcePropertiesService
            .builder()
            .cache(EDITOR_CONFIG_CACHE)
            .loader(editorConfigLoaderEc4j.editorConfigLoader)
            .build()

    private companion object {
        // Do not reuse the generic threadSafeEditorConfigCache to prevent that results are incorrect due to other
        // calls to KtLint that result in changing the cache
        val EDITOR_CONFIG_CACHE = ThreadSafeEditorConfigCache()

        private val READ_WRITE_LOCK = ReentrantReadWriteLock()
        private val IN_MEMORY_CACHE = HashMap<Path, List<Path>>()
    }
}
