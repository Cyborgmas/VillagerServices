package com.cyborgmas.villagerservices.registration;

import com.cyborgmas.villagerservices.VillagerServices;
import com.cyborgmas.villagerservices.trading.ServiceOffer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber(modid= VillagerServices.MOD_ID, bus= Mod.EventBusSubscriber.Bus.MOD)
public class RegistryRegistration {
   public static IForgeRegistry<ServiceOffer> services;

   @SubscribeEvent
   public static void registerRegistries(RegistryEvent.NewRegistry event) {
      services = new RegistryBuilder<ServiceOffer>()
              .setType(ServiceOffer.class)
              .setName(VillagerServices.getId("service_offer_registry"))
              .setDefaultKey(VillagerServices.getId("null_service"))
              .create();
      DeferredRegistration.registerAll(FMLJavaModLoadingContext.get().getModEventBus());
   }
}
