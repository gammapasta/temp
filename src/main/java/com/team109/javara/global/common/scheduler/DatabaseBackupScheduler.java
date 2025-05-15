package com.team109.javara.global.common.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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


    // 매주 일요일 새벽 1시에 실행
    @Scheduled(cron = "${app.backup.schedule}")
    public void backupDatabase() {
        log.info("Start db 백업 진행 시작");

        String dbName = extractDbNameFromUrl(dbUrl);
        if (dbName == null) {
            log.error("데이터베이스에 해당 이름 없음 URL: {}", dbUrl);
            return;
        }

        // 백업 파일명 생성 (날짜 포함)
        String dateSuffix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String backupFileName = dbName + "_backup_" + dateSuffix + ".sql";
        String fullBackupPath = backupDirectory + File.separator + backupFileName;

        // 백업 디렉토리 생성
        createBackupDirectory();

        // 백업 실행
        performBackup(dbName, fullBackupPath);

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

    private void performBackup(String dbName, String fullBackupPath) {
        // mysqldump 명령어 .my.cnf 파일
        String command = String.format("%s -u%s %s", mysqldumpPath, dbUsername, dbName);
        log.info("백업 시작 {} to {}", dbName, fullBackupPath);

        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.command("/bin/sh", "-c", command + " > " + fullBackupPath);


        try {
            Process process = processBuilder.start();

            StringBuilder errorOutput = new StringBuilder();
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
            }

            // 프로세스 완료 대기
            boolean finished = process.waitFor(10, TimeUnit.MINUTES);

            if (finished && process.exitValue() == 0) {
                File backupFile = new File(fullBackupPath);
                if (backupFile.exists() && backupFile.length() > 0) {
                    log.info("백업 성공 {}, {} bytes",
                            fullBackupPath, backupFile.length());
                } else {
                    log.warn("백업 완료했지만 파일이 없어요 {}", fullBackupPath);
                }
            } else {
                log.error("백업중 오류 Exit code: {}, Error: {}",
                        finished ? process.exitValue() : "Timeout", errorOutput);
            }
        } catch (IOException | InterruptedException e) {
            log.error("에러 " + dbName, e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }



    // JDBC URL에서 DB 이름 추출
    private String extractDbNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        try {
            int lastSlash = url.lastIndexOf('/');
            if (lastSlash == -1 || lastSlash == url.length() - 1) {
                return null;
            }

            String dbNameAndParams = url.substring(lastSlash + 1);
            int questionMark = dbNameAndParams.indexOf('?');

            if (questionMark != -1) {
                return dbNameAndParams.substring(0, questionMark);
            }
            return dbNameAndParams;
        } catch (Exception e) {
            return null;
        }
    }
}