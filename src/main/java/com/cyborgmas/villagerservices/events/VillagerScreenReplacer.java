package com.cyborgmas.villagerservices.events;

import com.cyborgmas.villagerservices.VillagerServices;
import com.cyborgmas.villagerservices.gui.ServiceMerchantContainer;
import com.cyborgmas.villagerservices.network.Network;
import com.cyborgmas.villagerservices.network.OpenServiceMerchantContainerMessage;
import com.cyborgmas.villagerservices.trading.ServiceMerchantOffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.ActionResultType;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkHooks;

@Mod.EventBusSubscriber(modid = VillagerServices.MOD_ID)
public class VillagerScreenReplacer {
   @SubscribeEvent
   public static void onVillagerInteraction(PlayerInteractEvent.EntityInteract event) {
      Entity entity = event.getTarget();
      PlayerEntity player = event.getPlayer();
      if(!(entity instanceof IMerchant) || !(player instanceof ServerPlayerEntity)) return;
      ServerPlayerEntity sp = (ServerPlayerEntity) player;
      IMerchant merchant = (IMerchant) entity;
      MerchantOffers offers = merchant.getOffers();
      if(offers.isEmpty()) return;
      NetworkHooks.openGui(sp, new SimpleNamedContainerProvider((id, inv, customer)-> new ServiceMerchantContainer(id, inv, merchant), entity.getDisplayName()), buf -> {});
      if(merchant instanceof VillagerEntity) {
         ((VillagerEntity) merchant).recalculateSpecialPricesFor(sp);
      }
      merchant.setCustomer(sp);
      OpenServiceMerchantContainerMessage msg =
              new OpenServiceMerchantContainerMessage(sp.currentWindowId, offers, getLevelFromVillager(entity), merchant.getXp(), merchant.func_213705_dZ(), merchant.func_223340_ej());
      Network.channel.sendTo(msg, sp.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
      event.setCancellationResult(ActionResultType.SUCCESS);
   }

   private static int getLevelFromVillager(Entity entity){ //TODO make pr to forge so that getLevel is an IMerchant impl instead of being directly on the villager.
      if(entity instanceof VillagerEntity){
         return ((VillagerEntity)entity).getVillagerData().getLevel();
      }
      return 1;
   }
}
