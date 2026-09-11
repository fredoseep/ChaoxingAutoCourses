package fredoseep.chaoxingauto.browser;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import fredoseep.chaoxingauto.answer.QuestionEngine;
import fredoseep.chaoxingauto.config.Config;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Browser {
    public static final List<Locator> excludedCourseLocator = new ArrayList<>();
    public static boolean initialize() {
        boolean deepseekSignedIn = false;
        if (Config.autoDeepseekTheQuestion) {
            deepseekSignedIn = signInDeepseek();
        }
        return signInChaoxing() && deepseekSignedIn;
    }

    private static boolean signInChaoxing() {
        try (Playwright playwright = Playwright.create()) {
            Path userDataDir = Paths.get("/home/fredoseep/.playwright_bili_data");
            BrowserType.LaunchPersistentContextOptions options = new BrowserType.LaunchPersistentContextOptions()
                    .setHeadless(false)
                    .setChannel("chrome");
            System.out.println("chrome launching...");
            BrowserContext context = playwright.chromium().launchPersistentContext(userDataDir, options);
            Page page = context.pages().getFirst();
            System.out.println("openning chaoxing main page...");
            page.navigate("https://www.chaoxing.com/");
            page.waitForLoadState();
            Locator appDownloadIndicator = page.locator("#layout_0_0").getByText("学习通下载");
            while (!appDownloadIndicator.isVisible()) {
                System.out.println("waiting for the page to load...");
                Thread.sleep(100);
            }
            Locator signInText = page.locator("#layout_0_0").getByText("登录");
            if (signInText.count() > 0 && !page.locator("#layout_0_0 svg").isVisible()) {
                System.out.println("Unsigned in status, need to sign in...");
                page.locator(".vdr.vertical > .full_rect > .btn-container > .btn-inner").first().click();
                page.locator("span").nth(1).click();
                while (page.locator("#quickCode").count() > 0) {
                    System.out.println("Waiting for the QR code scanning...");
                    Thread.sleep(1000);
                }
            }
            System.out.println("Successfully signed in chaoxing...");
            page.close();
            return true;
        } catch (Exception e) {
            System.out.println("Error at signing in chaoxing: " + e.getMessage());
            return false;
        }
    }

    private static boolean signInDeepseek() {
        try (Playwright playwright = Playwright.create()) {
            Path userDataDir = Paths.get("/home/fredoseep/.playwright_bili_data");
            BrowserType.LaunchPersistentContextOptions options = new BrowserType.LaunchPersistentContextOptions()
                    .setHeadless(false)
                    .setChannel("chrome");
            System.out.println("chrome launching...");
            BrowserContext context = playwright.chromium().launchPersistentContext(userDataDir, options);
            Page page = context.pages().getFirst();
            System.out.println("openning deepseek page...");
            page.navigate("https://chat.deepseek.com/");
            page.waitForLoadState();
            Locator avatarLocator = page.locator("img");
            while (!avatarLocator.isVisible()) {
                System.out.println("Waiting for user to sign in deepseek...");
                avatarLocator = page.locator("img");
                Thread.sleep(10000);
            }
            System.out.println("Already signed in deepseek");
            page.close();
            return true;
        } catch (Exception e) {
            System.out.println("Error at signing in deepseek: " + e.getMessage());
            return false;
        }
    }

    public static void operating() {
        if (Config.courseToOperate.isEmpty()) {
            System.out.println("no course to operate, quiting...");
            return;
        }
        System.out.println(Config.courseToOperate.size() + " courses to operate...");
        System.out.println(Config.courseToOperate.getFirst());

        try (Playwright playwright = Playwright.create()) {
            String userHome = System.getProperty("user.home");
            Path userDataDir = Paths.get(userHome, ".playwright_bili_data");
            BrowserType.LaunchPersistentContextOptions options = new BrowserType.LaunchPersistentContextOptions()
                    .setHeadless(false)
                    .setChannel("chrome");
            System.out.println("chrome launching...");
            BrowserContext context = playwright.chromium().launchPersistentContext(userDataDir, options);
            Page page = context.pages().getFirst();
            System.out.println("openning chaoxing main page...");
            page.navigate("https://www.chaoxing.com/");
            page.waitForLoadState();

            Locator appDownloadIndicator = page.locator("#layout_0_0").getByText("学习通下载");
            while (!appDownloadIndicator.isVisible()) {
                System.out.println("waiting for the page to load...");
                Thread.sleep(100);
            }

            System.out.println("Signing in...");
            while (page.locator(".vdr.vertical > .full_rect > .img-container > .img-box").first().count() < 0) {
                System.out.println("Waiting for the site to sync...");
                Thread.sleep(100);
            }
            page.locator("#layout_0_0 svg").hover();
            page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("进入空间")).click();
            page.waitForLoadState();

            Locator courseButton = page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName(" 课程"));
            while (courseButton.isVisible()) {
                System.out.println("Trying to click the courseButton...");
                courseButton.click();
            }

            for (String currentCourseName : Config.courseToOperate) {
                System.out.println("Current operating course is " + currentCourseName);
                BrowserContext browserContext = page.context();
                browserContext.addInitScript(
                        "Object.defineProperty(document, 'hidden', { get: () => false });\n" +
                                "Object.defineProperty(document, 'visibilityState', { get: () => 'visible' });\n" +
                                "document.hasFocus = () => true;\n" +
                                "const blockEvent = (e) => { e.stopImmediatePropagation(); };\n" +
                                "document.addEventListener('visibilitychange', blockEvent, true);\n" +
                                "window.addEventListener('blur', blockEvent, true);\n" +
                                "document.addEventListener('blur', blockEvent, true);\n" +
                                "window.addEventListener('focusout', blockEvent, true);\n" +
                                "document.addEventListener('mouseleave', blockEvent, true);\n" +
                                "const originalPause = HTMLVideoElement.prototype.pause;\n" +
                                "HTMLVideoElement.prototype.pause = function() {\n" +
                                "  console.log('已拦截网页自动暂停视频的操作');\n" +
                                "};"
                );

                Page coursePage = page.waitForPopup(() -> {
                    page.locator("iframe[name=\"frame_content\"]").contentFrame().getByRole(AriaRole.LINK, new FrameLocator.GetByRoleOptions().setName(currentCourseName)).click();
                });

                coursePage.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("章节")).click();
                FrameLocator listFrame = coursePage.frameLocator("#frame_content-zj");
                Locator firstUnfinishedCourse = listFrame.locator(".catalog_jindu.catalog_tishi120").first();
                while (firstUnfinishedCourse.isVisible()) firstUnfinishedCourse.click();

                coursePage.waitForLoadState();
                Thread.sleep(2000); // 预留时间让右侧 iframe 初始加载

                // 把 DeepSeek 页面提升到循环外，复用同一个标签页
                Page deepseekPage = null;
                if (Config.autoDeepseekTheQuestion) {
                    System.out.println("Opening deepseek tab...");
                    deepseekPage = browserContext.newPage();
                    deepseekPage.navigate("https://chat.deepseek.com/");
                    deepseekPage.waitForLoadState();
                    Locator avatarLocator = deepseekPage.locator("img");
                    while (!avatarLocator.isVisible()) {
                        System.out.println("Waiting for deepseek to sign in...");
                        Thread.sleep(2000);
                    }
                    System.out.println("DeepSeek ready.");
                }
                coursePage.bringToFront();

                // ================== 终极重构的主循环：以有效节点驱动 ==================
                while (true) {
                    Locator validPoint = getNextValidCourseButton(coursePage, currentCourseName);

                    if (validPoint == null) {
                        if (coursePage.locator(".catalog_points_yi").count() > 0) {
                            System.out.println("本课程剩余任务点均在排除名单中，跳过此课程。");
                        } else {
                            System.out.println("本课程所有任务点已完美完成！");
                        }
                        break; // 结束当前课程的 while 循环，进入下一个课程
                    }

                    // 获取当前任务点的 ID
                    String currentPointId = validPoint.locator("..").getAttribute("id");
                    System.out.println(">> 锁定有效任务节点: " + currentPointId);

                    // 强制精准点击目标节点，确保右侧 iframe 加载对应的界面
                    validPoint.locator("..").click();
                    System.out.println("等待右侧课程内容加载完成...");
                    Thread.sleep(3000);
                    coursePage.waitForLoadState();

                    boolean expectCountDecrease = true;

                    if (coursePage.getByText("章节测验", new Page.GetByTextOptions().setExact(true)).count() > 0) {
                        System.out.println("Chapter test found...");
                        if (!Config.autoDeepseekTheQuestion) {
                            System.out.println("自动答题已关闭，跳过测试并加入排除名单...");
                            fredoseep.chaoxingauto.file.FileInitialize.addExcludedId(currentCourseName, currentPointId);
                            expectCountDecrease = false;
                        } else {
                            // 调用答题引擎，根据返回的 boolean 判断是否真正提交完成
                            boolean submitted = QuestionEngine.QuestionAnswerWorkFlow(coursePage, deepseekPage);
                            if (!submitted) {
                                System.out.println("仅保存或未答完，打入排除黑名单...");
                                fredoseep.chaoxingauto.file.FileInitialize.addExcludedId(currentCourseName, currentPointId);
                                expectCountDecrease = false;
                            }
                        }
                    } else {
                        // ================= 看视频逻辑 =================
                        Locator playButton = coursePage.locator("#iframe").contentFrame().locator("iframe").contentFrame().getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("播放视频"));
                        while (!playButton.isVisible()) {
                            System.out.println("Waiting for the video player to load...");
                            Thread.sleep(500);
                        }
                        System.out.println("Trying to play the video...");
                        playButton.click();

                        Locator unfinishIndicator = coursePage.locator("#iframe").contentFrame().getByRole(AriaRole.OPTION, new FrameLocator.GetByRoleOptions().setName("任务点未完成")).locator("i");
                        try {
                            unfinishIndicator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
                        } catch (Exception e) {
                            System.out.println("警告：5秒内未加载出'任务点未完成'标志，可能极短或已完成...");
                        }

                        while (unfinishIndicator.isVisible()) {
                            System.out.println("Watching video, waiting for it to be done...");
                            Locator noteKnownButton = coursePage.locator("#iframe").contentFrame().locator("iframe").contentFrame().getByText("知道了");
                            if (noteKnownButton.isVisible()) {
                                noteKnownButton.click();
                                System.out.println("Known button clicked");
                                if (playButton.isVisible()) playButton.click();
                            }
                            Locator muteBtn = coursePage.locator("#iframe").contentFrame().locator("iframe").contentFrame().getByText("静音100%");
                            if (muteBtn.isVisible()) {
                                muteBtn.click();
                            }
                            Thread.sleep(8000);
                        }
                        System.out.println("Video process finished.");
                        expectCountDecrease = true;
                    }

                    // ================= 结算与洗白状态 =================
                    System.out.println("当前节点处理完毕，准备刷新...");
                    int oldCount = coursePage.locator(".catalog_points_yi").count();
                    coursePage.reload();
                    coursePage.waitForLoadState();

                    // 只有在预期成功扣减任务点时，才进行堵塞等待
                    if (expectCountDecrease) {
                        int maxRetries = 20;
                        Locator allPoints = coursePage.locator(".catalog_points_yi");
                        while (allPoints.count() >= oldCount && maxRetries > 0) {
                            System.out.println("等待后台进度数据同步更新 UI...");
                            Thread.sleep(500);
                            allPoints = coursePage.locator(".catalog_points_yi");
                            maxRetries--;
                        }
                        // 更新结束后，执行第二次洗白刷新，确保下一轮循环 DOM 是纯净的
                        System.out.println("执行状态洗白...");
                        coursePage.reload();
                        coursePage.waitForLoadState();
                    }
                }
            }
            page.pause();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    private static Locator getNextValidCourseButton(Page coursePage, String currentCourseName) {
        Locator allButtons = coursePage.locator(".catalog_points_yi");
        for (int i = 0; i < allButtons.count(); i++) {
            Locator btn = allButtons.nth(i);
            String parentId = btn.locator("..").getAttribute("id");
            List<String> excluded = Config.excludedCourseIds.getOrDefault(currentCourseName, new ArrayList<>());
            if (parentId != null && !excluded.contains(parentId)) {
                return btn;
            }
        }
        return null;
    }

}