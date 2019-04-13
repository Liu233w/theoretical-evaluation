package edu.nwpu.machunyan.theoreticalEvaluation.runner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.TestcaseItem;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import one.util.streamex.StreamEx;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 获取一个程序的所有测试用例（必须按照统一的格式储存，参考 resources 文件夹下的其他项目）
 */
public class TestcaseResolver {

    public static List<TestcaseItem> resolve(String dirName) throws URISyntaxException, IOException {

        final Path casePath = FileUtils.getFilePathFromResources(
            dirName + "/testplans/cases.json");
        final InputStreamReader jsonReader = new InputStreamReader(Files.newInputStream(casePath));
        final Type testcaseType = new TypeToken<List<TestcaseItem>>() {
        }.getType();

        final List<TestcaseItem> list = new Gson().fromJson(jsonReader, testcaseType);

        return StreamEx
            .of(list)
            .map(a -> {
                String input = a.getInput();
                if (input == null) {
                    input = "";
                }
                String[] params = a.getParams();
                if (params == null) {
                    params = new String[]{};
                }
                return new TestcaseItem(input, a.getOutput(), params, a.getName());
            })
            .toImmutableList();
    }
}
