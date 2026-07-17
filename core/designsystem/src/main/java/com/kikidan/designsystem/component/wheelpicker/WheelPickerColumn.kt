package com.kikidan.designsystem.component.wheelpicker

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTypography
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun WheelPickerColumn(
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleCount: Int = 5,
    directInputEnabled: Boolean = false,
    maxInputDigits: Int = 2,
    onDirectInputCommitted: (rawDigits: String) -> Unit = {},
) {
    require(visibleCount % 2 == 1) { "visibleCount 는 홀수여야 합니다." }

    val halfCount = visibleCount / 2
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    var editing by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf("") }
    var hasGainedFocus by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val clampedSelectedIndex = selectedIndex.coerceIn(0, items.lastIndex)
    val latestSelectedIndex by rememberUpdatedState(selectedIndex)
    val latestOnSelectedIndexChange by rememberUpdatedState(onSelectedIndexChange)

    val centeredIndex by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            if (info.visibleItemsInfo.isEmpty()) {
                clampedSelectedIndex
            } else {
                val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2f
                info.visibleItemsInfo.minByOrNull { abs((it.offset + it.size / 2f) - viewportCenter) }?.index
                    ?: clampedSelectedIndex
            }
        }
    }

    LaunchedEffect(Unit) { listState.scrollToItem(clampedSelectedIndex) }

    LaunchedEffect(selectedIndex, items) {
        if (!listState.isScrollInProgress) {
            if (centeredIndex != clampedSelectedIndex) listState.animateScrollToItem(
                clampedSelectedIndex
            )
        }
    }

    LaunchedEffect(listState, editing) {
        snapshotFlow { listState.isScrollInProgress }.drop(1).distinctUntilChanged()
            .filter { inProgress -> !inProgress && !editing }.collect {
                val idx = centeredIndex
                if (idx in items.indices && idx != latestSelectedIndex) latestOnSelectedIndexChange(
                    idx
                )
            }
    }


    fun selectIndex(index: Int) {
        if (index !in items.indices || index == selectedIndex) return
        scope.launch { listState.animateScrollToItem(index) }
    }

    fun startEdit() {
        editText = items.getOrElse(clampedSelectedIndex) { "" }
        hasGainedFocus = false
        editing = true
    }

    fun commitEdit(value: String) {
        if (!editing) return
        editing = false
        if (editText.isNotEmpty()) onDirectInputCommitted(value)
    }

    LaunchedEffect(editing) { if (editing) focusRequester.requestFocus() }

    // translationY로 밀려 들어간 만큼(위+아래) 바깥 레이아웃 높이에서 빼서, 시각적으로 안 쓰는
    // 여백을 없앤다. 단, LazyColumn 자체는 fullHeight로 그대로 측정해야 뷰포트 기반의
    // fraction/scale/translationY 계산(listState.layoutInfo)이 안 틀어진다.
    val fullHeight = WheelPickerDefaults.CenterContainerHeight * visibleCount
    val adjustedHeight = fullHeight -
            WheelPickerDefaults.CenterContainerHeight * (halfCount * WheelPickerDefaults.CENTER_FULL_FACTOR * 2)

    Box(
        modifier = modifier
            .height(adjustedHeight)
            .clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = WheelPickerDefaults.CenterContainerHeight * halfCount),
            userScrollEnabled = !editing,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .requiredHeight(fullHeight)
                .fillMaxSize(),
        ) {
            itemsIndexed(items, key = { index, _ -> index }) { index, item ->
                val isCenter = index == centeredIndex
                val distance = abs(index - centeredIndex)
                val textStyle = when (distance) {
                    0 -> TodakunTypography.body1Medium
                    1 -> TodakunTypography.body1Regular
                    else -> TodakunTypography.body1Regular
                }
                val textColor = when (distance) {
                    0 -> TodakunColor.black
                    1 -> TodakunColor.gray700
                    else -> TodakunColor.gray400
                }
                val itemHeight = WheelPickerDefaults.CenterContainerHeight

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .semantics { selected = isCenter }
                        .wheelPickerGraphics(listState, halfCount, index, itemHeight)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            if (isCenter) {
                                if (directInputEnabled) startEdit()
                            } else {
                                selectIndex(index)
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (!(editing && isCenter)) {
                        Text(text = item, style = textStyle, color = textColor)
                    }
                }
            }
        }

        if (editing) {
            EditTextField(
                maxInputDigits = maxInputDigits,
                placeHolder = editText,
                focusRequester = focusRequester,
                hasGainedFocus = hasGainedFocus,
                onChangeGainedFocus = { hasGainedFocus = it },
                onCommitEdit = { commitEdit(it) }
            )
        }
    }

}

@SuppressLint("RememberReturnType")
@Composable
private fun EditTextField(
    placeHolder: String,
    maxInputDigits: Int,
    hasGainedFocus: Boolean,
    focusRequester: FocusRequester,
    onChangeGainedFocus: (Boolean) -> Unit,
    onCommitEdit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEdited by remember { mutableStateOf(false) }
    var value by remember { mutableStateOf("") }

    BasicTextField(
        value = value,
        onValueChange = { new ->
            isEdited = true
            value = new.filter { it.isDigit() }.take(maxInputDigits)
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    onChangeGainedFocus(true)
                } else if (hasGainedFocus) {
                    onCommitEdit(value)
                }
            },
        textStyle = TodakunTypography.body1Medium.copy(
            color = TodakunColor.primary600,
            textAlign = TextAlign.Center,
        ),
        singleLine = true,
        cursorBrush = SolidColor(TodakunColor.primary600),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onCommitEdit(value) }),
        decorationBox = { innerTextField ->
            Box {
                if (!isEdited) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = placeHolder,
                        style = TodakunTypography.body1Medium.copy(
                            color = TodakunColor.primary600,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
                innerTextField()
            }
        }
    )
}

private fun Modifier.wheelPickerGraphics(
    listState: LazyListState,
    halfCount: Int,
    index: Int,
    itemHeight: Dp
): Modifier =
    graphicsLayer {
        val info = listState.layoutInfo
        val viewportCenter =
            (info.viewportStartOffset + info.viewportEndOffset) / 2f
        val itemInfo =
            info.visibleItemsInfo.firstOrNull { it.index == index }
        val itemPx = with(density) { itemHeight.toPx() }
        val itemCenter =
            itemInfo?.let { it.offset + it.size / 2f } ?: viewportCenter
        val fraction = ((itemCenter - viewportCenter) / itemPx).coerceIn(
            -halfCount.toFloat(), halfCount.toFloat()
        )
        val s = (1f - abs(fraction) * 0.08f).coerceIn(0.8f, 1f)
        scaleX = s
        scaleY = s
        translationY =
            -fraction * itemPx * WheelPickerDefaults.CENTER_FULL_FACTOR
        cameraDistance = 14f * this.density
    }


@Immutable
data class WheelPickerColumnState(
    val items: List<String>,
    val selectedIndex: Int,
    val width: Dp = Dp.Unspecified,
    val maxInputDigits: Int = 2,
)



