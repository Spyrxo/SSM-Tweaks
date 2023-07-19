package xyz.gholemgaeming.entity;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;

public class CustomEntityArrow extends EntityArrow {

    /*
     * The functions below are private in the parent super-class, with no way to access without reflection or slower means.
     * To circumvent this while still allowing for regular arrow behavior with no slowdowns in terms of access, we will
     * just re-create the functions in this custom class, and let the onTick() method handle the behavior like a regular arrow.
     */

    /** XYZ Block pos of a stuck arrow **/
    private int xTile = -1;
    private int yTile = -1;
    private int zTile = -1;

    /** Block the arrow is currently stuck in **/
    private Block inTile;
    private int inData;

    /** Is the arrow currently stuck in a block? **/
    private boolean inGround;

    /** Tickers for being stuck in a block or flying in the air **/
    private int ticksInGround;
    private int ticksInAir;

    /** Collision interaction values **/
    private int knockbackStrength;
    private double damage = 2.0d;

    /** {@link CustomEntityArrow} Default {@link World} constructor. **/
    public CustomEntityArrow(World nmsWorld) {
        super(nmsWorld);
    }

    /** {@link CustomEntityArrow} Default {@link World} Position constructor.
     * (this ones called to summon arrows in a "general" non-identified way. **/
    public CustomEntityArrow(World nmsWorld, double posX, double posY, double posZ) {
        super(nmsWorld, posX, posY, posZ);
    }

    /** {@link CustomEntityArrow} Player shooter constructor.
     * (this ones called to summon arrows when a 'player' entity shoots a bow) **/
    public CustomEntityArrow(World nmsWorld, EntityLivingBase shooter, float velocity) {
        super(nmsWorld, shooter, velocity);
    }

    /** {@link CustomEntityArrow} Entity shooter contructor.
     * (this ones called to summon arrows when an entity like a 'skeleton' shoots a bow) **/
    public CustomEntityArrow(World nmsWorld, EntityLivingBase shooterEntity, EntityLivingBase targetEntity, float velocity, float inaccuracy) {
        super(nmsWorld, shooterEntity, targetEntity, velocity, inaccuracy);
    }

