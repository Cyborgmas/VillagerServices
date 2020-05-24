package com.cyborgmas.villagerservices.gui;

import com.cyborgmas.villagerservices.network.ExecuteServiceMessage;
import com.cyborgmas.villagerservices.network.Network;
import com.cyborgmas.villagerservices.network.SelectServiceTradeMessage;
import com.cyborgmas.villagerservices.trading.ServiceMerchantOffer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.merchant.villager.VillagerData;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

import java.util.stream.Collectors;

public class ServiceMerchantScreen extends ContainerScreen<ServiceMerchantContainer> {
   private static final ResourceLocation MERCHANT_GUI_TEXTURE = new ResourceLocation("textures/gui/container/villager2.png");
   //VillagerServices.getId("textures/gui/container/servicevillager.png");
   private int selectedMerchantTrade;
   private final TradeButton[] buttons = new TradeButton[7];
   private Button serviceButton;
   private int scrollOffset;
   private boolean mouseClicked;

   public ServiceMerchantScreen(ServiceMerchantContainer container, PlayerInventory inv, ITextComponent titleIn) {
      super(container, inv, titleIn);
      this.xSize = 276;
   }

   @Override
   protected void init() {
      super.init();
      int xPos = (this.width - this.xSize) / 2;
      int yPos = (this.height - this.ySize) / 2;
      int yOffset = yPos + 16 + 2;

      for(int i = 0; i < 7; ++i) {
         this.buttons[i] = this.addButton(new TradeButton(xPos + 5, yOffset, i, (button) -> {
            if (button instanceof TradeButton) {
               this.selectedMerchantTrade = ((TradeButton)button).getGUITradeOffset() + this.scrollOffset;
               this.selectTrade();
            }
         }));
         yOffset += 20;
      }

      serviceButton = this.addButton(new Button(xPos + 204, yPos + 61,50, 20,"Execute", (button) -> this.executeService()));
   }

   private void executeService(){
      this.container.executeService();
      Network.channel.sendToServer(new ExecuteServiceMessage());
   }

   private void selectTrade() {
      this.container.setCurrentRecipeIndex(this.selectedMerchantTrade);
      this.container.putAllValidPaymentsInTradeSlots(this.selectedMerchantTrade);
      //this.container.changeTradeType(this.selectedMerchantTrade);
      SelectServiceTradeMessage msg = new SelectServiceTradeMessage(this.selectedMerchantTrade);
      Network.channel.sendToServer(msg);
   }

