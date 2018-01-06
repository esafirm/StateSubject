package nolambda.statesubject;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.support.annotation.NonNull;

public class AdjustableLifecycle implements LifecycleOwner {

    private LifecycleRegistry registry;

    public LifecycleRegistry getRegistry() {
        return registry;
    }

    public AdjustableLifecycle() {
        registry = new LifecycleRegistry(this);
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
        registry.handleLifecycleEvent(Lifecycle.Event.ON_START);
        registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return registry;
    }
}
