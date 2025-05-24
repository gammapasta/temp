package com.team109.javara.global.common.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static io.netty.util.internal.StringUtil.escapeCsv;

@Component
@Slf4j
@Profile({"prod"}) //서버에서 실행할때만 nohup java -jar "$JAR_FILE" --spring.profiles.active="prod" > "$LOG_FILE" 2>&1 &
public class DatabaseBackupScheduler {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${app.backup.directory}")
    private String backupDirectory;

    @Value("${app.backup.mysqldump.path:mysqldump}")
    private String mysqldumpPath;


    @Scheduled(cron = "${app.backup.schedule}")
    public void backupDatabase() {
        log.info("Start db 백업 진행 시작");

        String dbName = "db25111";

        // 파일명 생성
        String time = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = dbName + "_backup_" + time + ".sql";
        String backupPath = backupDirectory + File.separator + fileName; // 리눅스는 / File.separator로 함. 윈도우에서는 \

        // 디렉토리 생성
        createBackupDirectory();

        // 백업
        performBackup(dbName, backupPath);

    }

    private void createBackupDirectory() {
        File dir = new File(backupDirectory);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                log.error("백업 디렉토리 생성 실패: {}", backupDirectory);
            } else {
                log.info("백업 디렉토리 생성 성공: {}", backupDirectory);
            }
        }
    }

    private void performBackup(String dbName, String backupPath) {
        // mysqldump 명령어 .my.cnf [파일]
        String command = String.format("%s -u%s %s", mysqldumpPath, dbUsername, dbName);
        log.info("백업 시작 {}, {}", dbName, backupPath);

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("/bin/sh", "-c", command + " > " + backupPath);


        try {
            Process process = processBuilder.start();

            StringBuilder errorOutput = new StringBuilder();
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(5, TimeUnit.MINUTES);

            if (finished && process.exitValue() == 0) {
                File backupFile = new File(backupPath);
                if (backupFile.exists() && backupFile.length() > 0) {
                    log.info("백업 성공 {}, {} bytes", backupPath, backupFile.length());
                } else {
                    log.warn("백업 완료했지만 파일이 없어요 {}", backupPath);
                }
            } else {
                log.error("백업중 오류  {}", errorOutput);
            }
        } catch (IOException | InterruptedException e) {
            log.warn("에러 " + dbName, e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }





}