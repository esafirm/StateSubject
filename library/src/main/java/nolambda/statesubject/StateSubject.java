package nolambda.statesubject;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

import static android.arch.lifecycle.Lifecycle.State.DESTROYED;

public class StateSubject<T> {

    private final BehaviorSubject<T> subject = BehaviorSubject.create();
    private final Map<LifecycleOwner, Disposable> disposableMap = new HashMap<>();

    public T getValue() {
        return subject.getValue();
    }

    public boolean hasObservers() {
        return subject.hasObservers();
    }

    public boolean hasActiveObserver() {
        return disposableMap.size() > 0;
    }

    public void postValue(T value) {
        subject.onNext(value);
    }

    public Disposable subscribeForever(final Consumer<T> consumer) {
        return subject.subscribe(consumer);
    }

    public void unsubscribe(@NonNull LifecycleOwner owner) {
        disposeWithOwner(owner);
    }

    @NonNull
    public Disposable subscribe(@NonNull LifecycleOwner owner, @NonNull final Consumer<T> consumer) {
        return subscribe(owner, null, Schedulers.io(), AndroidSchedulers.mainThread(), consumer);
    }

    @NonNull
    public Disposable subscribe(@NonNull LifecycleOwner owner,
                                @NonNull final Scheduler subscribeScheduler,
                                @NonNull final Scheduler observeScheduler,
                                @NonNull final Consumer<T> consumer) {
        return subscribe(owner, null, subscribeScheduler, observeScheduler, consumer);
    }

    @NonNull
    public Disposable subscribe(@NonNull LifecycleOwner owner,
                                @Nullable ObservableTransformer<T, T> transformer,
                                @NonNull final Consumer<T> consumer) {
        return subscribe(owner, transformer, Schedulers.io(), AndroidSchedulers.mainThread(), consumer);
    }

    @NonNull
    public Disposable subscribe(@NonNull LifecycleOwner owner,
                                @Nullable ObservableTransformer<T, T> transformer,
                                @NonNull final Scheduler subscribeScheduler,
                                @NonNull final Scheduler observeScheduler,
                                @NonNull final Consumer<T> consumer) {

        if (owner.getLifecycle().getCurrentState() == DESTROYED) {
            return StateSubjectDisposable.EMPTY_DISPOSABLE;
        }

        LifecycleObserver observer = new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            public void onStart(@NonNull LifecycleOwner owner) {
                checkActive();
                disposableMap.put(owner, subscribeToConsumer(consumer, subscribeScheduler, observeScheduler, transformer));
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            public void onStop(@NonNull LifecycleOwner owner) {
                disposeWithOwner(owner);
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroy(@NonNull LifecycleOwner owner) {
                owner.getLifecycle().removeObserver(this);
                disposeWithOwner(owner);
            }
        };

        owner.getLifecycle().addObserver(observer);

        return new StateSubjectDisposable(this, observer, owner);
    }

    private void checkActive() {
        boolean wasInactive = disposableMap.isEmpty();
        if (wasInactive) {
            onActive();
        }
    }

    private void checkInactive() {
        boolean isInactive = disposableMap.isEmpty();
        if (isInactive) {
            onInactive();
        }
    }

    public void disposeWithOwner(@NonNull LifecycleOwner owner) {
        if (disposableMap.containsKey(owner)) {
            Disposable disposable = disposableMap.get(owner);
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
            disposableMap.remove(owner);
        }
        checkInactive();
    }

    private Disposable subscribeToConsumer(final Consumer<T> consumer,
                                           final Scheduler subscribeScheduler,
                                           final Scheduler observeScheduler,
                                           final ObservableTransformer<T, T> transformer) {

        final Observable<T> base = transformer == null
                ? subject
                : subject.compose(transformer);

        return base
                .subscribeOn(subscribeScheduler)
                .observeOn(observeScheduler)
                .subscribe(consumer);
    }

    protected void onActive() {
        // deliberately no-op }
    }

    protected void onInactive() {
        // deliberately no-op
    }

}
