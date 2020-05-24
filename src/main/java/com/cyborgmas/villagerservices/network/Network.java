package com.cyborgmas.villagerservices.network;

import com.cyborgmas.villagerservices.VillagerServices;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Objects;

public class Network {
   public static SimpleChannel channel;
   private static final ResourceLocation NAME = VillagerServices.getId("network");
   private static int id = 0;

   public static void init(){
      channel = NetworkRegistry.ChannelBuilder.named(NAME)
              .clientAcceptedVersions(s -> Objects.equals(s, "1"))
              .serverAcceptedVersions(s -> Objects.equals(s, "1"))
              .networkProtocolVersion(() -> "1")
              .simpleChannel();

      channel.messageBuilder(OpenServiceMerchantContainerMessage.class, id++)
              .decoder(OpenServiceMerchantContainerMessage::fromBytes)
              .encoder(OpenServiceMerchantContainerMessage::toBytes)
              .consumer(OpenServiceMerchantContainerMessage::handle)
              .add();

      channel.messageBuilder(SelectServiceTradeMessage.class, id++)
              .decoder(SelectServiceTradeMessage::fromBytes)
              .encoder(SelectServiceTradeMessage::toBytes)
              .consumer(SelectServiceTradeMessage::handle)
              .add();

      channel.messageBuilder(ExecuteServiceMessage.class, id++)
              .decoder(buffer -> new ExecuteServiceMessage())
              .encoder((executeServiceMessage, buffer) -> {})
              .consumer(ExecuteServiceMessage::handle)
              .add();
   }
}
