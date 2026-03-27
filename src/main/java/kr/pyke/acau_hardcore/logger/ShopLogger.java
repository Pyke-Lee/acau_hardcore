package kr.pyke.acau_hardcore.logger;

import kr.pyke.acau_hardcore.AcauHardCore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ShopLogger {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logTransaction(String shopID, String action, String player, String item, int amount, int perPrice, int total, String paymentType) {
        File logDir = new File("logs/custom_shop");
        if (!logDir.exists()) { logDir.mkdirs(); }

        File logFile = new File(logDir, shopID + ".log");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            String time = LocalDateTime.now().format(FORMATTER);
            String logLine = String.format("[%s] [%s] (%s) - (%s) x(%d) (%d %s) / (%d %s)\n", time, action, player, item, amount, perPrice, paymentType, total, paymentType);
            writer.write(logLine);
        }
        catch (Exception e) {
            AcauHardCore.LOGGER.error("상점 로그 파일 작성 중 IO 오류가 발생했습니다. 대상 상점 ID: {}, 플레이어: {}, 시도한 작업: {}", shopID, player, action);
            AcauHardCore.LOGGER.error("오류 상세 내용: {}", e.getMessage());
        }
    }
}
