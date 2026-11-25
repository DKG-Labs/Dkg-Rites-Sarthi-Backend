package com.sarthi.Sms.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * SMS Summary record (shift-wise or day-wise).
 *
 * One row per date (+ shift) + SMS & caster + rail grade + manufacturer:
 * heats cast / rejected / diverted, rejected heat numbers and bloom weights
 * (accepted = prime + CO).
 *
 * Filters: SMS & caster ("SMS 3 - CV1"), rail grade, rejection reason (Hydrogen / Chemical / Nitrogen / Oxygen).
 * Rejection rules are the same as the existing SMS summary, plus ladle chemistry "Reject" = Chemical:
 * Hydrogen (hydris > 1.6), Oxygen (> 20), Nitrogen (> 0.009), Chemical (ladle chemistry Reject),
 * Bloom (prime / CO blooms rejected), Other (heat remark says reject).
 * With a reason filter, only heats rejected for that reason count as rejected, and rows without one are hidden.
 */
@Service
@RequiredArgsConstructor
public class SmsShiftDaySummaryService {

    private final JdbcTemplate jdbc;

    private static final DateTimeFormatter IN = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter OUT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    /** Heat-level rows of SMS 2 and SMS 3 (blooms rolled up per heat so weights are not double counted). */
    private static String heatSql(String smsNo) {
        String heat = smsNo.equals("2") ? "heat_detail_sms2" : "heat_detail_sms3";
        String link = smsNo.equals("2") ? "duty_heat_sms2" : "duty_heat_sms3";
        String bloom = smsNo.equals("2") ? "bloom_detail_sms2" : "bloom_detail_sms3";
        return "SELECT sd.date, sd.shift, sd.sms, COALESCE(NULLIF(sd.organisation, ''), '-') AS org, sd.rail_grade, "
                + "shd.caster_number, shd.heat_number, shd.hydris, shd.oxygen, shd.nitrogen, shd.sent_to_ladle, shd.heat_remark, "
                + "shd.is_diverted, shd.total_cast_wt, shd.weight_of_prime_blooms, shd.weight_of_co_blooms, shd.weight_of_rejected_blooms, "
                + "COALESCE(bd.rej, 0) AS bloom_rej "
                + "FROM sms_duty sd "
                + "JOIN " + link + " dh ON sd.duty_id = dh.duty_id "
                + "JOIN " + heat + " shd ON dh.heat_number = shd.heat_number "
                + "LEFT JOIN (SELECT cast_number, SUM(COALESCE(number_of_prime_blooms_rejected, 0) + COALESCE(number_of_co_blooms_rejected, 0)) rej "
                + "           FROM " + bloom + " GROUP BY cast_number) bd ON bd.cast_number = shd.heat_number "
                + "WHERE sd.date BETWEEN ? AND ? ";
    }

