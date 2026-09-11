package fredoseep.chaoxingauto.answer;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import fredoseep.chaoxingauto.answer.type.MultipleChoiceHandler;
import fredoseep.chaoxingauto.answer.type.ShortAnswerHandler;
import fredoseep.chaoxingauto.answer.type.SingleChoiceHandler;

public class QuestionHandlerFactory {

    public static AbstractQuestionHandler createHandler(Locator questionLocator, Page deepseekPage,Page chaoxingPage) {
        int questionType = Integer.parseInt(questionLocator.getAttribute("data"));
        if (questionType == 0) {
            return new SingleChoiceHandler(questionLocator, deepseekPage,chaoxingPage);
        } else if (questionType == 1) {
            return new MultipleChoiceHandler(questionLocator, deepseekPage,chaoxingPage);
        }

        throw new IllegalArgumentException("Unknown Type: " + questionType);
    }
}