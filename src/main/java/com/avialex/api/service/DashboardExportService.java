package com.avialex.api.service;

import com.avialex.api.model.dto.MainDashboardResponseDTO;
import com.avialex.api.model.dto.MonthlyProcessStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

@Service
public class DashboardExportService {
    private final ProcessService processService;
    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_DATE;
    private static final char DELIM = ';';
    private static final String EOL = "\r\n"; // Windows-friendly line endings
    private static final byte[] BOM = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF}; // UTF-8 BOM to help Excel

    @Autowired
    public DashboardExportService(ProcessService processService) {
        this.processService = processService;
    }

    public ExportedCsv exportDashboard(LocalDate startDate, LocalDate endDate) {
        MainDashboardResponseDTO dto = processService.getDashboard(startDate, endDate);

        StringBuilder sb = new StringBuilder();

        // Summary: header (PT-BR) and a single data row with the dashboard metrics
        sb.append(joinHeader("Processos ativos", "Clientes ativos", "Valor recuperado (R$)", "Taxa de sucesso (%)", "Total de processos")).append(EOL);
        sb.append(joinValues(nullSafe(dto.activeProcess()), nullSafe(dto.activeClients()), formatDecimal(dto.recoveredValue()), nullSafe(dto.SuccessFee()), nullSafe(dto.totalProcesses()))).append(EOL);

        sb.append(EOL); // separation line

        // Monthly stats table (PT-BR headers)
        sb.append(joinHeader("Mês", "Processos ganhos", "Processos perdidos")).append(EOL);
        List<MonthlyProcessStats> months = dto.monthlyStats();
        if (months != null) {
            for (MonthlyProcessStats m : months) {
                sb.append(joinValues(nullSafe(m.month()), nullSafe(m.wonProcesses()), nullSafe(m.lostProcesses()))).append(EOL);
            }
        }

        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] bytes = new byte[BOM.length + body.length];
        System.arraycopy(BOM, 0, bytes, 0, BOM.length);
        System.arraycopy(body, 0, bytes, BOM.length, body.length);

        String filename = "dashboard_" + (startDate != null ? startDate.format(DATE_FMT) : "start") + "_" + (endDate != null ? endDate.format(DATE_FMT) : "end") + ".csv";

        return new ExportedCsv(bytes, filename);
    }

    private static String nullSafe(Object o) {
        return (o == null) ? "" : o.toString();
    }

    private static String joinHeader(String... cols) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.length; i++) {
            sb.append(escapeForCsv(cols[i]));
            if (i < cols.length - 1) sb.append(DELIM);
        }
        return sb.toString();
    }

    private static String joinValues(Object... cols) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.length; i++) {
            String s = (cols[i] == null) ? "" : cols[i].toString();
            sb.append(escapeForCsv(s));
            if (i < cols.length - 1) sb.append(DELIM);
        }
        return sb.toString();
    }

    private static String escapeForCsv(String s) {
        if (s == null) return "";
        boolean mustQuote = s.indexOf(DELIM) >= 0 || s.contains("\n") || s.contains("\r") || s.contains("\"");
        String out = s.replace("\"", "\"\"");
        if (mustQuote) {
            return '"' + out + '"';
        }
        return out;
    }

    private static String formatDecimal(java.math.BigDecimal bd) {
        if (bd == null) return "";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(PT_BR);
        DecimalFormat df = new DecimalFormat("#,##0.00", symbols);
        return df.format(bd);
    }
}
