package fredoseep.chaoxingauto.answer.type;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import fredoseep.chaoxingauto.answer.AbstractQuestionHandler;
import fredoseep.chaoxingauto.file.FileInitialize;

import java.util.ArrayList;
import java.util.List;

public class MultipleChoiceHandler extends AbstractQuestionHandler {

    public MultipleChoiceHandler(Locator locator, Page deepseekPage,Page chaoxingPage) {
        super(locator,deepseekPage,chaoxingPage);
    }

    @Override
    protected String buildDeepseekPrompt() {
        return FileInitialize.PROMPT+"\n"+"本题为多选题";
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
            if (typeValue.contains("multiple")) {
                if (rootObject.has("answer")) {
                    com.google.gson.JsonElement answerElement = rootObject.get("answer");
                    if (answerElement.isJsonArray()) {
                        JsonArray answerArray = answerElement.getAsJsonArray();
                        List<String> choiceList = new ArrayList<>();
                        answerArray.forEach(jsonElement -> {
                            choiceList.add(jsonElement.getAsString());
                        });
                        chaoxingPage.bringToFront();
                        chaoxingPage.waitForLoadState();
                        choiceList.forEach(choice->{
                            questionLocator.locator(".num_option_dx").all().forEach(locator -> {
                                System.out.println("Choosing " + choice);
                                if (locator.getAttribute("data").contains(choice)) {
                                    locator.click();
                                }
                            });
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
                System.out.println("Error: question Type not matching, " + typeValue + " received, multiple expected");
                return;
            }
        } else {
            System.out.println("Error: answer doesn't contain type key");
            return;
        }
    }
}
