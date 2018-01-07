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
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return registry;
    }
}
