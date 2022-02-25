package com.dbapp.extension.ai.management.runtime.process;

import com.dbapp.extension.ai.utils.SystemProperUtil;
import com.dbapp.extension.mirror.dto.AIModel;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.DateBuilder;

import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
public class AIModelProcess {

    private static File recordFile;

    static {
        recordFile = new File(SystemProperUtil.getResourcesPath() + SystemProperUtil.getFileSeparator() + "aiProcess.record");
        if (!recordFile.exists()) {
            try {
                recordFile.createNewFile();
            } catch (IOException e) {
                log.error("创建AI模型进程记录文件失败", e);
            }
        }
    }

    /**
     * AI模型信息
     */
    @Setter
    @Getter
    private AIModel aiModel;
    /**
     * AI任务执行中的进程
     * key-算法id；value-算法进程
     */
    private Map<String, AlgorithmProcess> aiScriptProcesses = new ConcurrentHashMap<>();

    public AIModelProcess(AIModel aiModel) {
        this.aiModel = aiModel;
    }

    /**
     * 放入进程
     *
     * @param algorithmId
     * @param process
     */
    public synchronized void putProcess(@NotNull String algorithmId, @NotNull Process process) {
        if (this.aiScriptProcesses.containsKey(algorithmId)) {
            AlgorithmProcess algorithmProcess = this.aiScriptProcesses.remove(algorithmId);
            if (algorithmProcess != null) {
                algorithmProcess.destroy();
            }
        }
        AlgorithmProcess algorithmProcess = new AlgorithmProcess(process);
        this.aiScriptProcesses.put(algorithmId, algorithmProcess);
        if (AlgorithmProcess.ERROR_PID.equals(algorithmProcess.getPID())) {
            return;
        }
        String record = String.format("%s.%s.PID=", getJobName(), algorithmId);
        try {
            AtomicReference<Boolean> isContain = new AtomicReference<>(false);
            String records = FileUtils.readLines(recordFile, StandardCharsets.UTF_8).parallelStream()
                    .map(line -> {
                        if (line.startsWith(record)) {
                            isContain.set(true);
                            return record + algorithmProcess.getPID();
                        }
                        return line;
                    })
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining("\n"));
            if (Boolean.FALSE.equals(isContain.get())) {
                records = records + "\n" + record + algorithmProcess.getPID();
            }
            FileUtils.write(recordFile, records, StandardCharsets.UTF_8, false);
        } catch (IOException e) {
            log.error(String.format("AI进程(%s%s)写入文件失败", record, algorithmProcess.getPID()), e);
        }
    }

    /**
     * 移除进程并销毁
     *
     * @param algorithmId
     */
    public synchronized void removeAndDestroyProcess(@NotNull String algorithmId) {
        if (this.aiScriptProcesses.containsKey(algorithmId)) {
            AlgorithmProcess algorithmProcess = this.aiScriptProcesses.remove(algorithmId);
            if (algorithmProcess != null) {
                algorithmProcess.destroy();
                String record = String.format("%s.%s.PID=", getJobName(), algorithmId);
                try {
                    AtomicReference<Boolean> isContain = new AtomicReference<>(false);
                    String records = FileUtils.readLines(recordFile, StandardCharsets.UTF_8).parallelStream()
                            .map(line -> {
                                if (line.startsWith(record)) {
                                    isContain.set(true);
                                    return "";
                                }
                                return line;
                            })
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.joining("\n"));
                    if (Boolean.TRUE.equals(isContain.get())) {
                        FileUtils.write(recordFile, records, StandardCharsets.UTF_8, false);
                    }
                } catch (IOException e) {
                    log.error(String.format("AI进程(%s%s)从文件移除失败", record, algorithmProcess.getPID()), e);
                }
            }
        }
    }

    /**
     * 任务名
     *
     * @return
     */
    public String getJobName() {
        return this.aiModel.getRuleId();
    }

    /**
     * 任务名组名
     *
     * @return
     */
    public String getJobGroupName() {
        return this.aiModel.getModelType();
    }

    /**
     * 触发器名
     *
     * @return
     */
    public String getTriggerName() {
        return this.aiModel.getRuleId();
    }

    /**
     * 触发器组名
     *
     * @return
     */
    public String getTriggerGroupName() {
        return this.aiModel.getModelType();
    }

    /**
     * 任务间隔
     *
     * @return
     */
    public int getInterval() {
        return this.aiModel.getDetectionParams().getWindow();
    }

    /**
     * 任务间隔单位
     *
     * @return
     */
    public DateBuilder.IntervalUnit getIntervalUnit() {
        String timeUnit = this.aiModel.getDetectionParams().getTimeUnit();
        DateBuilder.IntervalUnit intervalUnit;
        switch (timeUnit) {
            case "min":
                intervalUnit = DateBuilder.IntervalUnit.MINUTE;
                break;
            case "hour":
                intervalUnit = DateBuilder.IntervalUnit.HOUR;
                break;
            case "second":
                intervalUnit = DateBuilder.IntervalUnit.SECOND;
                break;
            default:
                intervalUnit = DateBuilder.IntervalUnit.MINUTE;
                break;
        }
        return intervalUnit;
    }

    /**
     * 比较AI模型是否修改，必须是模型RuleId相同的两个模型进行比较，若不同表示未修改
     *
     * @param aiModel AI模型
     * @return 比较结果
     */
    public synchronized boolean isChange(AIModel aiModel) {
        if (!Objects.equals(this.aiModel.getRuleId(), aiModel.getRuleId())) {
            return false;
        }
        return !Objects.equals(this.aiModel.getRuleName(), aiModel.getRuleName())
                || !Objects.equals(this.aiModel.getModelType(), aiModel.getModelType())
                || !this.aiModel.getDetectionParams().equals(aiModel.getDetectionParams())
                || !Objects.equals(this.aiModel.getDescription(), aiModel.getDescription());
    }

    /**
     * 摧毁子进程
     *
     * @return 摧毁结果
     */
    public synchronized boolean destroy() {
        boolean isDestroy = true;
        for (AlgorithmProcess algorithmProcess : this.aiScriptProcesses.values()) {
            if (algorithmProcess != null
                    && algorithmProcess.isAlive()
                    && !algorithmProcess.destroy()) {// 仍存活且未摧毁
                isDestroy = false;
            }
        }
        if (isDestroy) {
            this.aiScriptProcesses.clear();
        }
        return isDestroy;
    }

    public static String getProcessId(Process process) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        long pid = -1;
        Field field;
        if (Platform.isLinux() || Platform.isMac() || Platform.isAIX()) {
            Class<?> clazz = Class.forName("java.lang.UNIXProcess");
            field = clazz.getDeclaredField("pid");
            field.setAccessible(true);
            pid = (Integer) field.get(process);
        } else if (Platform.isWindows()) {
            field = process.getClass().getDeclaredField("handle");
            field.setAccessible(true);
            pid = Kernel32.INSTANCE.GetProcessId((Long) field.get(process));
        }
        return String.valueOf(pid);
    }

    public static void killProcess(String pid) throws IOException, InterruptedException {
        if (StringUtils.isBlank(pid) || AlgorithmProcess.ERROR_PID.equals(pid)) {
            throw new RuntimeException(String.format("Kill process error, PID=%s", pid));
        }
        String command;
        if (Platform.isLinux() || Platform.isMac() || Platform.isAIX()) {
            command = String.format("kill -9 %s", pid);
        } else if (Platform.isWindows()) {
            command = String.format("cmd.exe /c taskkill /PID %s /F /T", pid);
        } else {
            command = "";
        }
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
        StringBuilder killResult = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String buffer;
            while ((buffer = reader.readLine()) != null) {
                killResult.append(buffer).append('\n');
            }
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String buffer;
            while ((buffer = reader.readLine()) != null) {
                killResult.append(buffer).append('\n');
            }
        }
        log.info("终止PID-{}进程结果:{}", pid, killResult.toString());
    }

}

