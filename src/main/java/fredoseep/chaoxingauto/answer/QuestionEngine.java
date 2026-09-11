package fredoseep.chaoxingauto.answer;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import fredoseep.chaoxingauto.config.Config;

public class QuestionEngine {

    // 返回 true 表示成功提交，返回 false 表示仅保存或未答完（需要被排除）
    public static boolean QuestionAnswerWorkFlow(Page coursePage, Page deepseekPage) throws InterruptedException {
        System.out.println("QuestionEngine started");
        FrameLocator innerFrame = coursePage
                .frameLocator("#iframe")
                .frameLocator("iframe[src*='/ananas/modules/work/']")
                .frameLocator("#frame_content");
        Locator questionListLocator = innerFrame.locator(".newTiMu");
        System.out.println(questionListLocator.count() + " questions found");

        questionListLocator.all().forEach(locator -> {
            try {
                AbstractQuestionHandler handler = QuestionHandlerFactory.createHandler(locator, deepseekPage, coursePage);
                handler.process();
            } catch (Exception e) {
                System.out.println("跳过一题：" + e.getMessage());
            }
        });

        Thread.sleep(300);

        // 个性化设置 1: onlySaveDontSubmit
        if (Config.onlySaveDontSubmit) {
            System.out.println("Config已设置为仅保存，点击 btnSave...");
            innerFrame.locator(".btnSave").click();
            return false; // 代表没有最终提交完成任务点
        }

        System.out.println("Chapter finished, submitting...");
        innerFrame.locator(".btnSubmit").click();
        Thread.sleep(1500); // 必须等待动画弹窗完全出现

        // 个性化设置 3: 拦截二次确认弹窗中的 "未完成" 警告
        Locator popContent = coursePage.locator("#popcontent");
        if (popContent.isVisible()) {
            String popText = popContent.innerText();
            if (popText.contains("unfinished") || popText.contains("未")) {
                System.out.println("检测到未回答完毕的题目，取消提交并回退到保存...");
                coursePage.locator("#popno").click(); // 在 coursePage 点取消
                Thread.sleep(500);
                innerFrame.locator(".btnSave").click(); // 回退到保存
                return false;
            }
        }

        // 正常答完并提交
        coursePage.locator("#popok").click();
        return true;
    }
}