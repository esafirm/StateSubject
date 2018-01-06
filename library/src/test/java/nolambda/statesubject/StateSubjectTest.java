package nolambda.statesubject;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.support.annotation.NonNull;

import org.junit.Test;

import io.reactivex.disposables.Disposable;
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
}
