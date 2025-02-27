package at.hannibal2.skyhanni.features.inventory.experimentationtable

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.features.inventory.experimentationtable.ExperimentationTableApi.bookPattern
import at.hannibal2.skyhanni.features.inventory.experimentationtable.ExperimentationTableApi.ultraRarePattern
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ColorUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils.createSound
import at.hannibal2.skyhanni.utils.SoundUtils.playSound
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.renderXYAligned
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object UltraRareBookAlert {

    private val config get() = SkyHanniMod.feature.inventory.experimentationTable
    private val dragonSound by lazy { createSound("mob.enderdragon.growl", 1f) }

    private var enchantsFound = false

    private var lastNotificationTime = SimpleTimeMark.farPast()

    private fun notification(enchantsName: String) {
        lastNotificationTime = SimpleTimeMark.now()
        dragonSound.playSound()
        ChatUtils.chat("You have uncovered a §d§kXX§5 ULTRA-RARE BOOK! §d§kXX§e! You found: §9$enchantsName")
    }

    @HandleEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (lastNotificationTime.passedSince() > 5.seconds) return
        val gui = Minecraft.getMinecraft().currentScreen as? GuiContainer ?: return

        GlStateManager.pushMatrix()
        GlStateManager.translate(0f, -150f, 500f)

        Renderable.drawInsideRoundedRect(
            Renderable.string("§d§kXX§5 ULTRA-RARE BOOK! §d§kXX", 1.5),
            ColorUtils.TRANSPARENT_COLOR,
            horizontalAlign = RenderUtils.HorizontalAlignment.CENTER,
            verticalAlign = RenderUtils.VerticalAlignment.CENTER,
        ).renderXYAligned(0, 125, gui.width, gui.height)

        GlStateManager.translate(0f, 150f, -500f)
        GlStateManager.popMatrix()
    }

    @HandleEvent
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (!isEnabled()) return
        if (enchantsFound) return

        for (lore in event.inventoryItems.map { it.value.getLore() }) {
            val firstLine = lore.firstOrNull() ?: continue
            if (!ultraRarePattern.matches(firstLine)) continue
            val bookNameLine = lore.getOrNull(2) ?: continue
            bookPattern.matchMatcher(bookNameLine) {
                val enchantsName = group("enchant")
                notification(enchantsName)
                enchantsFound = true
            }
        }
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        enchantsFound = false
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(59, "inventory.helper.enchanting.ultraRareBookAlert", "inventory.experimentationTable.ultraRareBookAlert")
    }

    private fun isEnabled() =
        LorenzUtils.inSkyBlock && config.ultraRareBookAlert && ExperimentationTableApi.currentExperiment != null
}
