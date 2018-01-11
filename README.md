## StateSubject

Imitate `LiveData` behaviour, using RxJava's `Subject`.

## Gradle

Currently you have to explicitly using @aar suffix to resolve the artifact in jcenter.

```groovy
implementation 'nolambda:statesubject:1.0.0@aar'
```

## Usage

Simply change **observe** keyword, with **subscribe**

### Simple

```java
StateSubject<Integer> subject = new StateSubject()
subject.subscribe(lifecycleOwner, { item ->
    // 1 and then 2
})
subject.postValue(1)
subject.postValue(2)
```

### Disposing observer a.k.a removing observer in LiveData

Removing observer is easy, rather than using `removeObserver()` like `LiveData`, `StateSubject` return `Disposable` in every subscribe event.

```java
Disposable dispoasble = subject.subscribe(lifecycleOwner, this::renderData);
if(!dispoasble.isDisposed()){
    dispoasble.dispose();
}
```

### Manipulate Stream

Since it use RxJava `Subject`, manipulating stream is as easy as passing `ObservableTransformer`.

```java
StateSubject<String> subject = new StateSubject();
subject.subscribe(lifecycleOwner, s -> s.map(s -> s + "!"), s -> {
    // Will trigger J! K! T! in sequence
})
subject.postValue("J");
subject.postValue("K");
subject.postValue("T");
```

### Extending StateSubject 

Just replace `extends LiveData<>` with `extends StateSubject<>`

```java
public class StockLiveData extends StateSubject<BigDecimal> {
    private StockManager stockManager;

    private SimplePriceListener listener = new SimplePriceListener() {
        @Override
        public void onPriceChanged(BigDecimal price) {
            setValue(price);
        }
    };

    public StockLiveData(String symbol) {
        stockManager = new StockManager(symbol);
    }

    @Override
    protected void onActive() {
        stockManager.requestPriceUpdates(listener);
    }

    @Override
    protected void onInactive() {
        stockManager.removeUpdates(listener);
    }
}
```

---

Full parameters of `subscribe()`

```java
@NonNull
public Disposable subscribe(@NonNull LifecycleOwner owner,
                            @Nullable ObservableTransformer<T, T> transformer,
                            @NonNull final Scheduler subscribeScheduler,
                            @NonNull final Scheduler observeScheduler,
                            @NonNull final Consumer<T> consumer)
```

## More Samples 

You can jump to [sample module](https://github.com/esafirm/StateSubject/tree/master/sample) or [test files](https://github.com/esafirm/StateSubject/tree/master/library/src/test/java/nolambda/statesubject)

## What's Next

* Add Immutable variant

## License

MIT @ Esa Firman
