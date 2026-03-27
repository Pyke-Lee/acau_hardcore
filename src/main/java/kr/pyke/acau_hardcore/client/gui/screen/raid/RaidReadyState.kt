package kr.pyke.acau_hardcore.client.gui.screen.raid

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_RaidReadyUpdatePayload

object RaidReadyState {
    val members = mutableStateListOf<S2C_RaidReadyUpdatePayload.ReadyData>()
    val secondsLeft = mutableIntStateOf(30)

    fun update(payload: S2C_RaidReadyUpdatePayload) {
        members.clear()
        members.addAll(payload.members())
        secondsLeft.intValue = payload.secondsLeft()
    }

    fun clear() {
        members.clear()
        secondsLeft.intValue = 0
    }
}