package com.ellison.runningpomelo.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ellison.runningpomelo.R
import com.ellison.runningpomelo.model.*
import com.ellison.runningpomelo.util.LogUtil
import com.ellison.runningpomelo.viewmodel.GameViewModel

@Composable
fun Pomelo(
    modifier: Modifier = Modifier,
    state: ViewState = ViewState()
) {
    LogUtil.printLog(message = "Pomelo()")
    val viewModel: GameViewModel = viewModel()

    // Rotate 90 degree when quick falling / dying.
    val rotateDegree =
        if (state.isLifting) LiftingDegree
        else if (state.isFalling) FallingDegree
        else if (state.isQuickFalling) DyingDegree
        else if (state.isOver) DeadDegree
        else PendingDegree

    Box(
        modifier
    ) {
        var correctPomeloHeight = state.pomeloState.pomeloHeight

        if (state.playZoneSize.second > 0) {
            val playZoneHeightInDP = with(LocalDensity.current) {
                state.playZoneSize.second.toDp()
            }

            LogUtil.printLog(message = "Zone height:$playZoneHeightInDP  pomelo offset:${state.pomeloState.pomeloHeight}")
            val fallingThreshold = PomeloHitGroundThreshold

            if (correctPomeloHeight + fallingThreshold >= playZoneHeightInDP / 2) {
                // Send hit to ground action.
                LogUtil.printLog(message = "Pomelo hit ground")

                viewModel.dispatch(GameAction.HitGround)

                // Make sure pomelo not fall over ground.
                correctPomeloHeight = playZoneHeightInDP / 2 - fallingThreshold
            }
        }

        Image(
            painter = painterResource(id = R.drawable.pomelo_match),
            contentScale = ContentScale.FillBounds,
            contentDescription = null,
            modifier = Modifier
                .size(state.pomeloState.pomeloW, state.pomeloState.pomeloH)
                .align(Alignment.Center)
                .offset(y = correctPomeloHeight)
                .rotate(rotateDegree)  // Rotate 90 degree when dying/ over.
        )
    }
}

@Preview(widthDp = 411, heightDp = 660)
@Composable
fun PreviewPomelo() {
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.pomelo_match),
            contentScale = ContentScale.FillBounds,
            contentDescription = null,
            modifier = Modifier
                .size(PomeloSizeWidth, PomeloSizeHeight)
                .align(Alignment.Center)
                .offset(y = DefaultPomeloHeightOffset)
        )
    }
}

const val PendingDegree = 0f
const val LiftingDegree = -10f
const val FallingDegree = -LiftingDegree
const val DyingDegree = FallingDegree + 10f
const val DeadDegree = DyingDegree - 10f