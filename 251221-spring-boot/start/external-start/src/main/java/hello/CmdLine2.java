package hello;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import java.util.List;
import java.util.Set;

public class CmdLine2 {
    public static void main(String[] args) {
        for (String arg : args) {
            System.out.println("arg = " + arg);
        }
        ApplicationArguments appArgs = new DefaultApplicationArguments(args);
        System.out.println("List.of(appArgs.getSourceArgs()) = " + List.of(appArgs.getSourceArgs()));
        System.out.println("appArgs.getNonOptionArgs() = " + appArgs.getNonOptionArgs());
        System.out.println("appArgs.getOptionNames() = " + appArgs.getOptionNames());
        Set<String> optionNames = appArgs.getOptionNames();
        for (String optionName : optionNames) {
            System.out.println("optionName = " + optionName);
            System.out.println("appArgs.getOptionValues(optionName) = " + appArgs.getOptionValues(optionName));
        }
    }
}
/*
arg = --url=devdb
arg = --username=dev_user
arg = --password=dev_pw
arg = mode=on
List.of(appArgs.getSourceArgs()) = [--url=devdb, --username=dev_user, --password=dev_pw, mode=on]
appArgs.getNonOptionArgs() = [mode=on]
appArgs.getOptionNames() = [password, url, username]
optionName = password
appArgs.getOptionValues(optionName) = [dev_pw]
optionName = url
appArgs.getOptionValues(optionName) = [devdb]
optionName = username
appArgs.getOptionValues(optionName) = [dev_user]
 */