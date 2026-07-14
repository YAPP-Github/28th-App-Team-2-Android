package com.kikidan.designsystem.component.wheelpicker

import android.graphics.Color
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.LocalTodakunColor
import com.kikidan.designsystem.theme.LocalTodakunTypography
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

    if (items.isEmpty()) {
        Box(modifier = modifier.height(WheelPickerDefaults.CenterContainerHeight * visibleCount))
        return
    }

    val colors = LocalTodakunColor.current
    val typography = LocalTodakunTypography.current

    val halfCount = visibleCount / 2
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    var editing by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf("") }
    var suppressNextSettle by remember { mutableStateOf(false) }
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
                if (suppressNextSettle) {
                    suppressNextSettle = false
                } else {
                    val idx = centeredIndex
                    if (idx in items.indices && idx != latestSelectedIndex) latestOnSelectedIndexChange(
                        idx
                    )
                }
            }
    }


    fun selectIndex(index: Int) {
        if (index !in items.indices || index == selectedIndex) return
        suppressNextSettle = true
        onSelectedIndexChange(index)
        scope.launch { listState.animateScrollToItem(index) }
    }

    fun startEdit() {
        editText = items.getOrElse(clampedSelectedIndex) { "" }
        hasGainedFocus = false
        editing = true
    }

    fun commitEdit() {
        if (!editing) return
        editing = false
        if (editText.isNotEmpty()) onDirectInputCommitted(editText)
    }

    LaunchedEffect(editing) { if (editing) focusRequester.requestFocus() }

    Box(
        modifier = modifier.height(WheelPickerDefaults.CenterContainerHeight * visibleCount),
        contentAlignment = Alignment.Center,
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = WheelPickerDefaults.CenterContainerHeight * halfCount),
            userScrollEnabled = !editing,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(items, key = { index, _ -> index }) { index, item ->
                val isCenter = index == centeredIndex
                val distance = abs(index - centeredIndex)
                val textStyle = when (distance) {
                    0 -> typography.body1Medium
                    1 -> typography.body1Regular
                    else -> typography.body1Regular
                }
                val textColor = when (distance) {
                    0 -> colors.black
                    1 -> colors.gray700
                    else -> colors.gray400
                }
                val itemHeight = WheelPickerDefaults.CenterContainerHeight

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .testTag("wheel_picker_item_$index")
                        .semantics { selected = isCenter }
                        .graphicsLayer {
                            val info = listState.layoutInfo
                            val viewportCenter =
                                (info.viewportStartOffset + info.viewportEndOffset) / 2f
                            val itemInfo = info.visibleItemsInfo.firstOrNull { it.index == index }
                            val itemPx = with(density) { itemHeight.toPx() }
                            val itemCenter =
                                itemInfo?.let { it.offset + it.size / 2f } ?: viewportCenter
                            val fraction = ((itemCenter - viewportCenter) / itemPx).coerceIn(
                                -halfCount.toFloat(), halfCount.toFloat()
                            )
                            val s = (1f - abs(fraction) * 0.08f).coerceIn(0.8f, 1f)
                            scaleX = s
                            scaleY = s
                            cameraDistance = 14f * this.density
                        }
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
            BasicTextField(
                value = editText,
                onValueChange = { new ->
                    editText = new.filter { it.isDigit() }.take(maxInputDigits)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            hasGainedFocus = true
                        } else if (hasGainedFocus) {
                            commitEdit()
                        }
                    },
                textStyle = typography.body1Medium.copy(
                    color = colors.primary600,
                    textAlign = TextAlign.Center,
                ),
                singleLine = true,
                cursorBrush = SolidColor(colors.primary600),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { commitEdit() }),
            )
        }
    }
}


