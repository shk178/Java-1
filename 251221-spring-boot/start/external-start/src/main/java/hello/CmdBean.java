package hello;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class CmdBean {
    private final ApplicationArguments appArgs;
    @PostConstruct
    public void get() {
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