package club.redux.sunset.inventoryhelper.client

import club.redux.sunset.inventoryhelper.client.event.EventHandler
import com.github.dsx137.jable.extension.log4j
import net.fabricmc.api.ClientModInitializer

class InventoryHelperClient : ClientModInitializer {
    override fun onInitializeClient() {
        EventHandler.onInitialize()
        log4j.info("hello")
    }
}