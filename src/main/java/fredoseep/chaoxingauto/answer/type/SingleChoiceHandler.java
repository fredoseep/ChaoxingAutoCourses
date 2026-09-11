package fredoseep.chaoxingauto.answer.type;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import fredoseep.chaoxingauto.answer.AbstractQuestionHandler;
import fredoseep.chaoxingauto.file.FileInitialize;

public class SingleChoiceHandler extends AbstractQuestionHandler {

    public SingleChoiceHandler(Locator locator, Page deepseekPage,Page chaoxingPage) {
        super(locator,deepseekPage,chaoxingPage);
    }

    @Override
    protected String buildDeepseekPrompt() {
       return FileInitialize.PROMPT+"\n"+"本题为单选题";
    }

    @Override
    protected void insertAnswer(String answer) {
        if(answer.isBlank()){
            System.out.println("Error: Answer is blank");
            return;
        }
        JsonObject rootObject = gson.fromJson(answer, JsonObject.class);

        if (rootObject.has("type")) {
            String typeValue = rootObject.get("type").getAsString();
            if (typeValue.contains("single")) {
                if (rootObject.has("answer")) {
                    com.google.gson.JsonElement answerElement = rootObject.get("answer");
                    if (answerElement.isJsonArray()) {
                        JsonArray answerArray = answerElement.getAsJsonArray();
                        String choice = answerArray.get(0).getAsString();

                        chaoxingPage.bringToFront();
                        chaoxingPage.waitForLoadState();

                        questionLocator.locator(".num_option").all().forEach(locator -> {
                            System.out.println("Choosing " + choice);
                            if (locator.getAttribute("data").contains(choice)) {
                                locator.click();
                            }
                        });
                    } else {
                        System.out.println("Error: answer is not a JsonArray");
                        return;
                    }
                } else {
                    System.out.println("Error: answer doesn't contain answer key");
                    return;
                }
            } else {
                System.out.println("Error: question Type not matching, " + typeValue + " received, single expected");
                return;
            }
        } else {
            System.out.println("Error: answer doesn't contain type key");
            return;
        }
    }
}
