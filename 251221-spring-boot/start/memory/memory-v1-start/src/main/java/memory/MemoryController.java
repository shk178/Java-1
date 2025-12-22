package memory;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class MemoryController {
    private final Optional<MemoryFinder> memoryFinder;

    @GetMapping("/memory")
    public Memory system() {
        return memoryFinder
                .map(MemoryFinder::get)
                .orElse(null);
    }
}
