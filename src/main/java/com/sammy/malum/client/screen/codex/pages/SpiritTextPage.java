package com.sammy.malum.client.screen.codex.pages;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sammy.malum.client.screen.codex.ProgressionBookScreen;
import com.sammy.malum.core.helper.DataHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SpiritTextPage extends BookPage
{
    private final String headlineTranslationKey;
    private final String translationKey;
    private final ItemStack spiritStack;
    public SpiritTextPage(String headlineTranslationKey, String translationKey, ItemStack spiritStack)
    {
        super(DataHelper.prefix("textures/gui/book/pages/spirit_page.png"));
        this.headlineTranslationKey = headlineTranslationKey;
        this.translationKey = translationKey;
        this.spiritStack = spiritStack;
    }
    public SpiritTextPage(String headlineTranslationKey, String translationKey, Item spirit)
    {
        this(headlineTranslationKey,translationKey,spirit.getDefaultInstance());
    }

    public String headlineTranslationKey()
    {
        return "malum.gui.book.entry.page.headline." + headlineTranslationKey;
    }
    public String translationKey()
    {
        return "malum.gui.book.entry.page.text." + translationKey;
    }
    @Override
    public void renderLeft(Minecraft minecraft, PoseStack poseStack, float xOffset, float yOffset, int mouseX, int mouseY, float partialTicks)
    {
        int guiLeft = guiLeft();
        int guiTop = guiTop();
        Component component = new TranslatableComponent(headlineTranslationKey());
        ProgressionBookScreen.renderText(poseStack, component, guiLeft+75 - minecraft.font.width(component.getString())/2,guiTop+10);
        ProgressionBookScreen.renderWrappingText(poseStack, translationKey(), guiLeft+16,guiTop+79,125);
        ProgressionBookScreen.renderItem(poseStack, spiritStack, guiLeft+67, guiTop+44,mouseX,mouseY);
    }

    @Override
    public void renderRight(Minecraft minecraft, PoseStack poseStack, float xOffset, float yOffset, int mouseX, int mouseY, float partialTicks)
    {
        int guiLeft = guiLeft();
        int guiTop = guiTop();
        Component component = new TranslatableComponent(headlineTranslationKey());
        ProgressionBookScreen.renderText(poseStack, component, guiLeft+218 - minecraft.font.width(component.getString())/2,guiTop+10);
        ProgressionBookScreen.renderWrappingText(poseStack, translationKey(), guiLeft+158,guiTop+79,125);
        ProgressionBookScreen.renderItem(poseStack, spiritStack, guiLeft+209, guiTop+44,mouseX,mouseY);
    }
}
