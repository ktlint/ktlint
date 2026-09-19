package io.github.ktlint.core.rule.engine.internal

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import org.ec4j.core.EditorConfigLoader
import org.ec4j.core.Resource
import org.ec4j.core.model.EditorConfig
import org.ec4j.core.model.Glob
import org.ec4j.core.model.Property
import org.ec4j.core.model.Section
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.nio.file.Paths

class ThreadSafeEditorConfigCacheTest {
    @Test
    fun `Given a file which is requested multiple times then it is read only once and then stored into and retrieved from the cache`() {
        val threadSafeEditorConfigCache = ThreadSafeEditorConfigCache()

        val editorConfigLoader = EditorConfigLoaderMock(EDIT_CONFIG_1)
        val actual =
            listOf(
                threadSafeEditorConfigCache.get(FILE_1, editorConfigLoader),
                threadSafeEditorConfigCache.get(FILE_1, editorConfigLoader),
                threadSafeEditorConfigCache.get(FILE_1, editorConfigLoader),
            )

        // In logs, it can also be seen that the EditConfig entry is created only once and retrieved multiple times
        assertThat(editorConfigLoader.loadCount).isEqualTo(1)
        assertThat(actual).containsExactly(
            EDIT_CONFIG_1,
            EDIT_CONFIG_1,
            EDIT_CONFIG_1,
        )
    }

    @Test
    fun `Given that multiple files are stored into the cache and one of those files is requested another time then this file is still being retrieved from the cache`() {
        val threadSafeEditorConfigCache = ThreadSafeEditorConfigCache()
        val editorConfigLoaderFile1 = EditorConfigLoaderMock(EDIT_CONFIG_1)
        val editorConfigLoaderFile2 = EditorConfigLoaderMock(EDIT_CONFIG_2)

        val actual =
            listOf(
                threadSafeEditorConfigCache.get(FILE_1, editorConfigLoaderFile1),
                threadSafeEditorConfigCache.get(FILE_2, editorConfigLoaderFile2),
                threadSafeEditorConfigCache.get(FILE_1, editorConfigLoaderFile1),
            )

        // In logs, it can also be seen that the EditConfig entry for FILE_1 and FILE_2 are created only once and
        // retrieved once more for FILE_1
        assertThat(editorConfigLoaderFile1.loadCount).isEqualTo(1)
        assertThat(editorConfigLoaderFile2.loadCount).isEqualTo(1)
        assertThat(actual).containsExactly(
            EDIT_CONFIG_1,
            EDIT_CONFIG_2,
            EDIT_CONFIG_1,
        )
    }

    @Test
    fun `Given that a file is stored in the cache and then file is explicitly reloaded`() {
        val threadSafeEditorConfigCache = ThreadSafeEditorConfigCache()

        val editorConfigLoaderFile1 = EditorConfigLoaderMock(EDIT_CONFIG_1)
        threadSafeEditorConfigCache.get(FILE_1, editorConfigLoaderFile1)
        threadSafeEditorConfigCache.reloadIfExists(FILE_1)
        threadSafeEditorConfigCache.reloadIfExists(FILE_1)

        assertThat(editorConfigLoaderFile1.loadCount).isEqualTo(3)
    }

    @Test
    fun `Given that a file is stored in the cache and then the cache is cleared and the file is requested again then the file is to be reloaded`() {
        val threadSafeEditorConfigCache = ThreadSafeEditorConfigCache()

        val editorConfigLoaderFile1 = EditorConfigLoaderMock(EDIT_CONFIG_1)
        threadSafeEditorConfigCache.get(FILE_1, editorConfigLoaderFile1)
        threadSafeEditorConfigCache.clear()
        threadSafeEditorConfigCache.get(FILE_1, editorConfigLoaderFile1)
        threadSafeEditorConfigCache.get(FILE_1, editorConfigLoaderFile1)

        assertThat(editorConfigLoaderFile1.loadCount).isEqualTo(2)
    }

    private companion object {
        const val SOME_PROPERTY = "some-property"

        private fun String.resource() = Resource.Resources.ofPath(Paths.get(this), StandardCharsets.UTF_8)

        val FILE_1: Resource = "/some/path/to/file/1".resource()
        val FILE_2: Resource = "/some/path/to/file/2".resource()

        // Create unique instances of the EditConfig by setting a value to a property
        fun createUniqueInstanceOfEditConfig(id: String): EditorConfig =
            EditorConfig
                .builder()
                .section(
                    Section
                        .builder()
                        .glob(Glob("*.kt"))
                        .properties(
                            Property
                                .builder()
                                .name(SOME_PROPERTY)
                                .value(id),
                        ),
                ).build()

        val EDIT_CONFIG_1: EditorConfig = createUniqueInstanceOfEditConfig("edit-config-1")
        val EDIT_CONFIG_2: EditorConfig = createUniqueInstanceOfEditConfig("edit-config-2")
    }

    private class EditorConfigLoaderMock(
        private var initial: EditorConfig,
    ) : EditorConfigLoader(null, null) {
        var loadCount = 0

        override fun load(configFile: Resource): EditorConfig {
            loadCount++
            return initial
        }
    }
}
