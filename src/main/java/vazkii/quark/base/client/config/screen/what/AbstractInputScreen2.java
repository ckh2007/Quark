package vazkii.quark.base.client.config.screen.what;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import vazkii.quark.base.QuarkClient;
import vazkii.quark.base.client.config.screen.AbstractQScreen;
import vazkii.zeta.config.ChangeSet;
import vazkii.zeta.config.ValueDefinition;
import vazkii.zeta.config.client.ClientDefinitionExt;

//TODO: assumes the thing your editing has a direct ValueDefinition, which isn't true for certain types of config screens
// like, RGBA color inputs actually correspond to SectionDefinitions in the config
public abstract class AbstractInputScreen2<T> extends AbstractQScreen {
	protected final ChangeSet changes;
	protected final ValueDefinition<T> def;
	protected final ClientDefinitionExt<ValueDefinition<T>> ext;

	protected DefaultDiscardDone defaultDiscardDone;

	public AbstractInputScreen2(Screen parent, ChangeSet changes, ValueDefinition<T> def) {
		super(parent);
		this.changes = changes;
		this.def = def;
		this.ext = QuarkClient.ZETA_CLIENT.clientConfigManager.getExt(def);
	}

	protected T get() {
		return changes.get(def);
	}

	protected void set(T thing) {
		changes.set(def, thing);
	}

	protected abstract void forceUpdateWidgetsTo(T value);

	@Override
	protected void init() {
		super.init();

		defaultDiscardDone = new DefaultDiscardDone(this, changes, def) {
			@Override
			public void resetToDefault(Button b) {
				super.resetToDefault(b);
				forceUpdateWidgetsTo(get());
			}

			@Override
			public void discard(Button b) {
				super.discard(b);
				forceUpdateWidgetsTo(get());
			}
		};
		defaultDiscardDone.addWidgets(this::addRenderableWidget);

		updateButtonStatus(true);
	}

	//call changes.set() first, so isDirty will return an up-to-date value
	protected void updateButtonStatus(boolean valid) {
		defaultDiscardDone.done.active = valid;
		defaultDiscardDone.discard.active = !valid || changes.isDirty(def);
	}
}
