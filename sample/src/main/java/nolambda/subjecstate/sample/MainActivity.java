package nolambda.subjecstate.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import nolambda.statesubject.StateSubject;

public class MainActivity extends AppCompatActivity {

    private StateSubject<String> subject = new StateSubject<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Observable.interval(1, TimeUnit.SECONDS)
                .forEach(aLong -> {
                    subject.postValue(String.valueOf(aLong));
                });

        Disposable disposable = subject.subscribe(this, o -> o.map(s -> s + "!"), s -> {
            Log.d("Sample", "Loggin string from subject " + s);
        });

        Observable.timer(5, TimeUnit.SECONDS)
                .forEach(aLong -> disposable.dispose());
    }
}
