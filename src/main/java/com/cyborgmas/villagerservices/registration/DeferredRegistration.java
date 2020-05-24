package com.cyborgmas.villagerservices.registration;

import com.cyborgmas.villagerservices.VillagerServices;
import com.cyborgmas.villagerservices.gui.ServiceMerchantContainer;
import com.cyborgmas.villagerservices.trading.ServiceOffer;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class DeferredRegistration {
   private static final DeferredRegister<ContainerType<?>> CONTAINERS = new DeferredRegister<>(ForgeRegistries.CONTAINERS, VillagerServices.MOD_ID);
   private static final DeferredRegister<ServiceOffer> SERVICES = new DeferredRegister<>(RegistryRegistration.services, VillagerServices.MOD_ID);

   public static final RegistryObject<ContainerType<ServiceMerchantContainer>> SERVICE_MERCHANT_CONTAINER =
           CONTAINERS.register("service_merchant_container", () -> IForgeContainerType.create(ServiceMerchantContainer::new));

   private static final RegistryObject<ServiceOffer> DEFAULT_NULL_OFFER = SERVICES.register("null_service",
           ()-> new ServiceOffer(
                   player -> VillagerServices.LOGGER.error("Missing service in registry - data loss most likely occurred. " +
                           "This could be due to have switched between versions of a mod."),
                   MissingTextureSprite.getLocation(),
                   ServiceOffer.ServiceSide.SERVER
           ));

   public static final RegistryObject<ServiceOffer> TEST_OFFER = SERVICES.register("test_service",
           ()-> new ServiceOffer(
                   player -> player.sendMessage(new StringTextComponent("TEST WORKS YAY!")),
                   VillagerServices.getId("textures/gui/test_texture.png"),
                   VillagerServices.getId("textures/gui/test_background.png"),
                   ImmutableList.of("serviceoffer.killPlayer"),
                   ServiceOffer.ServiceSide.CLIENT
           ));

   public static final RegistryObject<ServiceOffer> SUMMON_LIGHTNING_BOLT = SERVICES.register("summon_lightning_service",
           ()-> new ServiceOffer(
                   player -> player.sendMessage(new StringTextComponent("Will hit you with lightning!")),//player.world.addEntity(new LightningBoltEntity(player.world, player.getPosX(), player.getPosY(), player.getPosZ(), false)),
                   VillagerServices.getId("textures/gui/summon_lightning.png"),
                   ImmutableList.of("serviceoffer.lightning"),
                   ServiceOffer.ServiceSide.SERVER
           ));

   public static void registerAll(IEventBus bus){
      CONTAINERS.register(bus);
      SERVICES.register(bus);
   }
}
