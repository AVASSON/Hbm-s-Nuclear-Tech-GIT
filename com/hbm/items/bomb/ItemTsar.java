package com.hbm.items.bomb;

import java.util.List;

import com.hbm.items.special.ItemRadioactive;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ItemTsar extends ItemRadioactive {
	
	public ItemTsar(float radiation) {
		super(radiation);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool)
	{
		list.add("Used in:");
		list.add("Tsar Bomba");
		super.addInformation(itemstack, player, list, bool);
	}

}
