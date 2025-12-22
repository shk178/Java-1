package hello;

import memory.MemoryController;
import memory.MemoryFinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class MemoryConfig {
    @Bean
    public MemoryController memoryController() {
        return new MemoryController(Optional.ofNullable(memoryFinder()));
    }
    @Bean
    public MemoryFinder memoryFinder() {
        return new MemoryFinder();
    }
}
