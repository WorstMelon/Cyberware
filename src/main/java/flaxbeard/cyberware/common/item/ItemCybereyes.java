package flaxbeard.cyberware.common.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.client.ClientUtils;
import flaxbeard.cyberware.common.handler.EssentialsMissingHandler;
import flaxbeard.cyberware.common.lib.LibConstants;

public class ItemCybereyes extends ItemCyberware
{

	public ItemCybereyes(String name, EnumSlot slot)
	{
		super(name, slot);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public boolean isEssential(ItemStack stack)
	{
		return true;		
	}
	
	@Override
	public boolean isIncompatible(ItemStack stack, ItemStack other)
	{
		return CyberwareAPI.getCyberware(other).isEssential(other);
	}
	
	@SubscribeEvent
	public void handleBlindnessImmunity(CyberwareUpdateEvent event)
	{
		EntityLivingBase e = event.getEntityLiving();
		
		if (CyberwareAPI.isCyberwareInstalled(e, new ItemStack(this)))
		{
			e.removePotionEffect(MobEffects.BLINDNESS);
		}
		
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void handleMissingEssentials(CyberwareUpdateEvent event)
	{
		EntityLivingBase e = event.getEntityLiving();
				
		if (CyberwareAPI.isCyberwareInstalled(e, new ItemStack(this)))
		{
			if (e.ticksExisted % 20 == 0)
			{
				boolean powerUsed = CyberwareAPI.getCapability(e).usePower(new ItemStack(this), getPowerConsumption(ItemStack.EMPTY));
				if (e.world.isRemote && e == Minecraft.getMinecraft().player)
				{
					isBlind = !powerUsed;
				}
			}
		}
		else if (e.world.isRemote && e == Minecraft.getMinecraft().player)
		{
			isBlind = false;
		}
		
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void overlayPre(RenderGameOverlayEvent.Pre event)
	{
		if (event.getType() == ElementType.ALL)
		{
			EntityPlayer e = Minecraft.getMinecraft().player;
			
			if (isBlind && !e.isCreative())
			{
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.color(1F, 1F, 1F, .9F);
				Minecraft.getMinecraft().getTextureManager().bindTexture(EssentialsMissingHandler.BLACK_PX);
				ClientUtils.drawTexturedModalRect(0, 0, 0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
				GlStateManager.popMatrix();
			}
		}
	}
	
	@Override
	public int getPowerConsumption(ItemStack stack)
	{
		return LibConstants.CYBEREYES_CONSUMPTION;
	}

	private static boolean isBlind;
}
