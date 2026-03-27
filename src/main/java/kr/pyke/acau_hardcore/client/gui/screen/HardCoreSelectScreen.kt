package kr.pyke.acau_hardcore.client.gui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.aperso.composite.component.Components
import dev.aperso.composite.core.ComposeScreen
import kr.pyke.acau_hardcore.AcauHardCore
import kr.pyke.acau_hardcore.network.payload.c2s.C2S_StartHardCorePayload
import kr.pyke.acau_hardcore.type.HARDCORE_TYPE
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier

object HardCoreSelectScreen {
    private val ColorBG = Color(0xFF161A22)
    private val ColorCard = Color(0xFF1E232E)
    private val ColorHover = Color(0xFF2D3648)
    private val ColorBorder = Color(0xFF3E4C66)
    private val ColorAccent = Color(0xFF4A6588)
    private val ColorTextMain = Color(0xFFE0E0E0)
    private val ColorTextSub = Color(0xFF8B9BB4)

    private val VanillaTex = Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "textures/gui/vanilla.png")
    private val ExpertTex = Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "textures/gui/expert.png")

    @JvmStatic
    fun create(): ComposeScreen {
        return ComposeScreen {
            val player = Minecraft.getInstance().player ?: return@ComposeScreen
            var currentTooltip by remember { mutableStateOf<AnnotatedString?>(null) }
            var mousePosition by remember { mutableStateOf(DpOffset.Zero) }
            val density = LocalDensity.current

            LaunchedEffect(Unit) {
                Minecraft.getInstance().mouseHandler.releaseMouse()
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .clickable(enabled = false) { }
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                    if (event.type == PointerEventType.Move || event.type == PointerEventType.Enter) {
                                        val pos = event.changes.first().position
                                        with(density) {
                                            mousePosition = DpOffset(pos.x.toDp(), pos.y.toDp())
                                        }
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.width(640.dp).height(380.dp),
                        color = ColorBG,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(2.dp, ColorBorder)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "하드코어 유형 선택",
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "플레이하실 하드코어 유형을 선택해 주세요. 한 번 선택하면 변경할 수 없습니다.",
                                color = ColorTextSub,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                SelectCard(
                                    modifier = Modifier.weight(1f),
                                    title = "바닐라 하드코어",
                                    texture = VanillaTex,
                                    onSelect = {
                                        ClientPlayNetworking.send(C2S_StartHardCorePayload(HARDCORE_TYPE.BEGINNER.key))
                                        Minecraft.getInstance().setScreen(null)
                                    },
                                    setTooltip = { currentTooltip = it },
                                    tooltipText = buildAnnotatedString {
                                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                                            append("바닐라 하드코어\n")
                                        }
                                        withStyle(SpanStyle(color = ColorTextSub)) {
                                            append("기본 하드코어 난이도와 동일합니다.\n")
                                        }
                                        withStyle(SpanStyle(color = Color(0xFF55FF55))) {
                                            append("• 기본 특수 광물 획득 확률 0.5%")
                                        }
                                    }
                                )

                                SelectCard(
                                    modifier = Modifier.weight(1f),
                                    title = "숙련자용 하드코어",
                                    texture = ExpertTex,
                                    onSelect = {
                                        ClientPlayNetworking.send(C2S_StartHardCorePayload(HARDCORE_TYPE.EXPERT.key))
                                        Minecraft.getInstance().setScreen(null)
                                    },
                                    setTooltip = { currentTooltip = it },
                                    tooltipText = buildAnnotatedString {
                                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                                            append("숙련자용 하드코어\n")
                                        }

                                        withStyle(SpanStyle(color = ColorTextSub)) {
                                            append("몬스터가 강화되는 대신 혜택이 존재합니다.\n")
                                        }
                                        withStyle(SpanStyle(color = Color(0xFFFF5555))) {
                                            append("• 몬스터 체력 50% 증가\n")
                                            append("• 몬스터 공격력 30% 증가\n")
                                            append("• 몬스터 이동속도 10% 증가\n")
                                            append("• 몬스터 방어력 4 증가\n")
                                            append("• 몬스터 밀치기 저항 20% 증가\n")
                                        }
                                        withStyle(SpanStyle(color = Color(0xFF55FF55))) {
                                            append("• 기본 특수 광물 획득 확률 1%\n")
                                            append("• 추가 특수 광물 획득 확률 100% 증가")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                if (currentTooltip != null) {
                    Box(
                        modifier = Modifier.offset(x = mousePosition.x + 12.dp, y = mousePosition.y + 12.dp)
                    ) {
                        Surface(
                            color = ColorCard,
                            border = BorderStroke(1.dp, ColorBorder),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = currentTooltip!!,
                                color = ColorTextMain,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SelectCard(
        modifier: Modifier,
        title: String,
        texture: Identifier,
        onSelect: () -> Unit,
        setTooltip: (AnnotatedString?) -> Unit,
        tooltipText: AnnotatedString
    ) {
        var isHovered by remember { mutableStateOf(false) }

        Surface(
            modifier = modifier
                .fillMaxHeight()
                .clickable { onSelect() }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            if (event.type == PointerEventType.Enter) {
                                isHovered = true
                                setTooltip(tooltipText)
                            } else if (event.type == PointerEventType.Exit) {
                                isHovered = false
                                setTooltip(null)
                            }
                        }
                    }
                },
            color = if (isHovered) { ColorHover } else { ColorCard },
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(2.dp, if (isHovered) { ColorAccent } else { ColorBorder })
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Components.AssetImage(
                        resource = texture,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = title
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}