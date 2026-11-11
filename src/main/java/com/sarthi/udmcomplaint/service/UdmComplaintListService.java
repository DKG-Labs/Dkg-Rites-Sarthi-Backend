package com.sarthi.udmcomplaint.service;

import com.sarthi.entity.UdmRejection;
import com.sarthi.repository.UdmRejectionRepository;
import com.sarthi.service.Impl.UdmRejectionService;
import com.sarthi.udmcomplaint.dto.UdmComplaintListResponse;
import com.sarthi.udmcomplaint.repository.UdmComplaintListRepository;
import com.sarthi.udmcomplaint.repository.UdmComplaintListRepository.Counts;
import com.sarthi.udmcomplaint.repository.UdmComplaintListRepository.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sarthi.udmcomplaint.UdmComplaintConstants.*;
import static com.sarthi.udmcomplaint.repository.UdmComplaintListRepository.*;
import static com.sarthi.udmcomplaint.service.UdmComplaintWorkflowSupport.validation;

/**
 * Role-scoped, server-paginated UDM complaint list.
 * The requested view must match a role the logged-in user holds (user_role_master); "board" is read-only for anyone signed in.
 */
@Service
@RequiredArgsConstructor
public class UdmComplaintListService {

    private static final int MAX_PAGE_SIZE = 500;
    private static final Set<String> VIEWS = Set.of(VIEW_CC, VIEW_CM, VIEW_SBU, VIEW_RB, VIEW_BOARD);
    private static final Set<String> MINE_VIEWS = Set.of(VIEW_CC, VIEW_CM, VIEW_SBU, VIEW_RB);

    private final UdmComplaintListRepository listRepository;
    private final UdmRejectionRepository rejectionRepository;
    private final UdmRejectionService rejectionService;
    private final UdmComplaintWorkflowSupport support;

    /**
     * @param view  cc | cm | sbu | rb | board
     * @param scope mine | all  (mine for cc / cm / sbu / rb)
     * @param tab   pending | closed
     */
    @Transactional(readOnly = true)
    public UdmComplaintListResponse list(String view, String scope, String tab, int page, int size, String q, Integer userId) {
        String v = view == null ? VIEW_BOARD : view.trim().toLowerCase(Locale.ROOT);
        if (!VIEWS.contains(v)) throw validation("Unknown view: " + view);
        requireViewRole(v, userId);

        boolean mine = MINE_VIEWS.contains(v) && "mine".equalsIgnoreCase(scope == null ? "" : scope.trim());
        boolean closed = "closed".equalsIgnoreCase(tab == null ? "" : tab.trim());
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        String search = q == null || q.isBlank() ? null : q.trim();
        if (search != null && search.length() > 100) search = search.substring(0, 100);

        Filter f = new Filter(v, mine, closed, userId, search);
        long total = listRepository.count(f);
        int totalPages = (int) Math.max(1, (total + safeSize - 1) / safeSize);
        if (safePage >= totalPages) safePage = totalPages - 1;

        List<Long> ids = listRepository.findIds(f, safePage * safeSize, safeSize);
        Map<Long, UdmRejection> byId = rejectionRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(UdmRejection::getId, Function.identity()));
        List<UdmRejection> ordered = ids.stream().map(byId::get).filter(Objects::nonNull).toList();

        Counts counts = listRepository.counts(f);
        UdmComplaintListResponse r = new UdmComplaintListResponse();
        r.setContent(rejectionService.toDtosWithComplaint(ordered));
        r.setPage(safePage);
        r.setSize(safeSize);
        r.setTotalElements(total);
        r.setTotalPages(totalPages);
        r.setPendingCount(counts.pending());
        r.setClosedCount(counts.closed());
        r.setMineCount(counts.mine());
        r.setAllCount(counts.all());
        return r;
    }

    /** Permission comes from the login (user_role_master), never from the request. */
    private void requireViewRole(String view, Integer userId) {
        if (VIEW_BOARD.equals(view)) return;   // read-only list, any signed-in user
        if (userId == null) throw validation("User not identified. Please log in again.");
        boolean ok = switch (view) {
            case VIEW_CC -> support.hasAnyRole(userId, List.of(ROLE_COMPLAINT_CELL.toLowerCase(Locale.ROOT)));
            case VIEW_CM -> support.hasAnyRole(userId, CM_ROLE_NAMES);
            case VIEW_SBU -> support.hasAnyRole(userId, SBU_HEAD_ROLE_NAMES);
            case VIEW_RB -> support.hasAnyRole(userId, RAILWAY_BOARD_ROLE_NAMES);
            default -> false;
        };
        if (!ok) throw validation("You do not have access to this view.");
    }
}
