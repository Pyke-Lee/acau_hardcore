package kr.pyke.acau_hardcore.client.gui.screen.raid

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.aperso.composite.core.ComposeScreen
import kr.pyke.acau_hardcore.network.payload.c2s.C2S_RaidReadyPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.Minecraft

object BossRaidReadyScreen {
    private val ColorBG = Color(0xFF161A22)
    private val ColorCard = Color(0xFF1E232E)
    private val ColorBorder = Color(0xFF3E4C66)
    private val ColorTextMain = Color(0xFFE0E0E0)
    private val ColorTextSub = Color(0xFF8B9BB4)
    private val ColorGreen = Color(0xFF55FF55)
    private val ColorRed = Color(0xFFFF5555)
    private val ColorYellow = Color(0xFFFFFF55)

    @JvmStatic
    fun create(raidTypeName: String, isInitiator: Boolean): ComposeScreen {
        return ComposeScreen {
            LaunchedEffect(Unit) {
                Minecraft.getInstance().mouseHandler.releaseMouse()
            }

            val readyMembers = RaidReadyState.members
            val secondsLeft = RaidReadyState.secondsLeft.intValue

            val allReady = readyMembers.isNotEmpty() && readyMembers.all { it.ready }
            LaunchedEffect(allReady) {
                if (allReady) {
                    Minecraft.getInstance().setScreen(null)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.width(420.dp).wrapContentHeight(),
                    color = ColorBG,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(2.dp, ColorBorder)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "레이드 준비 확인",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "$raidTypeName 레이드",
                            color = ColorTextSub,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val timerColor = when {
                            secondsLeft <= 5 -> ColorRed
                            secondsLeft <= 10 -> ColorYellow
                            else -> ColorTextMain
                        }

                        Text(
                            text = "${secondsLeft}초",
                            color = timerColor,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        readyMembers.forEach { member ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                color = ColorCard,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (member.ready) ColorGreen.copy(alpha = 0.4f) else ColorBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(10.dp),
                                        shape = CircleShape,
                                        color = if (member.ready) ColorGreen else ColorRed
                                    ) { }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Text(
                                        text = member.name,
                                        color = ColorTextMain,
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Text(
                                        text = if (member.ready) "준비 완료" else "대기 중",
                                        color = if (member.ready) ColorGreen else ColorYellow,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (!isInitiator) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ReadyButton(
                                    modifier = Modifier.weight(1f),
                                    text = "준비",
                                    color = ColorGreen,
                                    onClick = {
                                        ClientPlayNetworking.send(C2S_RaidReadyPayload(true))
                                    }
                                )

                                ReadyButton(
                                    modifier = Modifier.weight(1f),
                                    text = "거절",
                                    color = ColorRed,
                                    onClick = {
                                        ClientPlayNetworking.send(C2S_RaidReadyPayload(false))
                                        Minecraft.getInstance().setScreen(null)
                                    }
                                )
                            }
                        } else {
                            Text(
                                text = "파티원들의 준비를 기다리고 있습니다...",
                                color = ColorTextSub,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ReadyButton(
        modifier: Modifier,
        text: String,
        color: Color,
        onClick: () -> Unit
    ) {
        var isHovered by remember { mutableStateOf(false) }

        Surface(
            modifier = modifier
                .height(36.dp)
                .clickable { onClick() }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            if (event.type == PointerEventType.Enter) {
                                isHovered = true
                            } else if (event.type == PointerEventType.Exit) {
                                isHovered = false
                            }
                        }
                    }
                },
            color = if (isHovered) color.copy(alpha = 0.25f) else color.copy(alpha = 0.15f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, color.copy(alpha = if (isHovered) 0.8f else 0.5f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = color,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}