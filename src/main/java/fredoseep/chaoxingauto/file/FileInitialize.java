package fredoseep.chaoxingauto.file;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fredoseep.chaoxingauto.config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileInitialize {
    public static String PROMPT = "";

    public static boolean initialize() {
        System.out.println("Initializing config file...");
        Path configFilePath = Paths.get("config.json");

        try {
            if (!Files.exists(configFilePath)) {
                Path parent = configFilePath.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.createFile(configFilePath);
                System.out.println("Making config file...");

                JsonObject rootObject = new JsonObject();
                rootObject.addProperty("autoJumpWhenCheckPointChecked", true);
                rootObject.addProperty("autoDeepseekTheQuestion", true);
                rootObject.addProperty("onlySaveDontSubmit", true);
                rootObject.add("coursesToFinish", new JsonArray());
                // 初始化空的排除名单节点
                rootObject.add("excludedCourses", new JsonObject());

                Files.writeString(configFilePath, rootObject.toString());
                System.out.println("Config file finished initializing");
                return true;
            } else {
                System.out.println("Config file already exists");
                if (!Files.isReadable(configFilePath)) {
                    System.out.println("Error: configFile cannot be read, quitting...");
                    return false;
                }

                System.out.println("Reading config file...");
                String content = Files.readString(configFilePath);
                JsonObject rootObject = new Gson().fromJson(content, JsonObject.class);

                // ... 读取其他配置 (保持你原有的逻辑即可) ...
                if (rootObject.has("coursesToFinish") && rootObject.get("coursesToFinish").isJsonArray()) {
                    JsonArray jsonArray = rootObject.getAsJsonArray("coursesToFinish");
                    Config.courseToOperate.clear();
                    jsonArray.forEach(jsonElement -> Config.courseToOperate.add(jsonElement.getAsString()));
                }
                if (rootObject.has("autoJumpWhenCheckPointChecked")) Config.autoJumpWhenCheckPointChecked = rootObject.get("autoJumpWhenCheckPointChecked").getAsBoolean();
                if (rootObject.has("autoDeepseekTheQuestion")) Config.autoDeepseekTheQuestion = rootObject.get("autoDeepseekTheQuestion").getAsBoolean();
                if (rootObject.has("onlySaveDontSubmit")) Config.onlySaveDontSubmit = rootObject.get("onlySaveDontSubmit").getAsBoolean();

                // 读取排除名单
                if (rootObject.has("excludedCourses") && rootObject.get("excludedCourses").isJsonObject()) {
                    JsonObject excludedObj = rootObject.getAsJsonObject("excludedCourses");
                    for (String courseName : excludedObj.keySet()) {
                        JsonArray arr = excludedObj.getAsJsonArray(courseName);
                        List<String> ids = new ArrayList<>();
                        arr.forEach(e -> ids.add(e.getAsString()));
                        Config.excludedCourseIds.put(courseName, ids);
                    }
                } else {
                    rootObject.add("excludedCourses", new JsonObject());
                    Files.writeString(configFilePath, rootObject.toString()); // 补齐遗失的节点
                }

                System.out.println("Config file loaded successfully.");
                return true;
            }
        } catch (IOException e) {
            System.out.println("Error, failed at initializing config file: " + e.getMessage());
            return false;
        }
    }

    public static void addExcludedId(String courseName, String id) {
        Config.excludedCourseIds.computeIfAbsent(courseName, k -> new ArrayList<>());
        if (!Config.excludedCourseIds.get(courseName).contains(id)) {
            Config.excludedCourseIds.get(courseName).add(id);
        }

        try {
            Path configFilePath = Paths.get("config.json");
            String content = Files.readString(configFilePath);
            JsonObject rootObject = new Gson().fromJson(content, JsonObject.class);

            JsonObject excludedObj = rootObject.has("excludedCourses") ? rootObject.getAsJsonObject("excludedCourses") : new JsonObject();
            JsonArray idArray = new JsonArray();
            Config.excludedCourseIds.get(courseName).forEach(idArray::add);
            excludedObj.add(courseName, idArray);
            rootObject.add("excludedCourses", excludedObj);

            Files.writeString(configFilePath, rootObject.toString());
            System.out.println(">> 已将任务点 " + id + " 写入热更新排除名单！");
        } catch (Exception e) {
            System.out.println("热更新排除名单失败: " + e.getMessage());
        }
    }

    public static boolean promptInitialize() {
        String resourcePath = "prompt.txt";
        try (InputStream inputStream = FileInitialize.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) throw new IllegalArgumentException("Implemented Prompt file not found");
            PROMPT = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) { return false; }
    }
}