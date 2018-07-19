package ibsp.metaserver.monitor;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum ConnType {
    DEFAULT(0),
    SEND(1),
    RECIEVE(2),
    SENDANDRECIEVE(3);

    private int value;
    private static final Map<Integer, ConnType> map = new HashMap<>();

    static {
        for(ConnType s : EnumSet.allOf(ConnType.class)) {
            map.put(s.value ,s);
        }
    }

    private ConnType(int value) {
        this.value = value;
    }

    public static ConnType get(int code) {
        return map.get(code);
    }

    public int getValue() {
        return value;
    }
}
