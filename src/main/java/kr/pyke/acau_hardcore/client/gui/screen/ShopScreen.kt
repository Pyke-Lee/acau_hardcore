package kr.pyke.acau_hardcore.client.gui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.aperso.composite.component.Components
import dev.aperso.composite.core.ComposeScreen
import kotlinx.coroutines.delay
import kr.pyke.acau_hardcore.data.cache.AcauHardCoreCache
import kr.pyke.acau_hardcore.data.shop.ShopClientValidation
import kr.pyke.acau_hardcore.data.shop.ShopProduct
import kr.pyke.acau_hardcore.network.payload.c2s.C2S_ShopTransactionPayload
import kr.pyke.acau_hardcore.registry.component.ModComponents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import kotlin.math.ceil
import kotlin.time.Duration.Companion.milliseconds

object ShopScreen {
    private val ColorPanel = Color(0xFF1E232E)
    private val ColorItem = Color(0xFF252A36)
    private val ColorSelected = Color(0xFF2D3648)
    private val ColorTextMain = Color(0xFFE0E0E0)
    private val ColorTextSub = Color(0xFF8B9BB4)
    private val ColorAccent = Color(0xFF4A6588)
    private val ColorBorder = Color(0xFF3E4C66)

    private val ColorWaiting = Color(0xFFFFD54F)
    private val ColorProcessing = Color(0xFF4FC3F7)

    @JvmStatic
    fun create(shopId: String): ComposeScreen {
        return ComposeScreen {
            val player = Minecraft.getInstance().player ?: return@ComposeScreen
            val shopData = AcauHardCoreCache.SHOPS[shopId]

            if (shopData == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("상점 데이터를 불러올 수 없습니다.", color = ColorTextMain)
                }
                return@ComposeScreen
            }

            var globalCurrency by remember { mutableLongStateOf(0L) }
            var currentTooltip by remember { mutableStateOf<String?>(null) }
            var mousePosition by remember { mutableStateOf(DpOffset.Zero) }
            val density = LocalDensity.current

            var isBuyTab by remember { mutableStateOf(true) }
            var selectedProductIndex by remember { mutableStateOf<Int?>(null) }
            var selectedQuantity by remember { mutableIntStateOf(1) }
            var searchQuery by remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                if (!shopData.open_sound.isNullOrEmpty()) {
                    val soundId = Identifier.parse(shopData.open_sound!!)
                    val soundEvent = BuiltInRegistries.SOUND_EVENT.getValue(soundId)
                    if (soundEvent != null) {
                        player.playSound(soundEvent, 1.0f, 1.0f)
                    }
                }

                while (true) {
                    globalCurrency = ModComponents.HARDCORE_INFO.get(player).getCurrency()
                    delay(50.milliseconds)
                }
            }

            val products = shopData.products ?: emptyList()

            val filteredProducts = remember(products, isBuyTab, searchQuery) {
                products.mapIndexedNotNull { index, product ->
                    val stack = product.createItemStack(1)
                    val nameMatch = stack.hoverName.string.contains(searchQuery, ignoreCase = true)

                    if (nameMatch) {
                        if (isBuyTab && product.buyable) {
                            Pair(index, product)
                        }
                        else if (!isBuyTab && product.sellable) {
                            Pair(index, product)
                        }
                        else {
                            null
                        }
                    }
                    else {
                        null
                    }
                }
            }

            LaunchedEffect(isBuyTab, searchQuery) {
                selectedProductIndex = null
                selectedQuantity = 1
            }

