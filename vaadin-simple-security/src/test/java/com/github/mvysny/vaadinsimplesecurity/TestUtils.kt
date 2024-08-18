package com.github.mvysny.vaadinsimplesecurity

import org.junit.jupiter.api.assertThrows
import kotlin.test.expect

inline fun <reified E: Throwable> expectThrows(expectedExceptionMessage: String, block: () -> Unit) {
    val ex = assertThrows<E>(block)
    expect(expectedExceptionMessage) { ex.message!! }
}