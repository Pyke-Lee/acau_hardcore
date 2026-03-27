package kr.pyke.acau_hardcore.client.gui.screen.raid

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.aperso.composite.component.Components
import dev.aperso.composite.core.ComposeScreen
import kr.pyke.acau_hardcore.AcauHardCore
import kr.pyke.acau_hardcore.network.payload.c2s.C2S_RaidSelectPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack

object BossRaidSelectScreen {
    private val ColorBG = Color(0xFF161A22)
    private val ColorCard = Color(0xFF1E232E)
    private val ColorHover = Color(0xFF2D3648)
    private val ColorBorder = Color(0xFF3E4C66)
    private val ColorAccent = Color(0xFF4A6588)
    private val ColorDisabled = Color(0xFF2A2A2A)
    private val ColorDisabledBorder = Color(0xFF444444)
    private val ColorDisabledText = Color(0xFF555555)
    private val ColorTextMain = Color(0xFFE0E0E0)
    private val ColorTextSub = Color(0xFF8B9BB4)
    private val ColorRed = Color(0xFFFF5555)
    private val ColorGreen = Color(0xFF55FF55)
    private val ColorGold = Color(0xFFFFAA00)

    private val DragonTex = Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "textures/gui/ender_dragon.png")

    @JvmStatic
    fun create(): ComposeScreen {
        return ComposeScreen {
            LaunchedEffect(Unit) {
                Minecraft.getInstance().mouseHandler.releaseMouse()
            }

            val vanillaInProgress by RaidSelectState.vanillaInProgress
            val expertInProgress by RaidSelectState.expertInProgress
            val playerRaidTypeKey by RaidSelectState.playerRaidTypeKey
            val cooldownTicks by RaidSelectState.cooldownTicks

            val hasCooldown = cooldownTicks > 0
            val cooldownMin = (cooldownTicks / 20) / 60
            val cooldownSec = (cooldownTicks / 20) % 60

            val vanillaStatus = deriveCardStatus("vanilla", playerRaidTypeKey, vanillaInProgress, hasCooldown)
            val expertStatus = deriveCardStatus("expert", playerRaidTypeKey, expertInProgress, hasCooldown)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable { Minecraft.getInstance().setScreen(null) },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .width(720.dp)
                        .wrapContentHeight()
                        .clickable(enabled = false) { },
                    color = ColorBG,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(2.dp, ColorBorder)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "엔더 드래곤 레이드",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "레이드 유형을 선택하세요. 파티가 있으면 파티장만 시작할 수 있습니다.",
                            color = ColorTextSub,
                            fontSize = 13.sp
                        )

                        if (hasCooldown) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = Color(0xFF3A1A1A),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, ColorRed.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "쿨타임: ${cooldownMin}분 ${cooldownSec}초 남음",
                                    color = ColorRed,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            RaidCard(
                                modifier = Modifier.weight(1f),
                                title = "바닐라 레이드",
                                subtitle = "The End",
                                status = vanillaStatus,
                                rewardItems = RaidSelectState.vanillaRewardItems,
                                currency = RaidSelectState.vanillaCurrency,
                                accentColor = ColorGreen,
                                onSelect = {
                                    ClientPlayNetworking.send(C2S_RaidSelectPayload("vanilla"))
                                    Minecraft.getInstance().setScreen(null)
                                }
                            )

                            RaidCard(
                                modifier = Modifier.weight(1f),
                                title = "숙련자 레이드",
                                subtitle = "Expert End",
                                status = expertStatus,
                                rewardItems = RaidSelectState.expertRewardItems,
                                currency = RaidSelectState.expertCurrency,
                                accentColor = ColorRed,
                                onSelect = {
                                    ClientPlayNetworking.send(C2S_RaidSelectPayload("expert"))
                                    Minecraft.getInstance().setScreen(null)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private enum class CardStatus(val label: String?) {
        AVAILABLE(null),
        COOLDOWN("쿨타임 중"),
        IN_PROGRESS("진행 중"),
        WRONG_TYPE("진입 불가")
    }

    private fun deriveCardStatus(
        cardTypeKey: String,
        playerTypeKey: String,
        inProgress: Boolean,
        hasCooldown: Boolean
    ): CardStatus {
        if (hasCooldown) return CardStatus.COOLDOWN
        if (playerTypeKey != cardTypeKey) return CardStatus.WRONG_TYPE
        if (inProgress) return CardStatus.IN_PROGRESS
        return CardStatus.AVAILABLE
    }

    @Composable
    private fun RaidCard(
        modifier: Modifier,
        title: String,
        subtitle: String,
        status: CardStatus,
        rewardItems: List<ItemStack>,
        currency: Long,
        accentColor: Color,
        onSelect: () -> Unit
    ) {
        var isHovered by remember { mutableStateOf(false) }
        val available = status == CardStatus.AVAILABLE

        val bgColor = when {
            !available -> ColorDisabled
            isHovered -> ColorHover
            else -> ColorCard
        }
        val borderColor = when {
            !available -> ColorDisabledBorder
            isHovered -> ColorAccent
            else -> ColorBorder
        }

        Surface(
            modifier = modifier
                .clickable(enabled = available) { onSelect() }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            if (event.type == PointerEventType.Enter) {
                                isHovered = true
                            }
                            else if (event.type == PointerEventType.Exit) {
                                isHovered = false
                            }
                        }
                    }
                },
            color = bgColor,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(2.dp, borderColor)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 타이틀
                Text(
                    text = title,
                    color = if (available) Color.White else ColorDisabledText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = subtitle,
                    color = if (available) ColorTextSub else ColorDisabledText,
                    fontSize = 12.sp
                )

                // 상태 뱃지 (고정 높이 → 뱃지 유무와 무관하게 카드 크기 동일)
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier.height(22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (status.label != null) {
                        val badgeColor = when (status) {
                            CardStatus.IN_PROGRESS -> ColorGold
                            else -> ColorRed
                        }
                        Surface(
                            color = badgeColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = status.label,
                                color = badgeColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 드래곤 이미지
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Components.AssetImage(
                        resource = DragonTex,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = "엔더 드래곤"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 보상
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (available) Color(0xFF141820) else Color(0xFF1A1A1A),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (available) accentColor.copy(alpha = 0.3f) else ColorDisabledBorder)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp)
                    ) {
                        Text(
                            text = "보상",
                            color = if (available) accentColor else ColorDisabledText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        rewardItems.forEach { itemStack ->
                            val itemName = itemStack.hoverName.string
                            val count = itemStack.count
                            val displayText = if (count > 1) "$itemName x$count" else itemName

                            Text(
                                text = displayText,
                                color = if (available) ColorTextMain else ColorDisabledText,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }

                        if (currency > 0) {
                            Text(
                                text = String.format("%,d원", currency),
                                color = if (available) ColorGold else ColorDisabledText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}