package de.skit.grocy.households.invites;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import de.skit.grocy.common.enums.Role;
import de.skit.grocy.households.HouseholdEntity;
import de.skit.grocy.households.HouseholdRepository;
import de.skit.grocy.households.invites.dto.HouseholdInviteCreateRequest;
import de.skit.grocy.households.invites.dto.HouseholdInviteCreatedResponse;
import de.skit.grocy.households.invites.dto.HouseholdInviteListResponse;
import de.skit.grocy.households.invites.dto.HouseholdInvitePreviewResponse;
import de.skit.grocy.households.invites.enums.InviteStatus;
import de.skit.grocy.households.member.HouseholdMemberEntity;
import de.skit.grocy.households.member.HouseholdMemberRepository;
import de.skit.grocy.households.member.HouseholdMemberService;
import de.skit.grocy.common.enums.Permission;
import de.skit.grocy.security.HouseholdAuthorizationService;
import de.skit.grocy.security.UserPrincipal;
import de.skit.grocy.security.crypto.TokenHashes;

@Service
public class HouseholdInviteService {

    private static final int DEFAULT_MAX_USES = 1;
    private static final int DEFAULT_EXPIRES_HOURS = 2;

    private final HouseholdInviteRepository inviteRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository memberRepository;
    private final HouseholdMemberService householdMemberService;
    private final HouseholdAuthorizationService householdAuth;

    public HouseholdInviteService(
            HouseholdInviteRepository inviteRepository,
            HouseholdRepository householdRepository,
            HouseholdMemberRepository memberRepository,
            HouseholdMemberService householdMemberService,
            HouseholdAuthorizationService householdAuth) {
        this.inviteRepository = inviteRepository;
        this.householdRepository = householdRepository;
        this.memberRepository = memberRepository;
        this.householdMemberService = householdMemberService;
        this.householdAuth = householdAuth;
    }

    /**
     * POST /households/{householdId}/invites
     */
    @Transactional
    public HouseholdInviteCreatedResponse createInvite(
            UUID householdId,
            HouseholdInviteCreateRequest request,
            UUID currentUserId,
            String publicAppBaseUrl) {
        Role inviteRole = request.getRole();
        if (inviteRole == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "role is required");
        }
        if (inviteRole == Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only 1 OWNER allowed");
        }

