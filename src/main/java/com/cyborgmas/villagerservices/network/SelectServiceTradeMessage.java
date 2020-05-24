package com.cyborgmas.villagerservices.network;

import com.cyborgmas.villagerservices.client.ClientMerchantHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class SelectServiceTradeMessage {
   public int tradeId;

   public SelectServiceTradeMessage(){

   }

   public SelectServiceTradeMessage(int tradeId){
      this.tradeId = tradeId;
   }

   public static SelectServiceTradeMessage fromBytes(PacketBuffer buffer){
      SelectServiceTradeMessage msg = new SelectServiceTradeMessage();
      msg.tradeId = buffer.readInt();
      return msg;
   }

   public void toBytes(PacketBuffer buffer){
      buffer.writeInt(this.tradeId);
   }

   public void handle(Supplier<NetworkEvent.Context> context){
      ServerPlayerEntity sp = Objects.requireNonNull(context.get().getSender(), "Packet sender was null while sending it server side!");
      context.get().enqueueWork(()-> ClientMerchantHandler.handleSelectServiceTradeMessage(this, sp));
      context.get().setPacketHandled(true);
   }
}
