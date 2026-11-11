package com.sarthi.udmcomplaint.service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sarthi.entity.UdmRejection;
import com.sarthi.repository.UdmRejectionRepository;
import com.sarthi.udmcomplaint.dto.UdmComplaintHistoryDto;
import com.sarthi.udmcomplaint.dto.UdmDocumentFile;
import com.sarthi.udmcomplaint.entity.UdmComplaint;
import com.sarthi.udmcomplaint.entity.UdmComplaintDocument;
import com.sarthi.udmcomplaint.entity.UdmComplaintStage;
import com.sarthi.udmcomplaint.repository.UdmComplaintDocumentRepository;
import com.sarthi.udmcomplaint.repository.UdmComplaintRepository;
import com.sarthi.udmcomplaint.repository.UdmComplaintStageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.sarthi.udmcomplaint.UdmComplaintConstants.*;
import static com.sarthi.udmcomplaint.service.UdmComplaintWorkflowSupport.validation;

/**
 * "Download Merged PDF" for a UDM complaint, in this order:
 *  1. original rejection PDF from IREPS
 *  2. generated Complaint Timeline report (date, action, roles, performed by name + code, RIO, remarks, stage details)
 *  3. uploaded documents in workflow order (JI / Audit report → RCA evidence → CAPA evidence, round by round)
 * A part that cannot be read becomes a placeholder page, so the rest still downloads.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UdmComplaintMergedPdfService {

    private static final String IREPS_BASE = "https://ireps.gov.in";
    private static final String IREPS_HOST = "ireps.gov.in";
    private static final long MAX_REMOTE_BYTES = 25L * 1024 * 1024;
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private static final Map<String, String> ACTION_LABEL = Map.ofEntries(
            Map.entry(TR_RECEIVED, "Received from UDM"),
            Map.entry(TR_SEND_TO_CM, "Sent to Controlling Manager"),
            Map.entry(TR_CLOSE, "Closed by Complaint Cell"),
            Map.entry(TR_CM_SCHEDULE_JI, "Joint Inspection Scheduled"),
            Map.entry(TR_CM_START_AUDIT, "Audit Started"),
            Map.entry(TR_CM_COMPLETE_JI, "Joint Inspection Completed"),
            Map.entry(TR_CM_COMPLETE_AUDIT, "Audit Completed"),
            Map.entry(TR_CM_SUBMIT_RCA, "RCA Submitted"),
            Map.entry(TR_CM_SUBMIT_CAPA, "CAPA Submitted · Sent to SBU Head"),
            Map.entry(TR_SBU_FORWARD_TO_RB, "SBU Head · Forwarded to Railway Board"),
            Map.entry(TR_SBU_RETURN_TO_CM, "SBU Head · Returned to CM for Rework"),
            Map.entry(TR_RB_APPROVE, "Approved by Railway Board · Closed"),
            Map.entry(TR_RB_RETURN_TO_SBU, "Railway Board · Returned to SBU Head"));

    /** Which stage row belongs to which workflow action (matched in order of occurrence). */
    private static final Map<String, String> STAGE_OF_ACTION = Map.of(
            TR_CM_SCHEDULE_JI, STAGE_DECISION,
            TR_CM_START_AUDIT, STAGE_DECISION,
            TR_CM_COMPLETE_JI, STAGE_JI_COMPLETE,
            TR_CM_COMPLETE_AUDIT, STAGE_AUDIT_COMPLETE,
            TR_CM_SUBMIT_RCA, STAGE_RCA,
            TR_CM_SUBMIT_CAPA, STAGE_CAPA,
            TR_SBU_FORWARD_TO_RB, STAGE_SBU_REVIEW,
            TR_SBU_RETURN_TO_CM, STAGE_SBU_REVIEW,
            TR_RB_APPROVE, STAGE_RB_REVIEW,
            TR_RB_RETURN_TO_SBU, STAGE_RB_REVIEW);

    private static final Map<String, String> DOC_LABEL = Map.of(
            DOC_JI_REPORT, "JI Report", DOC_AUDIT_REPORT, "Audit Report",
            DOC_RCA_EVIDENCE, "RCA Evidence", DOC_CAPA_EVIDENCE, "CAPA Evidence");

    private static final Color INK = new Color(15, 23, 42);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color LINE = new Color(226, 232, 240);
    private static final Color SOFT = new Color(248, 250, 252);
    private static final Color GREEN = new Color(21, 128, 61);

    private final UdmRejectionRepository rejectionRepository;
    private final UdmComplaintRepository complaintRepository;
    private final UdmComplaintStageRepository stageRepository;
    private final UdmComplaintDocumentRepository documentRepository;
    private final UdmComplaintWorkflowService workflowService;
    private final UdmComplaintDocumentStorage storage;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    /**
     * Cheap fingerprint of everything that goes into the merged PDF (complaint version + documents + rejection PDF link).
     * Used as an HTTP ETag: if nothing changed, the browser gets 304 and no PDF bytes leave Azure again (egress).
     */
    @Transactional(readOnly = true)
    public String etag(Long udmRejectionId) {
        UdmRejection r = rejectionRepository.findById(udmRejectionId).orElse(null);
        if (r == null) return null;
        String version = complaintRepository.findByUdmRejectionId(udmRejectionId)
                .map(c -> c.getVersion() + ":" + c.getStatus() + ":" + c.getCurrentRound()).orElse("new");
        StringBuilder docIds = new StringBuilder();
        documentRepository.findByUdmRejectionIdAndActiveTrueOrderByUploadedAtAsc(udmRejectionId)
                .forEach(d -> docIds.append(d.getId()).append(','));
        String raw = udmRejectionId + "|" + version + "|" + docIds + "|" + r.getPdfPath() + "|v1";
        return "\"udm-" + Integer.toHexString(raw.hashCode()) + "-" + Integer.toHexString(raw.length()) + "\"";
    }

    @Transactional(readOnly = true)
    public UdmDocumentFile build(Long udmRejectionId) {
        UdmRejection r = rejectionRepository.findById(udmRejectionId)
                .orElseThrow(() -> validation("UDM rejection not found."));
        Optional<UdmComplaint> complaint = complaintRepository.findByUdmRejectionId(udmRejectionId);
        List<UdmComplaintHistoryDto> history = workflowService.getHistory(udmRejectionId);
        List<UdmComplaintStage> stages = stageRepository.findByUdmRejectionIdOrderByCreatedAtAsc(udmRejectionId);
        // Uploaded in workflow order (JI/Audit → RCA → CAPA, round after round) = upload order
        List<UdmComplaintDocument> docs = documentRepository.findByUdmRejectionIdAndActiveTrueOrderByUploadedAtAsc(udmRejectionId);

        List<byte[]> parts = new ArrayList<>();

        // 1. Original rejection PDF (IREPS)
        String url = rejectionPdfUrl(r.getPdfPath());
        byte[] original = url == null ? null : fetchIrepsPdf(url);
        parts.add(original != null ? original
                : placeholder("Original rejection PDF not available",
                        url == null ? "No rejection document link was received from UDM."
                                : "The document could not be downloaded from IREPS. Link: " + url));

        // 2. Timeline report
        parts.add(timelineReport(r, complaint.orElse(null), history, stages, docs));

        // 3. Uploaded documents
        for (UdmComplaintDocument d : docs) {
            try {
                parts.add(storage.download(d.getBlobName()));
            } catch (RuntimeException e) {
                log.warn("[UDM-COMPLAINT] merged PDF: document {} not readable: {}", d.getId(), e.getMessage());
                parts.add(placeholder(docLabel(d) + " not available", d.getOriginalName()));
            }
        }

        String name = "UDM_Complaint_" + safe(r.getRejectionNumber()) + "_" + LocalDate.now(IST) + ".pdf";
        return new UdmDocumentFile(name, "application/pdf", merge(parts));
    }

    // ------------------------------------------------------------------ IREPS download (SSRF-safe)

    private static String rejectionPdfUrl(String pdfPath) {
        if (pdfPath == null || pdfPath.isBlank()) return null;
        String p = pdfPath.trim();
        return p.toLowerCase(Locale.ROOT).startsWith("http") ? p : IREPS_BASE + (p.startsWith("/") ? "" : "/") + p;
    }

    /** Downloads only from https://ireps.gov.in (or its sub-domains); null on any failure. */
    private byte[] fetchIrepsPdf(String url) {
        try {
            URI uri = URI.create(url.replace(" ", "%20"));
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || !(host.equals(IREPS_HOST) || host.endsWith("." + IREPS_HOST))) {
                log.warn("[UDM-COMPLAINT] merged PDF: refused non-IREPS url {}", url);
                return null;
            }
            HttpRequest req = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(20)).GET().build();
            HttpResponse<InputStream> res = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
            if (res.statusCode() != 200) {
                log.warn("[UDM-COMPLAINT] merged PDF: IREPS returned {} for {}", res.statusCode(), url);
                return null;
            }
            try (InputStream in = res.body()) {
                byte[] bytes = in.readNBytes((int) MAX_REMOTE_BYTES + 1);
                if (bytes.length > MAX_REMOTE_BYTES || bytes.length < 5 || bytes[0] != '%' || bytes[1] != 'P') return null;
                return bytes;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            log.warn("[UDM-COMPLAINT] merged PDF: IREPS download failed for {}: {}", url, e.getMessage());
            return null;
        }
    }

    // ------------------------------------------------------------------ merge (PDFBox)

    private byte[] merge(List<byte[]> parts) {
        List<PDDocument> opened = new ArrayList<>();
        try (PDDocument out = new PDDocument(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            PDFMergerUtility merger = new PDFMergerUtility();
            for (byte[] part : parts) {
                PDDocument src = open(part);
                if (src == null) src = open(placeholder("Document could not be included",
                        "This file is damaged or password-protected."));
                if (src == null) continue;
                opened.add(src);
                merger.appendDocument(out, src);
            }
            out.save(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            log.error("[UDM-COMPLAINT] merged PDF failed: {}", e.getMessage(), e);
            throw validation("Could not prepare the merged PDF. Please try again.");
        } finally {
            for (PDDocument d : opened) {
                try { d.close(); } catch (Exception ignored) { /* closing */ }
            }
        }
    }

    private static PDDocument open(byte[] bytes) {
        try {
            PDDocument d = Loader.loadPDF(bytes);
            if (d.isEncrypted()) {
                try { d.setAllSecurityToBeRemoved(true); } catch (Exception ignored) { /* best effort */ }
            }
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ generated pages (OpenPDF)

    private byte[] timelineReport(UdmRejection r, UdmComplaint c, List<UdmComplaintHistoryDto> history,
                                  List<UdmComplaintStage> stages, List<UdmComplaintDocument> docs) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 40, 40);
        try {
            PdfWriter.getInstance(doc, bos);
            doc.open();
            Font h1 = new Font(Font.HELVETICA, 15, Font.BOLD, INK);
            Font h2 = new Font(Font.HELVETICA, 11, Font.BOLD, INK);
            Font small = new Font(Font.HELVETICA, 8.5f, Font.NORMAL, MUTED);
            Font body = new Font(Font.HELVETICA, 9.5f, Font.NORMAL, INK);
            Font bold = new Font(Font.HELVETICA, 9.5f, Font.BOLD, INK);

            doc.add(new Paragraph("UDM Complaint — Timeline Report", h1));
            doc.add(new Paragraph("Generated " + LocalDate.now(IST).format(DAY) + " · SARTHI", small));
            doc.add(spacer(6));

            // Complaint summary
            PdfPTable meta = new PdfPTable(new float[]{1.2f, 2f, 1.2f, 2f});
            meta.setWidthPercentage(100);
            cellPair(meta, "Rejection No.", r.getRejectionNumber(), bold, body, small);
            cellPair(meta, "Received from UDM", day(r.getFetchedDate() != null ? r.getFetchedDate().atStartOfDay() : null), bold, body, small);
            cellPair(meta, "Vendor", join(r.getVendorName(), r.getVendorCode()), bold, body, small);
            cellPair(meta, "Consignee", r.getConsignee(), bold, body, small);
            cellPair(meta, "PO No. / Sr.", join(r.getPoNumber(), r.getPoSerialNumber()), bold, body, small);
            cellPair(meta, "IC No. / Date", join(r.getIcNumber(), day(r.getIcDate())), bold, body, small);
            cellPair(meta, "Rejected Qty", join(r.getRejectedQty() != null ? r.getRejectedQty().toPlainString() : null, r.getUom()), bold, body, small);
            cellPair(meta, "Current Status", c == null ? "Pending with Complaint Cell" : statusLabel(c.getStatus()), bold, body, small);
            doc.add(meta);
            if (r.getReasonForRejection() != null && !r.getReasonForRejection().isBlank()) {
                doc.add(spacer(4));
                doc.add(new Paragraph("Reason for rejection: " + r.getReasonForRejection(), body));
            }

            // Timeline
            doc.add(spacer(10));
            doc.add(new Paragraph("Complaint Timeline", h2));
            doc.add(spacer(4));
            Map<String, Deque<UdmComplaintStage>> stageQueues = new HashMap<>();
            for (UdmComplaintStage s : stages) stageQueues.computeIfAbsent(s.getStage(), k -> new ArrayDeque<>()).add(s);

            PdfPTable tl = new PdfPTable(new float[]{0.35f, 1.1f, 4.2f});
            tl.setWidthPercentage(100);
            header(tl, new String[]{"#", "Date", "Action / Performed by / Remarks"}, small);
            if (history.isEmpty()) {
                addRow(tl, "1", day(r.getFetchedDate() != null ? r.getFetchedDate().atStartOfDay() : null),
                        para("Received from UDM", bold, "Pending with Complaint Cell", body, null, small), 0);
            }
            int i = 0;
            for (UdmComplaintHistoryDto h : history) {
                i++;
                String when = TR_RECEIVED.equals(h.getAction()) && r.getFetchedDate() != null
                        ? r.getFetchedDate().format(DAY) : dayStr(h.getActionDate());
                Paragraph p = new Paragraph();
                p.add(new Chunk(ACTION_LABEL.getOrDefault(h.getAction(), h.getAction()), bold));
                String flow = join(h.getFromRole(), h.getToRole(), " → ");
                if (!flow.isBlank()) p.add(new Chunk("   " + flow, small));
                String by = h.getActionByName() == null ? null
                        : h.getActionByName() + (h.getActionByEmployeeCode() != null ? " (" + h.getActionByEmployeeCode() + ")" : "");
                if (by != null && !TR_RECEIVED.equals(h.getAction())) p.add(new Chunk("\nBy: " + by, body));
                if (h.getAssignedRio() != null) p.add(new Chunk("   ·   RIO " + h.getAssignedRio(), small));
                String stageKey = STAGE_OF_ACTION.get(h.getAction());
                UdmComplaintStage s = stageKey == null ? null : Optional.ofNullable(stageQueues.get(stageKey)).map(Deque::poll).orElse(null);
                String details = stageDetails(s);
                if (!details.isBlank()) p.add(new Chunk("\n" + details, small));
                boolean repeatsTitle = h.getRemarks() != null
                        && h.getRemarks().trim().equalsIgnoreCase(ACTION_LABEL.getOrDefault(h.getAction(), ""));
                if (h.getRemarks() != null && !h.getRemarks().isBlank() && !repeatsTitle) {
                    p.add(new Chunk("\nRemarks: " + h.getRemarks(), body));
                }
                addRow(tl, String.valueOf(i), when, p, i);
            }
            doc.add(tl);

            // Attached documents (in merged order)
            doc.add(spacer(10));
            doc.add(new Paragraph("Attached Documents (follow this page)", h2));
            doc.add(spacer(4));
            if (docs.isEmpty()) {
                doc.add(new Paragraph("No documents were uploaded.", body));
            } else {
                PdfPTable dt = new PdfPTable(new float[]{0.35f, 1.4f, 3.2f, 1.1f});
                dt.setWidthPercentage(100);
                header(dt, new String[]{"#", "Document", "File", "Uploaded"}, small);
                int n = 0;
                for (UdmComplaintDocument d : docs) {
                    n++;
                    String label = docLabel(d) + (d.getRoundNo() != null && d.getRoundNo() > 1 ? " (Round " + d.getRoundNo() + ")" : "");
                    addRow(dt, String.valueOf(n), label, new Paragraph(d.getOriginalName(), body), n, day(d.getUploadedAt()));
                }
                doc.add(dt);
            }
            doc.close();
            return bos.toByteArray();
        } catch (DocumentException e) {
            throw validation("Could not prepare the timeline report.");
        }
    }

    private static String stageDetails(UdmComplaintStage s) {
        if (s == null) return "";
        List<String> parts = new ArrayList<>();
        if (s.getRoundNo() != null && s.getRoundNo() > 1) parts.add("Round " + s.getRoundNo());
        switch (s.getStage()) {
            case STAGE_DECISION -> {
                parts.add(PATH_JI.equals(s.getPath()) ? "Path: Joint Inspection" : "Path: Audit of Record / Firm");
                if (s.getStageDate() != null) parts.add("JI date: " + s.getStageDate().format(DAY));
                if (s.getJiTeam() != null) parts.add("JI team: " + s.getJiTeam());
                List<String> flags = new ArrayList<>();
                if (Boolean.TRUE.equals(s.getFlagFailed90())) flags.add("failed within 90 days");
                if (Boolean.TRUE.equals(s.getFlagQtyOver5())) flags.add("rejected > 5%");
                if (Boolean.TRUE.equals(s.getFlagCritical())) flags.add("critical");
                if (!flags.isEmpty()) parts.add("Flags: " + String.join(", ", flags));
            }
            case STAGE_JI_COMPLETE -> {
                if (s.getStageDate() != null) parts.add("Actual JI date: " + s.getStageDate().format(DAY));
                parts.add("Outcome: " + nz(s.getJiOutcome()));
                parts.add("Attributable to: " + nz(s.getAttributableTo()));
            }
            case STAGE_AUDIT_COMPLETE -> {
                parts.add("Audit: " + nz(s.getAuditType()) + (s.getStageDate() != null ? " on " + s.getStageDate().format(DAY) : ""));
                parts.add("Findings: " + nz(s.getFindings()));
                parts.add("Attributable to: " + nz(s.getAttributableTo()));
            }
            case STAGE_RCA -> parts.add("Root cause: " + nz(s.getRootCause()));
            case STAGE_CAPA -> {
                parts.add("Corrective: " + nz(s.getCorrectiveActions()).replace("|", ", "));
                parts.add("Preventive: " + nz(s.getPreventiveActions()).replace("|", ", "));
                parts.add("Vendor compliance: " + yn(s.getVendorCompliance()) + " · CAPA implemented: " + yn(s.getCapaImplemented()));
            }
            default -> { /* SBU / RB review: decision is in the action title, remarks shown separately */ }
        }
        return String.join("  |  ", parts);
    }

    private static byte[] placeholder(String title, String message) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 48, 48, 72, 48);
        try {
            PdfWriter.getInstance(doc, bos);
            doc.open();
            doc.add(new Paragraph(title, new Font(Font.HELVETICA, 14, Font.BOLD, INK)));
            doc.add(spacer(8));
            doc.add(new Paragraph(message == null ? "" : message, new Font(Font.HELVETICA, 10, Font.NORMAL, MUTED)));
            doc.close();
        } catch (DocumentException e) {
            return new byte[0];
        }
        return bos.toByteArray();
    }

    // ------------------------------------------------------------------ small helpers

    private static void header(PdfPTable t, String[] cols, Font f) {
        for (String col : cols) {
            PdfPCell cell = new PdfPCell(new Phrase(col.toUpperCase(Locale.ROOT), f));
            cell.setBackgroundColor(SOFT);
            cell.setBorderColor(LINE);
            cell.setPadding(5);
            t.addCell(cell);
        }
        t.setHeaderRows(1);
    }

    private static void addRow(PdfPTable t, String no, String when, Paragraph content, int index, String... extra) {
        Font f = new Font(Font.HELVETICA, 9, Font.NORMAL, INK);
        t.addCell(cell(new Phrase(no, f), index));
        t.addCell(cell(new Phrase(when == null ? "" : when, f), index));
        PdfPCell c = new PdfPCell(content);
        style(c, index);
        t.addCell(c);
        for (String e : extra) t.addCell(cell(new Phrase(e == null ? "" : e, f), index));
    }

    private static Paragraph para(String title, Font tf, String text, Font bf, String extra, Font ef) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(title, tf));
        if (text != null) p.add(new Chunk("\n" + text, bf));
        if (extra != null) p.add(new Chunk("\n" + extra, ef));
        return p;
    }

    private static PdfPCell cell(Phrase p, int index) {
        PdfPCell c = new PdfPCell(p);
        style(c, index);
        return c;
    }

    private static void style(PdfPCell c, int index) {
        c.setBorderColor(LINE);
        c.setPadding(5);
        if (index % 2 == 0) c.setBackgroundColor(SOFT);
    }

    private static void cellPair(PdfPTable t, String label, String value, Font lf, Font vf, Font small) {
        PdfPCell l = new PdfPCell(new Phrase(label, small));
        l.setBorderColor(LINE);
        l.setBackgroundColor(SOFT);
        l.setPadding(5);
        PdfPCell v = new PdfPCell(new Phrase(value == null || value.isBlank() ? "—" : value, lf));
        v.setBorderColor(LINE);
        v.setPadding(5);
        t.addCell(l);
        t.addCell(v);
    }

    private static Paragraph spacer(float h) {
        Paragraph p = new Paragraph(" ");
        p.setLeading(h);
        return p;
    }

    private static String statusLabel(String s) {
        if (s == null) return "Pending with Complaint Cell";
        return switch (s) {
            case ST_PENDING_CC -> "Pending with Complaint Cell";
            case ST_SENT_TO_CM -> "Sent to CM";
            case ST_JI_SCHEDULED -> "JI Scheduled";
            case ST_JI_COMPLETED -> "JI Completed";
            case ST_AUDIT_IN_PROGRESS -> "Audit in Progress";
            case ST_AUDIT_COMPLETED -> "Audit Completed";
            case ST_RCA_SUBMITTED -> "RCA Submitted";
            case ST_CAPA_SUBMITTED -> "Pending with SBU Head";
            case ST_RETURNED_TO_CM -> "Returned to CM (Rework)";
            case ST_SENT_TO_RB -> "Pending with Railway Board";
            case ST_RETURNED_TO_SBU -> "Returned by Railway Board";
            case ST_CLOSED -> "Closed";
            default -> s;
        };
    }

    private static String docLabel(UdmComplaintDocument d) { return DOC_LABEL.getOrDefault(d.getDocType(), d.getDocType()); }

    private static String day(LocalDateTime t) { return t == null ? null : t.format(DAY); }

    /** History dates come as LocalDateTime strings (server time = IST). */
    private static String dayStr(String iso) {
        if (iso == null) return "";
        try { return LocalDateTime.parse(iso).format(DAY); } catch (Exception e) { return iso; }
    }

    private static String join(String a, String b) { return join(a, b, " · "); }

    private static String join(String a, String b, String sep) {
        boolean ha = a != null && !a.isBlank();
        boolean hb = b != null && !b.isBlank();
        return ha && hb ? a + sep + b : ha ? a : hb ? b : "";
    }

    private static String nz(String s) { return s == null || s.isBlank() ? "—" : s; }

    private static String yn(String v) { return "Y".equals(v) ? "Yes" : "N".equals(v) ? "No" : "—"; }

    private static String safe(String s) { return s == null ? "complaint" : s.replaceAll("[^A-Za-z0-9_-]", "_"); }
}