        // 1) Household existiert?
        HouseholdEntity household = householdRepository.findById(householdId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Household not found"));

        // 2) Darf der aktuelle User einladen?
        HouseholdMemberEntity creatorMembership = memberRepository
                .findByHouseholdIdAndUserId(householdId, currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a household member"));
        if (!creatorMembership.getRole().canInvite()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to create invites");
        }

        inviteRepository.revokeActiveInvitesForRole(
                householdId,
                inviteRole,
                InviteStatus.PENDING,
                InviteStatus.REVOKED,
                OffsetDateTime.now());

        // 3) Token generieren + Hash speichern
        String token = generateToken();
        String tokenHash = TokenHashes.sha256Hex(token);

        HouseholdInviteEntity invite = new HouseholdInviteEntity();
        invite.setHouseholdId(household.getId());
        invite.setTokenHash(tokenHash);
        invite.setRole(inviteRole);
        invite.setExpiresAt(OffsetDateTime.now().plusHours(DEFAULT_EXPIRES_HOURS));
        invite.setMaxUses(DEFAULT_MAX_USES);
        invite.setCreatedByUserId(currentUserId);
        invite.setStatus(InviteStatus.PENDING);

        inviteRepository.save(invite);

        String url = buildJoinUrl(publicAppBaseUrl, token);

        HouseholdInviteCreatedResponse resp = new HouseholdInviteCreatedResponse();
        resp.setToken(token);
        resp.setUrl(url);
        resp.setExpiresAt(invite.getExpiresAt());
        return resp;
    }

    /**
     * GET /invites/{token}
     */
    public HouseholdInvitePreviewResponse getInvitePreview(String token) {
        String tokenHash = TokenHashes.sha256Hex(token);

        HouseholdInviteEntity invite = inviteRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite not found"));

        if (invite.getStatus() == InviteStatus.PENDING && invite.isExpired()) {
            invite.setStatus(InviteStatus.EXPIRED);
            inviteRepository.save(invite);
        }

        if (invite.getStatus() != InviteStatus.PENDING || !invite.isUsable()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite not found");
        }

        HouseholdEntity household = householdRepository.findById(invite.getHouseholdId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite not found"));

        HouseholdInvitePreviewResponse resp = new HouseholdInvitePreviewResponse();
        resp.setHouseholdId(household.getId());
        resp.setHouseholdName(household.getName());
        resp.setRole(invite.getRole());
        resp.setExpiresAt(invite.getExpiresAt());
        resp.setStatus(invite.getStatus());
        resp.setRemainingUses(Math.max(0, invite.getMaxUses() - invite.getUses()));
        return resp;
    }

    /**
     * POST /invites/{token}/accept
     *
     * Wichtig: transactional + findForUpdate (PESSIMISTIC_WRITE) => Einmal-Link
     * sicher.
     */
    @Transactional
    public void acceptInvite(String token, UUID currentUserId) {
        String tokenHash = TokenHashes.sha256Hex(token);

        HouseholdInviteEntity invite = inviteRepository.findForUpdateByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite not found"));

        // Ablauf/Status prüfen (unter Lock!)
        if (invite.getStatus() == InviteStatus.REVOKED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite not found");
        }
        if (invite.isExpired()) {
            invite.setStatus(InviteStatus.EXPIRED);
            // save nicht zwingend nötig (Transaction flush), aber explizit ist ok:
            inviteRepository.save(invite);
            throw new ResponseStatusException(HttpStatus.GONE, "Invite expired");
        }
        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invite not usable");
        }
        if (invite.getUses() >= invite.getMaxUses()) {
            invite.setStatus(InviteStatus.ACCEPTED);
            inviteRepository.save(invite);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invite already used");
        }

        UUID householdId = invite.getHouseholdId();

        if (memberRepository.existsByHouseholdIdAndUserId(householdId, currentUserId)) {
            return;
        }

        householdMemberService.addUserToHouseholdIfNotExists(
                householdId,
                currentUserId,
                invite.getRole());

        // Invite verbrauchen + status setzen (Entity helper)
        invite.markAccepted(currentUserId);
        inviteRepository.save(invite);
    }

    @Transactional(readOnly = true)
    public List<HouseholdInviteListResponse> listInvites(UUID householdId, UserPrincipal principal) {
        householdAuth.require(householdId, principal.getUser(), Permission.MEMBER_INVITE);
        return inviteRepository.findByHouseholdIdOrderByCreatedAtDesc(householdId).stream()
                .map(invite -> new HouseholdInviteListResponse(
                        invite.getId(),
                        invite.getRole(),
                        invite.getStatus(),
                        invite.getExpiresAt(),
                        invite.getUses(),
                        invite.getMaxUses(),
                        invite.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void revokeInvite(UUID householdId, UUID inviteId, UserPrincipal principal) {
        householdAuth.require(householdId, principal.getUser(), Permission.MEMBER_INVITE);
        HouseholdInviteEntity invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite not found"));
        if (!householdId.equals(invite.getHouseholdId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite not found");
        }
        if (invite.getStatus() == InviteStatus.PENDING) {
            invite.setStatus(InviteStatus.REVOKED);
            inviteRepository.save(invite);
        }
    }

    // ----------------- helpers -----------------

    private static String generateToken() {
        return TokenHashes.randomUrlToken();
    }

    private static String buildJoinUrl(String baseUrl, String token) {
        // baseUrl z.B. "https://app.de" oder "http://localhost:5173"
        String trimmed = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return trimmed + "/join?token=" + token;
    }
}
