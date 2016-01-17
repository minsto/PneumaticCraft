package pneumaticCraft.client.semiblock;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.semiblock.ISemiBlock;
import pneumaticCraft.common.semiblock.SemiBlockHeatFrame;
import pneumaticCraft.common.semiblock.SemiBlockLogistics;
import pneumaticCraft.common.semiblock.SemiBlockManager;

public class ClientSemiBlockManager{
    private static final Map<Class<? extends ISemiBlock>, ISemiBlockRenderer> renderers = new HashMap<Class<? extends ISemiBlock>, ISemiBlockRenderer>();

    static {
        registerRenderer(SemiBlockLogistics.class, new SemiBlockRendererLogistics());
        registerRenderer(SemiBlockHeatFrame.class, new SemiBlockRendererHeatFrame());
    }

    public static void registerRenderer(Class<? extends ISemiBlock> semiBlock, ISemiBlockRenderer renderer){
        renderers.put(semiBlock, renderer);
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event){
        Minecraft mc = FMLClientHandler.instance().getClient();
        EntityPlayer player = mc.thePlayer;
        double playerX = player.prevPosX + (player.posX - player.prevPosX) * event.partialTicks;
        double playerY = player.prevPosY + (player.posY - player.prevPosY) * event.partialTicks;
        double playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * event.partialTicks;

        GL11.glPushMatrix();
        GL11.glTranslated(-playerX, -playerY, -playerZ);
        RenderHelper.enableStandardItemLighting();

        for(Map<BlockPos, ISemiBlock> map : SemiBlockManager.getInstance(player.worldObj).getSemiBlocks().values()) {
            for(ISemiBlock semiBlock : map.values()) {
                ISemiBlockRenderer renderer = getRenderer(semiBlock);
                if(renderer != null) {
                    GL11.glPushMatrix();
                    GL11.glTranslated(semiBlock.getPos().getX(), semiBlock.getPos().getY(), semiBlock.getPos().getZ());
                    renderer.render(semiBlock, event.partialTicks);
                    GL11.glPopMatrix();
                }
            }
        }
        RenderHelper.disableStandardItemLighting();
        GL11.glPopMatrix();
    }

    public static ISemiBlockRenderer getRenderer(ISemiBlock semiBlock){
        Class clazz = semiBlock.getClass();
        while(clazz != Object.class && !renderers.containsKey(clazz)) {
            clazz = clazz.getSuperclass();
        }
        return renderers.get(clazz);
    }
}
