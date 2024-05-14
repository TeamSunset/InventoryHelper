package club.redux.sunset.inventoryhelper.client.event

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.item.Items
import net.minecraft.screen.slot.SlotActionType

object EventHandler {
    fun onInitialize() {
        ClientTickEvents.START_CLIENT_TICK.register {
            val client = MinecraftClient.getInstance()
            val player = client.player ?: return@register
            if (player.isCreative || player.isSpectator) return@register
            val screen = InventoryScreen(player)
            val slots = screen.screenHandler.slots

            val click = { slot: Int, button: Int, actionType: SlotActionType ->
                client.interactionManager?.clickSlot(
                    screen.screenHandler.syncId,
                    slot,
                    button,
                    actionType,
                    player
                )
            }

            val stringSlots = slots.filter { it.stack.item == Items.STRING }
            val craftingSlots = listOf(
                slots[1],
                slots[2],
                slots[3],
                slots[4],
            )
            val otherSlots = slots.filter { it !in craftingSlots }
            if (screen.screenHandler == null && otherSlots.any { it.stack.item == Items.AIR }) {
                if (stringSlots.sumOf { it.stack.count } >= 4) {
                    stringSlots.forEach {
                        click(it.id, 0, SlotActionType.PICKUP)
                        click(it.id, 0, SlotActionType.QUICK_CRAFT)
                        craftingSlots.filter { slot -> slot.stack.item == Items.AIR }.forEach { slot ->
                            click(slot.id, 1, SlotActionType.QUICK_CRAFT)
                        }
                        click(it.id, 2, SlotActionType.QUICK_CRAFT)
                    }
                    if (slots[0].stack.item != Items.AIR) {
                        click(0, 0, SlotActionType.QUICK_MOVE)
                    }
                } else {
                    craftingSlots.forEach {
                        if (it.stack.item != Items.AIR) {
                            click(it.id, 0, SlotActionType.QUICK_MOVE)
                        }
                    }
                }
            }
        }
    }
}