package androidx.core.app;

public abstract class BaseNoticeJobIntentService extends JobIntentService {

    int a = 0;

    @Override
    GenericWorkItem dequeueWork() {
        try {
            return super.dequeueWork();
        } catch (Exception e) {
            return null;
        }
    }
}
