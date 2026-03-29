package dev.skymansandy.jsoncmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import dev.skymansandy.jsoncmp.domain.store.JsonAction
import dev.skymansandy.jsoncmp.domain.store.JsonStore
import dev.skymansandy.jsoncmp.domain.store.JsonStoreImpl

/** Creates and retains a [JsonStore] across recompositions, syncing when [initialJson] changes. */
@Composable
fun rememberJsonStore(
    initialJson: String,
    isEditing: Boolean = false,
): JsonStore {
    val store = retain {
        JsonStoreImpl(
            initialJson = initialJson,
            isEditing = isEditing,
        )
    }

    LaunchedEffect(initialJson) {
        if (initialJson.isNotBlank() && initialJson != store.state.value.raw) {
            store.dispatch(JsonAction.UpdateJson(initialJson))
        }
    }

    RetainedEffect(Unit) {
        onRetire { store.close() }
    }

    return store
}
