package com.ellison.runningpomelo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ellison.runningpomelo.model.GameAction
import com.ellison.runningpomelo.model.GameStatus
import com.ellison.runningpomelo.ui.theme.RunningPomeloTheme
import com.ellison.runningpomelo.util.SplashScreenController
import com.ellison.runningpomelo.util.StatusBarUtil
import com.ellison.runningpomelo.view.Clickable
import com.ellison.runningpomelo.view.GameScreen
import com.ellison.runningpomelo.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    // `@RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Need to be called before setContentView or other view operation on the root view.
        val splashScreen = installSplashScreen()

        // Expand screen to status bar.
        StatusBarUtil.transparentStatusBar(this)

        setContent {
            RunningPomeloTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    val gameViewModel: GameViewModel = viewModel()

                    // Send a auto tick action to view model and trigger game start.
                    LaunchedEffect(key1 = Unit) {
                        while (isActive) {
                            delay(AutoTickDuration)
                            if (gameViewModel.viewState.value.gameStatus != GameStatus.Waiting) {
                                gameViewModel.dispatch(GameAction.AutoTick)
                            }
                        }
                    }

                    val lifecycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(key1 = Unit) {
                        val observer = object : LifecycleObserver {
                            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                            fun onPause() {
                                // Todo send pause action
                            }

                            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
                            fun onResume() {
                                // Todo send resume action
                            }
                        }

                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    Running(Clickable(

                        onStart = {
                            gameViewModel.dispatch(GameAction.Start)
                        },

                        onTap = {
                            gameViewModel.dispatch(GameAction.TouchLift)
                        },

                        onRestart = {
                            gameViewModel.dispatch(GameAction.Restart)
                        },

                        onExit = {
                            finish()
                        }
                    ))
                }
            }
        }

        SplashScreenController(splashScreen, viewModel).apply {
            customizeSplashScreen()
        }

        // Log.d("Splash", "onCreate() splashScreen:${getSplashScreen()}}")
    }
}

@Composable
fun Running(clickable: Clickable = Clickable()) {
    GameScreen(clickable = clickable)
}

const val AutoTickDuration = 51L // 300L Control pomelo and pipe speed.