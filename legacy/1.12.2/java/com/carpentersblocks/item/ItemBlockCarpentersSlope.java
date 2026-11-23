package com.carpentersblocks.item;

import com.carpentersblocks.block.BlockCarpentersSlope;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/**
 * Slope item: one subtype per slope offered in the creative tab, like 1.7.10.
 */
public class ItemBlockCarpentersSlope extends ItemBlock {

	public ItemBlockCarpentersSlope(Block block) {
		super(block);
		setHasSubtypes(true);
		setMaxDamage(0);
	}

	@Override
	public int getMetadata(int damage) {
		return damage;
	}

	@Override
	public String getTranslationKey(ItemStack itemStack) {
		int damage = itemStack.getItemDamage();
		if (damage < 0 || damage >= BlockCarpentersSlope.slopeType.length) {
			damage = 0;
		}
		return super.getTranslationKey(itemStack) + "." + BlockCarpentersSlope.slopeType[damage];
	}

}
