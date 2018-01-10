package nolambda.statesubject;

public class ActiveInactiveState extends StateSubject<Boolean> {

    private boolean isActive;

    public boolean isActive() {
        return isActive;
    }

    @Override
    protected void onActive() {
        isActive = true;
    }

    @Override
    protected void onInactive() {
        isActive = false;
    }
}