    /** The main <method>onTick()</method> update method which is called every tick by
     * the minecraft world instance. This handles the arrows position, motion and collision
     * logic.
     * <p>
     * For our custom arrow instance, we can basically just copy the source code exactly to
     * keep the arrow logic consistent, but we have gutted out the code that lets it "bounce".
     * This way, the visuals are consistent, but you will never see the client-side bouncing.
     * **/
    @Override
    public void onUpdate() {

        // [!] important, we call the parent entity ticking method to handle the profiler
        super.onEntityUpdate();

        // [] now we just handle the normal arrow logic
        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
            float lvt_1_1_ = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.prevRotationYaw = this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 180.0 / 3.1415927410125732);
            this.prevRotationPitch = this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)lvt_1_1_) * 180.0 / 3.1415927410125732);
        }

        BlockPos lvt_1_2_ = new BlockPos(this.xTile, this.yTile, this.zTile);
        IBlockState lvt_2_1_ = this.worldObj.getBlockState(lvt_1_2_);
        Block lvt_3_1_ = lvt_2_1_.getBlock();
        if (lvt_3_1_.getMaterial() != Material.air) {
            lvt_3_1_.setBlockBoundsBasedOnState(this.worldObj, lvt_1_2_);
            AxisAlignedBB lvt_4_1_ = lvt_3_1_.getCollisionBoundingBox(this.worldObj, lvt_1_2_, lvt_2_1_);
            if (lvt_4_1_ != null && lvt_4_1_.isVecInside(new Vec3(this.posX, this.posY, this.posZ))) {
                this.inGround = true;
            }
        }

        if (this.arrowShake > 0) {
            --this.arrowShake;
        }

        if (this.inGround) {
            int lvt_4_2_ = lvt_3_1_.getMetaFromState(lvt_2_1_);
            if (lvt_3_1_ == this.inTile && lvt_4_2_ == this.inData) {
                ++this.ticksInGround;
                if (this.ticksInGround >= 1200) {
                    this.setDead();
                }
            } else {
                this.inGround = false;
                this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
                this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
                this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
                this.ticksInGround = 0;
                this.ticksInAir = 0;
            }

        } else {
            ++this.ticksInAir;
            Vec3 lvt_4_3_ = new Vec3(this.posX, this.posY, this.posZ);
            Vec3 lvt_5_1_ = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            MovingObjectPosition lvt_6_1_ = this.worldObj.rayTraceBlocks(lvt_4_3_, lvt_5_1_, false, true, false);
            lvt_4_3_ = new Vec3(this.posX, this.posY, this.posZ);
            lvt_5_1_ = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            if (lvt_6_1_ != null) {
                lvt_5_1_ = new Vec3(lvt_6_1_.hitVec.xCoord, lvt_6_1_.hitVec.yCoord, lvt_6_1_.hitVec.zCoord);
            }

            Entity lvt_7_1_ = null;
            List<Entity> lvt_8_1_ = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0, 1.0, 1.0));
            double lvt_9_1_ = 0.0;

            int lvt_11_5_;
            float lvt_13_5_;
            for(lvt_11_5_ = 0; lvt_11_5_ < lvt_8_1_.size(); ++lvt_11_5_) {
                Entity lvt_12_1_ = (Entity)lvt_8_1_.get(lvt_11_5_);
                if (lvt_12_1_.canBeCollidedWith() && (lvt_12_1_ != this.shootingEntity || this.ticksInAir >= 5)) {
                    lvt_13_5_ = 0.3F;
                    AxisAlignedBB lvt_14_1_ = lvt_12_1_.getEntityBoundingBox().expand((double)lvt_13_5_, (double)lvt_13_5_, (double)lvt_13_5_);
                    MovingObjectPosition lvt_15_1_ = lvt_14_1_.calculateIntercept(lvt_4_3_, lvt_5_1_);
                    if (lvt_15_1_ != null) {
                        double lvt_16_1_ = lvt_4_3_.squareDistanceTo(lvt_15_1_.hitVec);
                        if (lvt_16_1_ < lvt_9_1_ || lvt_9_1_ == 0.0) {
                            lvt_7_1_ = lvt_12_1_;
                            lvt_9_1_ = lvt_16_1_;
                        }
                    }
                }
            }

            if (lvt_7_1_ != null) {
                lvt_6_1_ = new MovingObjectPosition(lvt_7_1_);
            }

            if (lvt_6_1_ != null && lvt_6_1_.entityHit != null && lvt_6_1_.entityHit instanceof EntityPlayer) {
                EntityPlayer lvt_11_2_ = (EntityPlayer)lvt_6_1_.entityHit;
                if (lvt_11_2_.capabilities.disableDamage || this.shootingEntity instanceof EntityPlayer && !((EntityPlayer)this.shootingEntity).canAttackPlayer(lvt_11_2_)) {
                    lvt_6_1_ = null;
                }
            }

            float lvt_11_6_;
            float lvt_15_2_;
            if (lvt_6_1_ != null) {
                if (lvt_6_1_.entityHit != null) {
                    lvt_11_6_ = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                    int lvt_12_2_ = MathHelper.ceiling_double_int((double)lvt_11_6_ * this.damage);
                    if (this.getIsCritical()) {
                        lvt_12_2_ += this.rand.nextInt(lvt_12_2_ / 2 + 2);
                    }

                    DamageSource lvt_13_3_;
                    if (this.shootingEntity == null) {
                        lvt_13_3_ = DamageSource.causeArrowDamage(this, this);
                    } else {
                        lvt_13_3_ = DamageSource.causeArrowDamage(this, this.shootingEntity);
                    }

                    if (this.isBurning() && !(lvt_6_1_.entityHit instanceof EntityEnderman)) {
                        lvt_6_1_.entityHit.setFire(5);
                    }

                    if (lvt_6_1_.entityHit.attackEntityFrom(lvt_13_3_, (float)lvt_12_2_)) {
                        if (lvt_6_1_.entityHit instanceof EntityLivingBase) {
                            EntityLivingBase lvt_14_2_ = (EntityLivingBase)lvt_6_1_.entityHit;
                            if (!this.worldObj.isRemote) {
                                lvt_14_2_.setArrowCountInEntity(lvt_14_2_.getArrowCountInEntity() + 1);
                            }

                            if (this.knockbackStrength > 0) {
                                lvt_15_2_ = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
                                if (lvt_15_2_ > 0.0F) {
                                    lvt_6_1_.entityHit.addVelocity(this.motionX * (double)this.knockbackStrength * 0.6000000238418579 / (double)lvt_15_2_, 0.1, this.motionZ * (double)this.knockbackStrength * 0.6000000238418579 / (double)lvt_15_2_);
                                }
                            }

                            if (this.shootingEntity instanceof EntityLivingBase) {
                                EnchantmentHelper.applyThornEnchantments(lvt_14_2_, this.shootingEntity);
                                EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase)this.shootingEntity, lvt_14_2_);
                            }

                            if (this.shootingEntity != null && lvt_6_1_.entityHit != this.shootingEntity && lvt_6_1_.entityHit instanceof EntityPlayer && this.shootingEntity instanceof EntityPlayerMP) {
                                ((EntityPlayerMP)this.shootingEntity).playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(6, 0.0F));
                            }
                        }

                        this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                        if (!(lvt_6_1_.entityHit instanceof EntityEnderman)) {
                            this.setDead();
                        }
                    }

                    // YOU SEE THIS SHIT HERE, THIS IS WHY IT ARROW BOUNCES, GET DA FUUUUUUUUUUUUUUUUGGIN OUTTA HERE
                    /*else {
                        this.motionX *= -0.10000000149011612;
                        this.motionY *= -0.10000000149011612;
                        this.motionZ *= -0.10000000149011612;
                        this.rotationYaw += 180.0F;
                        this.prevRotationYaw += 180.0F;
                        this.ticksInAir = 0;
                    }*/

                } else {
                    BlockPos lvt_11_4_ = lvt_6_1_.getBlockPos();
                    this.xTile = lvt_11_4_.getX();
                    this.yTile = lvt_11_4_.getY();
                    this.zTile = lvt_11_4_.getZ();
                    IBlockState lvt_12_3_ = this.worldObj.getBlockState(lvt_11_4_);
                    this.inTile = lvt_12_3_.getBlock();
                    this.inData = this.inTile.getMetaFromState(lvt_12_3_);
                    this.motionX = (double)((float)(lvt_6_1_.hitVec.xCoord - this.posX));
                    this.motionY = (double)((float)(lvt_6_1_.hitVec.yCoord - this.posY));
                    this.motionZ = (double)((float)(lvt_6_1_.hitVec.zCoord - this.posZ));
                    lvt_13_5_ = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                    this.posX -= this.motionX / (double)lvt_13_5_ * 0.05000000074505806;
                    this.posY -= this.motionY / (double)lvt_13_5_ * 0.05000000074505806;
                    this.posZ -= this.motionZ / (double)lvt_13_5_ * 0.05000000074505806;
                    this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                    this.inGround = true;
                    this.arrowShake = 7;
                    this.setIsCritical(false);
                    if (this.inTile.getMaterial() != Material.air) {
                        this.inTile.onEntityCollidedWithBlock(this.worldObj, lvt_11_4_, lvt_12_3_, this);
                    }
                }
            }

            if (this.getIsCritical()) {
                for(lvt_11_5_ = 0; lvt_11_5_ < 4; ++lvt_11_5_) {
                    this.worldObj.spawnParticle(EnumParticleTypes.CRIT, this.posX + this.motionX * (double)lvt_11_5_ / 4.0, this.posY + this.motionY * (double)lvt_11_5_ / 4.0, this.posZ + this.motionZ * (double)lvt_11_5_ / 4.0, -this.motionX, -this.motionY + 0.2, -this.motionZ, new int[0]);
                }
            }

            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            lvt_11_6_ = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 180.0 / 3.1415927410125732);

            for(this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)lvt_11_6_) * 180.0 / 3.1415927410125732); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
            }

            while(this.rotationPitch - this.prevRotationPitch >= 180.0F) {
                this.prevRotationPitch += 360.0F;
            }

            while(this.rotationYaw - this.prevRotationYaw < -180.0F) {
                this.prevRotationYaw -= 360.0F;
            }

            while(this.rotationYaw - this.prevRotationYaw >= 180.0F) {
                this.prevRotationYaw += 360.0F;
            }

            this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
            this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
            float lvt_12_4_ = 0.99F;
            lvt_13_5_ = 0.05F;
            if (this.isInWater()) {
                for(int lvt_14_3_ = 0; lvt_14_3_ < 4; ++lvt_14_3_) {
                    lvt_15_2_ = 0.25F;
                    this.worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * (double)lvt_15_2_, this.posY - this.motionY * (double)lvt_15_2_, this.posZ - this.motionZ * (double)lvt_15_2_, this.motionX, this.motionY, this.motionZ, new int[0]);
                }

                lvt_12_4_ = 0.6F;
            }

            if (this.isWet()) {
                this.extinguish();
            }

            this.motionX *= (double)lvt_12_4_;
            this.motionY *= (double)lvt_12_4_;
            this.motionZ *= (double)lvt_12_4_;
            this.motionY -= (double)lvt_13_5_;
            this.setPosition(this.posX, this.posY, this.posZ);
            this.doBlockCollisions();
        }
    }

    @Override
    public void setDamage(double damage) {
        this.damage = damage;
        super.setDamage(this.damage);
    }

    @Override
    public void setKnockbackStrength(int knockbackStrength) {
        this.knockbackStrength = knockbackStrength;
        super.setKnockbackStrength(this.knockbackStrength);
    }
}
