package com.cyborgmas.villagerservices.capability;

import com.cyborgmas.villagerservices.VillagerServices;
import com.cyborgmas.villagerservices.trading.ServiceMerchantOffer;
import com.cyborgmas.villagerservices.trading.ServiceOffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.nbt.*;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceSerializerCap implements IServiceSerializer, ICapabilitySerializable<CompoundNBT> {
   @CapabilityInject(IServiceSerializer.class)
   public static final Capability<IServiceSerializer> INSTANCE = null;

   private final LazyOptional<IServiceSerializer> holder = LazyOptional.of(()->this);
   public static final ResourceLocation NAME = VillagerServices.getId("service_serializer");

   private static final String NBT_IDS = "offer_ids";
   private static final String NBT_OFFERS = "offer_names";

   private final Map<Integer, String> offers = new HashMap<>();

   @Override
   public void serializeServices(MerchantOffers merchantOffers) {
      for(int i = 0; i < merchantOffers.size(); i ++){
         MerchantOffer offer = merchantOffers.get(i);
         if(offer instanceof ServiceMerchantOffer) {
            offers.put(i, ((ServiceMerchantOffer) offer).getName());
         }
      }
   }

   @Override
   public void deserializeServices(MerchantOffers merchantOffers) {
      offers.forEach((id, res) -> merchantOffers.set(id, new ServiceMerchantOffer(merchantOffers.get(id), ServiceOffer.getFromRegistry(res))));
   }

   @Nonnull
   @Override
   public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
      return INSTANCE.orEmpty(cap, holder);
   }

   public static boolean canAttachTo(Entity entity){
      return entity instanceof IMerchant &&
              !entity.getCapability(INSTANCE).isPresent();
   }

   public static void register() {
      CapabilityManager.INSTANCE.register(IServiceSerializer.class, new Storage(), ServiceSerializerCap::new);
   }

   @Override
   public CompoundNBT serializeNBT() {
      CompoundNBT nbt = new CompoundNBT();
      ListNBT ids = new ListNBT();
      ListNBT offerNames = new ListNBT();
      ids.addAll(offers.keySet().stream().map(IntNBT::valueOf).collect(Collectors.toList()));
      offerNames.addAll(offers.values().stream().map(StringNBT::valueOf).collect(Collectors.toList()));
      nbt.put(NBT_IDS, ids);
      nbt.put(NBT_OFFERS, offerNames);
      return nbt;
   }

   @Override
   public void deserializeNBT(CompoundNBT nbt) {
      List<Integer> ids =
              nbt.getList(NBT_IDS, Constants.NBT.TAG_INT)
              .stream()
              .filter(inbt -> inbt instanceof IntNBT)
              .map(inbt -> ((IntNBT)inbt).getInt())
              .collect(Collectors.toList());
      List<String> offerNames =
              nbt.getList(NBT_OFFERS, Constants.NBT.TAG_STRING)
              .stream()
              .filter(inbt -> inbt instanceof StringNBT)
              .map(INBT::getString)
              .collect(Collectors.toList());
      if(ids.size() != offerNames.size()) {
         throw new IllegalStateException("This is impossible! The two lists are the result of a map!");
      }
      for(int i = 0; i < ids.size(); i++){
         offers.put(ids.get(i), offerNames.get(i));
      }
   }

   static class Storage implements Capability.IStorage<IServiceSerializer> {
      @Nullable
      @Override
      public INBT writeNBT(Capability<IServiceSerializer> cap, IServiceSerializer instance, Direction side) {
         if(instance instanceof ServiceSerializerCap) {
            return ((ServiceSerializerCap) instance).serializeNBT();
         }
         return new CompoundNBT();
      }

      @Override
      public void readNBT(Capability<IServiceSerializer> cap, IServiceSerializer instance, Direction side, INBT nbt) {
         if(instance instanceof ServiceSerializerCap){
            ((ServiceSerializerCap) instance).deserializeNBT((CompoundNBT) nbt);
         }
      }
   }
}
