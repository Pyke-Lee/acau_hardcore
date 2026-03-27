package kr.pyke.acau_hardcore.client.gui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.aperso.composite.core.ComposeScreen
import kr.pyke.acau_hardcore.data.helprequest.HelpRequest
import kr.pyke.acau_hardcore.data.cache.AcauHardCoreCache
import kr.pyke.acau_hardcore.network.payload.c2s.*
import kr.pyke.acau_hardcore.type.HELP_REQUEST_STATE
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.Minecraft
import java.text.SimpleDateFormat
import java.util.*

enum class HelpFilter(val displayName: String, val state: HELP_REQUEST_STATE?) {
    ALL("전체", null),
    WAITING("대기", HELP_REQUEST_STATE.WAITING),
    PROCESSING("처리 중", HELP_REQUEST_STATE.PROCESSING),
    COMPLETED("완료", HELP_REQUEST_STATE.COMPLETED)
}

object HelpRequestScreen {
    private val ColorPanel = Color(0xFF1E232E)
    private val ColorItem = Color(0xFF252A36)
    private val ColorSelected = Color(0xFF2D3648)
    private val ColorTextMain = Color(0xFFE0E0E0)
    private val ColorTextSub = Color(0xFF8B9BB4)
    private val ColorAccent = Color(0xFF4A6588)
    private val ColorBorder = Color(0xFF3E4C66)

    private val ColorWaiting = Color(0xFFFFD54F)
    private val ColorProcessing = Color(0xFF4FC3F7)
    private val ColorCompleted = Color(0xFF81C784)

