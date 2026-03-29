package dev.skymansandy.jsoncmp.domain.store

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.StateFlow

/** Reactive state holder for JSON content — parsing, folding, formatting, and sorting. */
@Stable
internal interface JsonHolder {

    val state: StateFlow<JsonHolderState>

    fun dispatch(action: JsonAction)
}
