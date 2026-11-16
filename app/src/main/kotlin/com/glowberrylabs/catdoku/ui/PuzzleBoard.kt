package com.glowberrylabs.catdoku.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glowberrylabs.catdoku.R
import com.glowberrylabs.catdoku.game.AutoMarkResolver
import com.glowberrylabs.catdoku.game.CellMark
import com.glowberrylabs.catdoku.game.Position
import com.glowberrylabs.catdoku.game.Puzzle
import com.glowberrylabs.catdoku.game.PuzzleState

@Composable
fun PuzzleBoard(
    state: PuzzleState,
    autoMarkEnabled: Boolean,
    regionLettersEnabled: Boolean,
    onTap: (Position) -> Unit,
    onDoubleTap: (Position) -> Unit,
    modifier: Modifier = Modifier
) {
    val puzzle = state.puzzle
    val autoMarks = remember(state.marks, autoMarkEnabled) {
        if (autoMarkEnabled) AutoMarkResolver.autoMarks(state) else emptySet()
    }
    val labelCells = remember(puzzle) { regionLabelCells(puzzle) }
    val transition = rememberInfiniteTransition(label = "hint")
    val hintAlpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hintAlpha"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCells(puzzle)
            drawRegionBoundaries(puzzle)
        }
        Column(modifier = Modifier.fillMaxSize()) {
            for (row in 0 until puzzle.size) {
                Row(modifier = Modifier.weight(1f)) {
                    for (col in 0 until puzzle.size) {
                        val position = Position(row, col)
                        val index = pencilIndex(puzzle, position)
                        Cell(
                            state = state,
                            position = position,
                            isAutoMarked = index in autoMarks,
                            regionLettersEnabled = regionLettersEnabled,
                            showRegionLetter = index in labelCells,
                            hintAlpha = hintAlpha,
                            onTap = onTap,
                            onDoubleTap = onDoubleTap,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Cell(
    state: PuzzleState,
    position: Position,
    isAutoMarked: Boolean,
    regionLettersEnabled: Boolean,
    showRegionLetter: Boolean,
    hintAlpha: Float,
    onTap: (Position) -> Unit,
    onDoubleTap: (Position) -> Unit,
    modifier: Modifier = Modifier
) {
    val mark = state.markAt(position)
    val region = state.puzzle.regionAt(position.row, position.col)
    val regionLetter = ('A' + region).toString()
    val toggleNoteLabel = stringResource(R.string.toggle_note)
    val placeCatLabel = stringResource(R.string.place_cat)
    val stateLabel = stringResource(
        when {
            mark == CellMark.CAT -> R.string.cell_cat
            mark == CellMark.NOTE -> R.string.cell_note
            mark == CellMark.MISTAKE -> R.string.cell_mistake
            isAutoMarked -> R.string.cell_excluded
            else -> R.string.cell_empty
        }
    )
    val description = stringResource(
        R.string.cell_description,
        position.row + 1,
        position.col + 1,
        regionLetter,
        stateLabel
    )
    val isHint = state.hintCell == position

    Box(
        modifier = modifier
            .combinedClickable(
                onClickLabel = toggleNoteLabel,
                onClick = { onTap(position) },
                onDoubleClick = { onDoubleTap(position) }
            )
            .semantics {
                contentDescription = description
                customActions = listOf(
                    CustomAccessibilityAction(placeCatLabel) {
                        onDoubleTap(position)
                        true
                    },
                    CustomAccessibilityAction(toggleNoteLabel) {
                        onTap(position)
                        true
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        CellContent(
            mark = mark,
            isAutoMarked = isAutoMarked,
            regionLettersEnabled = regionLettersEnabled,
            showRegionLetter = showRegionLetter,
            regionLetter = regionLetter,
            isHint = isHint,
            hintAlpha = hintAlpha
        )
    }
}

@Composable
private fun CellContent(
    mark: CellMark,
    isAutoMarked: Boolean,
    regionLettersEnabled: Boolean,
    showRegionLetter: Boolean,
    regionLetter: String,
    isHint: Boolean,
    hintAlpha: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
            .then(
                if (isHint) {
                    Modifier.border(
                        width = 2.dp,
                        color = RegionPalette.gold.copy(alpha = hintAlpha),
                        shape = RoundedCornerShape(14.dp)
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (regionLettersEnabled && showRegionLetter) {
            RegionLetter(
                letter = regionLetter,
                modifier = Modifier.align(Alignment.TopStart)
            )
        }
        CatMark(visible = mark == CellMark.CAT)
        NoteMark(mark = mark, isAutoMarked = isAutoMarked)
    }
}

@Composable
private fun RegionLetter(letter: String, modifier: Modifier = Modifier) {
    Text(
        text = letter,
        modifier = modifier
            .padding(start = 4.dp, top = 2.dp)
            .clearAndSetSemantics { },
        color = RegionPalette.ink.copy(alpha = 0.75f),
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
private fun CatMark(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(120)) +
            scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                initialScale = 0.4f
            ),
        exit = fadeOut(animationSpec = tween(100)) +
            scaleOut(animationSpec = tween(100), targetScale = 0.5f)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_cat),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(0.76f)
        )
    }
}

@Composable
private fun NoteMark(mark: CellMark, isAutoMarked: Boolean) {
    AnimatedVisibility(
        visible = mark == CellMark.NOTE || mark == CellMark.MISTAKE || isAutoMarked,
        enter = fadeIn(animationSpec = tween(120)) +
            scaleIn(animationSpec = tween(120), initialScale = 0.7f),
        exit = fadeOut(animationSpec = tween(90)) +
            scaleOut(animationSpec = tween(90), targetScale = 0.7f)
    ) {
        XMark(
            color = when {
                mark == CellMark.MISTAKE -> RegionPalette.coral
                mark == CellMark.NOTE -> RegionPalette.ink.copy(alpha = 0.7f)
                else -> RegionPalette.ink.copy(alpha = 0.28f)
            },
            heavy = mark == CellMark.MISTAKE,
            modifier = Modifier.fillMaxSize(0.54f)
        )
    }
}

@Composable
private fun XMark(color: Color, heavy: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val inset = size.minDimension * 0.2f
        val stroke = size.minDimension * if (heavy) 0.12f else 0.08f
        drawLine(
            color = color,
            start = Offset(inset, inset),
            end = Offset(size.width - inset, size.height - inset),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width - inset, inset),
            end = Offset(inset, size.height - inset),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawCells(puzzle: Puzzle) {
    val cell = size.width / puzzle.size
    val inset = cell * 0.04f
    val corner = CornerRadius(cell * 0.16f)
    for (row in 0 until puzzle.size) {
        for (col in 0 until puzzle.size) {
            drawRoundRect(
                color = RegionPalette.region(puzzle.regionAt(row, col)),
                topLeft = Offset(col * cell + inset, row * cell + inset),
                size = Size(cell - inset * 2f, cell - inset * 2f),
                cornerRadius = corner
            )
        }
    }
}

private fun DrawScope.drawRegionBoundaries(puzzle: Puzzle) {
    val cell = size.width / puzzle.size
    val stroke = cell * 0.045f
    val color = RegionPalette.backgroundTop.copy(alpha = 0.78f)
    for (row in 0 until puzzle.size) {
        for (col in 0 until puzzle.size - 1) {
            if (puzzle.regionAt(row, col) != puzzle.regionAt(row, col + 1)) {
                val x = (col + 1) * cell
                drawLine(
                    color = color,
                    start = Offset(x, row * cell),
                    end = Offset(x, (row + 1) * cell),
                    strokeWidth = stroke
                )
            }
        }
    }
    for (row in 0 until puzzle.size - 1) {
        for (col in 0 until puzzle.size) {
            if (puzzle.regionAt(row, col) != puzzle.regionAt(row + 1, col)) {
                val y = (row + 1) * cell
                drawLine(
                    color = color,
                    start = Offset(col * cell, y),
                    end = Offset((col + 1) * cell, y),
                    strokeWidth = stroke
                )
            }
        }
    }
}

private fun regionLabelCells(puzzle: Puzzle): Set<Int> {
    val firstCell = IntArray(puzzle.size) { -1 }
    puzzle.regions.forEachIndexed { index, region ->
        if (firstCell[region] == -1) {
            firstCell[region] = index
        }
    }
    return firstCell.filter { it >= 0 }.toSet()
}

private fun pencilIndex(puzzle: Puzzle, position: Position): Int = position.row * puzzle.size + position.col
