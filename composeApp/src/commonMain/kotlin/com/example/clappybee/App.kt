package com.example.clappybee

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import clappybee.composeapp.generated.resources.Res
import clappybee.composeapp.generated.resources.background
import clappybee.composeapp.generated.resources.bee_sprite
import clappybee.composeapp.generated.resources.moving_background
import clappybee.composeapp.generated.resources.pipe
import clappybee.composeapp.generated.resources.pipe_cap
import com.example.clappybee.domain.Game
import com.example.clappybee.domain.GameStatus
import com.example.clappybee.ui.orange
import com.example.clappybee.util.ChewyFontFamily
import com.example.clappybee.util.Platform
import com.example.clappybee.util.getPlatform
import com.stevdza_san.sprite.component.drawSpriteView
import com.stevdza_san.sprite.domain.SpriteSheet
import com.stevdza_san.sprite.domain.SpriteSpec
import com.stevdza_san.sprite.domain.rememberSpriteState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.PlayArrow

const val BEE_FRAME_SIZE = 80
const val PIPE_CAP_HEIGHT = 50F

@Composable
@Preview
fun App() {
    MaterialTheme {

        val platform = remember { getPlatform() }
        var screenWidth by remember { mutableStateOf(0) }
        var screenHeight by remember { mutableStateOf(0) }
        var game by remember { mutableStateOf(Game(platform = platform)) }

        val spriteState = rememberSpriteState(
            totalFrames = 9,
            framesPerRow = 3
        )

        val spriteSpec = remember {
            SpriteSpec(
                screenWidth = screenWidth.toFloat(),
                default = SpriteSheet(
                    frameWidth = BEE_FRAME_SIZE,
                    frameHeight = BEE_FRAME_SIZE,
                    image = Res.drawable.bee_sprite
                )
            )
        }

        val currentFrame by spriteState.currentFrame.collectAsState()
        val sheetImage = spriteSpec.imageBitmap

        val animatedAngle by animateFloatAsState(
            targetValue = when {
                game.beeVelocity > game.beeMaxVelocity / 1.1 -> 30f
                else -> 0f
            }
        )

        DisposableEffect(Unit) {
            onDispose {
                spriteState.stop()
                spriteState.cleanup()
                game.cleanup()
            }
        }

        LaunchedEffect(game.status) {
            while(game.status == GameStatus.Started) {
                withFrameMillis {
                    game.updateGameProgress()
                }
            }
            if (game.status == GameStatus.Over) {
                spriteState.stop()
            }
        }

        val scope = rememberCoroutineScope()
        val backgroundOffsetX = remember { Animatable(0f) }
        var imageWidth by remember { mutableStateOf(0) }
        val pipeImage = imageResource(Res.drawable.pipe)
        val pipeCapImage = imageResource(Res.drawable.pipe_cap)

        LaunchedEffect(game.status) {
            while(game.status == GameStatus.Started) {
                backgroundOffsetX.animateTo(
                    targetValue = -imageWidth.toFloat(),
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = when(platform) {
                                Platform.Android -> 4000
                                Platform.iOS -> 4000
                                Platform.Web -> 8000
                                Platform.Desktop -> 7000
                            },
                        ),
                        repeatMode = RepeatMode.Restart
                    )
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(Res.drawable.background),
                contentDescription = "Background image",
                contentScale = ContentScale.Crop
            )
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged {
                        imageWidth = it.width
                    }
                    .offset {
                        IntOffset(
                            x = backgroundOffsetX.value.toInt(),
                            y = 0
                        )
                    },
                painter = painterResource(Res.drawable.moving_background),
                contentDescription = "Moving Background image",
                contentScale = ContentScale.FillHeight
            )
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        IntOffset(
                            x = backgroundOffsetX.value.toInt() + imageWidth,
                            y = 0
                        )
                    },
                painter = painterResource(Res.drawable.moving_background),
                contentDescription = "Moving Background image",
                contentScale = ContentScale.FillHeight
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    val size = it.size
                    if(screenWidth != size.width || screenHeight != size.height) {
                        screenWidth = size.width
                        screenHeight = size.height
                        game = game.copy(
                            screenWidth = size.width,
                            screenHeight = size.height
                        )
                    }
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if(game.status == GameStatus.Started) {
                        game.jump()
                    }
                }
        ) {
            rotate(
                degrees = animatedAngle,
                pivot = Offset(
                    x = game.bee.x - game.beeRadius,
                    y = game.bee.y - game.beeRadius
                )
            ) {
                drawSpriteView(
                    spriteState = spriteState,
                    spriteSpec = spriteSpec,
                    currentFrame = currentFrame,
                    image = sheetImage,
                    offset = IntOffset(
                        x = (game.bee.x- game.beeRadius).toInt(),
                        y = (game.bee.y - game.beeRadius).toInt()
                    )
                )
            }
            game.pipePairs.forEach { pipePair ->
//                drawRect(
//                    color = Color.Blue,
//                    topLeft = Offset(
//                        x = pipePair.x - game.pipeWidth / 2,
//                        y = 0f
//                    ),
//                    size = Size(game.pipeWidth, pipePair.topHeight)
//                )
//                drawRect(
//                    color = Color.Blue,
//                    topLeft = Offset(
//                        x = pipePair.x - game.pipeWidth / 2,
//                        y = pipePair.y + game.pipeGapSize / 2
//                    ),
//                    size = Size(game.pipeWidth, pipePair.bottomHeight)
//                )
                drawImage(
                    image = pipeImage,
                    dstOffset = IntOffset(
                        x = (pipePair.x - (game.pipeWidth / 2)).toInt(),
                        y = 0
                    ),
                    dstSize = IntSize(
                        width = game.pipeWidth.toInt(),
                        height = (pipePair.topHeight - PIPE_CAP_HEIGHT).toInt()
                    )
                )
                drawImage(
                    image = pipeCapImage,
                    dstOffset = IntOffset(
                        x = (pipePair.x - game.pipeWidth / 2).toInt(),
                        y = (pipePair.topHeight - PIPE_CAP_HEIGHT).toInt()
                    ),
                    dstSize = IntSize(
                        width = game.pipeWidth.toInt(),
                        height = PIPE_CAP_HEIGHT.toInt()
                    )
                )
                drawImage(
                    image = pipeCapImage,
                    dstOffset = IntOffset(
                        x = (pipePair.x - game.pipeWidth / 2).toInt(),
                        y = (pipePair.y + game.pipeGapSize / 2).toInt()
                    ),
                    dstSize = IntSize(
                        width = game.pipeWidth.toInt(),
                        height = PIPE_CAP_HEIGHT.toInt()
                    )
                )
                drawImage(
                    image = pipeImage,
                    dstOffset = IntOffset(
                        x = (pipePair.x - (game.pipeWidth / 2)).toInt(),
                        y = (pipePair.y + game.pipeGapSize / 2 + PIPE_CAP_HEIGHT).toInt()
                    ),
                    dstSize = IntSize(
                        width = game.pipeWidth.toInt(),
                        height = (pipePair.bottomHeight - PIPE_CAP_HEIGHT).toInt()
                    )
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "BEST: ${game.bestScore}",
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.displaySmall.fontSize,
                fontFamily = ChewyFontFamily()
            )
            Text(
                text = "${game.currentScore}",
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.displaySmall.fontSize,
                fontFamily = ChewyFontFamily()
            )
        }

        if (game.status == GameStatus.Idle) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    modifier = Modifier.height(54.dp),
                    shape = RoundedCornerShape(size = 20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = orange
                    ),
                    onClick = {
                        game.start()
                        spriteState.start()
                    }) {
//                    Icon(
//                        imageVector = Icons.Default.PlayArrow,
//                        contentDescription = null,
//                        tint = Color.White
//                    )
//                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "START")
                }
            }
        }

        if (game.status == GameStatus.Over) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Black.copy(alpha = 0.5f)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Game Over!",
                    color = Color.White,
                    fontSize = MaterialTheme.typography.displayMedium.fontSize,
                    fontFamily =  ChewyFontFamily()
                )
                Text(
                    text = "SCORE: ${game.currentScore}",
                    color = Color.White,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontFamily =  ChewyFontFamily()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    modifier = Modifier.height(54.dp),
                    shape = RoundedCornerShape(size = 20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = orange
                    ), onClick = {
                        game.restart()
                        spriteState.start()
                        scope.launch {
                            backgroundOffsetX.snapTo(0f)
                        }
                    }
                ) {
                    Text(text = "RESTART")
                }
            }
        }

    }
}