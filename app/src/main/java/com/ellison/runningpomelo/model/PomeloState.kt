package com.ellison.runningpomelo.model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class PomeloState(
    var pomeloHeight: Dp = DefaultPomeloHeightOffset,
    var isLifting: Boolean = false,
    var pomeloH: Dp = PomeloSizeHeight,
    var pomeloW: Dp = PomeloSizeWidth
) {
    fun lift(): PomeloState =
        copy(pomeloHeight = pomeloHeight - PomeloLiftVelocity, isLifting = true)

    fun fall(): PomeloState =
        copy(pomeloHeight = pomeloHeight + PomeloFallVelocity, isLifting = false)

    fun over(groundOffset: Dp): PomeloState =
        copy(pomeloHeight = groundOffset)

    fun quickFall(): PomeloState =
        copy(pomeloHeight = pomeloHeight + PomeloQuickFallVelocity)

    fun correct(): PomeloState =
        copy(pomeloH = PomeloSizeHeight, pomeloW = PomeloSizeWidth)
}

val DefaultPomeloHeightOffset = 0.dp

val HighPomeloHeightOffset = (-272).dp

val LowPomeloHeightOffset = 272.dp

// val PomeloSizeWidth = 72.dp // 48.dp // Control pomelo's size
// val PomeloSizeHeight = 50.dp // 48.dp // Control pomelo's size

// Pomelo height calculated from pipe distance.
const val PomeloPipeDistanceFraction = 0.30f
var PomeloSizeHeight = PipeDistance * PomeloPipeDistanceFraction
var PomeloSizeWidth = PomeloSizeHeight * 1.44f

// Need consider pomelo's height when calculating hit ground or not.
// val PomeloHitGroundThreshold = 0.dp
val PomeloHitGroundThreshold = PomeloSizeHeight / 2 // PomeloSizeHeight / 3

const val PomeloFallToGroundTimes = 20
var PomeloFallVelocity = 8.dp
var PomeloQuickFallVelocity = PomeloFallVelocity * 4

val PomeloLiftVelocity = PomeloFallVelocity * 8
val PomeloQuickLiftVelocity = PomeloLiftVelocity * 1.5f