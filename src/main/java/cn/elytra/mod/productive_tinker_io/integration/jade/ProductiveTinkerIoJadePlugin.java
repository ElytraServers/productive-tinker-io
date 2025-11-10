package cn.elytra.mod.productive_tinker_io.integration.jade;

import cn.elytra.mod.productive_tinker_io.ProductiveTinkerIo;
import cn.elytra.mod.productive_tinker_io.common.block.BasinBlock;
import cn.elytra.mod.productive_tinker_io.common.blockEntity.BasinBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin(ProductiveTinkerIo.MODID)
public class ProductiveTinkerIoJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(BasinJadeProvider.INSTANCE, BasinBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(BasinJadeProvider.INSTANCE, BasinBlock.class);
    }

}
