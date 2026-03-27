package kr.pyke.acau_hardcore.client.gui.screen

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.aperso.composite.component.Components
import dev.aperso.composite.core.ComposeScreen
import kr.pyke.acau_hardcore.data.randombox.BoxDefinition
import kr.pyke.acau_hardcore.registry.item.randombox.BoxItemHelper
import kr.pyke.acau_hardcore.data.randombox.ClientBoxRegistry
import kr.pyke.acau_hardcore.network.payload.c2s.C2S_ClaimRandomBoxRewardPayload
import kr.pyke.acau_hardcore.type.BOX_RARITY
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.Minecraft
import net.minecraft.core.HolderLookup
import net.minecraft.world.item.ItemStack
import kotlin.random.Random

data class RouletteSlotData(val stack: ItemStack, val rarity: BOX_RARITY, val color: Color)

object RandomBoxScreen {
    private val ColorBg = Color(0xFF0D0D0D)
    private val ColorPanel = Color(0xFF1E232E)
    private val ColorBorder = Color(0xFF3E4C66)
    private val ColorTextMain = Color(0xFFE0E0E0)
    private val ColorTextSub = Color(0xFF8B9BB4)
    private val ColorIndicator = Color(0xFFFFD54F)

    private const val SLOT_SIZE_DP = 64
    private const val SLOT_GAP_DP = 4
    private const val SLOT_STEP_DP = SLOT_SIZE_DP + SLOT_GAP_DP
    private const val TOTAL_SLOTS = 100

    private fun stripFormatting(text: String): String {
        return text.replace(Regex("§[0-9a-fk-or]", RegexOption.IGNORE_CASE), "")
    }

