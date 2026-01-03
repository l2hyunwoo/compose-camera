package io.github.l2hyunwoo.compose.camera.sample

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.applyTestTagsAsResourceId(): Modifier {
    return this.semantics {
        testTagsAsResourceId = true
    }
}
