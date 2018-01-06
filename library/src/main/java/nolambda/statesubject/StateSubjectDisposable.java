package nolambda.statesubject;

import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;

import io.reactivex.disposables.Disposable;

public class StateSubjectDisposable implements Disposable {

    static final Disposable EMPTY_DISPOSABLE = new Disposable() {
        @Override
        public void dispose() {
        }

        @Override
        public boolean isDisposed() {
            return true;
        }
    };

    private StateSubject subject;
    private LifecycleObserver observer;
    private LifecycleOwner owner;

    public StateSubjectDisposable(StateSubject subject, LifecycleObserver observer, LifecycleOwner owner) {
        this.subject = subject;
        this.observer = observer;
        this.owner = owner;
    }

    @Override
    public void dispose() {
        subject.unsubscribe(owner);
        owner.getLifecycle().removeObserver(observer);
        subject = null;
        observer = null;
        owner = null;
    }

    @Override
    public boolean isDisposed() {
        return subject == null && observer == null && owner == null;
    }
}
