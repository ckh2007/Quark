package vazkii.quark.base.block;

import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.module.QuarkModule;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/**
 * @author WireSegal
 * Created at 9:14 PM on 10/8/19.
 */
public class QuarkFenceGateBlock extends FenceGateBlock implements IQuarkBlock {

    private final QuarkModule module;
    private BooleanSupplier enabledSupplier = () -> true;

    public QuarkFenceGateBlock(String regname, QuarkModule module, CreativeModeTab creativeTab, Properties properties) {
        super(properties);
        this.module = module;

        RegistryHelper.registerBlock(this, regname);
        if(creativeTab != null)
            RegistryHelper.setCreativeTab(this, creativeTab);
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if(isEnabled() || group == CreativeModeTab.TAB_SEARCH)
            super.fillItemCategory(group, items);
    }

    @Override
    public QuarkFenceGateBlock setCondition(BooleanSupplier enabledSupplier) {
        this.enabledSupplier = enabledSupplier;
        return this;
    }

    @Override
    public boolean doesConditionApply() {
        return enabledSupplier.getAsBoolean();
    }

    @Nullable
    @Override
    public QuarkModule getModule() {
        return module;
    }

}
