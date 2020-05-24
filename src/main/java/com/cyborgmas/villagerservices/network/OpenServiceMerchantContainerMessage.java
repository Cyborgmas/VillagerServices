package com.cyborgmas.villagerservices.network;

import com.cyborgmas.villagerservices.client.ClientMerchantHandler;
import net.minecraft.item.MerchantOffers;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenServiceMerchantContainerMessage {
   public int containerId;
   public MerchantOffers offers;
   public int level;
   public int xp;
   public boolean hasXpBar;
   public boolean hasLimitedTrades;

   public OpenServiceMerchantContainerMessage(){

   }

   public OpenServiceMerchantContainerMessage(int id, MerchantOffers offers, int level, int xp, boolean hasXpBar, boolean hasLimitedTrades){
      this.containerId = id;
      this.offers = offers;
      this.level = level;
      this.xp = xp;
      this.hasXpBar = hasXpBar;
      this.hasLimitedTrades = hasLimitedTrades;
   }

   public static OpenServiceMerchantContainerMessage fromBytes(PacketBuffer buf){
      OpenServiceMerchantContainerMessage msg = new OpenServiceMerchantContainerMessage();
      msg.containerId = buf.readVarInt();
      msg.offers = OffersToPacketBuffer.read(buf); //THIS CAUSES TO LOSE DATA ABOUT SERVICETRADES! IMPLEMENT MY OWN!
      msg.level = buf.readVarInt();
      msg.xp = buf.readVarInt();
      msg.hasXpBar = buf.readBoolean();
      msg.hasLimitedTrades = buf.readBoolean();
      return msg;
   }

   public void toBytes(PacketBuffer buf){
      buf.writeVarInt(this.containerId);
      OffersToPacketBuffer.write(buf, this.offers);
      buf.writeVarInt(this.level);
      buf.writeVarInt(this.xp);
      buf.writeBoolean(this.hasXpBar);
      buf.writeBoolean(this.hasLimitedTrades);
   }

   public void handle(Supplier<NetworkEvent.Context> context) {
      context.get().enqueueWork(() -> ClientMerchantHandler.handleOpenServiceMerchantContainerMessage(this));
      context.get().setPacketHandled(true);
   }
}
