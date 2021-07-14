package uk.joshiejack.gastronomy.tileentity;

import net.minecraft.tileentity.ITickableTileEntity;
import uk.joshiejack.gastronomy.cooking.Appliance;
import uk.joshiejack.gastronomy.tileentity.base.TileCookingFluids;

public class MixerTileEntity extends TileCookingFluids implements ITickableTileEntity {
    private static final float MAX_OFFSET1 = 0.5F;
    private static final float MIN_OFFSET1 = -0.5F;
    private static final float MAX_OFFSET2 = 0F / 1.75F;
    private static final float MIN_OFFSET2 = 1F / 1.75F;

    public float blade = 0F;
    public MixerTileEntity() {
        super(Appliance.MIXER, GastronomyTileEntities.MIXER.get());
    }

    @Override
    public void tick() {

    }
//
//    @Override
//    public SpecialRenderData createRenderData() {
//        return new SpecialRenderDataCooking() {
//            @Override
//            public void rotate(World world) {
//                for (int k = 0; k < rotations.length; k++) {
//                    rotations[k] = rotations[k] + 10F;
//                }
//
//                for (int k = 0; k < offset1.length; k++) {
//                    if (world.rand.nextFloat() < 0.1F) {
//                        offset1[k] = clampOffset1(offset1[k] + (world.rand.nextBoolean() ? 0.05F : -0.05F));
//                        offset2[k] = clampOffset2(offset2[k] + (world.rand.nextBoolean() ? 0.05F : -0.05F));
//                    }
//                }
//            }
//        };
//    }
//
//    @Override
//    public void animate() {
//        super.animate();
//        blade += 100F;
//        //Play the sound effect
//        if (cookTimer == 1) {
//            world.playSound(pos.getX(), pos.getY() + 0.5D, pos.getZ(), GastronomySounds.MIXER, SoundCategory.BLOCKS, 1.5F, world.rand.nextFloat() * 0.1F + 0.9F, false);
//        }
//    }
//
//    private float clampOffset1(float f) {
//        return Math.max(MIN_OFFSET1, Math.min(MAX_OFFSET1, f));
//    }
//
//    private float clampOffset2(float f) {
//        return Math.max(MIN_OFFSET2, Math.min(MAX_OFFSET2, f));
//    }
}
