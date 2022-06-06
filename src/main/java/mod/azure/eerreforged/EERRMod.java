package mod.azure.eerreforged;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod("eerreforged")
public class EERRMod {

	public EERRMod() {
		MinecraftForge.EVENT_BUS.register(this);
	}
}
