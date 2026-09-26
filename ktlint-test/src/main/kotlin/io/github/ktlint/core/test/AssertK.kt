package io.github.ktlint.core.test

import assertk.assertThat
import assertk.assertions.isSuccess

public fun assertDoesNotThrow(function: () -> Unit) {
    assertThat(runCatching { function }).isSuccess()
}

public fun assertDoesNotThrow(
    name: String,
    function: () -> Unit,
) {
    assertThat(runCatching { function }, name = name)
        .isSuccess()
}
