package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import net.minecraft.item.ItemStack

class RenderItemTooltipEvent(val stack: ItemStack) : SkyHanniEvent()
