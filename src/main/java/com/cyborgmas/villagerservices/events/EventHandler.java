package com.cyborgmas.villagerservices.events;

import com.cyborgmas.villagerservices.VillagerServices;
import com.cyborgmas.villagerservices.capability.ServiceSerializerCap;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.CrashReportExtender;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

@Mod.EventBusSubscriber(modid = VillagerServices.MOD_ID)
public class EventHandler {
   @SubscribeEvent
   public static void attachCap(AttachCapabilitiesEvent<Entity> event) {
      Entity entity = event.getObject();
      if(ServiceSerializerCap.canAttachTo(entity)){
         event.addCapability(ServiceSerializerCap.NAME, new ServiceSerializerCap());
      }
   }

   @SubscribeEvent
   public static void villagerDeath(LivingDeathEvent event){
      Entity entity = event.getEntity();
      if(entity instanceof IMerchant) {
         entity.getCapability(ServiceSerializerCap.INSTANCE).invalidate();
      }
   }

   @SubscribeEvent
   public static void deserializeServices(FMLServerStartedEvent event){
      try {
         event.getServer().getWorlds().forEach(world ->
                 world.getEntities().filter(entity ->
                         entity instanceof IMerchant).forEach(merchant ->
                         merchant.getCapability(ServiceSerializerCap.INSTANCE).ifPresent(serializer ->
                                 serializer.deserializeServices(((IMerchant)merchant).getOffers()))));
      } catch (Throwable throwable) {
         CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Attempting to deserialize service offers and add them to merchants");
         event.getServer().addServerInfoToCrashReport(crashReport);
         throw new ReportedException(crashReport);
      }
   }

   @SubscribeEvent
   public static void serializeServices(FMLServerStoppingEvent event){
      try {
         event.getServer().getWorlds().forEach(world ->
                 world.getEntities().filter(entity ->
                         entity instanceof IMerchant).forEach(merchant ->
                         merchant.getCapability(ServiceSerializerCap.INSTANCE).ifPresent(serializer ->
                                 serializer.serializeServices(((IMerchant)merchant).getOffers()))));
      } catch (Throwable throwable) {
         CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Attempting to serialize service offers from merchants");
         event.getServer().addServerInfoToCrashReport(crashReport);
         throw new ReportedException(crashReport);
      }
   }
}
