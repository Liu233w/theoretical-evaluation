package edu.nwpu.machunyan.theoreticalEvaluation.application;

public class RunTcas {

//    // 准备好的测试用例
//    private final ArrayList<IProgramInput> testcases = buildTestcasesObject();
//    // 程序文件夹
//    private final Path versionsDir = FileUtils.getFilePathFromResources("tcas/versions");
//    // 一共多少个版本
//    private final int lastVersionNum = 41;
//    // 结果的输出位置
//    private static final String resultOutputPath = "./target/outputs/tcasRunningResult.json";
//
//    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
//        new RunTcas().runAllProgramsAndOutputResultsToFile();
//    }
//
//    /**
//     * 从之前的输出中获取中间的运行结果
//     *
//     * @return key 为程序的 title（版本），value 为该程序的所有运行结果和覆盖值
//     */
//    public static HashMap<String, ArrayList<RunResultFromRunner>> getRunResultsFromSavedFile() throws FileNotFoundException {
//
//        final JsonObject jsonObject = FileUtils.getJsonFromFile(resultOutputPath).getAsJsonObject();
//
//        final HashMap<String, ArrayList<RunResultFromRunner>> result = new HashMap<>();
//
//        for (Map.Entry<String, JsonElement> stringJsonElementEntry : jsonObject.entrySet()) {
//
//            final JsonObject runResultsJsonObject = stringJsonElementEntry.getValue().getAsJsonObject();
//            final ArrayList<RunResultFromRunner> runResults = RunResultsJsonProcessor.loadFromJson(runResultsJsonObject, GccReadFromStdIoInput.class);
//
//            result.put(stringJsonElementEntry.getKey(), runResults);
//        }
//
//        return result;
//    }
//
//    private ProgressBar progressBar;
//
//    private void runAllProgramsAndOutputResultsToFile() throws InterruptedException, IOException {
//
//        // 初始化线程池
//        final int availableProcessors = Runtime.getRuntime().availableProcessors();
//        LogUtils.logInfo("ready to start, thread pool size: " + availableProcessors);
//        final ExecutorService threadPool = Executors.newFixedThreadPool(availableProcessors);
//
//        //初始化进度条
//        progressBar = new ProgressBar("", testcases.size() * lastVersionNum);
//
//        // 填充任务
//        final JsonObject result = new JsonObject();
//        for (int versionNum = 1; versionNum <= lastVersionNum; versionNum++) {
//
//            // 准备当前版本的程序
//            final String versionNumString = "v" + versionNum;
//            final Path sourceFilePath = versionsDir.resolve(versionNumString).resolve("tcas.c");
//
//            threadPool.submit(() -> {
//                try {
//                    final JsonObject resultRecord = runAndGetJsonObject(versionNumString, sourceFilePath);
//                    synchronized (result) {
//                        result.add(versionNumString, resultRecord);
//                    }
//                } catch (CoverageRunnerException e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//
//        // 等待所有任务完成
//        threadPool.shutdown();
//        threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
//
//        progressBar.close();
//
//        // 输出结果
//        FileUtils.printJsonToFile(Paths.get(resultOutputPath), result);
//    }
//
//    /**
//     * 运行某个版本程序的所有测试用例，获得结果 jsonObject
//     *
//     * @param versionNumString
//     * @param sourceFilePath
//     * @return
//     * @throws CoverageRunnerException
//     */
//    private JsonObject runAndGetJsonObject(String versionNumString, Path sourceFilePath) throws CoverageRunnerException {
//
//        final RunningScheduler runningScheduler = new RunningScheduler(
//                new Program(versionNumString, sourceFilePath.toString()),
//                new GccReadFromStdIoRunner(),
//                testcases,
//                progressBar);
//
//        // 使用测试用例运行程序
//        LogUtils.logInfo("start running version: " + versionNumString);
//        final ArrayList<RunResultFromRunner> runResults = runningScheduler.runAndGetResults();
//        final long passedCount = runResults.stream().filter(RunResultFromRunner::isCorrect).count();
//
//        LogUtils.logInfo("passed test case count for version " + versionNumString + ": " + passedCount);
//
//        return RunResultsJsonProcessor.bumpToJson(runResults, GccReadFromStdIoInput.class);
//    }
//
//    private ArrayList<IProgramInput> buildTestcasesObject() throws URISyntaxException, IOException {
//
//        final Path casePath = FileUtils.getFilePathFromResources("tcas/testplans/cases.json");
//        final InputStreamReader jsonReader = new InputStreamReader(Files.newInputStream(casePath));
//
//
//        final JsonArray list = new JsonParser().parse(jsonReader).getAsJsonArray();
//
//        final ArrayList<IProgramInput> result = new ArrayList<>(list.size());
//        for (JsonElement item :
//                list) {
//            final JsonObject testcase = item.getAsJsonObject();
//            final String[] input = new Gson().fromJson(testcase.get("input"), String[].class);
//            final String output = testcase.get("output").getAsString();
//
//            result.add(new GccReadFromStdIoInput(input, output));
//        }
//
//        return result;
//    }
//
//    private RunTcas() throws IOException, URISyntaxException {
//    }
}
