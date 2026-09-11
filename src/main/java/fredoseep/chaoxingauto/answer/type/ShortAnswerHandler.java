package fredoseep.chaoxingauto.answer.type;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import fredoseep.chaoxingauto.answer.AbstractQuestionHandler;
import fredoseep.chaoxingauto.file.FileInitialize;

public class ShortAnswerHandler extends AbstractQuestionHandler {

    public ShortAnswerHandler(Locator locator, Page deepseekPage,Page chaoxingPage) {
        super(locator,deepseekPage,chaoxingPage);
    }

    @Override
    protected String buildDeepseekPrompt() {
        return FileInitialize.PROMPT+"\n"+"本题为简答题";
    }

    @Override
    protected void insertAnswer(String answer) {
    }
}