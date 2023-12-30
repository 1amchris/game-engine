package engineTester;

import shared.renderers.DisplayManager;

public class DateTimeManager {

    public static DateTimeManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DateTimeManager();
        }

        return INSTANCE;
    }

    private static final float INITIAL_TIME = 0;
    private static final float TIME_IN_ONE_DAY = 24_000;

    private static DateTimeManager INSTANCE;

    private float time = INITIAL_TIME;

    private DateTimeManager() {
    }

    public void update() {
        time += DisplayManager.getDeltaTime();
    }

    public int getCurrentDayStatus() {
        float timeOfDay = getCurrentTimeOfDay();

        if (5_000 <= timeOfDay && timeOfDay < 8_000) {
            return DateTime.DAWN;

        } else if (8_000 <= timeOfDay && timeOfDay < 17_000) {
            return DateTime.DAY;

        } else if (17_000 <= timeOfDay && timeOfDay < 20_000) {
            return DateTime.DUSK;

        } else {
            return DateTime.NIGHT;
        }
    }

    public float getCurrentTimeOfDay() {
        return time % TIME_IN_ONE_DAY;
    }

    public int getCurrentDay() {
        return (int) Math.floor(time / TIME_IN_ONE_DAY);
    }
}
