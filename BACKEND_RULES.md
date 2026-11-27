# SARTHI Backend Rules

Spring Boot 3 · Java 17 · MySQL (Azure). Applies to every backend change. CLAUDE.md (repo root) rules come first — especially: nothing is compiled, run, committed, pushed or queried without the user's go-ahead.

## 1. Structure

### Where code goes
| Product / area | Root package | Layout |
|---|---|---|
| ERC + shared | `com.sarthi` | `controller/ service/ service/Impl/ repository/ entity/ dto/ enums/ constant/ exception/ util/ config/` (sub-folders per module: `rawmaterial/`, `processmaterial/`, `finalmaterial/`, `ercdiversion/`, `certificate/` …) |
| Sleeper | `com.sarthi.Sleeper` | same layer folders inside |
| Rail Pad | `com.sarthi.SRailPad` | same layer folders; features as sub-packages (`diversion/{controller,dto,entity,repository,service}`) |
| Rail mill | `com.sarthi.Sms` | same layer folders + own `constant/ exception/ util/ job/` |
| Super Admin tools | `com.sarthi.superadmin.<feature>` | small feature package (`dpchange/`) |

- New **module inside a product** → sub-package in each layer folder (`controller/xyz/`, `service/xyz/`, …) like `rawmaterial/`.
- New **self-contained feature** (own tables, few classes) → one feature package with its own `controller/ dto/ entity/ repository/ service/` like `SRailPad/diversion`.
- Never put one product's logic in another product's package or service; shared code goes to `com.sarthi.util` / a shared service.

### Layers — one job each
| Layer | Name pattern | Does | Never |
|---|---|---|---|
| **Controller** | `XyzController` (`@RestController`, `@RequestMapping("/api/...")`) | read params/body, call **one** service method, wrap in `ResponseBuilder` | business logic, repository calls, `@Transactional`, building entities |
| **Service (interface)** | `XyzService` in `service/` | public contract of the module | — |
| **Service impl** | `XyzServiceImpl` in `service/Impl/` (`@Service`) | business rules, validation, role checks, `@Transactional`, mapping entity ↔ DTO | HTTP types (`ResponseEntity`, `HttpServletRequest`), raw JSON building |
| **Repository** | `XyzRepository extends JpaRepository<Entity, Id>` | data access: derived queries, `@Query` JPQL / native, projections | logic, loops, calls to other repositories |
| **Entity** | `Xyz` / `XyzEntity` (`@Entity @Table(name="snake_case")`) | maps one table; `@Column(name=...)` explicit; Lombok `@Data`/`@Getter` | business methods, JSON annotations for API shape |
| **DTO** | `XyzDto`, `XyzRequestDto`, `XyzResponseDto` in `dto/` | API input/output, validation annotations | JPA annotations, lazy relations |
| **Enum / constant** | `XyzStatus`, `AppConstant` | statuses, types, roles, error codes | — |
| **Exception** | `BusinessException`, `InvalidInputException`, `ErrorDetails` (rail mill: `Sms*`) | central errors with user message | new ad-hoc exception types per feature |
| **Util / helper** | `XyzUtil`, `XyzHelper` (static, stateless) or `@Component` | shared pure logic (IC number parse/build, date conversion) | DB access |
| **Job** | `XyzJob` (`@Scheduled`) | triggers a service method | logic itself |
| **Config** | `XyzConfig` (`@Configuration`) | beans, properties binding | changes without permission (security, CORS, datasource) |

- Flow is always **Controller → Service (interface) → ServiceImpl → Repository → Entity**, DTOs crossing the controller boundary. Small single-feature packages may use a concrete `@Service` class without an interface (as `DpChangeService`) — follow what the surrounding package does.
- Entity ↔ DTO mapping in the service impl (or a `XyzMapper` helper when reused); never return entities from controllers.
- Read-heavy lists: repository projections / `Object[]` with a comment naming the columns, mapped to a DTO in the service.
- Constants/enums for statuses, roles, call types (`AppConstant`, `com.sarthi.enums`); no new magic strings when an enum exists.
- Reuse before create: check for an existing repository method, helper (`IcNumberSuffix`, `DiversionCallDateHelper`, `RailMillCommonUtils`) or service first.
- A new endpoint = controller method + service interface method + impl + (repository method) + DTOs — all in the module's own folders, named consistently (`getRailpadIcDetails` in controller, service and impl).

## 1a. Code quality — "ponytail" code (neat, tight, nothing loose)
- **Small and single-purpose:** one method does one thing; aim ≤ 40 lines per method. Split long methods into well-named private helpers. A new class instead of growing a 3000-line service.
- **Names say what it is:** `fullyRejected`, `divertedQtyByHeat`, `nextMpaEmployeeCode` — no `data2`, `temp`, `flag`, `x`. Booleans read as questions (`isCancelled`, `hasExtDp`). Constants in UPPER_CASE.
- **DRY:** the same logic in two places → one shared helper (e.g. one `IcNumbers.build`, not four copies). Copy-paste across products only when the products must really differ — then say why in a comment.
- **Early return, flat code:** guard clauses at the top (`if (x == null) return …`), max ~2 levels of nesting; no `else` after `return`.
- **Null-safe and typed:** `Optional`, `Objects.requireNonNullElse`, safe `Number` → `double` conversion; no raw `Object[]` index magic without a comment naming the columns. Prefer a small record/DTO over long `Object[]` / `Map<String,Object>` chains in new code.
- **No dead code:** no commented-out blocks, unused imports, `System.out`, debug `log.info` spam, TODOs without an owner. Delete — git keeps history.
- **Comments explain why, not what:** one short line above non-obvious rules ("JSPL has only URM", "ext DP wins for cutoff"). Javadoc on public service methods with the business rule.
- **Logging:** `log.info` for business events (who, what, which call/PO), `log.warn` for recoverable issues, `log.error(msg, e)` with the exception; never log secrets or full payloads.
- **Exceptions:** catch only what you can handle; never swallow (`catch (Exception e) {}`); rethrow as the central business exception with a user message.
- **Formatting:** follow the file's existing style (4-space indent, braces on same line, imports grouped, no wildcard imports in new files); keep line endings as the file has them (CRLF/LF).
- **Immutability:** `final` fields with constructor injection (`@RequiredArgsConstructor`) in new classes; no field `@Autowired` in new code.
- **SOLID in practice:** services depend on repositories/interfaces, not on other controllers; no circular service dependencies (use events or a small shared helper instead of `@Lazy` hacks in new code).

