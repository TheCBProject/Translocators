package codechicken.translocator.reference;

import codechicken.core.launch.CodeChickenCorePlugin;

/**
 * Created by covers1624 on 5/17/2016.
 */
public class Reference {

    public static final String MOD_ID = "Translocator";
    public static final String MOD_NAME = "Translocator";

    public static final String MOD_PREFIX = MOD_ID.toLowerCase() + ":";

    public static final String VERSION = "${mod_version}";
    public static final String DEPENDENCIES = "required-after:CodeChickenCore@[" + CodeChickenCorePlugin.version + ",)";

    public static final String COMMON_PROXY = "codechicken.translocator.proxy.CommonProxy";
    public static final String CLIENT_PROXY = "codechicken.translocator.proxy.ClientProxy";
}
