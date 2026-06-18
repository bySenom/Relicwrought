package io.github.bysenom.relicwrought.player;

public record PlayerArpgProfile(
        int dataVersion,
        boolean classSelected,
        String classId,
        boolean starterKitGranted,
        String starterKitId,
        int starterKitVersion,
        long selectionTimestamp
) {
    public static final int CURRENT_VERSION = 1;

    public static PlayerArpgProfile empty() {
        return new PlayerArpgProfile(CURRENT_VERSION, false, "", false, "", 0, 0);
    }

    public PlayerArpgProfile withClassSelected(String classId, long timestamp) {
        return new PlayerArpgProfile(
                CURRENT_VERSION, true, classId,
                starterKitGranted, starterKitId, starterKitVersion, timestamp
        );
    }

    public PlayerArpgProfile withKitGranted(String kitId, int kitVersion) {
        return new PlayerArpgProfile(
                CURRENT_VERSION, classSelected, classId,
                true, kitId, kitVersion, selectionTimestamp
        );
    }
}