## 2. API contract
- Responses: `ResponseBuilder.getSuccessResponse(...)` / `getErrorResponse(new ErrorDetails(...))` (rail mill: `SmsResponseBuilder`). Keep `responseStatus` + `responseData` shape.
- Never change an existing endpoint's path, params or response fields. Add optional params with safe defaults (`@RequestParam(defaultValue = "false")`), or a new endpoint.
- Business errors: throw `BusinessException` / `InvalidInputException` with a clear user message — the frontend shows it as is. No stack traces or SQL in messages.
- Validate on the backend even when the UI validates (dates, quantities, roles, status transitions).

## 3. Security
- Current user from the JWT principal (`UserMaster` in `SecurityContextHolder`), never from a request param or body field.
- Check the role in the service for every action that changes data (approve, revert, delete, edit qty, DP change).
- Native SQL: bind parameters only. Table/column names that vary must come from a fixed whitelist in code (see `DpChangeService.table()/column()`), never from input.
- Never log or return passwords, tokens, OTPs, `.env` values or DB credentials. `.env` / `application.properties` are not edited without permission.

## 4. Database
- Schema changes: a script in `database_migrations/V<yyyymmdd>_<nnn>__<desc>.sql` (idempotent where possible, with a check query at the end). The user runs it on UAT, then prod — before the backend that needs it is deployed. Say so in the reply.
- Adding an entity field: also add it to every native query that maps explicit columns into that entity (e.g. `UserMasterRepository.findAllUsersOptimized`), or Hibernate fails at runtime. `SELECT *` queries are fine once the column exists.
- No N+1: load lists with one bulk / `IN (...)` / `GROUP BY` query, then map in memory. No repository call inside a loop over rows.
- Pagination on list endpoints; filter on indexed columns.
- Collations differ between tables (`utf8mb4_unicode_ci` vs `utf8mb4_0900_ai_ci`): add `COLLATE utf8mb4_unicode_ci` on both sides when joining text columns across such tables.
- Data fixes / deletes: soft delete (status column) where the feature already does; scripts only, never run by Claude.

## 5. Dates & numbers
- The DB runs in UTC; business dates are IST. Use `ZoneId.of("Asia/Kolkata")` for "today" rules (e.g. duty date today/yesterday).
- When a value must round-trip exactly (DP change, revert), read/write DATETIME as text (`DATE_FORMAT` / `CAST(? AS DATETIME)`), not `Timestamp`/`LocalDateTime`, to avoid time-zone shifts.
- Quantities: say the unit (MT / Nos / Sets / Mtr); round MT to 3 decimals; compare doubles with a tolerance.

## 6. Domain rules that are easy to break
- Products and tables: ERC `inspection_calls`, `inspection_complete_details`; Sleeper `sleeper_*`; Rail Pad `rail_*` (older Rail Pad calls store `po_no` as `PO/serial` — strip the serial before joining `po_header`).
- Call numbers: `ER/EP/EF-`, `SF-`, `RPP/RPF-`; old calls use MMDDYY+serial, new ones YYMMDD+2-digit. Never assume one shape.
- IC (certificate) numbers: read the call no. / IE by **pattern** (helpers), never by position (`split("/")[1]`, `SUBSTRING_INDEX(...,'/',2)`); see `docs/IC_Number_Railway_Plan.md`. Issued ICs are never renumbered.
- Role names: rail mill `R Process IE` / `R Main IE` / `R Analyst` (shown as "Rail …"); Rail Pad `Rail Process IE` / `Rail Main IE` (shown as "Rail Pad …"). Match roles by exact name, never by `contains`.
- Diversion: PARTIAL vs COMPLETE behave differently (only COMPLETE hides the source IC); shared APIs used by diversion screens must not change their numbers — add a flag instead.
- DP / call cutoff: vendor screens use Ext. DP if present, else DP; cutoff = that date − 4 days.
- Workflow: status changes go through the product's workflow service/transition tables so history and dashboards stay correct; don't update status columns directly.

## 7. Transactions, concurrency, jobs
- `@Transactional` on service methods that write more than one row; row locks (`@Lock` / `SELECT … FOR UPDATE`) for balances (diversion qty, baskets, scrap pending qty).
- Claim-then-act for jobs that can run on two servers (e.g. `status ACTIVE → REVERTING` before reverting).
- Scheduled / heavy work: async, logged, with a status and failure reason; never block a request thread.

## 8. Changes & review
- Change only what was asked; keep returned data identical in refactors; report other issues instead of fixing them silently.
- Guard new logic narrowly (by product, call category, role) so other flows are untouched; state in the reply why existing flows are unaffected.
- After editing, list: files changed, DB script (if any) and when to run it, what to test on UAT, and "not compiled".
- Optional, on request: `/code-review` (low/medium), `security-auditor` / `modernize-harden` for a read-only audit, `test-engineer` to write characterization tests before risky refactors (running them needs permission).