    @JvmStatic
    fun create(): ComposeScreen {
        return ComposeScreen {
            val player = Minecraft.getInstance().player ?: return@ComposeScreen
            val playerUuid = player.uuid
            var requestList by remember { mutableStateOf(AcauHardCoreCache.getAll()) }
            var nameMap by remember { mutableStateOf(HashMap(AcauHardCoreCache.displayNames)) }

            DisposableEffect(Unit) {
                val helpListener = Runnable {
                    requestList = AcauHardCoreCache.getAll()
                }
                val nameListener = Runnable {
                    nameMap = HashMap(AcauHardCoreCache.displayNames)
                }

                AcauHardCoreCache.addHelpListener(helpListener)
                AcauHardCoreCache.addNameListener(nameListener)

                onDispose {
                    AcauHardCoreCache.removeHelpListener(helpListener)
                    AcauHardCoreCache.removeNameListener(nameListener)
                }
            }

            var currentFilter by remember { mutableStateOf(HelpFilter.ALL) }
            var isFilterMenuExpanded by remember { mutableStateOf(false) }
            var selectedId by remember { mutableStateOf<UUID?>(null) }
            val dateFormat = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }

            val filteredList by remember(requestList, currentFilter) {
                derivedStateOf {
                    val filter = currentFilter.state
                    if (filter == null) { requestList }
                    else { requestList.filter { it.status() == filter } }
                }
            }

            val selectedRequest by remember(requestList, selectedId) {
                derivedStateOf {
                    selectedId?.let { id -> requestList.find { it.requestId() == id } }
                }
            }

            LaunchedEffect(filteredList, nameMap) {
                val uuids = mutableSetOf<UUID>()
                for (request in filteredList) {
                    uuids.add(request.requesterUuid())
                    request.handlerUuid().ifPresent { uuids.add(it) }
                }
                val missing = AcauHardCoreCache.getMissingNames(uuids)
                if (missing.isNotEmpty()) {
                    ClientPlayNetworking.send(C2S_RequestDisplayNamesPayload(missing))
                }
            }

            fun getPlayerName(uuid: UUID): String {
                return nameMap[uuid] ?: "Unknown"
            }

            fun refresh() {
                requestList = AcauHardCoreCache.getAll()
                nameMap = HashMap(AcauHardCoreCache.displayNames)
            }

            fun changeStatus(request: HelpRequest, status: HELP_REQUEST_STATE) {
                val updated = request.withStatus(status, playerUuid)
                AcauHardCoreCache.update(updated)
                requestList = AcauHardCoreCache.getAll()
                ClientPlayNetworking.send(
                    C2S_HelpRequestChangeStatusPayload(request.requestId(), status.serializedName)
                )

                if (status == HELP_REQUEST_STATE.PROCESSING) {
                    ClientPlayNetworking.send(C2S_TeleportToPlayerPayload(request.requesterUuid()))
                }
            }

            fun deleteRequest(request: HelpRequest) {
                AcauHardCoreCache.remove(request.requestId())
                requestList = AcauHardCoreCache.getAll()
                if (selectedId == request.requestId()) { selectedId = null }
                ClientPlayNetworking.send(C2S_HelpRequestDeletePayload(request.requestId()))
            }

            fun purgeCompleted() {
                val completedIds = requestList
                    .filter { it.status() == HELP_REQUEST_STATE.COMPLETED }
                    .map { it.requestId() }
                completedIds.forEach { AcauHardCoreCache.remove(it) }
                requestList = AcauHardCoreCache.getAll()
                selectedId = null
                ClientPlayNetworking.send(C2S_HelpRequestPurgePayload())
            }

            fun statusColor(status: HELP_REQUEST_STATE): Color = when (status) {
                HELP_REQUEST_STATE.WAITING -> ColorWaiting
                HELP_REQUEST_STATE.PROCESSING -> ColorProcessing
                HELP_REQUEST_STATE.COMPLETED -> ColorCompleted
            }

            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.85f)) {
                    Row(modifier = Modifier.fillMaxWidth().zIndex(10f), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("도움 요청 관리", color = ColorTextMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("대기 중: ${requestList.count { it.status() == HELP_REQUEST_STATE.WAITING }}건", color = ColorWaiting, fontSize = 12.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                                Surface(onClick = { isFilterMenuExpanded = !isFilterMenuExpanded }, modifier = Modifier.height(40.dp).widthIn(min = 120.dp), shape = RoundedCornerShape(4.dp), color = ColorPanel, border = BorderStroke(1.dp, ColorBorder)) {
                                    Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                                        Row(modifier = Modifier.align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = ColorAccent, modifier = Modifier.size(16.dp))
                                            Text(text = currentFilter.displayName, color = ColorTextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Drop", tint = ColorTextSub, modifier = Modifier.align(Alignment.CenterEnd).rotate(if (isFilterMenuExpanded) 180f else 0f))
                                    }
                                }

                                if (isFilterMenuExpanded) {
                                    Surface(modifier = Modifier.offset(x = (-60).dp, y = 44.dp).width(120.dp).zIndex(10f)
                                            .layout { measurable, constraints ->
                                                val placeable = measurable.measure(constraints)
                                                layout(0, 0) { placeable.place(0, 0) }
                                            }, shape = RoundedCornerShape(4.dp), color = ColorPanel, border = BorderStroke(1.dp, ColorBorder), shadowElevation = 4.dp) {
                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                            HelpFilter.entries.forEach { filter ->
                                                Row(modifier = Modifier.fillMaxWidth().clickable { currentFilter = filter; isFilterMenuExpanded = false }.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = filter.displayName, color = if (filter == currentFilter) ColorAccent else ColorTextMain, fontWeight = if (filter == currentFilter) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Surface(onClick = { refresh() }, modifier = Modifier.size(40.dp), shape = RoundedCornerShape(4.dp), color = ColorPanel, border = BorderStroke(1.dp, ColorBorder)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = ColorTextMain)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Surface(onClick = { purgeCompleted() }, modifier = Modifier.height(40.dp), shape = RoundedCornerShape(4.dp), color = ColorPanel, border = BorderStroke(1.dp, ColorBorder)) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                                    Text("완료 정리", color = ColorTextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxSize().padding(top = 4.dp).zIndex(0f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LazyColumn(modifier = Modifier.weight(0.45f).fillMaxHeight().background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).border(1.dp, ColorBorder, RoundedCornerShape(4.dp)).padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            items(filteredList, key = { it.requestId() }) { request ->
                                HelpListItem(request = request, isSelected = selectedId == request.requestId(), dateString = dateFormat.format(Date(request.requestTime())), requesterName = getPlayerName(request.requesterUuid()), statusColor = statusColor(request.status()), onClick = { selectedId = request.requestId() })
                            }
                        }

                        Box(modifier = Modifier.weight(0.55f).fillMaxHeight().background(ColorPanel, RoundedCornerShape(4.dp)).border(1.dp, ColorBorder, RoundedCornerShape(4.dp)).padding(20.dp)) {
                            val sel = selectedRequest
                            if (sel != null) {
                                key(sel.requestId()) {
                                    HelpDetailView(request = sel, dateString = dateFormat.format(Date(sel.requestTime())), requesterName = getPlayerName(sel.requesterUuid()), handlerName = sel.handlerUuid().map { getPlayerName(it) }.orElse(null), statusColor = statusColor(sel.status()), onChangeStatus = { status -> changeStatus(sel, status) }, onDelete = { deleteRequest(sel) })
                                }
                            }
                            else {
                                Text(text = "관리할 요청을 선택해주세요.", color = ColorTextSub, modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun HelpListItem(request: HelpRequest, isSelected: Boolean, dateString: String, requesterName: String, statusColor: Color, onClick: () -> Unit) {
        val backgroundColor = if (isSelected) ColorSelected else ColorItem
        val borderColor = if (isSelected) ColorAccent else Color.Transparent
        val statusIcon = when (request.status()) {
            HELP_REQUEST_STATE.WAITING -> Icons.Default.HourglassEmpty
            HELP_REQUEST_STATE.PROCESSING -> Icons.Default.Build
            HELP_REQUEST_STATE.COMPLETED -> Icons.Default.CheckCircle
        }

        Row(modifier = Modifier.fillMaxWidth().height(68.dp).background(backgroundColor, RoundedCornerShape(4.dp)).border(1.dp, borderColor, RoundedCornerShape(4.dp)).clickable { onClick() }.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(statusIcon, contentDescription = "Status", tint = statusColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = requesterName, color = ColorTextMain, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    }
                    Text(text = request.status().displayName, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = request.message().ifBlank { "(메시지 없음)" }, color = ColorTextSub, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    Text(text = dateString, color = ColorTextSub.copy(alpha = 0.7f), fontSize = 10.sp)
                }
            }
        }
    }

    @Composable
    fun HelpDetailView(request: HelpRequest, dateString: String, requesterName: String, handlerName: String?, statusColor: Color, onChangeStatus: (HELP_REQUEST_STATE) -> Unit, onDelete: () -> Unit) {
        val scrollState = rememberScrollState()

        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = requesterName, color = ColorTextMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Surface(shape = RoundedCornerShape(4.dp), color = statusColor.copy(alpha = 0.15f), border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))) {
                    Text(text = request.status().displayName, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "요청 시간: $dateString", color = ColorTextSub, fontSize = 13.sp)
                if (handlerName != null) {
                    Text(text = "담당자: $handlerName", color = ColorTextSub, fontSize = 13.sp)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 1.dp, color = ColorBorder)

            Column(modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(scrollState)) {
                Text(text = request.message().ifBlank { "(메시지 없음)" }, color = if (request.message().isBlank()) ColorTextSub else ColorTextMain, fontSize = 14.sp, lineHeight = 20.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp, color = ColorBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(onClick = { onDelete() }, shape = RoundedCornerShape(4.dp), color = Color(0xFF5C2E2E), border = BorderStroke(1.dp, Color(0xFF8B4545))) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF9A9A), modifier = Modifier.size(16.dp))
                        Text("삭제", color = Color(0xFFEF9A9A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (request.status() != HELP_REQUEST_STATE.WAITING) {
                        StatusButton("대기", ColorWaiting) { onChangeStatus(HELP_REQUEST_STATE.WAITING) }
                    }
                    if (request.status() != HELP_REQUEST_STATE.PROCESSING) {
                        StatusButton("처리", ColorProcessing) { onChangeStatus(HELP_REQUEST_STATE.PROCESSING) }
                    }
                    if (request.status() != HELP_REQUEST_STATE.COMPLETED) {
                        StatusButton("완료", ColorCompleted) { onChangeStatus(HELP_REQUEST_STATE.COMPLETED) }
                    }
                }
            }
        }
    }

    @Composable
    fun StatusButton(label: String, color: Color, onClick: () -> Unit) {
        Surface(onClick = onClick, shape = RoundedCornerShape(4.dp), color = color.copy(alpha = 0.15f), border = BorderStroke(1.dp, color.copy(alpha = 0.4f))) {
            Text(text = label, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
        }
    }
}