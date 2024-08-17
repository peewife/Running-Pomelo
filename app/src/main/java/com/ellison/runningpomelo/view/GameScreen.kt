package com.ellison.runningpomelo.view

import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ellison.runningpomelo.*
import com.ellison.runningpomelo.model.*
import com.ellison.runningpomelo.ui.theme.ForegroundEarthYellow
import com.ellison.runningpomelo.util.LogUtil
import com.ellison.runningpomelo.viewmodel.GameViewModel

@Composable
fun GameScreen(
    clickable: Clickable = Clickable()
) {
    LogUtil.printLog(message = "GameScreen()")
    val viewModel: GameViewModel = viewModel()
    val viewState by viewModel.viewState.collectAsState()
    var screenSize: Pair<Int, Int>

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ForegroundEarthYellow)
            // .background(DefaultBlackBackground)
            .run {
                pointerInteropFilter {
                    // Send lift action to let pomelo up.
                    // Todo only work when running status.
                    when (it.action) {
                        ACTION_DOWN -> {
                            LogUtil.printLog(message = "GameScreen ACTION_DOWN status:${viewState.gameStatus}")
                            if (viewState.gameStatus == GameStatus.Waiting) clickable.onStart()
                            else if (viewState.gameStatus == GameStatus.Running) clickable.onTap()
                            else return@pointerInteropFilter false
                        }

                        // Todo send quick lift when long tap event.
                        MotionEvent.ACTION_MOVE -> {
                            LogUtil.printLog(message = "GameScreen ACTION_MOVE")
                            return@pointerInteropFilter false
                        }

                        MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                            LogUtil.printLog(message = "GameScreen ACTION_CANCEL/UP")
                            return@pointerInteropFilter false
                        }
                    }

                    false
                }
            }
    ) {

        Box(modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .onGloballyPositioned {
                screenSize = Pair(it.size.width, it.size.height)
                LogUtil.printLog(
                    message = "Screen size:[${screenSize.first},${screenSize.second}]\n" +
                            "Zone size:[${viewState.playZoneSize.first},${viewState.playZoneSize.second}]"
                )

                if (viewState.playZoneSize.first <= 0 || viewState.playZoneSize.second <= 0) {
                    LogUtil.printLog(message = "Screen size first detect.")
                    viewModel.dispatch(GameAction.ScreenSizeDetect, screenSize)
                }
            }
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
        ) {
            LogUtil.printLog(message = "GameScreen Box Lambda")

            // Far background
            FarBackground(Modifier.fillMaxSize())

            // Put two group pipes
            PipeCouple(
                modifier = Modifier.fillMaxSize(),
                state = viewState,
                pipeIndex = 0
            )

            PipeCouple(
                modifier = Modifier.fillMaxSize(),
                state = viewState,
                pipeIndex = 1
            )

            // Put real time or over score board
            ScoreBoard(
                modifier = Modifier.fillMaxSize(),
                state = viewState,
                clickable = clickable
            )

            val playZoneWidthInDP = with(LocalDensity.current) {
                viewState.playZoneSize.first.toDp()
            }

            val playZoneHeightInDP = with(LocalDensity.current) {
                viewState.playZoneSize.second.toDp()
            }

            LogUtil.printLog(message = "Zone:[$playZoneWidthInDP,$playZoneHeightInDP]\n" +
                    "first pipe:${viewState.pipeStateList[0]}\n" +
                    "second pipe:${viewState.pipeStateList[1]}\n" +
                    "pomelo height offset:${viewState.pomeloState.pomeloHeight}"
            )

            // Check hit first or second pipe.
            // And send hit pipe action if pomelo hit any pipe.
            if (viewState.gameStatus == GameStatus.Running) {
                viewState.pipeStateList.forEachIndexed { pipeIndex, pipeState ->
                    CheckPipeStatus(
                        viewState.pomeloState.pomeloHeight,
                        pipeState,
                        playZoneWidthInDP,
                        playZoneHeightInDP
                    ).also {
                        when (it) {
                            PipeStatus.PomeloHit -> {
                                LogUtil.printLog(message = "Send hit pipe action")
                                viewModel.dispatch(GameAction.HitPipe)
                            }

                            PipeStatus.PomeloCrossed -> {
                                LogUtil.printLog(message = "Send crossed pipe action with index:$pipeIndex")
                                viewModel.dispatch(GameAction.CrossedPipe, pipeIndex = pipeIndex)
                            }

                            // No care other status.
                            else -> {}
                        }
                    }
                }
            }

            // Put pomelo
            Pomelo(
                modifier = Modifier.fillMaxSize(),
                state = viewState
            )
        }

        Box(modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .fillMaxWidth()
            .fillMaxHeight(1f)
        ) {
            NearForeground(
                modifier = Modifier.fillMaxSize(),
                state = viewState
            )
        }
    }
}

@Composable
fun CheckPipeStatus(pomeloHeightOffset: Dp, pipeState: PipeState, zoneWidth: Dp, zoneHeight: Dp): PipeStatus {
    LogUtil.printLog(message = "CheckPipeStatus()")

    if (pipeState.offset - PipeCoverWidth > - zoneWidth / 2 + PomeloSizeWidth / 2) {
        LogUtil.printLog(message = "Pomelo is coming")
        return PipeStatus.PomeloComing
    } else if (pipeState.offset - PipeCoverWidth < - zoneWidth / 2 - PomeloSizeWidth / 2) {
        LogUtil.printLog(message = "Pomelo crossed successfully")
        return PipeStatus.PomeloCrossed
    } else {
        val pomeloTop = (zoneHeight - PomeloSizeHeight) / 2 + pomeloHeightOffset
        val pomeloBottom = (zoneHeight + PomeloSizeHeight) / 2 + pomeloHeightOffset

        if (pomeloTop < pipeState.upHeight || pomeloBottom > zoneHeight - pipeState.downHeight) {
            LogUtil.printLog(message = "Pomelo hit unluckily")
            return PipeStatus.PomeloHit
        }

        LogUtil.printLog(message = "Pomelo still crossing, good luck.")
        return PipeStatus.PomeloCrossing
    }
 }

@Preview(widthDp = 411, heightDp = 840)
@Composable
fun PreviewGameScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ForegroundEarthYellow)
    ) {

        Box(modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
        ) {
            LogUtil.printLog(message = "GameScreen Box Lambda")

            // Far background
            FarBackground(Modifier.fillMaxSize())

            PipeScreen()

            PreviewPomelo()

            RealTimeBoardPreview()
        }

        previewForeground()
    }
}

data class Clickable(
    val onStart: () -> Unit = {},
    val onTap: () -> Unit = {},
    val onRestart: () -> Unit = {},
    val onExit: () -> Unit = {}
)