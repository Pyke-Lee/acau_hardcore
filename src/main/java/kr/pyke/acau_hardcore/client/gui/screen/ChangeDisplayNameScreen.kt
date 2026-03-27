package kr.pyke.acau_hardcore.client.gui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.aperso.composite.core.ComposeScreen
import kr.pyke.acau_hardcore.data.cache.AcauHardCoreCache
import kr.pyke.acau_hardcore.network.payload.c2s.C2S_ChangeDisplayNamePayload
import net.minecraft.client.Minecraft

object ChangeDisplayNameScreen {
    private val ColorPanel = Color(0xFF1E232E)
    private val ColorItem = Color(0xFF252A36)
    private val ColorTextMain = Color(0xFFE0E0E0)
    private val ColorTextSub = Color(0xFF8B9BB4)
    private val ColorAccent = Color(0xFF4A6588)
    private val ColorBorder = Color(0xFF3E4C66)
    private val ColorError = Color(0xFFE57373)

    private var globalErrorMessage = mutableStateOf<String?>(null)
    private var isWaitingResponse = mutableStateOf(false)

    @JvmStatic
    fun handleResponse(success: Boolean, message: String) {
        isWaitingResponse.value = false

        if (success) { Minecraft.getInstance().setScreen(null) }
        else { globalErrorMessage.value = message }
    }

    @JvmStatic
    fun create(): ComposeScreen {
        globalErrorMessage.value = null
        isWaitingResponse.value = false

        return ComposeScreen {
            val player = Minecraft.getInstance().player ?: return@ComposeScreen

            var inputName by remember { mutableStateOf("") }
            val errorMessage by remember { globalErrorMessage }
            val isWaiting by remember { isWaitingResponse }

            fun requestChange() {
                if (isWaiting) return

                if (inputName.isBlank()) {
                    globalErrorMessage.value = "닉네임을 입력해주세요."
                    return
                }
                if (inputName.length !in 2..16) {
                    globalErrorMessage.value = "2~16자 사이로 입력해주세요."
                    return
                }

                isWaitingResponse.value = true
                C2S_ChangeDisplayNamePayload.send(inputName)
                globalErrorMessage.value = null
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.width(360.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = ColorPanel,
                    border = BorderStroke(1.dp, ColorBorder),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = ColorAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "닉네임 변경",
                                    color = ColorTextMain,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
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

                        player.let {
                            val currentName = AcauHardCoreCache.displayNames[it.uuid] ?: it.name.string

                            Text(
                                text = "현재 이름: $currentName",
                                color = ColorTextSub,
                                fontSize = 12.sp,
                                modifier = Modifier.align(Alignment.Start)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        BasicTextField(
                            value = inputName,
                            onValueChange = { if (it.length <= 16) inputName = it },
                            enabled = !isWaiting,
                            textStyle = TextStyle(
                                color = if (isWaiting) ColorTextSub else ColorTextMain,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(ColorAccent),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .background(ColorItem, RoundedCornerShape(4.dp))
                                        .border(1.dp, if (errorMessage != null) ColorError else ColorBorder, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (inputName.isEmpty()) {
                                        Text(
                                            text = "새로운 닉네임을 입력하세요",
                                            color = ColorTextSub.copy(alpha = 0.5f),
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage!!,
                                color = ColorError,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "※ 2~16자 이내, 중복되지 않는 이름만 가능합니다.",
                                color = ColorTextSub.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { Minecraft.getInstance().setScreen(null) },
                                enabled = !isWaiting,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ColorItem,
                                    disabledContainerColor = ColorItem.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, ColorBorder),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Text("취소", color = ColorTextSub)
                            }

                            Button(
                                onClick = { requestChange() },
                                enabled = !isWaiting,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ColorAccent,
                                    disabledContainerColor = ColorAccent.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                if (isWaiting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                }
                                else { Text("변경하기", color = Color.White, fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }
            }
        }
    }
}