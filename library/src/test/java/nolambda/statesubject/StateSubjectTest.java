package nolambda.statesubject;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.support.annotation.NonNull;

import org.junit.Test;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

import static junit.framework.Assert.assertTrue;

public class StateSubjectTest {

    private static final LifecycleOwner ALWAYS_ON = new LifecycleOwner() {
        private LifecycleRegistry mRegistry = init();

        private LifecycleRegistry init() {
            LifecycleRegistry registry = new LifecycleRegistry(this);
            registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
            registry.handleLifecycleEvent(Lifecycle.Event.ON_START);
            registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
            return registry;
        }

        @NonNull
        @Override
        public Lifecycle getLifecycle() {
            return mRegistry;
        }
    };

    @Test
    public void testAdjustableLifecycle() {
        AdjustableLifecycle lifecycle = new AdjustableLifecycle();
        assertTrue(lifecycle.getLifecycle().getCurrentState() == Lifecycle.State.INITIALIZED);

        lifecycle.getRegistry().markState(Lifecycle.State.STARTED);
        assertTrue(lifecycle.getLifecycle().getCurrentState() == Lifecycle.State.STARTED);

        lifecycle.getRegistry().markState(Lifecycle.State.DESTROYED);
        assertTrue(lifecycle.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED);
    }

    @Test
    public void testSubject() {
        BehaviorSubject<Integer> subject = BehaviorSubject.create();
        assertTrue(!subject.hasObservers());

        Disposable disposable = subject.subscribe();
        assertTrue(subject.hasObservers());

        disposable.dispose();
        assertTrue(!subject.hasObservers());
    }

    @Test
    public void testHasObserver() {
        AdjustableLifecycle adjustableLifecycle = new AdjustableLifecycle();
        adjustableLifecycle.getRegistry().markState(Lifecycle.State.STARTED);

        StateSubject<String> subject = new StateSubject<>();
        assertTrue(!subject.hasActiveObserver());
        assertTrue(!subject.hasActiveObserver());

        Disposable disposable = subject.subscribe(
                adjustableLifecycle,
                Schedulers.trampoline(),
                Schedulers.trampoline(),
                String::toUpperCase);

        assertTrue(subject.hasObservers());
        assertTrue(subject.hasActiveObserver());

        disposable.dispose();

        assertTrue(!subject.hasObservers());
        assertTrue(!subject.hasActiveObserver());
    }

    @Test
    public void testSubjectValue() {
        StateSubject<Integer> subject = new StateSubject<>();
        subject.postValue(1);

        assertTrue(subject.getValue() == 1);

        subject.postValue(2);
        subject.postValue(3);

        assertTrue(subject.getValue() == 3);
    }

    @Test
    public void testSingleObserver() {
        StateSubject<Integer> subject = new StateSubject<>();
        subject.postValue(1);
        subject.subscribe(ALWAYS_ON,
                Schedulers.trampoline(),
                Schedulers.trampoline(),
                integer -> assertTrue(integer == 1));
    }

    @Test
    public void testMultiObserver() {
        StateSubject<Integer> subject = new StateSubject<>();

        subject.subscribe(ALWAYS_ON,
                Schedulers.trampoline(),
                Schedulers.trampoline(),
                integer -> assertTrue(integer == 1));

        subject.subscribe(ALWAYS_ON,
                Schedulers.trampoline(),
                Schedulers.trampoline(),
                integer -> assertTrue(integer == 1));

        subject.postValue(1);
    }

    @Test
    public void testActiveInactive() {
        ActiveInactiveState subject = new ActiveInactiveState();
        Disposable disposable = subscribeTest(ALWAYS_ON, subject, aBoolean -> { /* no op */ });
        assertTrue(subject.isActive());

        disposable.dispose();
        assertTrue(!subject.isActive());

        AdjustableLifecycle lifecycle = new AdjustableLifecycle();
        lifecycle.getRegistry().markState(Lifecycle.State.STARTED);

        Disposable d1 = subscribeTest(ALWAYS_ON, subject, aBoolean -> { /* no op */ });
        Disposable d2 = subscribeTest(lifecycle, subject, aBoolean -> { /* no op */ });

        assertTrue(subject.isActive());

        d1.dispose();
        assertTrue(subject.isActive());

        d2.dispose();
        assertTrue(!subject.isActive());
    }

    private <T> Disposable subscribeTest(@NonNull LifecycleOwner owner, StateSubject<T> subject, Consumer<T> consumer) {
        return subject.subscribe(
                owner,
                Schedulers.trampoline(),
                Schedulers.trampoline(),
                consumer
        );
    }
}
