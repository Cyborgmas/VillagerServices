package com.cyborgmas.villagerservices.trading;

import com.cyborgmas.villagerservices.registration.RegistryRegistration;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ServiceOffer extends ForgeRegistryEntry<ServiceOffer> {
   private final IService service;
   private final ServiceSide side;
   @Nullable
   public final ResourceLocation background;
   public final ResourceLocation texture;
   public final List<String> tooltip;

   public ServiceOffer(IService service, ResourceLocation texture, ServiceSide side){
      this(service, texture, null, Collections.emptyList(), side);
   }

   public ServiceOffer(IService service, ResourceLocation texture, List<String> tooltip, ServiceSide side){
      this(service, texture, null, tooltip, side);
   }

   /**
    * @param service the service to execute when the trade is completed.
    * @param tooltip must be a translation string, it will be used in {@link I18n#format(String, Object...)}
    * @param texture must be a 16 x 16 texture.
    * @param background must be a 26 x 26 texture.
    */
   public ServiceOffer(IService service, ResourceLocation texture, @Nullable ResourceLocation background, List<String> tooltip, ServiceSide side){
      this.service = service;
      this.texture = texture;
      this.background = background;
      this.tooltip = tooltip;
      this.side = side;
   }

   public static ServiceOffer getFromRegistry(String res) {
      return RegistryRegistration.services.getValue(new ResourceLocation(res));
   }

   public void executeService(PlayerEntity player){
      if(side == ServiceSide.BOTH) {
         service.executeService(player);
      } else if(side == ServiceSide.CLIENT && player instanceof ClientPlayerEntity) {
         service.executeService(player);
      } else if (side == ServiceSide.SERVER && player instanceof ServerPlayerEntity) {
         service.executeService(player);
      }
   }

   public enum ServiceSide {
      BOTH,
      CLIENT,
      SERVER
   }
}