    public List<Map<String, Object>> summary(String fromDate, String toDate, String groupBy,
                                             String smsCaster, String railGrade, String reason) {
        LocalDate from = parse(fromDate, "From date");
        LocalDate to = parse(toDate, "To date");
        if (to.isBefore(from)) { LocalDate t = from; from = to; to = t; }
        boolean dayWise = "DAY".equalsIgnoreCase(groupBy);
        String wantReason = blank(reason) ? null : reason.trim().toUpperCase();
        String wantGrade = blank(railGrade) ? null : norm(railGrade);
        String wantSmsCaster = blank(smsCaster) ? null : norm(smsCaster);

        // sd.date is a DATE: BETWEEN from AND to covers both ends
        Object[] range = {Date.valueOf(from), Date.valueOf(to)};

        List<Map<String, Object>> heats = new ArrayList<>();
        heats.addAll(jdbc.queryForList(heatSql("2"), range));
        heats.addAll(jdbc.queryForList(heatSql("3"), range));

        // group key -> aggregate
        Map<String, Agg> groups = new LinkedHashMap<>();
        for (Map<String, Object> h : heats) {
            String sms = str(h.get("sms"));
            String caster = str(h.get("caster_number"));
            String smsCasterLabel = caster.isEmpty() ? sms : sms + " - " + caster;
            String grade = str(h.get("rail_grade"));
            if (wantSmsCaster != null && !norm(smsCasterLabel).equals(wantSmsCaster)) continue;
            if (wantGrade != null && !norm(grade).equals(wantGrade) && !norm("R" + grade).equals(wantGrade)) continue;

            LocalDate d = ((Date) h.get("date")).toLocalDate();
            String shift = str(h.get("shift"));
            String org = str(h.get("org"));
            String key = d + "|" + (dayWise ? "" : shift) + "|" + smsCasterLabel + "|" + grade + "|" + org;
            Agg a = groups.computeIfAbsent(key, k -> new Agg(d, dayWise ? null : shift, smsCasterLabel, grade, org));

            Set<String> reasons = reasonsOf(h);
            boolean rejected = wantReason == null ? !reasons.isEmpty() : reasons.contains(wantReason);
            a.heats.add(str(h.get("heat_number")));
            if (rejected) {
                a.rejected.add(str(h.get("heat_number")));
                a.reasons.addAll(wantReason == null ? reasons : Set.of(wantReason));
            }
            if (truthy(h.get("is_diverted"))) a.diverted.add(str(h.get("heat_number")));
            a.castWt = a.castWt.add(dec(h.get("total_cast_wt")));
            a.primeWt = a.primeWt.add(dec(h.get("weight_of_prime_blooms")));
            a.coWt = a.coWt.add(dec(h.get("weight_of_co_blooms")));
            a.rejWt = a.rejWt.add(dec(h.get("weight_of_rejected_blooms")));
        }

        List<Agg> rows = new ArrayList<>(groups.values());
        if (wantReason != null) rows.removeIf(a -> a.rejected.isEmpty());
        rows.sort(Comparator.comparing((Agg a) -> a.date).reversed()
                .thenComparing(a -> a.shift == null ? "" : a.shift)
                .thenComparing(a -> a.smsCaster));

        List<Map<String, Object>> out = new ArrayList<>();
        for (Agg a : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("date", a.date.toString());
            m.put("shift", a.shift);
            m.put("dateAndShift", a.date.format(OUT) + (a.shift == null ? "" : ", " + a.shift));
            m.put("manufacturer", a.org);
            m.put("smsCaster", a.smsCaster);
            m.put("railGrade", a.grade);
            m.put("numberOfHeatsCast", a.heats.size());
            m.put("numberOfHeatsRejected", a.rejected.size());
            m.put("numberOfDivertedHeats", a.diverted.size());
            m.put("rejectedHeatNumbers", String.join(", ", a.rejected));
            m.put("reasonForRejection", String.join(", ", a.reasons));
            m.put("weightOfHeatsCast", a.castWt);
            m.put("weightOfPrimeBlooms", a.primeWt);
            m.put("weightOfCOBlooms", a.coWt);
            m.put("weightOfAcceptedBlooms", a.primeWt.add(a.coWt));
            m.put("weightOfRejectedBlooms", a.rejWt);
            out.add(m);
        }
        return out;
    }

    /** Why a heat was rejected (empty = accepted). */
    private static Set<String> reasonsOf(Map<String, Object> h) {
        Set<String> r = new LinkedHashSet<>();
        if (dec(h.get("hydris")).compareTo(new BigDecimal("1.6")) > 0) r.add("HYDROGEN");
        if ("REJECT".equalsIgnoreCase(str(h.get("sent_to_ladle")))) r.add("CHEMICAL");
        if (dec(h.get("nitrogen")).compareTo(new BigDecimal("0.009")) > 0) r.add("NITROGEN");
        if (dec(h.get("oxygen")).compareTo(new BigDecimal("20")) > 0) r.add("OXYGEN");
        if (dec(h.get("bloom_rej")).signum() > 0) r.add("BLOOM");
        if (r.isEmpty() && str(h.get("heat_remark")).toLowerCase().contains("reject")) r.add("OTHER");
        return r;
    }

    private static final class Agg {
        final LocalDate date; final String shift; final String smsCaster; final String grade; final String org;
        final Set<String> heats = new LinkedHashSet<>(), rejected = new LinkedHashSet<>(), diverted = new LinkedHashSet<>();
        final Set<String> reasons = new LinkedHashSet<>();
        BigDecimal castWt = BigDecimal.ZERO, primeWt = BigDecimal.ZERO, coWt = BigDecimal.ZERO, rejWt = BigDecimal.ZERO;
        Agg(LocalDate d, String s, String sc, String g, String o) { date = d; shift = s; smsCaster = sc; grade = g; org = o; }
    }

    private static LocalDate parse(String v, String label) {
        if (blank(v)) throw new IllegalArgumentException(label + " is required.");
        try { return LocalDate.parse(v.trim(), IN); } catch (Exception e) { /* try ISO */ }
        try { return LocalDate.parse(v.trim()); } catch (Exception e) {
            throw new IllegalArgumentException(label + " is not a valid date (dd/MM/yyyy).");
        }
    }

    private static String norm(String v) { return v == null ? "" : v.replaceAll("[\\s\\-]+", "").toUpperCase(); }
    private static boolean blank(String v) { return v == null || v.isBlank() || "ALL".equalsIgnoreCase(v.trim()); }
    private static String str(Object o) { return o == null ? "" : o.toString().trim(); }
    private static boolean truthy(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean b) return b;
        if (o instanceof Number n) return n.intValue() != 0;
        return "true".equalsIgnoreCase(o.toString()) || "1".equals(o.toString());
    }
    private static BigDecimal dec(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal b) return b;
        try { return new BigDecimal(o.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}