@Slf4j
class AlgorithmProcess {
    static final String ERROR_PID = "-1";
    @Getter
    private String PID;
    @NotNull
    private Process process;

    public AlgorithmProcess(@NotNull Process process) {
        this.process = process;
        this.PID = getProcessID();
    }

    public boolean destroy() {
        if (StringUtils.isBlank(this.PID) || ERROR_PID.equals(this.PID)) {
            this.process.destroyForcibly();
            return !this.process.isAlive();
        }
        try {
            AIModelProcess.killProcess(this.PID);
        } catch (Exception e) {
            log.error(String.format("终止进程(%s)失败", this.PID), e);
        } finally {
            if (this.process != null) {
                this.process.destroyForcibly();
            }
        }
        if (this.process == null) {
            return true;
        }
        return !this.process.isAlive();
    }

    public boolean isAlive() {
        if (this.process != null) {
            return this.process.isAlive();
        }
        return false;
    }

    private String getProcessID() {
        try {
            return AIModelProcess.getProcessId(process);
        } catch (Exception e) {
            log.error("获取进程PID异常", e);
            return ERROR_PID;
        }
    }
}

interface Kernel32 extends Library {
    Kernel32 INSTANCE = Native.loadLibrary("kernel32", Kernel32.class);

    long GetProcessId(Long hProcess);
}
