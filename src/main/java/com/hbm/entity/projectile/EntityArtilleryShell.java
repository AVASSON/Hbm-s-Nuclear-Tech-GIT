package com.hbm.entity.projectile;

import com.hbm.entity.logic.IChunkLoader;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class EntityArtilleryShell extends EntityThrowableNT implements IChunkLoader {

	private Ticket loaderTicket;
	
	private int turnProgress;
	private double syncPosX;
	private double syncPosY;
	private double syncPosZ;
	private double syncYaw;
	private double syncPitch;
	@SideOnly(Side.CLIENT)
	private double velocityX;
	@SideOnly(Side.CLIENT)
	private double velocityY;
	@SideOnly(Side.CLIENT)
	private double velocityZ;

	private double targetX;
	private double targetY;
	private double targetZ;
	private boolean didWhistle = false;
	
	public EntityArtilleryShell(World world) {
		super(world);
		this.ignoreFrustumCheck = true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return true;
	}
	
	public void setTarget(int x, int y, int z) {
		this.targetX = x;
		this.targetY = y;
		this.targetZ = z;
	}
	
	@Override
	public void onUpdate() {
		
		if(!worldObj.isRemote) {
			super.onUpdate();
			
			if(!didWhistle) {
				double speed = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
				double deltaX = this.posX - this.targetX;
				double deltaZ = this.posZ - this.targetZ;
				double dist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
				
				if(speed * 18 > dist) {
					worldObj.playSoundEffect(this.targetX, this.targetY, this.targetZ, "hbm:turret.mortarWhistle", 5.0F, 1.0F);
					this.didWhistle = true;
				}
			}
			
		} else {
			if(this.turnProgress > 0) {
				double interpX = this.posX + (this.syncPosX - this.posX) / (double) this.turnProgress;
				double interpY = this.posY + (this.syncPosY - this.posY) / (double) this.turnProgress;
				double interpZ = this.posZ + (this.syncPosZ - this.posZ) / (double) this.turnProgress;
				double d = MathHelper.wrapAngleTo180_double(this.syncYaw - (double) this.rotationYaw);
				this.rotationYaw = (float) ((double) this.rotationYaw + d / (double) this.turnProgress);
				this.rotationPitch = (float)((double)this.rotationPitch + (this.syncPitch - (double)this.rotationPitch) / (double)this.turnProgress);
				--this.turnProgress;
				this.setPosition(interpX, interpY, interpZ);
			} else {
				this.setPosition(this.posX, this.posY, this.posZ);
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void setVelocity(double p_70016_1_, double p_70016_3_, double p_70016_5_) {
		this.velocityX = this.motionX = p_70016_1_;
		this.velocityY = this.motionY = p_70016_3_;
		this.velocityZ = this.motionZ = p_70016_5_;
	}
	
	@SideOnly(Side.CLIENT)
	public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int theNumberThree) {
		this.syncPosX = x;
		this.syncPosY = y;
		this.syncPosZ = z;
		this.syncYaw = yaw;
		this.syncPitch = pitch;
		this.turnProgress = theNumberThree;
		this.motionX = this.velocityX;
		this.motionY = this.velocityY;
		this.motionZ = this.velocityZ;
	}

	@Override
	protected void onImpact(MovingObjectPosition mop) {
		
		if(!worldObj.isRemote) {
			Vec3 vec = Vec3.createVectorHelper(motionX, motionY, motionZ).normalize();
			this.worldObj.newExplosion(this, mop.hitVec.xCoord - vec.xCoord, mop.hitVec.yCoord - vec.yCoord, mop.hitVec.zCoord - vec.zCoord, 15F, false, false);
			this.setDead();
		}
	}

	@Override
	public void init(Ticket ticket) {
		if(!worldObj.isRemote && ticket != null) {
			if(loaderTicket == null) {
				loaderTicket = ticket;
				loaderTicket.bindEntity(this);
				loaderTicket.getModData();
			}
			ForgeChunkManager.forceChunk(loaderTicket, new ChunkCoordIntPair(chunkCoordX, chunkCoordZ));
		}
	}

	@Override
	protected float getAirDrag() {
		return 1.0F;
	}

	@Override
	public double getGravityVelocity() {
		return 9.81 * 0.05;
	}
}