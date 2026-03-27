package kr.pyke.acau_hardcore.client.gui.screen.raid

import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_OpenRaidSelectPayload
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_RaidSelectUpdatePayload
import net.minecraft.world.item.ItemStack

object RaidSelectState {
    val vanillaInProgress = mutableStateOf(false)
    val expertInProgress = mutableStateOf(false)
    val playerRaidTypeKey = mutableStateOf("")
    val cooldownTicks = mutableLongStateOf(0L)

    val vanillaRewardItems = mutableStateListOf<ItemStack>()
    var vanillaCurrency = 0L
    val expertRewardItems = mutableStateListOf<ItemStack>()
    var expertCurrency = 0L

    fun initFull(payload: S2C_OpenRaidSelectPayload) {
        vanillaInProgress.value = payload.vanillaInProgress()
        expertInProgress.value = payload.expertInProgress()
        playerRaidTypeKey.value = payload.playerRaidTypeKey()
        cooldownTicks.longValue = payload.cooldownTicks()

        vanillaRewardItems.clear()
        vanillaRewardItems.addAll(payload.vanillaRewardItems())
        vanillaCurrency = payload.vanillaCurrency()

        expertRewardItems.clear()
        expertRewardItems.addAll(payload.expertRewardItems())
        expertCurrency = payload.expertCurrency()
    }

    fun updateStatus(payload: S2C_RaidSelectUpdatePayload) {
        vanillaInProgress.value = payload.vanillaInProgress()
        expertInProgress.value = payload.expertInProgress()
    }

    fun clear() {
        vanillaInProgress.value = false
        expertInProgress.value = false
        playerRaidTypeKey.value = ""
        cooldownTicks.longValue = 0L
        vanillaRewardItems.clear()
        vanillaCurrency = 0L
        expertRewardItems.clear()
        expertCurrency = 0L
    }
}