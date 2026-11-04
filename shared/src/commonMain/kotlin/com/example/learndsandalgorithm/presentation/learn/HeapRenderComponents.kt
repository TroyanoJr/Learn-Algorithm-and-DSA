package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val HeapCardBorderColor = Color(0xFF262634)
internal val HeapOrangeAccent = Color(0xFFFA6E13)
internal val HeapBlueAccent = Color(0xFF38BDF8)
internal val HeapGreenAccent = Color(0xFF10B981)
internal val HeapRedAccent = Color(0xFFEF4444)
internal val HeapTextPrimary = Color(0xFFFFFFFF)
internal val HeapTextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun HeapTreeNode(
    value: Int,
    index: Int,
    isHighlighted: Boolean = false,
    isParent: Boolean = false,
    isSwapped: Boolean = false,
    nodeSize: Dp = 40.dp
) {
    val bgColor = when {
        isSwapped -> Color(0xFF2A1C12)
        isHighlighted -> Color(0xFF2A1C12)
        isParent -> Color(0xFF122838)
        else -> Color(0xFF13233A)
    }

    val borderColor = when {
        isSwapped -> HeapOrangeAccent
        isHighlighted -> HeapOrangeAccent
        isParent -> HeapBlueAccent
        else -> HeapCardBorderColor
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "[$index]",
            color = if (isHighlighted) HeapOrangeAccent else HeapTextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )

        Surface(
            color = bgColor,
            shape = CircleShape,
            border = BorderStroke(1.5.dp, borderColor),
            modifier = Modifier.size(nodeSize)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$value",
                    color = if (isHighlighted || isSwapped) HeapOrangeAccent else HeapTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
internal fun HeapArrayRow(
    array: List<Int>,
    highlightedIndex: Int? = null,
    parentIndex: Int? = null,
    swappedIndices: Pair<Int, Int>? = null,
    heapSize: Int = array.size
) {
    Surface(
        color = Color(0xFF13131C),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, HeapCardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "ARRAY REPRESENTATION (Indices 0..${array.lastIndex}):",
                color = HeapBlueAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                array.forEachIndexed { idx, value ->
                    val isHighlighted = idx == highlightedIndex
                    val isParent = idx == parentIndex
                    val isSwapped = swappedIndices != null && (idx == swappedIndices.first || idx == swappedIndices.second)
                    val isInHeap = idx < heapSize

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                    ) {
                        Surface(
                            color = when {
                                isSwapped -> Color(0xFF2A1C12)
                                isHighlighted -> Color(0xFF2A1C12)
                                isParent -> Color(0xFF122838)
                                isInHeap -> Color(0xFF13233A)
                                else -> Color(0xFF13131C)
                            },
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isSwapped -> HeapOrangeAccent
                                    isHighlighted -> HeapOrangeAccent
                                    isParent -> HeapBlueAccent
                                    isInHeap -> HeapBlueAccent
                                    else -> HeapCardBorderColor
                                }
                            ),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$value",
                                    color = if (!isInHeap) HeapTextSecondary.copy(alpha = 0.4f) else if (isHighlighted || isSwapped) HeapOrangeAccent else HeapTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "[$idx]",
                            color = if (isHighlighted) HeapOrangeAccent else HeapTextSecondary,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}
