package kr.pyke.acau_hardcore.client.gui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.aperso.composite.core.ComposeScreen
import kr.pyke.acau_hardcore.network.payload.c2s.C2S_SelectPrefixPayload
import kr.pyke.acau_hardcore.prefix.PrefixData
import kr.pyke.acau_hardcore.prefix.PrefixRegistry
import kr.pyke.acau_hardcore.registry.component.ModComponents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.Minecraft

object PrefixScreen {
    private val ColorPanel = Color(0xFF1E232E)
    private val ColorItem = Color(0xFF252A36)
    private val ColorItemHover = Color(0xFF2E3545)
    private val ColorTextMain = Color(0xFFE0E0E0)
    private val ColorTextSub = Color(0xFF8B9BB4)
    private val ColorAccent = Color(0xFF4A6588)
    private val ColorBorder = Color(0xFF3E4C66)
    private val ColorEquipBtn = Color(0xFF5C8A6A)
    private val ColorUnequipBtn = Color(0xFFE57373)

    private var isWaitingResponse = mutableStateOf(false)

    @JvmStatic
    fun handleResponse(success: Boolean) {
        isWaitingResponse.value = false
    }

    @JvmStatic
    fun create(): ComposeScreen {
        isWaitingResponse.value = false

        return ComposeScreen {
            val player = Minecraft.getInstance().player ?: return@ComposeScreen
            val isWaiting by remember { isWaitingResponse }

            val prefixesComponent = ModComponents.PREFIXES.get(player)
            val unlockedIds = prefixesComponent.prefixes
            val selectedId = prefixesComponent.selectedPrefix

            val allUnlockedPrefixes = unlockedIds.mapNotNull { PrefixRegistry.get(it) }

            val equippedPrefix = if (selectedId != "none") {
                PrefixRegistry.get(selectedId)
            } else {
                null
            }
            val unequippedPrefixes = allUnlockedPrefixes.filter { it.id() != selectedId }.sortedBy { it.id() }

            fun requestEquip(id: String) {
                if (isWaiting) {
                    return
                }
                isWaitingResponse.value = true
                ClientPlayNetworking.send(C2S_SelectPrefixPayload(id))
            }

            fun requestUnequip() {
                if (isWaiting) {
                    return
                }
                isWaitingResponse.value = true
                ClientPlayNetworking.send(C2S_SelectPrefixPayload("none"))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(enabled = false) { },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .width(400.dp)
                        .fillMaxHeight(0.8f),
                    shape = RoundedCornerShape(8.dp),
                    color = ColorPanel,
                    border = BorderStroke(1.dp, ColorBorder),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = ColorAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "칭호 선택",
                                    color = ColorTextMain,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }

                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = ColorTextSub,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { Minecraft.getInstance().setScreen(null) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "보유한 칭호: ${unlockedIds.size}개",
                            color = ColorTextSub,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (equippedPrefix != null) {
                                item {
                                    PrefixListItem(
                                        data = equippedPrefix,
                                        isEquipped = true,
                                        isWaiting = isWaiting,
                                        onAction = { requestUnequip() }
                                    )
                                }
                            }

                            items(unequippedPrefixes) { prefixData ->
                                PrefixListItem(
                                    data = prefixData,
                                    isEquipped = false,
                                    isWaiting = isWaiting,
                                    onAction = { requestEquip(prefixData.id()) }
                                )
                            }

                            if (allUnlockedPrefixes.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "보유 중인 칭호가 없습니다.",
                                            color = ColorTextSub.copy(alpha = 0.5f),
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun PrefixListItem(
        data: PrefixData,
        isEquipped: Boolean,
        isWaiting: Boolean,
        onAction: () -> Unit
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    color = if (isEquipped) {
                        ColorItem.copy(alpha = 0.8f)
                    } else if (isHovered) {
                        ColorItemHover
                    } else {
                        ColorItem
                    },
                    shape = RoundedCornerShape(6.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (isEquipped) {
                        ColorAccent
                    } else {
                        ColorBorder.copy(alpha = 0.5f)
                    },
                    shape = RoundedCornerShape(6.dp)
                )
                .hoverable(interactionSource)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.prefix(),
                    color = ColorTextMain,
                    fontSize = 16.sp,
                    fontWeight = if (isEquipped) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    }
                )

                if (isEquipped) {
                    Button(
                        onClick = onAction,
                        enabled = !isWaiting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorUnequipBtn,
                            disabledContainerColor = ColorUnequipBtn.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("해제", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (isHovered) {
                    Button(
                        onClick = onAction,
                        enabled = !isWaiting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorEquipBtn,
                            disabledContainerColor = ColorEquipBtn.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("장착", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}