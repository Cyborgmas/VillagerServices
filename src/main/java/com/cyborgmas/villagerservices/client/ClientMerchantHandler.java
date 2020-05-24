package com.cyborgmas.villagerservices.client;

import com.cyborgmas.villagerservices.VillagerServices;
import com.cyborgmas.villagerservices.gui.ServiceMerchantContainer;
import com.cyborgmas.villagerservices.network.ExecuteServiceMessage;
import com.cyborgmas.villagerservices.network.OpenServiceMerchantContainerMessage;
import com.cyborgmas.villagerservices.network.SelectServiceTradeMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;

public class ClientMerchantHandler {
   public static void handleOpenServiceMerchantContainerMessage(OpenServiceMerchantContainerMessage msg) {
      if(Minecraft.getInstance().player == null) { //should this crash?
         VillagerServices.LOGGER.error("The player is null on the client while trying to open a merchant container!");
         return;
      }

      Container container = Minecraft.getInstance().player.openContainer;
      if(msg.containerId == container.windowId && container instanceof ServiceMerchantContainer){
         ServiceMerchantContainer merc = (ServiceMerchantContainer) container;
         merc.setClientSideOffers(msg.offers);
         merc.setXp(msg.xp);
         merc.setMerchantLevel(msg.level);
         merc.setHasExperienceBar(msg.hasXpBar);
         merc.setHasLimitedTrades(msg.hasLimitedTrades);
      }
      else {
         VillagerServices.LOGGER.error("This should not happen! The OpenServiceMerchantContainerMessage was sent with the wrong information!");
      }
   }

   public static void handleSelectServiceTradeMessage(SelectServiceTradeMessage msg, ServerPlayerEntity player){
      int tradeId = msg.tradeId;
      Container container = player.openContainer;
      if(container instanceof ServiceMerchantContainer){
         ServiceMerchantContainer merc = (ServiceMerchantContainer) container;
         merc.setCurrentRecipeIndex(tradeId);
         merc.putAllValidPaymentsInTradeSlots(tradeId);
         //merc.changeTradeType(tradeId);
      } else {
         VillagerServices.LOGGER.error("This should not happen! The OpenServiceMerchantContainerMessage was sent with the wrong information!");
      }
   }

   public static void handleServiceExecution(ExecuteServiceMessage msg, ServerPlayerEntity player){
      Container container = player.openContainer;
      if(container instanceof ServiceMerchantContainer){
         ((ServiceMerchantContainer) container).executeService();
      } else {
         VillagerServices.LOGGER.error("This should not happen! The OpenServiceMerchantContainerMessage was sent with the wrong information!");
      }
   }
}
