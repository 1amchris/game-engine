package shared.disposers;

import java.util.ArrayList;
import java.util.List;

public class Disposer implements Disposable {
    
    private final List<Disposable> disposables = new ArrayList<>();

    public void dispose() {
        for(Disposable disposable: this.disposables) {
            disposable.dispose();
        }
    }

    public <TDisposable extends Disposable> TDisposable create(TDisposable disposable) {
        disposables.add(disposable);
        return disposable;
    }
}
