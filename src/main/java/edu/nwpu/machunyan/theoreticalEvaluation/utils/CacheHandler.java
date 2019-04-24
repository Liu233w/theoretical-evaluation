package edu.nwpu.machunyan.theoreticalEvaluation.utils;

import lombok.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 用来缓存中间的计算结果，避免由于中断程序导致丢失计算结果
 */
@ToString
public class CacheHandler {

    private final static String DEFAULT_CACHE_BASE_LOCATION = "./target/outputs/.cache";
    @Getter
    private final String cacheName;
    @Getter
    @Setter
    private String cacheBaseLocation = DEFAULT_CACHE_BASE_LOCATION;

    public CacheHandler(String cacheName) {
        this.cacheName = cacheName;
    }

    // 辅助函数，用来将抛出checked exception的lambda转换成抛出unchecked exception的lambda
    private static <T> Consumer<T> unchecked(ThrowingConsumer<T> f) {
        return t -> {
            try {
                f.consume(t);
            } catch (Exception ex) {
                throw Lombok.sneakyThrow(ex);
            }
        };
    }

    public Path getCacheDirectory() {
        return Paths.get(cacheBaseLocation).resolve(cacheName);
    }

    private Path resolveCacheLocation(String key) {
        return getCacheDirectory().resolve(key + ".json");
    }

    public boolean isKeyCached(String key) {
        return resolveCacheLocation(key).toFile().exists();
    }

    public <T> Optional<T> tryLoadCache(String key, Class<T> type) {
        final Path path = resolveCacheLocation(key);
        try {
            final T res = FileUtils.loadObject(path, type);
            return Optional.of(res);
        } catch (FileNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * 将对象保存进缓存中（必须确保对象可以序列化，并且能够通过反射获取类型）
     * <p>
     * eg.
     * TestcaseWeightForProgram 可以被缓存，但 List<TestcaseWeightForProgram> 不行，
     * 必须被装进 TestcaseWeightJam。
     *
     * @param key
     * @param object
     */
    @SneakyThrows(IOException.class)
    public void saveCache(String key, Object object) {
        final Path path = resolveCacheLocation(key);
        FileUtils.saveObject(path, object);
    }

    @SneakyThrows(IOException.class)
    public void deleteCache(String key) {
        final Path path = resolveCacheLocation(key);
        Files.deleteIfExists(path);
    }

    @SneakyThrows(IOException.class)
    public void deleteAllCaches() {
        final Path directory = getCacheDirectory();
        if (directory.toFile().exists()) {
            Files.list(directory)
                .forEach(unchecked(Files::delete));
            Files.delete(directory);
        }
    }

    public interface ThrowingConsumer<T> {
        void consume(T t) throws Exception;
    }
}
