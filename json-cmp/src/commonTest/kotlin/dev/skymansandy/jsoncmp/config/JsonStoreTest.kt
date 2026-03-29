package dev.skymansandy.jsoncmp.config

import dev.skymansandy.jsoncmp.TestData
import dev.skymansandy.jsoncmp.model.JsonNode
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlin.test.Test

class JsonStoreTest {

    private fun createStore(json: String, isEditing: Boolean = false): JsonStore {
        // Unconfined makes coroutines run synchronously for testing
        return JsonStore(json, isEditing = isEditing)
    }

    // ── Initialization ──

    @Test
    fun initParsesValidJson() {
        val store = createStore(TestData.SIMPLE_OBJECT)
        val state = store.state.value

        state.parsedJson.shouldNotBeNull()
        state.error.shouldBeNull()
        state.allLines.shouldNotBeEmpty()
    }

    @Test
    fun initHandlesInvalidJson() {
        val store = createStore(TestData.INVALID_JSON)
        val state = store.state.value

        state.parsedJson.shouldBeNull()
        state.error.shouldNotBeNull()
        state.allLines.shouldBeEmpty()
    }

    @Test
    fun initHandlesEmptyJson() {
        val store = createStore("")
        val state = store.state.value

        state.parsedJson.shouldBeNull()
        state.error.shouldBeNull()
        state.allLines.shouldBeEmpty()
    }

    @Test
    fun initHandlesWhitespaceOnly() {
        val store = createStore("   ")
        val state = store.state.value

        state.parsedJson.shouldBeNull()
        state.error.shouldBeNull()
        state.allLines.shouldBeEmpty()
    }

    @Test
    fun initSetsEditingFlag() {
        val editing = createStore(TestData.SIMPLE_OBJECT, isEditing = true)
        val viewing = createStore(TestData.SIMPLE_OBJECT, isEditing = false)

        editing.state.value.isEditing.shouldBeTrue()
        viewing.state.value.isEditing.shouldBeFalse()
    }

    @Test
    fun initSetsIsCompactToFalse() {
        val store = createStore(TestData.SIMPLE_OBJECT)

        store.state.value.isCompact.shouldBeFalse()
    }

    // ── Format ──

    @Test
    fun formatCompactProducesMinifiedJson() {
        val store = createStore(TestData.SIMPLE_OBJECT, isEditing = true)
        store.dispatch(JsonAction.Format(compact = true))
        val state = store.state.value

        state.isCompact.shouldBeTrue()
        state.raw shouldNotContain "\n"
        state.raw shouldNotContain "  "
    }

    @Test
    fun formatBeautifyProducesIndentedJson() {
        val store = createStore("""{"name":"John","age":30}""", isEditing = true)
        store.dispatch(JsonAction.Format(compact = false))
        val state = store.state.value

        state.isCompact.shouldBeFalse()
        state.raw shouldContain "\n"
        state.raw shouldContain "  "
    }

    @Test
    fun formatDoesNothingWhenNoParsedJson() {
        val store = createStore(TestData.INVALID_JSON, isEditing = true)
        val originalRaw = store.state.value.raw
        store.dispatch(JsonAction.Format(compact = true))

        store.state.value.raw shouldBe originalRaw
    }

    @Test
    fun formatToggleCompactThenBeautifyRoundtrips() {
        val store = createStore(TestData.SIMPLE_OBJECT, isEditing = true)

        store.dispatch(JsonAction.Format(compact = true))
        val compactJson = store.state.value.raw

        store.dispatch(JsonAction.Format(compact = false))
        val beautifiedJson = store.state.value.raw

        beautifiedJson shouldContain "\n"
        compactJson shouldNotContain "\n"
    }

    // ── SortKeys ──

    @Test
    fun sortkeysAscendingReordersKeys() {
        val store = createStore(TestData.UNSORTED_KEYS, isEditing = true)
        store.dispatch(JsonAction.SortKeys(ascending = true))

        val obj = store.state.value.parsedJson as JsonNode.JObject
        obj.fields.map { it.first } shouldBe listOf("a", "b", "c")
    }

    @Test
    fun sortkeysDescendingReordersKeys() {
        val store = createStore(TestData.UNSORTED_KEYS, isEditing = true)
        store.dispatch(JsonAction.SortKeys(ascending = false))

        val obj = store.state.value.parsedJson as JsonNode.JObject
        obj.fields.map { it.first } shouldBe listOf("c", "b", "a")
    }

    @Test
    fun sortkeysUpdatesRawJson() {
        val store = createStore(TestData.UNSORTED_KEYS, isEditing = true)
        store.dispatch(JsonAction.SortKeys(ascending = true))
        val raw = store.state.value.raw

        val aIdx = raw.indexOf("\"a\"")
        val bIdx = raw.indexOf("\"b\"")
        val cIdx = raw.indexOf("\"c\"")

        (aIdx < bIdx).shouldBeTrue()
        (bIdx < cIdx).shouldBeTrue()
    }

    @Test
    fun sortkeysClearsError() {
        val store = createStore(TestData.UNSORTED_KEYS, isEditing = true)
        store.dispatch(JsonAction.SortKeys(ascending = true))

        store.state.value.error.shouldBeNull()
    }

    @Test
    fun sortkeysDoesNothingWhenNoParsedJson() {
        val store = createStore(TestData.INVALID_JSON, isEditing = true)
        val originalRaw = store.state.value.raw
        store.dispatch(JsonAction.SortKeys(ascending = true))

        store.state.value.raw shouldBe originalRaw
    }

    // ── CollapseAll / ExpandAll ──

    @Test
    fun collapseallFoldsAllFoldableLines() {
        val store = createStore(TestData.SIMPLE_OBJECT)
        val foldableIds = store.state.value.allLines.mapNotNull { it.foldId }

        store.dispatch(JsonAction.CollapseAll)

        foldableIds.forEach { id ->
            store.state.value.foldState[id] shouldBe true
        }
    }

    @Test
    fun expandallClearsFoldState() {
        val store = createStore(TestData.SIMPLE_OBJECT)

        store.dispatch(JsonAction.CollapseAll)
        store.state.value.foldState.isNotEmpty().shouldBeTrue()

        store.dispatch(JsonAction.ExpandAll)
        store.state.value.foldState.isEmpty().shouldBeTrue()
    }

    @Test
    fun collapseThenExpandRoundtrip() {
        val store = createStore(TestData.NESTED_OBJECT)

        store.dispatch(JsonAction.CollapseAll)
        store.state.value.foldState.isNotEmpty().shouldBeTrue()

        store.dispatch(JsonAction.ExpandAll)
        store.state.value.foldState.isEmpty().shouldBeTrue()
    }
}
