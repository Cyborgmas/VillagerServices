package com.cyborgmas.villagerservices.events;

import com.cyborgmas.villagerservices.gui.ServiceMerchantScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import java.util.List;

/**
 * These events are fired when drawing all the related textures into the {@link ServiceMerchantScreen}
 * These are {@link Cancelable}, in which case normal blitting won't happen
 * These allow for more control over what is drawn pertaining to Services.
 *
 * REMINDER THESE ARE ALL {@link Dist#CLIENT} ONLY AS SPECIFIED IN {@link GuiScreenEvent}
 */
@Cancelable
public class VillagerServiceDrawEvent extends GuiScreenEvent {
   public final int xOffset;
   public final int yOffset;

   /**
    * The last 2 params represent the top-left corner of where the rendering occurs.
    * @param gui the {@link ServiceMerchantScreen} being rendered
    * @param xOffset the xPosition that would be used to render.
    * @param yOffset the yPosition that would be used to render.
    */
   public VillagerServiceDrawEvent(ServiceMerchantScreen gui, int xOffset, int yOffset) {
      super(gui);
      this.xOffset = xOffset;
      this.yOffset = yOffset;
   }

   @Override
   public ServiceMerchantScreen getGui() {
      return (ServiceMerchantScreen) super.getGui();
   }

   public static class TextureDrawEvent extends VillagerServiceDrawEvent {
      public final ResourceLocation texture;
      /**
       * @param gui the {@link ServiceMerchantScreen} being rendered
       * @param texture the texture being rendered. Can be null when using {@link DrawBackground}
       * @param xOffset the xPosition that would be used to draw.
       * @param yOffset the yPosition that would be used to draw.
       *                The last 2 params represent the top-left corner of where the texture is drawn.
       */
      public TextureDrawEvent(ServiceMerchantScreen gui, ResourceLocation texture, int xOffset, int yOffset) {
         super(gui, xOffset, yOffset);
         this.texture = texture;
      }
   }

   public static class DrawBackground extends TextureDrawEvent {
      /**
       * @param background a 26 x 26 texture
       */
      public DrawBackground(ServiceMerchantScreen gui, ResourceLocation background, int xOffset, int yOffset) {
         super(gui, background, xOffset, yOffset);
      }
   }

   public static class DrawServiceIconInTradeButton extends TextureDrawEvent {
      /**
       * @param texture a 16 x 16 texture
       */
      public DrawServiceIconInTradeButton(ServiceMerchantScreen gui, ResourceLocation texture, int xOffset, int yOffset) {
         super(gui, texture, xOffset, yOffset);
      }
   }

   public static class DrawServiceIconInSlot extends TextureDrawEvent {
      /**
       * @param texture a 16 x 16 texture
       */
      public DrawServiceIconInSlot(ServiceMerchantScreen gui, ResourceLocation texture, int xOffset, int yOffset) {
         super(gui, texture, xOffset, yOffset);
      }
   }

   /**
    * This event can be canceled, though that probably leads to making it impossible to execute the service.
    */
   public static class DrawButton extends VillagerServiceDrawEvent {
      public final Button button;

      /**
       * @param button The button drawn under the service "slot" that executes the service when clicked.
       *               Most of the fields in the button are not final, they can be easily changed to move the button
       *               around or change its text.
       */
      public DrawButton(ServiceMerchantScreen gui, Button button) {
         super(gui, button.x, button.y);
         this.button = button;
      }
   }

   /**
    * Fired before rendering one of the 2 tooltips.
    */
   public static class ToolTipEvent extends VillagerServiceDrawEvent {
      public final List<String> tooltip;
      public final boolean slotTooltip;
      public final int mouseX;
      public final int mouseY;

      /**
       * @param xLeft constant position depending on isSlot
       * @param yTop constant position depending on isSlot
       * @param tooltip the strings that will be rendered - they were previously passed through {@link I18n#format} but won't be again
       *                Can't increase the location of where to render the tooltip but can make it conditional
       *                and change it based on the position of the mouse compared to the top-left corner.
       * @param isSlot True if rendering the tooltip when hovering over the slot. The hovering area will be 26x26.
       *               False if rendering the tooltip when hovering over the icon in the button. The hovering area will be 16x16
       */
      public ToolTipEvent(ServiceMerchantScreen gui, int xLeft, int yTop, int mouseX, int mouseY, List<String> tooltip, boolean isSlot) {
         super(gui, xLeft, yTop);
         this.tooltip = tooltip;
         this.slotTooltip = isSlot;
         this.mouseX = mouseX;
         this.mouseY = mouseY;
      }
   }
}
