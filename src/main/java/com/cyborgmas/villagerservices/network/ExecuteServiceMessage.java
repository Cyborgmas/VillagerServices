package com.cyborgmas.villagerservices.network;

import com.cyborgmas.villagerservices.client.ClientMerchantHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ExecuteServiceMessage {
   public ExecuteServiceMessage(){

   }

   public void handle(Supplier<NetworkEvent.Context> context){
      ServerPlayerEntity sp = Objects.requireNonNull(context.get().getSender(), "Packet sender was null while sending it server side!");
      context.get().enqueueWork(()-> ClientMerchantHandler.handleServiceExecution(this, sp));
      context.get().setPacketHandled(true);
   }
}
