package dev.skymansandy.jsoncmp.domain.store

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.StateFlow

/** Reactive state holder for JSON content — parsing, folding, formatting, and sorting. */
@Stable
interface JsonStore {

    val state: StateFlow<JsonStoreState>

    fun dispatch(action: JsonAction)
}
