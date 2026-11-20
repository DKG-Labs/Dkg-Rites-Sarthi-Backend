package com.sarthi.bookset;

import com.sarthi.entity.UserMaster;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Book No + Set No can be used on only one IC (ERC, Sleeper, Rail Pad).
 * check(): used by "Verify Book &amp; Set". claim(): used by every Save Changes / Edit save.
 */
@Service
@RequiredArgsConstructor
public class BookSetUsageGuard {

    private final IcBookSetUsageRepository repository;

    /** The IC that already holds this pair, if it is a different IC. Empty when free or held by this same IC. */
    @Transactional(readOnly = true)
    public Optional<IcBookSetUsage> findConflict(String bookNo, String setNo, String icNumber) {
        String book = norm(bookNo), set = norm(setNo), key = icKey(icNumber);
        if (book == null || set == null) return Optional.empty();
        return repository.findByBookNoAndSetNo(book, set)
                .filter(u -> key == null || !u.getIcKey().equals(key));
    }

    public static String conflictMessage(IcBookSetUsage u) {
        return "Book No. " + u.getBookNo() + " / Set No. " + u.getSetNo()
                + " is already used for IC " + u.getIcNumber()
                + (u.getUserId() != null ? " by user ID " + u.getUserId() : "")
                + ". Please use a different Book No. / Set No.";
    }

    /**
     * Records that this IC uses the pair. Throws BookSetInUseException when another IC already has it.
     * If this IC earlier held a different pair, that pair is released.
     */
    @Transactional
    public void claim(String bookNo, String setNo, String icNumber, String productStage) {
        String book = norm(bookNo), set = norm(setNo), key = icKey(icNumber);
        if (book == null || set == null || key == null) return;

        Optional<IcBookSetUsage> existing = repository.findByBookNoAndSetNo(book, set);
        if (existing.isPresent()) {
            IcBookSetUsage u = existing.get();
            if (!u.getIcKey().equals(key)) throw new BookSetInUseException(conflictMessage(u));
            return; // same IC re-saving its own pair
        }

        // this IC is switching to a new pair: free the old one
        repository.findByIcKey(key).forEach(repository::delete);

        IcBookSetUsage u = new IcBookSetUsage();
        u.setBookNo(book);
        u.setSetNo(set);
        u.setIcKey(key);
        u.setIcNumber(icNumber.trim());
        u.setProductStage(productStage);
        u.setUserId(currentUserId());
        u.setCreatedAt(LocalDateTime.now());
        try {
            repository.saveAndFlush(u);
        } catch (DataIntegrityViolationException race) {
            // another IC claimed it at the same moment
            throw new BookSetInUseException("Book No. " + book + " / Set No. " + set
                    + " was just used for another IC. Please use a different Book No. / Set No.");
        }
    }

    /** "E/EF-26110903/RAJE" -> "EF-26110903"; "RPP-101626003" -> "RPP-101626003". */
    static String icKey(String icNumber) {
        if (icNumber == null || icNumber.isBlank()) return null;
        String[] parts = icNumber.trim().split("\\s*/\\s*");
        String key = parts.length >= 3
                ? String.join("/", java.util.Arrays.copyOfRange(parts, 1, parts.length - 1))
                : icNumber.trim();
        return key.toUpperCase();
    }

    private static String norm(String v) {
        if (v == null) return null;
        String t = v.trim().toUpperCase();
        return t.isEmpty() ? null : t;
    }

    private static String currentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserMaster u && u.getUserId() != null) {
                return String.valueOf(u.getUserId());
            }
        } catch (Exception ignored) { }
        return null;
    }
}