            val itemsPerPage = 5
            val maxPage = maxOf(1, ceil(filteredProducts.size / itemsPerPage.toDouble()).toInt())
            var currentPage by remember(isBuyTab, searchQuery) { mutableIntStateOf(1) }

            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier.width(700.dp).padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = shopData.displayName ?: "상점",
                                color = ColorTextMain,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )

                            Surface(
                                color = ColorItem,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, ColorBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${String.format("%,d", globalCurrency)} 원",
                                        color = ColorWaiting,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier.width(700.dp).height(380.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = ColorPanel,
                            border = BorderStroke(2.dp, ColorBorder)
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier
                                        .weight(0.55f)
                                        .fillMaxHeight()
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth().height(36.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .background(if (isBuyTab) ColorAccent else ColorItem)
                                                .clickable { isBuyTab = true },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "구매",
                                                color = ColorTextMain,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .background(if (!isBuyTab) ColorAccent else ColorItem)
                                                .clickable { isBuyTab = false },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "판매",
                                                color = ColorTextMain,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                                        color = ColorItem,
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(1.dp, ColorBorder)
                                    ) {
                                        BasicTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            textStyle = TextStyle(color = ColorTextMain, fontSize = 14.sp),
                                            singleLine = true,
                                            cursorBrush = SolidColor(ColorTextMain),
                                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                                            decorationBox = { innerTextField ->
                                                if (searchQuery.isEmpty()) {
                                                    Text("아이템 검색...", color = ColorTextSub, fontSize = 14.sp)
                                                }
                                                innerTextField()
                                            }
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val startIndex = (currentPage - 1) * itemsPerPage
                                        val currentProducts = filteredProducts.drop(startIndex).take(itemsPerPage)

                                        for (i in 0 until itemsPerPage) {
                                            if (i < currentProducts.size) {
                                                val (originalIndex, product) = currentProducts[i]
                                                val isSelected = selectedProductIndex == originalIndex

                                                key(originalIndex) {
                                                    ShopListItem(
                                                        product = product,
                                                        isSelected = isSelected,
                                                        isBuyTab = isBuyTab,
                                                        onClick = {
                                                            selectedProductIndex = originalIndex
                                                            selectedQuantity = 1
                                                        }
                                                    )
                                                }
                                            }
                                            else {
                                                Spacer(modifier = Modifier.fillMaxWidth().height(48.dp))
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                    ) {
                                        Surface(
                                            color = if (currentPage > 1) ColorAccent else ColorItem,
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.clickable(enabled = currentPage > 1) { currentPage-- }
                                        ) {
                                            Text(
                                                text = "<",
                                                color = ColorTextMain,
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                            )
                                        }

                                        Text(
                                            text = "$currentPage / $maxPage",
                                            color = ColorTextSub,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )

                                        Surface(
                                            color = if (currentPage < maxPage) ColorAccent else ColorItem,
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.clickable(enabled = currentPage < maxPage) { currentPage++ }
                                        ) {
                                            Text(
                                                text = ">",
                                                color = ColorTextMain,
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(1.dp).fillMaxHeight().background(ColorBorder))

                                Box(
                                    modifier = Modifier
                                        .weight(0.45f)
                                        .fillMaxHeight()
                                        .padding(16.dp)
                                ) {
                                    if (selectedProductIndex != null) {
                                        val selectedProduct = products[selectedProductIndex!!]
                                        key(selectedProductIndex) {
                                            ShopDetailPanel(
                                                shopId = shopId,
                                                productIndex = selectedProductIndex!!,
                                                product = selectedProduct,
                                                player = player,
                                                isBuyTab = isBuyTab,
                                                quantity = selectedQuantity,
                                                onQuantityChange = { selectedQuantity = it }
                                            )
                                        }
                                    }
                                    else {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text("아이템을 선택해주세요.", color = ColorTextSub)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (currentTooltip != null) {
                    Box(
                        modifier = Modifier
                            .offset(x = mousePosition.x, y = mousePosition.y)
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints)
                                layout(0, 0) {
                                    placeable.placeRelative(-placeable.width, 0)
                                }
                            }
                    ) {
                        Surface(
                            color = ColorPanel,
                            border = BorderStroke(1.dp, ColorBorder),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = currentTooltip!!,
                                color = ColorTextMain,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ShopListItem(product: ShopProduct, isSelected: Boolean, isBuyTab: Boolean, onClick: () -> Unit) {
        val itemStack = remember(product) { product.createItemStack(1) }
        var isHovered by remember { mutableStateOf(false) }

        val barterItemName = remember(product, isBuyTab) {
            if (product.payment_type != "currency") {
                val itemIdStr = if (isBuyTab) product.barter_buy_item else product.barter_sell_item
                if (!itemIdStr.isNullOrEmpty()) {
                    val item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemIdStr))
                    ItemStack(item).hoverName.string
                }
                else {
                    ""
                }
            }
            else {
                ""
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(if (isSelected || isHovered) ColorSelected else ColorPanel)
                .border(1.dp, if (isSelected) ColorAccent else ColorBorder)
                .clickable { onClick() }
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
                }
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                if (!itemStack.isEmpty) {
                    Components.Item(
                        item = itemStack,
                        modifier = Modifier.fillMaxSize(),
                        decorations = false,
                        tooltip = true
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = itemStack.hoverName.string,
                color = ColorTextMain,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val priceStr = if (product.payment_type == "currency") {
                if (isBuyTab) "${String.format("%,d", product.buy_price)} 원" else "${String.format("%,d", product.sell_price)} 원"
            }
            else {
                if (isBuyTab) "${String.format("%,d", product.barter_buy_amount)} $barterItemName" else "${String.format("%,d", product.barter_sell_amount)} $barterItemName"
            }

            Text(
                text = priceStr,
                color = ColorWaiting,
                fontSize = 13.sp
            )
        }
    }

    @Composable
    fun ShopDetailPanel(
        shopId: String,
        productIndex: Int,
        product: ShopProduct,
        player: LocalPlayer,
        isBuyTab: Boolean,
        quantity: Int,
        onQuantityChange: (Int) -> Unit
    ) {
        val itemStack = remember(product) { product.createItemStack(1) }
        var currentCurrency by remember { mutableLongStateOf(0L) }
        var productCount by remember { mutableIntStateOf(0) }
        var barterBuyCount by remember { mutableIntStateOf(0) }

        val barterBuyName = remember(product) {
            if (!product.barter_buy_item.isNullOrEmpty()) {
                val item = BuiltInRegistries.ITEM.getValue(Identifier.parse(product.barter_buy_item!!))
                ItemStack(item).hoverName.string
            }
            else {
                ""
            }
        }

        val barterSellName = remember(product) {
            if (!product.barter_sell_item.isNullOrEmpty()) {
                val item = BuiltInRegistries.ITEM.getValue(Identifier.parse(product.barter_sell_item!!))
                ItemStack(item).hoverName.string
            }
            else {
                ""
            }
        }

        LaunchedEffect(Unit) {
            while (true) {
                currentCurrency = ModComponents.HARDCORE_INFO.get(player).getCurrency()
                productCount = ShopClientValidation.countItem(player, itemStack.item, product.componentPatch, product.strict_match)

                if (product.payment_type != "currency" && !product.barter_buy_item.isNullOrEmpty()) {
                    val barterItem = BuiltInRegistries.ITEM.getValue(Identifier.parse(product.barter_buy_item!!))
                    barterBuyCount = ShopClientValidation.countItem(player, barterItem, DataComponentPatch.EMPTY, false)
                }
                delay(50.milliseconds)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                if (!itemStack.isEmpty) {
                    Components.Item(
                        item = itemStack,
                        modifier = Modifier.fillMaxSize(),
                        decorations = true,
                        tooltip = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = itemStack.hoverName.string,
                color = ColorTextMain,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (product.buyable) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "구매가:", color = ColorTextSub, fontSize = 13.sp)
                        if (product.payment_type == "currency") {
                            Text(text = "${String.format("%,d", product.buy_price)} 원", color = ColorWaiting, fontSize = 13.sp)
                        }
                        else {
                            Text(text = "${String.format("%,d", product.barter_buy_amount)} $barterBuyName", color = ColorWaiting, fontSize = 13.sp)
                        }
                    }
                }

                if (product.sellable) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "판매가:", color = ColorTextSub, fontSize = 13.sp)
                        if (product.payment_type == "currency") {
                            Text(text = "${String.format("%,d", product.sell_price)} 원", color = ColorProcessing, fontSize = 13.sp)
                        }
                        else {
                            Text(text = "${String.format("%,d", product.barter_sell_amount)} $barterSellName", color = ColorProcessing, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(text = "수량", color = ColorTextSub, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuantityButton("-10") { onQuantityChange(maxOf(1, quantity - 10)) }
                QuantityButton("-1") { onQuantityChange(maxOf(1, quantity - 1)) }

                var quantityText by remember(quantity) { mutableStateOf(quantity.toString()) }

                Surface(
                    color = ColorItem,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, ColorBorder),
                    modifier = Modifier.width(54.dp).height(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        BasicTextField(
                            value = quantityText,
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { it.isDigit() }
                                quantityText = filtered
                                val parsed = filtered.toIntOrNull() ?: 1
                                if (parsed != quantity) {
                                    onQuantityChange(parsed)
                                }
                            },
                            textStyle = TextStyle(color = ColorTextMain, fontSize = 14.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            cursorBrush = SolidColor(ColorTextMain),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                QuantityButton("+1") { onQuantityChange(quantity + 1) }
                QuantityButton("+10") { onQuantityChange(quantity + 10) }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "합계:", color = ColorTextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                if (product.payment_type == "currency") {
                    val total = if (isBuyTab) product.buy_price.toLong() * quantity else product.sell_price.toLong() * quantity
                    Text(text = "${String.format("%,d", total)} 원", color = ColorWaiting, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                else {
                    val total = if (isBuyTab) product.barter_buy_amount.toLong() * quantity else product.barter_sell_amount.toLong() * quantity
                    val itemName = if (isBuyTab) barterBuyName else barterSellName
                    Text(text = "${String.format("%,d", total)} $itemName", color = ColorWaiting, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val canExecute = if (isBuyTab) {
                barterBuyCount.let { ShopClientValidation.canBuy(player, product, quantity, currentCurrency) }
            }
            else {
                ShopClientValidation.canSell(player, product, quantity) && productCount >= quantity
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable(enabled = canExecute) {
                        if (canExecute) {
                            ClientPlayNetworking.send(C2S_ShopTransactionPayload(shopId, productIndex, quantity, isBuyTab))
                        }
                    },
                color = if (canExecute) ColorAccent else ColorItem,
                border = BorderStroke(1.dp, if (canExecute) ColorAccent else ColorBorder),
                shape = RoundedCornerShape(6.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isBuyTab) "구매하기" else "판매하기",
                        color = if (canExecute) Color.White else ColorTextSub,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    @Composable
    fun QuantityButton(text: String, onClick: () -> Unit) {
        var isHovered by remember { mutableStateOf(false) }

        Surface(
            color = if (isHovered) ColorItem else ColorPanel,
            border = BorderStroke(1.dp, if (isHovered) ColorTextSub else ColorBorder),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .size(32.dp)
                .clickable { onClick() }
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
                }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = text, color = ColorTextMain, fontSize = 13.sp)
            }
        }
    }
}