package codechicken.translocator.reference;

import codechicken.lib.util.ArrayUtils;
import com.google.common.collect.ImmutableList;

/**
 * Created by covers1624 on 5/23/2016.
 */
public class VariantReference {

    public static final String[] translocatorNames = new String[] { "item", "liquid" };
    public static final ImmutableList<String> translocatorNamesList = ImmutableList.copyOf(ArrayUtils.arrayToLowercase(translocatorNames));

}
