package kr.pyke.acau_hardcore.client.gui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.aperso.composite.component.Components
import dev.aperso.composite.core.ComposeScreen
import kr.pyke.acau_hardcore.type.MAIL_STATE
import kr.pyke.acau_hardcore.data.mailbox.MailBoxData
import kr.pyke.acau_hardcore.network.payload.c2s.C2S_ClaimMailPayload
import kr.pyke.acau_hardcore.network.payload.c2s.C2S_RemoveMailPayload
import kr.pyke.acau_hardcore.registry.component.ModComponents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.Minecraft
import java.text.SimpleDateFormat
import java.util.*

enum class MailFilter(val displayName: String) {
    ALL("전체"),
    UNREAD("안 읽음"),
    READ("읽음")
}

object MailBoxScreen {
    private val ColorPanel = Color(0xFF1E232E)
    private val ColorItem = Color(0xFF252A36)
    private val ColorSelected = Color(0xFF2D3648)
    private val ColorTextMain = Color(0xFFE0E0E0)
    private val ColorTextSub = Color(0xFF8B9BB4)
    private val ColorAccent = Color(0xFF4A6588)
    private val ColorBorder = Color(0xFF3E4C66)

    @JvmStatic
    fun create(): ComposeScreen {
        return ComposeScreen {
            val player = Minecraft.getInstance().player ?: return@ComposeScreen

            var rawMailList by remember {
                mutableStateOf(ModComponents.MAIL_BOX.get(player).mails.sortedByDescending { it.sentDate() })
            }
            var currentFilter by remember { mutableStateOf(MailFilter.UNREAD) }
            var isFilterMenuExpanded by remember { mutableStateOf(false) }
            var selectedMail by remember { mutableStateOf<MailBoxData?>(null) }
            val dateFormat = remember { SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()) }

            val filteredMailList by remember {
                derivedStateOf {
                    when (currentFilter) {
                        MailFilter.ALL -> rawMailList
                        MailFilter.UNREAD -> rawMailList.filter { it.state() != MAIL_STATE.READ }
                        MailFilter.READ -> rawMailList.filter { it.state() == MAIL_STATE.READ }
                    }
                }
            }

            fun refreshMails() {
                val latestMails = ArrayList(ModComponents.MAIL_BOX.get(player).mails)
                if (selectedMail != null) {
                    selectedMail = latestMails.find { it.mailUUID() == selectedMail!!.mailUUID() }
                }
                rawMailList = latestMails.sortedByDescending { it.sentDate() }
            }

            fun updateMail(mail: MailBoxData) {
                ClientPlayNetworking.send(C2S_RemoveMailPayload(mail.mailUUID()))
                rawMailList =
                    rawMailList.map { if (it.mailUUID() == mail.mailUUID()) it.withState(MAIL_STATE.READ) else it }
                if (selectedMail?.mailUUID() == mail.mailUUID()) {
                    selectedMail = selectedMail?.withState(MAIL_STATE.READ)
                }
            }

            fun claimMail(mail: MailBoxData) {
                ClientPlayNetworking.send(C2S_ClaimMailPayload(mail.mailUUID()))
                rawMailList =
                    rawMailList.map { if (it.mailUUID() == mail.mailUUID()) it.withState(MAIL_STATE.READ) else it }
                if (selectedMail?.mailUUID() == mail.mailUUID()) {
                    selectedMail = selectedMail?.withState(MAIL_STATE.READ)
                }
            }

            fun claimAllMails() {
                rawMailList.forEach { mail ->
                    if (mail.state() != MAIL_STATE.READ && mail.itemStackList().isNotEmpty()) {
                        ClientPlayNetworking.send(C2S_ClaimMailPayload(mail.mailUUID()))
                    }
                }
                refreshMails()
            }

            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.85f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(10f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("우편함", color = ColorTextMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("새로운 우편을 확인하려면 새로고침하세요.", color = ColorTextSub, fontSize = 12.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                                Surface(
                                    onClick = { isFilterMenuExpanded = !isFilterMenuExpanded },
                                    modifier = Modifier.height(40.dp).widthIn(min = 120.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    color = ColorPanel,
                                    border = BorderStroke(1.dp, ColorBorder)
                                ) {
                                    Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                                        Row(
                                            modifier = Modifier.align(Alignment.CenterStart),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.FilterList,
                                                contentDescription = "Filter",
                                                tint = ColorAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = currentFilter.displayName,
                                                color = ColorTextMain,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = "Drop",
                                            tint = ColorTextSub,
                                            modifier = Modifier
                                                .align(Alignment.CenterEnd)
                                                .rotate(if (isFilterMenuExpanded) 180f else 0f)
                                        )
                                    }
                                }

                                if (isFilterMenuExpanded) {
                                    Surface(
                                        modifier = Modifier
                                            .offset(x = (-60).dp, y = 44.dp)
                                            .width(120.dp)
                                            .zIndex(10f)
                                            .layout { measurable, constraints ->
                                                val placeable = measurable.measure(constraints)
                                                layout(0, 0) {
                                                    placeable.place(0, 0)
                                                }
                                            },
                                        shape = RoundedCornerShape(4.dp),
                                        color = ColorPanel,
                                        border = BorderStroke(1.dp, ColorBorder),
                                        shadowElevation = 4.dp
                                    ) {
                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                            MailFilter.entries.forEach { filter ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            currentFilter = filter
                                                            isFilterMenuExpanded = false
                                                        }
                                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = filter.displayName,
                                                        color = if (filter == currentFilter) ColorAccent else ColorTextMain,
                                                        fontWeight = if (filter == currentFilter) FontWeight.Bold else FontWeight.Normal,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Surface(
                                onClick = { refreshMails() },
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(4.dp),
                                color = ColorPanel,
                                border = BorderStroke(1.dp, ColorBorder)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = ColorTextMain)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Surface(
                                onClick = { claimAllMails() },
                                modifier = Modifier.height(40.dp),
                                shape = RoundedCornerShape(4.dp),
                                color = ColorPanel,
                                border = BorderStroke(1.dp, ColorBorder)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = "모두 받기",
                                        color = ColorTextMain,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 4.dp)
                            .zIndex(0f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(0.4f)
                                .fillMaxHeight()
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .border(1.dp, ColorBorder, RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            items(filteredMailList, key = { it.mailUUID() }) { mail ->
                                MailListItem(
                                    mail = mail,
                                    isSelected = (selectedMail?.mailUUID() == mail.mailUUID()),
                                    dateString = dateFormat.format(Date(mail.sentDate())),
                                    onClick = { selectedMail = mail },
                                    onClaim = { claimMail(mail) }
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(0.6f)
                                .fillMaxHeight()
                                .background(ColorPanel, RoundedCornerShape(4.dp))
                                .border(1.dp, ColorBorder, RoundedCornerShape(4.dp))
                                .padding(20.dp)
                        ) {
                            if (selectedMail != null) {
                                key(selectedMail!!.mailUUID()) {
                                    MailDetailView(
                                        mail = selectedMail!!,
                                        dateString = dateFormat.format(Date(selectedMail!!.sentDate())),
                                        onConfirm = { updateMail(selectedMail!!) },
                                        onClaim = { claimMail(selectedMail!!) }
                                    )
                                }
                            } else {
                                Text(
                                    text = "확인할 우편을 선택해주세요.",
                                    color = ColorTextSub,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun MailListItem(
        mail: MailBoxData,
        isSelected: Boolean,
        dateString: String,
        onClick: () -> Unit,
        onClaim: () -> Unit
    ) {
        var isHovered by remember { mutableStateOf(false) }
        val backgroundColor = if (isSelected) ColorSelected else ColorItem
        val borderColor = if (isSelected) ColorAccent else Color.Transparent
        val hasItems = mail.itemStackList().isNotEmpty()
        val isRead = mail.state() == MAIL_STATE.READ
        val iconVector =
            if (hasItems) Icons.Default.CardGiftcard else if (isRead) Icons.Default.Drafts else Icons.Default.Email
        val iconColor = if (hasItems && !isRead) Color(0xFFFFD54F) else ColorTextSub

        Row(
            modifier = Modifier.fillMaxWidth().height(68.dp).background(backgroundColor, RoundedCornerShape(4.dp))
                .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            if (event.type == PointerEventType.Enter) isHovered = true
                            else if (event.type == PointerEventType.Exit) isHovered = false
                        }
                    }
                }
                .clickable { onClick() }.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = "Type",
                            tint = iconColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = mail.mailTitle(),
                            color = if (isRead) ColorTextSub else ColorTextMain,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    if (isHovered && !isRead) {
                        Surface(
                            onClick = if (hasItems) onClaim else onClick,
                            shape = RoundedCornerShape(4.dp),
                            color = ColorAccent,
                            border = BorderStroke(1.dp, ColorBorder),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                Text(
                                    text = if (hasItems) "받기" else "확인",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "From. ${mail.senderName()}",
                        color = ColorTextSub,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = dateString,
                        color = ColorTextSub.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }

    @Composable
    fun MailDetailView(mail: MailBoxData, dateString: String, onConfirm: () -> Unit, onClaim: () -> Unit) {
        val hasItems = mail.itemStackList().isNotEmpty()
        val isRead = mail.state() == MAIL_STATE.READ
        val scrollState = rememberScrollState()

        Column(modifier = Modifier.fillMaxSize()) {
            Text(text = mail.mailTitle(), color = ColorTextMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "보낸 사람: ${mail.senderName()}", color = ColorTextSub, fontSize = 13.sp)
                Text(text = dateString, color = ColorTextSub, fontSize = 13.sp)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 1.dp, color = ColorBorder)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = mail.mailMessage(),
                    color = ColorTextMain,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            if (hasItems) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(thickness = 1.dp, color = ColorBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("동봉된 아이템:", color = ColorTextSub, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    mail.itemStackList().forEachIndexed { index, stack ->
                        key(index) {
                            if (!stack.isEmpty) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(ColorItem, RoundedCornerShape(4.dp))
                                        .border(1.dp, ColorBorder, RoundedCornerShape(4.dp))
                                        .padding(4.dp)
                                ) {
                                    Components.Item(
                                        stack,
                                        modifier = Modifier.fillMaxSize(),
                                        decorations = true,
                                        tooltip = true
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (!isRead) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = if (hasItems) onClaim else onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = ColorAccent),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(text = if (hasItems) "받기" else "확인", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}