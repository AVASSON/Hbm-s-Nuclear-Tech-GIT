package com.hbm.tileentity;

import java.util.List;
import java.util.Random;

import com.hbm.handler.FluidTypeHandler.FluidType;
import com.hbm.interfaces.IConsumer;
import com.hbm.interfaces.IFluidContainer;
import com.hbm.inventory.FluidTank;
import com.hbm.inventory.MachineRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemBattery;
import com.hbm.items.tool.ItemAssemblyTemplate;
import com.hbm.lib.Library;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.TEAssemblerPacket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityMachineChemplant extends TileEntity implements ISidedInventory, IConsumer, IFluidContainer {

	private ItemStack slots[];

	public int power;
	public static final int maxPower = 100000;
	public int progress;
	public int maxProgress = 100;
	public float rotation = 0;
	int age = 0;
	int consumption = 100;
	int speed = 100;
	public FluidTank[] tanks;
	
	Random rand = new Random();
	
	private String customName;
	
	public TileEntityMachineChemplant() {
		slots = new ItemStack[21];
		tanks = new FluidTank[4];
		tanks[0] = new FluidTank(FluidType.NONE, 16000, 0);
		tanks[1] = new FluidTank(FluidType.NONE, 16000, 0);
		tanks[2] = new FluidTank(FluidType.NONE, 16000, 0);
		tanks[3] = new FluidTank(FluidType.NONE, 16000, 0);
		tanks[0].index = 0;
		tanks[1].index = 1;
		tanks[2].index = 2;
		tanks[3].index = 3;
		tanks[0].takeIn = true;
		tanks[1].takeIn = true;
		tanks[2].takeIn = false;
		tanks[3].takeIn = false;
		tanks[0].letOut = false;
		tanks[1].letOut = false;
		tanks[2].letOut = true;
		tanks[3].letOut = true;
	}

	@Override
	public int getSizeInventory() {
		return slots.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return slots[i];
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		if(slots[i] != null)
		{
			ItemStack itemStack = slots[i];
			slots[i] = null;
			return itemStack;
		} else {
		return null;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemStack) {
		slots[i] = itemStack;
		if(itemStack != null && itemStack.stackSize > getInventoryStackLimit())
		{
			itemStack.stackSize = getInventoryStackLimit();
		}
	}

	@Override
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.chemplant";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return this.customName != null && this.customName.length() > 0;
	}
	
	public void setCustomName(String name) {
		this.customName = name;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		if(worldObj.getTileEntity(xCoord, yCoord, zCoord) != this)
		{
			return false;
		}else{
			return player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <=128;
		}
	}
	
	//You scrubs aren't needed for anything (right now)
	@Override
	public void openInventory() {}
	@Override
	public void closeInventory() {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		if(i == 0)
			if(itemStack.getItem() instanceof ItemBattery)
				return true;
		
		if(i == 1)
			return true;
		
		return false;
	}
	
	@Override
	public ItemStack decrStackSize(int i, int j) {
		if(slots[i] != null)
		{
			if(slots[i].stackSize <= j)
			{
				ItemStack itemStack = slots[i];
				slots[i] = null;
				return itemStack;
			}
			ItemStack itemStack1 = slots[i].splitStack(j);
			if (slots[i].stackSize == 0)
			{
				slots[i] = null;
			}
			
			return itemStack1;
		} else {
			return null;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList list = nbt.getTagList("items", 10);
		
		this.power = nbt.getInteger("powerTime");
		slots = new ItemStack[getSizeInventory()];
		
		for(int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if(b0 >= 0 && b0 < slots.length)
			{
				slots[b0] = ItemStack.loadItemStackFromNBT(nbt1);
			}
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("powerTime", power);
		NBTTagList list = new NBTTagList();
		
		for(int i = 0; i < slots.length; i++)
		{
			if(slots[i] != null)
			{
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("slot", (byte)i);
				slots[i].writeToNBT(nbt1);
				list.appendTag(nbt1);
			}
		}
		nbt.setTag("items", list);
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(int p_94128_1_)
    {
        return new int[] { 0 };
    }

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, int j) {
		return this.isItemValidForSlot(i, itemStack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		return false;
	}
	
	public int getPowerScaled(int i) {
		return (power * i) / maxPower;
	}
	
	public int getProgressScaled(int i) {
		return (progress * i) / maxProgress;
	}
	
	@Override
	public void updateEntity() {

		if(!worldObj.isRemote)
		{
			power = Library.chargeTEFromItems(slots, 0, power, maxPower);
			
			tanks[0].loadTank(17, 19, slots);
			tanks[1].loadTank(18, 20, slots);
			tanks[2].unloadTank(9, 11, slots);
			tanks[3].unloadTank(10, 12, slots);
		}
		
	}
	
	//I can't believe that worked.
	public ItemStack[] cloneItemStackProper(ItemStack[] array) {
		ItemStack[] stack = new ItemStack[array.length];
		
		for(int i = 0; i < array.length; i++)
			if(array[i] != null)
				stack[i] = array[i].copy();
			else
				stack[i] = null;
		
		return stack;
	}
	
	//Unloads output into chests
	public boolean tryFillContainer(IInventory inventory, int slot) {
		
		int size = inventory.getSizeInventory();

		for(int i = 0; i < size; i++) {
			if(inventory.getStackInSlot(i) != null) {
				
				if(slots[slot] == null)
					return false;
				
				ItemStack sta1 = inventory.getStackInSlot(i).copy();
				ItemStack sta2 = slots[slot].copy();
				if(sta1 != null && sta2 != null) {
					sta1.stackSize = 1;
					sta2.stackSize = 1;
				
					if(ItemStack.areItemStacksEqual(sta1, sta2) && ItemStack.areItemStackTagsEqual(sta1, sta2) && inventory.getStackInSlot(i).stackSize < inventory.getStackInSlot(i).getMaxStackSize()) {
						slots[slot].stackSize--;
						
						if(slots[slot].stackSize <= 0)
							slots[slot] = null;
						
						ItemStack sta3 = inventory.getStackInSlot(i).copy();
						sta3.stackSize++;
						inventory.setInventorySlotContents(i, sta3);
					
						return true;
					}
				}
			}
		}
		for(int i = 0; i < size; i++) {
			
			if(slots[slot] == null)
				return false;
			
			ItemStack sta2 = slots[slot].copy();
			if(inventory.getStackInSlot(i) == null && sta2 != null) {
				sta2.stackSize = 1;
				slots[slot].stackSize--;
				
				if(slots[slot].stackSize <= 0)
					slots[slot] = null;
				
				inventory.setInventorySlotContents(i, sta2);
					
				return true;
			}
		}
		
		return false;
	}
	
	//Loads assembler's input queue from chests
	public boolean tryFillAssembler(IInventory inventory, int slot) {
		
		if(MachineRecipes.getOutputFromTempate(slots[4]) == null || MachineRecipes.getRecipeFromTempate(slots[4]) == null)
			return false;
		else {
			List<ItemStack> list = MachineRecipes.getRecipeFromTempate(slots[4]);
			
			for(int i = 0; i < list.size(); i++)
				list.get(i).stackSize = 1;


			if(inventory.getStackInSlot(slot) == null)
				return false;
			
			ItemStack stack = inventory.getStackInSlot(slot).copy();
			stack.stackSize = 1;
			
			boolean flag = false;
			
			for(int i = 0; i < list.size(); i++)
				if(ItemStack.areItemStacksEqual(stack, list.get(i)) && ItemStack.areItemStackTagsEqual(stack, list.get(i)))
					flag = true;
			
			if(!flag)
				return false;
			
		}
		
		for(int i = 6; i < 18; i++) {
			
			if(slots[i] != null) {
			
				ItemStack sta1 = inventory.getStackInSlot(slot).copy();
				ItemStack sta2 = slots[i].copy();
				if(sta1 != null && sta2 != null) {
					sta1.stackSize = 1;
					sta2.stackSize = 1;
			
					if(ItemStack.areItemStacksEqual(sta1, sta2) && ItemStack.areItemStackTagsEqual(sta1, sta2) && slots[i].stackSize < slots[i].getMaxStackSize()) {
						ItemStack sta3 = inventory.getStackInSlot(slot).copy();
						sta3.stackSize--;
						if(sta3.stackSize <= 0)
							sta3 = null;
						inventory.setInventorySlotContents(slot, sta3);
				
						slots[i].stackSize++;
						return true;
					}
				}
			}
		}
		
		for(int i = 6; i < 18; i++) {

			ItemStack sta2 = inventory.getStackInSlot(slot).copy();
			if(slots[i] == null && sta2 != null) {
				sta2.stackSize = 1;
				slots[i] = sta2.copy();
				
				ItemStack sta3 = inventory.getStackInSlot(slot).copy();
				sta3.stackSize--;
				if(sta3.stackSize <= 0)
					sta3 = null;
				inventory.setInventorySlotContents(slot, sta3);
				
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hasSpace(ItemStack stack) {

		ItemStack st = stack.copy();
		
		if(st == null)
			return true;
		
		for(int i = 1; i < 10; i++) {
			if(slots[i] == null)
				return true;
		}
		
		int size = st.stackSize;
		st.stackSize = 1;
		
		ItemStack[] fakeArray = slots.clone();
		boolean flag = true;
		for(int i = 0; i < stack.stackSize; i++) {
			if(!canAddItemToArray(st, fakeArray))
				flag = false;
		}
		
		return flag;
	}
	
	public void addItemToInventory(ItemStack stack) {

		ItemStack st = stack.copy();
		
		if(st == null)
			return;
		
		int size = st.stackSize;
		st.stackSize = 1;
		
		for(int i = 0; i < size; i++)
			canAddItemToArray(st, this.slots);
		
	}
	
	public boolean canAddItemToArray(ItemStack stack, ItemStack[] array) {

		ItemStack st = stack.copy();
		
		if(st == null)
			return true;
		
		for(int i = 1; i < 10; i++) {
			
			if(array[i] != null) {
				ItemStack sta = array[i].copy();
			
				if(sta != null && sta.getItem() == st.getItem() && sta.stackSize < st.getMaxStackSize()) {
					array[i].stackSize++;
					return true;
				}
			}
		}
		
		for(int i = 1; i < 10; i++) {
			if(array[i] == null) {
				array[i] = stack.copy();
				return true;
			}
		}
		
		return false;
	}
	
	//boolean true: remove items, boolean false: simulation mode
	public boolean removeItems(List<ItemStack> stack, ItemStack[] array) {
		
		if(stack == null)
			return false;
		
		for(int i = 0; i < stack.size(); i++) {
			for(int j = 0; j < stack.get(i).stackSize; j++) {
				ItemStack sta = stack.get(i).copy();
				sta.stackSize = 1;
			
				if(!canRemoveItemFromArray(sta, array))
					return false;
			}
		}
		
		return true;
		
	}
	
	public boolean canRemoveItemFromArray(ItemStack stack, ItemStack[] array) {

		ItemStack st = stack.copy();
		
		if(st == null)
			return true;
		
		for(int i = 6; i < 18; i++) {
			
			if(array[i] != null) {
				ItemStack sta = array[i].copy();
				sta.stackSize = 1;
			
				if(sta != null && isItemAcceptible(sta, st) && array[i].stackSize > 0) {
					array[i].stackSize--;
					
					if(array[i].stackSize <= 0)
						array[i] = null;
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean isItemAcceptible(ItemStack stack1, ItemStack stack2) {
		
		if(stack1 != null && stack2 != null) {
			if(ItemStack.areItemStacksEqual(stack1, stack2))
				return true;
		
			int[] ids1 = OreDictionary.getOreIDs(stack1);
			int[] ids2 = OreDictionary.getOreIDs(stack2);
			
			if(ids1 != null && ids2 != null && ids1.length > 0 && ids2.length > 0) {
				for(int i = 0; i < ids1.length; i++)
					for(int j = 0; j < ids2.length; j++)
						if(ids1[i] == ids2[j])
							return true;
			}
		}
		
		return false;
	}

	@Override
	public void setPower(int i) {
		power = i;
		
	}

	@Override
	public int getPower() {
		return power;
		
	}

	@Override
	public int getMaxPower() {
		return maxPower;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}

	@Override
	public void setFillstate(int fill, int index) {
		if(index < 4 && tanks[index] != null)
			tanks[index].setFill(fill);
	}

	@Override
	public void setType(FluidType type, int index) {
		if(index < 4 && tanks[index] != null)
			tanks[index].setTankType(type);
	}
}