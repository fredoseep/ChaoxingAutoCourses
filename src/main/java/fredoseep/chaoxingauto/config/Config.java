package fredoseep.chaoxingauto.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    public static final List<String> courseToOperate = new ArrayList<>();
    public static boolean autoJumpWhenCheckPointChecked = true;
    public static boolean autoDeepseekTheQuestion = true;
    public static boolean onlySaveDontSubmit = true;
    public static final Map<String, List<String>> excludedCourseIds = new HashMap<>();
}
