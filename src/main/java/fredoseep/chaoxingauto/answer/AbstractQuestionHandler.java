package fredoseep.chaoxingauto.answer;

import com.google.gson.Gson;
import com.microsoft.playwright.FileChooser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FilePayload;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

public abstract class AbstractQuestionHandler {
    protected Locator questionLocator;
    protected Page deepseekPage;
    protected Page chaoxingPage;
    protected String base64QuestionImage;
    protected Gson gson = new Gson();

    public AbstractQuestionHandler(Locator questionLocator, Page deepseekPage,Page chaoxingPage) {
        this.questionLocator = questionLocator;
        this.deepseekPage = deepseekPage;
        this.chaoxingPage = chaoxingPage;
    }

    public void process() {
        screenShotTheQuestion();
        String prompt = buildDeepseekPrompt();
        String answer = askDeepseek(prompt, base64QuestionImage);
        insertAnswer(answer);
    }

    public void screenShotTheQuestion() {
        byte[] imageBytes = questionLocator.screenshot();
        this.base64QuestionImage = Base64.getEncoder().encodeToString(imageBytes);
        System.out.println("Screen shot of the target question taken");
    }

    protected abstract String buildDeepseekPrompt();

    protected abstract void insertAnswer(String answer);

    private String askDeepseek(String prompt, String base64Image) {
        if (deepseekPage == null) {
            System.out.println("deepseekPage is null, quiting...");
            return "";
        }
        try {
            deepseekPage.bringToFront();
            deepseekPage.locator("div").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^New chat$|^开启新对话$"))).nth(2).click();
            deepseekPage.waitForLoadState();
            System.out.println("Asking deepseek...");
            Locator deepThinkToggleButton = deepseekPage.locator(".ds-toggle-button").first();
            if (!Boolean.parseBoolean(deepThinkToggleButton.getAttribute("aria-pressed"))) {
                System.out.println("Toggling deepThink...");
                deepThinkToggleButton.click();
            }
            Locator chatInput = deepseekPage.locator("textarea[name='search']");
            System.out.println("Inputting prompt...");
            chatInput.fill(prompt);
            System.out.println("Trying uploading screenshot...");
            Locator fileInput = deepseekPage.locator("input[type='file']");
            fileInput.setInputFiles(new FilePayload(
                    "question.png",
                    "image/png",
                    Base64.getDecoder().decode(base64Image)
            ));
            Locator questionPicLocator = deepseekPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("question.png"));
            while (!questionPicLocator.isVisible()) {
                System.out.println("Waiting for question pic to upload...");
                questionPicLocator = deepseekPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("question.png"));
                Thread.sleep(100);
            }
            System.out.println("Sending the question...");
            Thread.sleep(1000);
            chatInput.press("Enter");
            Locator answerLocator = deepseekPage.locator(".ds-assistant-message-main-content").last();
            Locator answerFinishedFlag = deepseekPage.locator(".ds-flex");
            while(!answerLocator.isVisible()&&answerFinishedFlag.count()<=1){
                System.out.println("Waiting for deepseek to answer...");
                answerLocator = deepseekPage.locator(".ds-assistant-message-main-content").last();
                answerFinishedFlag = deepseekPage.locator(".ds-flex");
                Thread.sleep(3000);
            }
            System.out.println("Got answer: " + answerLocator.innerText());
            return answerLocator.innerText();

        } catch (Exception e) {
            System.out.println("Error at asking deepseek: " + e.getMessage());
            return "";
        }
    }
}