   @Override
   protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
      int i = this.container.getMerchantLevel();
      int j = this.ySize - 94;
      if (i > 0 && i <= 5 && this.container.getHasExperienceBar()) {
         String s2 = this.title.getFormattedText();
         String s1 = "- " + I18n.format("merchant.level." + i);
         int k = this.font.getStringWidth(s2);
         int l = this.font.getStringWidth(s1);
         int i1 = k + l + 3;
         int j1 = 49 + this.xSize / 2 - i1 / 2;
         this.font.drawString(s2, (float)j1, 6.0F, 4210752);
         this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 107.0F, (float)j, 4210752);
         this.font.drawString(s1, (float)(j1 + k + 3), 6.0F, 4210752);
      } else {
         String s = this.title.getFormattedText();
         this.font.drawString(s, (float)(49 + this.xSize / 2 - this.font.getStringWidth(s) / 2), 6.0F, 4210752);
         this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 107.0F, (float)j, 4210752);
      }

      String s3 = I18n.format("merchant.trades");
      int k1 = this.font.getStringWidth(s3);
      this.font.drawString(s3, (float)(5 - k1 / 2 + 48), 6.0F, 4210752);
   }

   protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.getTextureManager().bindTexture(MERCHANT_GUI_TEXTURE);
      int xInit = (this.width - this.xSize) / 2;
      int yInit = (this.height - this.ySize) / 2;
      blit(xInit, yInit, this.getBlitOffset(), 0.0F, 0.0F, this.xSize, this.ySize, 256, 512);
      MerchantOffers merchantoffers = this.container.getOffers();
      if (!merchantoffers.isEmpty()) {
         int k = this.selectedMerchantTrade;
         if (k < 0 || k >= merchantoffers.size()) {
            return;
         }
         MerchantOffer offer = merchantoffers.get(k);
         //here perform check for if service trade
         if(this.container.canExecuteService()) {
            drawServiceBackgroundTexture(xInit, yInit, offer);
         }
         this.minecraft.getTextureManager().bindTexture(MERCHANT_GUI_TEXTURE);
         if (offer.hasNoUsesLeft()) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            blit(this.guiLeft + 83 + 99, this.guiTop + 35, this.getBlitOffset(), 311.0F, 0.0F, 28, 21, 256, 512);
         }
      }
   }

   @Override
   public void render(int mouseX, int mouseY, float partialTicks) {
      this.renderBackground();
      super.render(mouseX, mouseY, partialTicks);
      MerchantOffers offers = this.container.getOffers();
      if (!offers.isEmpty()) {
         int xInit = (this.width - this.xSize) / 2;
         int yInit = (this.height - this.ySize) / 2;
         int yOffset = yInit + 16 + 1;
         int xOffset = xInit + 5 + 5;
         RenderSystem.pushMatrix();
         RenderSystem.enableRescaleNormal();
         this.minecraft.getTextureManager().bindTexture(MERCHANT_GUI_TEXTURE);
         this.renderScrollBar(xInit, yInit, offers);
         int offerIndex = 0;

         for(MerchantOffer offer : offers) {
            if (this.doTradesNotFit(offers.size()) && (offerIndex < this.scrollOffset || offerIndex >= 7 + this.scrollOffset)) {
               ++offerIndex;
            } else {
               ItemStack firstPrice = offer.getBuyingStackFirst();
               ItemStack actualPrice = offer.func_222205_b();
               ItemStack secondPrice = offer.getBuyingStackSecond();
               ItemStack result = offer.getSellingStack();
               this.itemRenderer.zLevel = 100.0F;
               int itemYOffset = yOffset + 2;
               this.renderFirstPrice(actualPrice, firstPrice, xOffset, itemYOffset);
               if (!secondPrice.isEmpty()) {
                  this.itemRenderer.renderItemAndEffectIntoGUI(secondPrice, xInit + 5 + 35, itemYOffset);
                  this.itemRenderer.renderItemOverlays(this.font, secondPrice, xInit + 5 + 35, itemYOffset);
               }

               this.renderArrow(offer, xInit, itemYOffset);
               if(offer instanceof ServiceMerchantOffer){
                  this.renderFrontServiceTextures(xInit + 5 + 68, itemYOffset, ((ServiceMerchantOffer) offer).getTexture());
               } else {
                  this.itemRenderer.renderItemAndEffectIntoGUI(result, xInit + 5 + 68, itemYOffset);
                  this.itemRenderer.renderItemOverlays(this.font, result, xInit + 5 + 68, itemYOffset);
               }
               this.itemRenderer.zLevel = 0.0F;
               yOffset += 20;
               ++offerIndex;
            }
         }

         if (this.container.getHasExperienceBar()) {
            this.renderXpBar(xInit, yInit);
         }

         for(TradeButton button : this.buttons) {
            if (button.isHovered()) {
               button.renderToolTip(mouseX, mouseY);
            }
            button.visible = button.GUITradeOffset < this.container.getOffers().size();
         }

         MerchantOffer currentTrade = this.selectedMerchantTrade == -1 ? null : offers.get(this.selectedMerchantTrade);
         if (currentTrade != null && currentTrade.hasNoUsesLeft() && this.isPointInRegion(186, 35, 22, 21, mouseX, mouseY) && this.container.hasLimitedTrades()) {
            this.renderTooltip(I18n.format("merchant.deprecated"), mouseX, mouseY);
         }

         boolean isService = currentTrade instanceof ServiceMerchantOffer;
         if(isService) {
            boolean canExec = this.container.canExecuteService();
            serviceButton.visible = canExec;
            if(canExec && this.isPointInRegion(215, 33, 26, 26, mouseX, mouseY)) {
               this.renderTooltip(((ServiceMerchantOffer)currentTrade).getTooltip().stream().map(I18n::format).collect(Collectors.toList()), mouseX, mouseY);
            }
         } else {
            serviceButton.visible = false;
         }
         RenderSystem.popMatrix();
         RenderSystem.enableDepthTest();
      }
      this.renderHoveredToolTip(mouseX, mouseY);
   }

   private void drawServiceBackgroundTexture(int x, int y, MerchantOffer offer){
      if(offer instanceof ServiceMerchantOffer) {
         ResourceLocation background = ((ServiceMerchantOffer) offer).getBackground();
         ResourceLocation texture = ((ServiceMerchantOffer) offer).getTexture();
         if(background != null) {
            this.minecraft.getTextureManager().bindTexture(background);
            blit(x+215,y+33,this.getBlitOffset()+50,0,0,26,26, 26,26);
         }
         this.minecraft.getTextureManager().bindTexture(texture);
         blit(x+215+5,y+33+5,this.getBlitOffset()+100, 0,0,16, 16, 16, 16);
      }
   }

   private void renderFrontServiceTextures(int x, int y, ResourceLocation texture){
      this.minecraft.getTextureManager().bindTexture(texture);
      blit(x, y,this.getBlitOffset()+50, 0, 0 , 16, 16, 16, 16);
      this.minecraft.getTextureManager().bindTexture(MERCHANT_GUI_TEXTURE);
   }

   //verify this is a correct name
   private void renderXpBar(int x, int y) {
      this.minecraft.getTextureManager().bindTexture(MERCHANT_GUI_TEXTURE);
      int i = this.container.getMerchantLevel();
      int j = this.container.getXp();
      if (i < 5) {
         blit(x + 136, y + 16, this.getBlitOffset(), 0.0F, 186.0F, 102, 5, 256, 512);
         int k = VillagerData.func_221133_b(i); //these seem to be level related function
         if (j >= k && VillagerData.func_221128_d(i)) { //this one too
            int l = 100;
            float f = (float)(100 / (VillagerData.func_221127_c(i) - k));
            int i1 = Math.min(MathHelper.floor(f * (float)(j - k)), 100);
            blit(x + 136, y + 16, this.getBlitOffset(), 0.0F, 191.0F, i1 + 1, 5, 256, 512);
            int j1 = this.container.getPendingExp();
            if (j1 > 0) {
               int k1 = Math.min(MathHelper.floor((float)j1 * f), 100 - i1);
               blit(x + 136 + i1 + 1, y + 16 + 1, this.getBlitOffset(), 2.0F, 182.0F, k1, 3, 256, 512);
            }
         }
      }
   }

   private void renderScrollBar(int x, int y, MerchantOffers offers) {
      int i = offers.size() + 1 - 7;
      if (i > 1) {
         int j = 139 - (27 + (i - 1) * 139 / i);
         int k = 1 + j / i + 139 / i;
         int yOffset = Math.min(113, this.scrollOffset * k);
         if (this.scrollOffset == i - 1) {
            yOffset = 113;
         }
         blit(x + 94, y + 18 + yOffset, this.getBlitOffset(), 0.0F, 199.0F, 6, 27, 256, 512);
      } else {
         blit(x + 94, y + 18, this.getBlitOffset(), 6.0F, 199.0F, 6, 27, 256, 512);
      }
   }

   private void renderArrow(MerchantOffer offer, int x, int y) {
      RenderSystem.enableBlend();
      this.minecraft.getTextureManager().bindTexture(MERCHANT_GUI_TEXTURE);
      if (offer.hasNoUsesLeft()) {
         blit(x + 5 + 35 + 20, y + 3, this.getBlitOffset(), 25.0F, 171.0F, 10, 9, 256, 512);
      } else {
         blit(x + 5 + 35 + 20, y + 3, this.getBlitOffset(), 15.0F, 171.0F, 10, 9, 256, 512);
      }
   }

   private void renderFirstPrice(ItemStack actualPrice, ItemStack firstPrice, int x, int y) {
      this.itemRenderer.renderItemAndEffectIntoGUI(actualPrice, x, y);
      if (firstPrice.getCount() == actualPrice.getCount()) {
         this.itemRenderer.renderItemOverlays(this.font, actualPrice, x, y);
      } else {
         this.itemRenderer.renderItemOverlayIntoGUI(this.font, firstPrice, x, y, firstPrice.getCount() == 1 ? "1" : null);
         this.itemRenderer.renderItemOverlayIntoGUI(this.font, actualPrice, x + 14, y, actualPrice.getCount() == 1 ? "1" : null);
         this.minecraft.getTextureManager().bindTexture(MERCHANT_GUI_TEXTURE);
         this.setBlitOffset(this.getBlitOffset() + 300);
         blit(x + 7, y + 12, this.getBlitOffset(), 0.0F, 176.0F, 9, 2, 256, 512);
         this.setBlitOffset(this.getBlitOffset() - 300);
      }
   }

   @Override
   public boolean mouseScrolled(double x, double y, double scrollAmount) {
      int i = this.container.getOffers().size();
      if (this.doTradesNotFit(i)) {
         int j = i - 7;
         this.scrollOffset = (int)((double)this.scrollOffset - scrollAmount);
         this.scrollOffset = MathHelper.clamp(this.scrollOffset, 0, j);
      }
      return true;
   }

   @Override
   public boolean mouseDragged(double x, double y, int mouseCode, double xOffset, double yOffset) { //not sure for last 2 names. Only use is in advancmentScreen
      int i = this.container.getOffers().size();
      if (this.mouseClicked) {
         int j = this.guiTop + 18;
         int k = j + 139;
         int l = i - 7;
         float f = ((float)y - (float)j - 13.5F) / ((float)(k - j) - 27.0F);
         f = f * (float)l + 0.5F;
         this.scrollOffset = MathHelper.clamp((int)f, 0, l);
         return true;
      } else {
         return super.mouseDragged(x, y, mouseCode, xOffset, yOffset);
      }
   }

   @Override
   public boolean mouseClicked(double x, double y, int mouseCode) {
      this.mouseClicked = false;
      int i = (this.width - this.xSize) / 2;
      int j = (this.height - this.ySize) / 2;
      if (this.doTradesNotFit(this.container.getOffers().size())
              && x > (double)(i + 94) && x < (double)(i + 94 + 6) && y > (double)(j + 18) && y <= (double)(j + 18 + 139 + 1)) {
         this.mouseClicked = true;
      }
      return super.mouseClicked(x, y, mouseCode);
   }

   //only 7 trades at a time can be displayed.
   public boolean doTradesNotFit(int trades){
      return trades > 7;
   }

   class TradeButton extends Button {
      final int GUITradeOffset; //this can only be between 0 and 7 (excluded) since there can only ever be 7 buttons in GUI

      public TradeButton(int x, int y, int GUITradeOffset, Button.IPressable onPress) {
         super(x, y, 89, 20, "", onPress);
         this.GUITradeOffset = GUITradeOffset;
         this.visible = false;
      }

      public int getGUITradeOffset() {
         return this.GUITradeOffset;
      }

      @Override
      public void onPress() { //doing it on each tick currently. would this be a better alternative?
         //ServiceMerchantScreen.this.serviceButton.visible = ServiceMerchantScreen.this.container.getOffers().get(GUITradeOffset + ServiceMerchantScreen.this.scrollOffset) instanceof ServiceMerchantOffer;
         super.onPress();
      }

      public void renderToolTip(int x, int y) {
         //prevents when some buttons are missing/not there
         if (this.isHovered && ServiceMerchantScreen.this.container.getOffers().size() > this.GUITradeOffset + ServiceMerchantScreen.this.scrollOffset) {
            if (x < this.x + 20) {
               ItemStack firstStackPrice = ServiceMerchantScreen.this.container.getOffers().get(this.GUITradeOffset + ServiceMerchantScreen.this.scrollOffset).func_222205_b();
               ServiceMerchantScreen.this.renderTooltip(firstStackPrice, x, y);
            } else if (x < this.x + 50 && x > this.x + 30) {
               ItemStack secondStackPrice = ServiceMerchantScreen.this.container.getOffers().get(this.GUITradeOffset + ServiceMerchantScreen.this.scrollOffset).getBuyingStackSecond();
               if (!secondStackPrice.isEmpty()) {
                  ServiceMerchantScreen.this.renderTooltip(secondStackPrice, x, y);
               }
            } else if (x > this.x + 65) {
               int id = this.GUITradeOffset + ServiceMerchantScreen.this.scrollOffset;
               MerchantOffer offer =  ServiceMerchantScreen.this.container.getOffers().get(id);
               if(offer instanceof ServiceMerchantOffer) { //TODO render tooltip also in the result slot location
                  ServiceMerchantScreen.this.renderTooltip(((ServiceMerchantOffer) offer).getTooltip().stream().map(I18n::format).collect(Collectors.toList()), x, y);
               } else {
                  ItemStack offerStack = offer.getSellingStack();
                  ServiceMerchantScreen.this.renderTooltip(offerStack, x, y);
               }
            }
         }
      }
   }
}