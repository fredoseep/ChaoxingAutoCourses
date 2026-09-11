package fredoseep.chaoxingauto;


import fredoseep.chaoxingauto.browser.Browser;
import fredoseep.chaoxingauto.file.FileInitialize;

import java.io.File;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        if (initialize()) {
            System.out.println("Initialization finished");
        } else {
            System.out.println("Error: fail to initialize,quiting...");
            return;
        }
        Browser.operating();
    }

    private static boolean initialize() {
        return FileInitialize.initialize()&& FileInitialize.promptInitialize() && Browser.initialize();
    }
}