    @JvmStatic
    fun create(boxId: String, winningStack: ItemStack, rewardIndex: Int, rarityKey: String): ComposeScreen {
        return ComposeScreen {
            val mc = Minecraft.getInstance()
            mc.player ?: return@ComposeScreen
            val registries = mc.level?.registryAccess()
            val winningRarity = BOX_RARITY.byKey(rarityKey)
            val density = LocalDensity.current

            val winningIndex = remember { TOTAL_SLOTS - 8 + Random.nextInt(-2, 3) }

            val allSlots = remember {
                buildAllSlots(
                    boxId,
                    RouletteSlotData(winningStack, winningRarity, Color(winningRarity.argbColor)),
                    winningIndex,
                    registries
                )
            }

            val scrollState = rememberScrollState()
            var skipped by remember { mutableStateOf(false) }
            var isFinished by remember { mutableStateOf(false) }
            var animationComplete by remember { mutableStateOf(false) }
            var claimed by remember { mutableStateOf(false) }
            var containerWidthPx by remember { mutableIntStateOf(0) }

            val slotStepPx = with(density) { SLOT_STEP_DP.dp.toPx() }.toInt()
            val slotSizePx = with(density) { SLOT_SIZE_DP.dp.toPx() }.toInt()
            val gapPx = with(density) { SLOT_GAP_DP.dp.toPx() }.toInt()
            val halfContainerDp = with(density) { (containerWidthPx / 2).toDp() }
            val targetPx = gapPx + winningIndex * slotStepPx + slotSizePx / 2

            fun claimReward() {
                if (claimed) return
                claimed = true
                ClientPlayNetworking.send(C2S_ClaimRandomBoxRewardPayload())
            }

            DisposableEffect(Unit) {
                onDispose { claimReward() }
            }

            LaunchedEffect(skipped, containerWidthPx) {
                if (containerWidthPx == 0) return@LaunchedEffect

                val target = gapPx + winningIndex * slotStepPx + slotSizePx / 2

                if (skipped) {
                    scrollState.scrollTo(target)
                    animationComplete = true
                    return@LaunchedEffect
                }
                scrollState.animateScrollTo(
                    value = target,
                    animationSpec = tween(
                        durationMillis = 7000,
                        easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
                    )
                )
                animationComplete = true
            }

            LaunchedEffect(animationComplete) {
                if (animationComplete) {
                    kotlinx.coroutines.delay(600)
                    claimReward()
                    isFinished = true
                }
            }

            Box(
                modifier = Modifier.fillMaxSize().background(ColorBg.copy(alpha = 0.85f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isFinished) {
                        Text("개봉 중...", color = ColorTextMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .height((SLOT_SIZE_DP + 16).dp)
                                .onSizeChanged { containerWidthPx = it.width }
                                .background(ColorPanel, RoundedCornerShape(4.dp))
                                .border(1.dp, ColorBorder, RoundedCornerShape(4.dp))
                                .clipToBounds(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(scrollState)
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(SLOT_GAP_DP.dp)
                            ) {
                                Spacer(modifier = Modifier.width(halfContainerDp))

                                allSlots.forEachIndexed { index, slot ->
                                    SlotItem(
                                        stack = slot.stack,
                                        color = slot.color,
                                        isHighlighted = animationComplete && index == winningIndex
                                    )
                                }

                                Spacer(modifier = Modifier.width(halfContainerDp))
                            }

                            Box(
                                modifier = Modifier.align(Alignment.Center)
                                    .width(2.dp).fillMaxHeight()
                                    .background(ColorIndicator)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (!animationComplete) {
                            Surface(
                                onClick = { skipped = true },
                                shape = RoundedCornerShape(4.dp),
                                color = ColorPanel,
                                border = BorderStroke(1.dp, ColorBorder)
                            ) {
                                Text(
                                    "스킵", color = ColorTextSub, fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                )
                            }
                        }
                    } else {
                        val rarityColor = Color(winningRarity.argbColor)
                        val itemName = stripFormatting(winningStack.hoverName.string)
                        val countText = if (winningStack.count > 1) " x${winningStack.count}" else ""

                        Text("축하합니다!", color = ColorIndicator, fontWeight = FontWeight.Bold, fontSize = 20.sp)

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier.size(96.dp)
                                .background(ColorPanel, RoundedCornerShape(8.dp))
                                .border(2.dp, rarityColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Components.Item(winningStack, modifier = Modifier.size(64.dp), decorations = true, tooltip = true)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "$itemName$countText",
                            color = rarityColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Surface(
                            onClick = { mc.setScreen(null) },
                            shape = RoundedCornerShape(4.dp),
                            color = rarityColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                "확인", color = rarityColor, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 32.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun buildAllSlots(
        boxId: String, winningSlot: RouletteSlotData, winningIndex: Int,
        registries: HolderLookup.Provider?
    ): List<RouletteSlotData> {
        val definition = ClientBoxRegistry.get(boxId)
        val random = Random.Default

        return List(TOTAL_SLOTS) { i ->
            if (i == winningIndex) winningSlot
            else randomDecoy(definition, random, registries)
        }
    }

    private fun randomDecoy(
        definition: BoxDefinition?, random: Random, registries: HolderLookup.Provider?
    ): RouletteSlotData {
        if (definition == null || definition.rewards().isEmpty()) {
            return RouletteSlotData(ItemStack.EMPTY, BOX_RARITY.COMMON, Color(BOX_RARITY.COMMON.argbColor))
        }

        val rewards = definition.rewards()
        val totalWeight = rewards.sumOf { it.weight() }
        var roll = random.nextInt(totalWeight)

        for (reward in rewards) {
            roll -= reward.weight()
            if (roll < 0) {
                val stack = BoxItemHelper.createRewardStack(reward, registries)
                return RouletteSlotData(stack, reward.rarity(), Color(reward.rarity().argbColor))
            }
        }

        val fallback = rewards.last()
        return RouletteSlotData(
            BoxItemHelper.createRewardStack(fallback, registries),
            fallback.rarity(), Color(fallback.rarity().argbColor)
        )
    }

    @Composable
    fun SlotItem(stack: ItemStack, color: Color, isHighlighted: Boolean) {
        Box(
            modifier = Modifier.size(SLOT_SIZE_DP.dp)
                .background(
                    if (isHighlighted) color.copy(alpha = 0.2f) else ColorPanel,
                    RoundedCornerShape(4.dp)
                )
                .border(
                    width = if (isHighlighted) 2.dp else 1.dp,
                    color = if (isHighlighted) color else ColorBorder,
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Components.Item(stack, modifier = Modifier.size(48.dp), decorations = true, tooltip = false)

            Box(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth().height(2.dp).background(color)
            )
        }
    }
}