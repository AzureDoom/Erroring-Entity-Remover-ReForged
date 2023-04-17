package mod.azure.eerreforged;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod("eerreforged")
public class EERRMod {

	public static final Logger LOGGER = LogManager.getLogger("eer");
	
	public EERRMod() {
		MinecraftForge.EVENT_BUS.register(this);
	}
}
